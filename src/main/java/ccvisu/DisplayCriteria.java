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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ashgan
 * This class is used to determine which vertices or 
 * edges should be displayed
 */
public class DisplayCriteria {

  // The associated writer instance on which
  // the display criteria is applied.
  //  private WriterDataGraphicsDISP writer;

  private Color            addedVertexColor     = Color.RED;
  private Color            removedVertexColor   = Color.CYAN;
  private Color            unchangedVertexColor = Color.GRAY;

  private Color            addedEdgeColor       = Color.RED;
  private Color            removedEdgeColor     = Color.BLUE;
  private Color            unchangedEdgeColor   = Color.BLACK;

  private Set<GraphVertex> undoList             = new HashSet<GraphVertex>();

  public DisplayCriteria(GraphData gr) {
    init(gr);
  }

  public DisplayCriteria(GraphData gr, Color addedVertexCol,
                         Color removedVertexCol, Color unchangedVertexCol,
                         Color addedEdgeCol, Color removedEdgeCol,
                         Color unchangedEdgeCol) {
    addedVertexColor = addedVertexCol;
    removedVertexColor = removedVertexCol;
    unchangedVertexColor = unchangedVertexCol;
    addedEdgeColor = addedEdgeCol;
    removedEdgeColor = removedEdgeCol;
    unchangedEdgeColor = unchangedEdgeCol;
    init(gr);
  }

  public void init(GraphData gr) {
    undoList = new HashSet<GraphVertex>(gr.vertices);
  }

  /**
   * @param filterType <br>
   * 0: Display unchanged vertices<br>
   * 1: Display removed vertices<br>
   * 2: Display added vertices
   */
  private boolean vertexPredicate(GraphVertex v, int filterType) {
    switch (filterType) {
    case 0: // is unchanged
      return matchColorWith(v.color, unchangedVertexColor);
    case 1: // is removed
      return matchColorWith(v.color, removedVertexColor);
    case 2: // is added
      return matchColorWith(v.color, addedVertexColor);
    default:
      return false;
    }
  }

  /**
   * Makes all the vertices invisible except those  
   * that satisfy the filtering predicate.
   *  
   * Warning: This method does not change the 
   * graph argument which it takes, but it might 
   * change the attributes of the vertices 
   * of the graph.
   * 
   * This method can preserve the current view 
   * ("zooming" feature). For that end, pass
   * a "true" value to the argument 
   * "onlyVisibles".
   * 
   * @param onlyVisibles if true, then only the 
   * currently visible vertex members (with a true 
   * showVertex attribute) are affected by this 
   * method
   */
  public void filterVertices(final GraphData gr, int filterType,
                             boolean onlyVisibles) {
    resetView(gr.vertices);
    for (GraphVertex currVertex : gr.vertices) {
      if (!vertexPredicate(currVertex, filterType)) {
        currVertex.showVertex = false;
      } else if (!onlyVisibles) {
        currVertex.showVertex = true;
      }
    }
  }

  /**
   * @param filterType <br>
   * 0: Display unchanged edges<br>
   * 1: Display removed edges<br>
   * 2: Display added edges
   */
  private boolean edgePredicate(GraphEdge e, int filterType) {
    switch (filterType) {
    case 0: // is unchanged
      return matchColorWith(e.color, unchangedEdgeColor);
    case 1: // is removed
      return matchColorWith(e.color, removedEdgeColor);
    case 2: // is added
      return matchColorWith(e.color, addedEdgeColor);
    default:
      return false;
    }
  }

  /**
   * Makes visible all the incident vertices of those 
   * edges which satisfy the edge filtering predicate
   * along with all the incident edges of these 
   * vertices.
   *  
   * Warning: This method does not change the 
   * graph argument which it takes, but it might change 
   * the attributes of the vertices of the graph.

   * This method can preserve the current view 
   * ("zooming" feature). For that end, pass
   * a "true" value to the argument 
   * "onlyVisibles".
   * 
   * @param gr
   * @param onlyVisibles if true, then only the 
   * currently visible vertex members (with a true 
   * showVertex attribute) are affected by this 
   * method
   */
  public void filterEdges(final GraphData gr, int filterType,
                          boolean onlyVisibles) {
    Set<GraphVertex> verticesToDisplay = new HashSet<GraphVertex>();

    // Determine the vertices which are incident 
    // to the desired edges
    for (GraphEdge currEdge : gr.edges) {
      assert (gr.vertices.size() > currEdge.x);
      assert (gr.vertices.size() > currEdge.y);
      if (edgePredicate(currEdge, filterType)) {
        GraphVertex src = gr.vertices.get(currEdge.x);
        GraphVertex tar = gr.vertices.get(currEdge.y);
        if (onlyVisibles) {
          // Display the tail (src) and head (tar) 
          // vertices of the current edge only if both 
          // of them are visible simultaneously in 
          // the current view
          if (src.showVertex && tar.showVertex) {
            verticesToDisplay.add(src);
            verticesToDisplay.add(tar);
          }
        } else {
          verticesToDisplay.add(src);
          verticesToDisplay.add(tar);
          src.showVertex = true;
          tar.showVertex = true;
        }
      }
    }

    // Now hide all the vertices that are not 
    // marked by the previous section
    for (GraphVertex currVer : gr.vertices) {
      if (!verticesToDisplay.contains(currVer)) {
        currVer.showVertex = false;
      }
    }
  }

  /**
   * Keeps the track of currently visible vertices in case later
   * an "undo" operation is performed to restore the changes made
   * by this instance
   * @param vertices
   */
  public void resetUndoList(Collection<GraphVertex> vertices) {
    undoList = new HashSet<GraphVertex>();

    for (GraphVertex currVer : vertices) {
      if (currVer.showVertex) {
        undoList.add(currVer);
      }
    }
  }

  /**
   * Resets the vertex display filter (Undo filtering). 
   * Shows all vertices within the zooming rectangle. 
   * If the rectangle margins are not specified,
   * then this method considers the whole canvas as 
   * the rectangular area which hosts vertices and edges
   */
  public void resetView(final List<GraphVertex> vertices) {
    assert (undoList != null);

    if (undoList.isEmpty()) { return; }

    for (GraphVertex currVer : vertices) {
      if (undoList.contains(currVer)) {
        currVer.showVertex = true;
      } else {
        currVer.showVertex = false;
      }
    }
  }

  private boolean matchColorWith(Color col1, Color col2) {
    return (col1.getAlpha() == col2.getAlpha())
           && (col1.getRed() == col2.getRed())
           && (col1.getGreen() == col2.getGreen())
           && (col1.getBlue() == col2.getBlue());
  }
}
