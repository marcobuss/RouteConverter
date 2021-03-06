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

package slash.navigation.download.datasources;

import slash.common.type.CompactCalendar;

/**
 * A file that may be downloaded
 *
 * @author Christian Pesch
 */

public class File {
    private final String uri;
    private final Long size;
    private final String checksum;
    private final CompactCalendar timestamp;

    public File(String uri, Long size, String checksum, CompactCalendar timestamp) {
        this.uri = uri;
        this.size = size;
        this.checksum = checksum;
        this.timestamp = timestamp;
    }

    public String getUri() {
        return uri;
    }

    public Long getSize() {
        return size;
    }

    public String getChecksum() {
        return checksum;
    }

    public CompactCalendar getTimestamp() {
        return timestamp;
    }
}
