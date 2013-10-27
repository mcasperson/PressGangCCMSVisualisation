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
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import ccvisu.Options.Verbosity;

/***************************************************************************
 * Parse group information from a file and mark the vertices accordingly.
 * @author   Dirk Beyer
 **************************************************************************/
public class MarkerScript extends Marker {

  /**
   * Group properties
   */
  private class GroupProperties {
    private String mPattern;
    private Color  mColor;

    /** Constructor */
    public GroupProperties(String pPattern, Color pColor, String pName) {
      mPattern = pPattern;
      mColor = pColor;
    }
  }

  private ArrayList<GroupProperties> mPropList;

  /**
   * Constructor
   * @param in      BufferedReader for reading group information
   */
  public MarkerScript(BufferedReader in, Verbosity pVerbosity) {
    ReaderDataGraphRSF lRSFReader = new ReaderDataGraphRSF(in, pVerbosity);
    Relation lPatterns = lRSFReader.readTuples();
    mPropList = new ArrayList<GroupProperties>();
    for (List<String> pattern : lPatterns) {
      if (pattern.size() < 3) {
        System.err.println("RSF reading problem. Follwing format is expected:");
        System.err.println("  GROUP <reg-exp-pattern> <color> [<name>]");
      }
      Color lColor = Colors.get(pattern.get(2));
      if (lColor == null) {
        // Getting color from string via name did not work. Try color code.
        lColor = new Color(Integer.parseInt(pattern.get(2), 16));
        System.err.println(pattern.get(2) + "/"
                           + Integer.parseInt(pattern.get(2), 16));
      }
      String lName = "";
      if (pattern.size() > 3) {
        lName = pattern.get(3);
      }
      GroupProperties newProp =
                                new GroupProperties(pattern.get(1), lColor,
                                    lName);
      mPropList.add(newProp);
    }

  }

  /*****************************************************************
   * Special marking for certain vertices by setting attributes of the vertex
   * @param vertex  Vertex of the graph representation.
   *****************************************************************/
  @Override
  public void mark(GraphVertex vertex) {
    for (GroupProperties prop : mPropList) {
      if (vertex.name.matches(prop.mPattern)) {
        vertex.color = prop.mColor;
      }
    }
  }
}
