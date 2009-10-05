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
package slash.navigation.simple;

import slash.navigation.*;
import slash.common.util.CompactCalendar;
import slash.common.util.Transfer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The base of all Sygic formats.
 *
 * @author Christian Pesch
 */

public abstract class SygicFormat extends SimpleLineBasedFormat<SimpleRoute> {
    private static final String COMMENT_LINE = ";";
    protected static final String TAB = "\t";

    private static final Pattern LINE_PATTERN = Pattern.
            compile(BEGIN_OF_LINE +
                    "(" + POSITION + ")" + TAB +
                    "(" + POSITION + ")" + TAB +
                    "([^" + TAB + "]+)" + TAB + "?" +
                    "(.*)" +
                    END_OF_LINE);

    public String getExtension() {
        return ".txt";
    }

    public <P extends BaseNavigationPosition> SimpleRoute createRoute(RouteCharacteristics characteristics, String name, List<P> positions) {
        return new Wgs84Route(this, characteristics, (List<Wgs84Position>) positions);
    }

    protected boolean isValidLine(String line) {
        return isPosition(line) || line != null && (line.length() == 0 || line.startsWith(COMMENT_LINE));
    }

    protected boolean isPosition(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        return matcher.matches();
    }

    protected Wgs84Position parsePosition(String line, CompactCalendar startDate) {
        Matcher lineMatcher = LINE_PATTERN.matcher(line);
        if (!lineMatcher.matches())
            throw new IllegalArgumentException("'" + line + "' does not match");
        String longitude = lineMatcher.group(1);
        String latitude = lineMatcher.group(2);
        String comment = Transfer.trim(lineMatcher.group(3));
        String phone = Transfer.trim(lineMatcher.group(4));
        if (phone != null)
            comment = comment + " " + phone;
        return new Wgs84Position(Transfer.parseDouble(longitude), Transfer.parseDouble(latitude),
                null, null, null, comment);
    }
}