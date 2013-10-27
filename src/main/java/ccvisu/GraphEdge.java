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

/*****************************************************************
 * Represents an edge between two vertices x and y 
 * (given as vertex ids) of weight w.
 * @version  $Revision: 1.16 $; $Date: 2007/12/15 01:20:50 $
 * @author   Dirk Beyer
 *****************************************************************/
public class GraphEdge implements Comparable<GraphEdge> {
  /** The name of the relation the egde belongs to*/
  public String  relName   = "Edge";
  /** Source vertex of edge.*/
  public int     x         = 0;
  /** Target vertex of edge.*/
  public int     y         = 0;
  /** Edge weight.*/
  public float   w         = 1.0f;
  /** The color of the edge.*/
  public Color   color     = Color.BLACK;
  /** True if the name shall be annotated in the visualization.*/
  public boolean showName  = false;
  /** True if the edge shall never be included in the visualization.*/
  public boolean auxiliary = false;

  public GraphEdge() {
  }

  public GraphEdge(String pName, int pX, int pY, Color pColor,
                   boolean pAuxiliary) {
    relName = pName;
    x = pX;
    y = pY;
    color = pColor;
    auxiliary = pAuxiliary;
  }

  @Override
  public int compareTo(GraphEdge pEdge) {
    int result = new Integer(this.x).compareTo(new Integer(pEdge.x));
    if (result != 0) { return result; }
    result = new Integer(this.y).compareTo(new Integer(pEdge.y));
    if (result != 0) { return result; }
    result = new Float(this.w).compareTo(new Float(pEdge.w));
    if (result != 0) { return result; }
    result =
             new Integer(this.color.getRGB()).compareTo(new Integer(pEdge.color
                 .getRGB()));
    if (result != 0) { return result; }
    result = new Boolean(this.showName).compareTo(new Boolean(pEdge.showName));
    if (result != 0) { return result; }
    result =
             new Boolean(this.auxiliary)
                 .compareTo(new Boolean(pEdge.auxiliary));
    if (result != 0) { return result; }
    result = this.relName.compareTo(pEdge.relName);
    return result;
  }
};
