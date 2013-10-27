/*
 * CCVisu is a tool for visual graph clustering
 * and general force-directed graph layout.
 * This file is part of CCVisu. 
 * 
 * Copyright (C) 2005-2010  Dirk Beyer
 * 
 * CCVisu is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * CCVisu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with CCVisu; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Please find the GNU Lesser General Public License in file
 * license_lgpl.txt or http://www.gnu.org/licenses/lgpl.txt
 * 
 * Dirk Beyer    (firstname.lastname@uni-passau.de)
 * University of Passau, Bavaria, Germany
 */

package ccvisu;

import java.awt.Color;

/**
 * Colors for easy access by name. 
 * Useful to write things like: Color myColor = Colors.get("blue");
 * @author Dirk Beyer
 */
public enum Colors {

  WHITE(255, 255, 255),

  LIGHTGRAY(192, 192, 192),

  GRAY(128, 128, 128),

  DARKGRAY(64, 64, 64),

  BLACK(0, 0, 0),

  RED(255, 0, 0),

  GREEN(0, 255, 0),

  BLUE(0, 0, 255),

  LIGHTRED(255, 128, 128),

  LIGHTGREEN(128, 255, 128),

  LIGHTBLUE(128, 128, 255),

  DARKRED(128, 0, 0),

  DARKGREEN(0, 128, 0),

  DARKBLUE(0, 0, 128),

  YELLOW(255, 255, 0),

  MAGENTA(255, 0, 255),

  CYAN(0, 255, 255),

  LIGHTYELLOW(255, 255, 192),

  LIGHTMAGENTA(255, 192, 255),

  LIGHTCYAN(192, 255, 255),

  DARKYELLOW(128, 128, 0),

  DARKMAGENTA(128, 0, 128),

  DARKCYAN(0, 128, 128),

  // Mixtures
  PINK(255, 175, 175),

  ORANGE(255, 200, 0),

  CHOCOLATE(139, 69, 19),

  DARKOLIVEGREEN(110, 139, 61);

  private final Color color;

  /*****************************************************************
   * Constructor.
   * @param r, g, b   RGB int values for the color to create.
   *****************************************************************/
  private Colors(int r, int g, int b) {
    color = new Color(r, g, b);
  }

  /*****************************************************************
   * Returns the Color object.
   * @return        Color object.
   *****************************************************************/
  public Color get() {
    return color;
  }

  private static boolean verbose = true;

  /*****************************************************************
   * Returns the Color object for a given name.
   * @param nameOfColor  String that represents a color name.
   * @return             Color object if it exists, null otherwise.
   *****************************************************************/
  public static Colors valueOfUpper(String nameOfColor) {
    try {
      return Colors.valueOf(nameOfColor.toUpperCase());
    } catch (Exception e) {
      if (Colors.verbose) {
        System.err.println("Runtime error: Color '" + nameOfColor
                           + "' unknown.");
        System.err.print("Available colors are: ");
        for (Colors c : Colors.values()) {
          System.out.print(c.toString() + ' ');
        }
        System.err.println();
      }
      return null;
    }
  }

  /*****************************************************************
   * Returns the Color object for a given name.
   * @param nameOfColor  String that represents a color name.
   * @return             Color object if it exists, null otherwise.
   *****************************************************************/
  public static Color get(String nameOfColor) {
    Colors col = Colors.valueOfUpper(nameOfColor);
    if (col != null) { return col.get(); }
    return null;
  }

  /*****************************************************************
   * Returns the name of a given Color object.
   * @param color        Color object for which to find the name.
   * @return             String with name if color exists, null otherwise.
   *****************************************************************/
  public static String toString(Color color) {
    for (Colors c : Colors.values()) {
      if (c.get().getRGB() == color.getRGB()) { return c.toString(); }
    }
    return null;
  }
}
