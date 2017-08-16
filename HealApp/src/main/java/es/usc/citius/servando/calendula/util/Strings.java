/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2016 CITIUS - USC
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.util;

/**
 * Created by joseangel.pineiro on 3/2/15.
 */
public class Strings {

    public static String toCamelCase(String s, String spacer) {
        String[] parts = s.split(spacer);
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + spacer + toProperCase(part);
        }
        return camelCaseString.replaceFirst(spacer, "");
    }

    public static String toProperCase(String s) {
        if (s.length() > 1)
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        else return s;
    }

    public static String firstPart(String str) {
        try {
            String[] parts = str.split(" ");
            String s = parts[0].toLowerCase();
            if ((s.contains("acido") || s.contains("ácido")) && parts.length > 1) {
                return Strings.toCamelCase(s + " " + parts[1], " ");
            }
            return Strings.toProperCase(s);

        } catch (Exception e) {
            return str;
        }
    }
}
