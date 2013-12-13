/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/
package slash.navigation.download;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.io.*;

import static java.io.File.createTempFile;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.*;
import static slash.common.io.InputOutput.readBytes;
import static slash.common.io.Transfer.UTF8_ENCODING;
import static slash.navigation.download.DownloadManager.WAIT_TIMEOUT;
import static slash.navigation.download.DownloadState.*;

public class DownloadManagerIT {
    private static final String DOWNLOAD = System.getProperty("download", "http://static.routeconverter.com/download/test/");
    private static final String LOREM_IPSUM_DOLOR_SIT_AMET = "Lorem ipsum dolor sit amet";
    private static final String EXPECTED = LOREM_IPSUM_DOLOR_SIT_AMET + ", consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
            "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\n";

    private DownloadManager service;
    private File target;

    private String readFileToString(File file) throws IOException {
        return new String(readBytes(new FileInputStream(file)), UTF8_ENCODING);
    }

    private void writeStringToFile(File file, String string) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new FileOutputStream(file));
        writer.print(string);
        writer.close();
        assertEquals(string.length(), file.length());
    }

    @Before
    public void setUp() throws IOException {
        service = new DownloadManager();
        target = createTempFile("local", ".txt");
    }

    @After
    public void tearDown() {
        if (target.exists())
            assertTrue(target.delete());
        service.interrupt();
    }

    private static final Object LOCK = new Object();

    void waitFor(final Download download, final DownloadState expectedState) {
        final boolean[] found = new boolean[1];
        found[0] = false;

        TableModelListener l = new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (expectedState.equals(download.getState())) {
                    synchronized (LOCK) {
                        found[0] = true;
                        LOCK.notifyAll();
                    }
                }
            }
        };

        long start = currentTimeMillis();
        service.getModel().addTableModelListener(l);
        try {
            while (true) {
                synchronized (LOCK) {
                    if (found[0] || currentTimeMillis() - start > WAIT_TIMEOUT)
                        break;
                    try {
                        LOCK.wait(1000);
                    } catch (InterruptedException e) {
                        // intentionally left empty
                    }
                }
            }
        } finally {
            service.getModel().removeTableModelListener(l);
        }
    }

    @Test
    public void testInvalidUrl() throws IOException {
        Download download = service.queueForDownload("Does not exist", DOWNLOAD + "doesntexist.txt", target);
        waitFor(download, Failed);

        assertEquals(Failed, download.getState());
    }

    @Test
    public void testFreshDownload() throws IOException {
        assertTrue(target.delete());

        Download download = service.queueForDownload("447 Bytes", DOWNLOAD + "447bytes.txt", target);
        waitFor(download, Succeeded);

        assertEquals(Succeeded, download.getState());
        String actual = readFileToString(target);
        assertEquals(EXPECTED, actual);
    }

    @Test
    public void testResumeDownload() throws IOException {
        writeStringToFile(target, LOREM_IPSUM_DOLOR_SIT_AMET);

        Download download = service.queueForDownload("447 Bytes", DOWNLOAD + "447bytes.txt", target);
        waitFor(download, Succeeded);

        assertEquals(Succeeded, download.getState());
        String actual = readFileToString(target);
        assertEquals(EXPECTED, actual);
    }

    @Test
    public void testNotModifiedDownload() throws IOException {
        writeStringToFile(target, EXPECTED);

        Download download = service.queueForDownload("447 Bytes", DOWNLOAD + "447bytes.txt", target);
        waitFor(download, NotModified);

        assertEquals(Succeeded, download.getState());
        String actual = readFileToString(target);
        assertEquals(EXPECTED, actual);
    }

    @Test
    public void testDownloadAndExtract() throws IOException {
        File extracted = new File(target.getParentFile(), "447bytes.txt");
        if(extracted.exists())
            assertTrue(extracted.delete());

        try {
            Download download = service.queueForDownloadAndProcess("447 Bytes in a ZIP", DOWNLOAD + "447bytes.zip", target, new Extractor(target.getParentFile()));
            waitFor(download, Processing);
            assertEquals(Processing, download.getState());

            waitFor(download, Succeeded);
            assertEquals(Succeeded, download.getState());

            assertEquals(423, target.length());
            assertTrue(extracted.exists());
            String actual = readFileToString(extracted);
            assertEquals(EXPECTED, actual);
        } finally {
            if(extracted.exists())
                assertTrue(extracted.delete());
        }
    }
}
