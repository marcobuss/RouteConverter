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
package slash.navigation.brouter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static slash.common.TestCase.assertDoubleEquals;

public class BRouterTest {
    private BRouter router = new BRouter();

    @Test
    public void testLongitude() {
        assertEquals(190032100, router.asLongitude(10.0321));
        assertEquals(10.0321, router.asLongitude(190032100), 0.00001);
    }

    @Test
    public void testLatitude() {
        assertEquals(143569480, router.asLatitude(53.56948));
        assertDoubleEquals(53.56948, router.asLatitude(143569480));
    }

    @Test
    public void createFileKey() {
        assertEquals("E0_N0", router.createFileKey(0.1, 4.9));
        assertEquals("E5_N5", router.createFileKey(5.1, 9.9));
        assertEquals("E50_N50", router.createFileKey(50.1, 54.9));
        assertEquals("E175_N85", router.createFileKey(179.9, 89.9));
        assertEquals("W0_S0", router.createFileKey(-0.1, -4.9));
        assertEquals("W5_S5", router.createFileKey(-5.1, -9.9));
        assertEquals("W50_S50", router.createFileKey(-50.1, -54.9));
        assertEquals("W175_S85", router.createFileKey(-179.9, -89.9));
    }
}
