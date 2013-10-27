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
import java.util.StringTokenizer;

import ccvisu.Options.Verbosity;

/*****************************************************************
 * Reader for layouts in text format.
 * @version  $Revision: 1.27 $; $Date: 2007/12/15 01:20:51 $
 * @author   Dirk Beyer
 *****************************************************************/
public class ReaderDataLAY extends ReaderData {

  /**
   * Constructor.
   * @param in  Stream reader object.
   */
  public ReaderDataLAY(BufferedReader in, Verbosity pVerbosity) {
    super(in, pVerbosity);
  }

  /*****************************************************************
   * Reads the layout data from stream reader <code>in</code>, in text format LAY.
   * @param graph   <code>GraphData</code> object to store the layout data in.
   *****************************************************************/
  @Override
  public void read(GraphData graph) {
    try {
      String lLine;
      while ((lLine = mIn.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(lLine);
        String lTmp = st.nextToken();
        if (!lTmp.startsWith("#")) {
          if (lTmp.equals("LAY")) {
            // OK, this is the default.
            lTmp = st.nextToken();
          } else {
            // To handle old LAY files without the 'LAY' as relation name.
            // 'lTmp' contains actually the x coordinate. 
          }
          GraphVertex lNewVertex = new GraphVertex();
          lNewVertex.pos.x = Float.parseFloat(lTmp);
          lNewVertex.pos.y = Float.parseFloat(st.nextToken());
          lNewVertex.pos.z = Float.parseFloat(st.nextToken());
          lNewVertex.degree = Float.parseFloat(st.nextToken());
          lNewVertex.name = readEntry(st);
          if (st.hasMoreTokens()) {
            lNewVertex.color = new Color(Integer.parseInt(st.nextToken()));
          }
          if (st.hasMoreTokens()) {
            lNewVertex.showName =
                                  (Boolean.valueOf(st.nextToken()))
                                      .booleanValue();
          }
          // Add vertex-to-number entry for vertex.
          if (graph.nameToVertex.containsKey(lNewVertex.name)) {
            System.err.println("Input error: Vertex '" + lNewVertex.name
                               + "' exists twice in layout.");
          }
          lNewVertex.id = graph.vertices.size();
          graph.vertices.add(lNewVertex);
          graph.nameToVertex.put(lNewVertex.name, lNewVertex);
          CCVisu.marker.mark(lNewVertex);
        }
      }
    } catch (Exception e) {
      System.err.println("Runtime error: Exception while reading "
                         + "the layout (readLayoutText).");
      System.err.println(e);
    }
    return;
  }
};
