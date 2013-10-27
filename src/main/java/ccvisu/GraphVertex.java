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
 * Represents a vertex of the graph, including name, id, 
 * and several attributes.
 * @version  $Revision: 1.23 $; $Date: 2007/12/15 01:20:50 $
 * @author   Dirk Beyer
 *****************************************************************/
public class GraphVertex {
  public String   name        = "";
  public int      id          = 0;
  /** True if the vertex is the source vertex of an edge.*/
  public boolean  isSource    = false;
  /** (Weighted) Edge degree of the vertex.*/
  public float    degree      = 0.0f;
  public Color    color       = Color.GREEN;
  /** True if the name shall be annotated in the visualization.*/
  public boolean  showName    = false;
  /** True if the vertex shall be displayed.*/
  public boolean  showVertex  = true;
  /** True if the vertex shall never be included in the visualization.*/
  public boolean  auxiliary   = false;
  /** True if the position of this node should not be changed by the layouter. */
  public boolean  fixedPos    = false;
  /** True if the vertex is reflexive and a self loop should be drawn. */
  public boolean  hasSelfLoop = false;

  /** Position of this vertex. */
  public Position pos         = new Position();

};
