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

import java.io.PrintWriter;
import java.util.List;

import ccvisu.Options.Option;

/*****************************************************************
 * Writer for graphical output of layout data.
 * @version  $Revision: 1.26 $; $Date: 2007/12/15 01:20:50 $
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class WriterDataGraphics extends WriterData {

  protected Options options;

  /**
   * Constructor.
   * @param graph       Graph representation, contains the positions of the vertices.
   */
  public WriterDataGraphics(PrintWriter out, GraphData graph, Options options) {
    super(out, graph);
    this.options = options;
  }

  /*****************************************************************
   * Writes the layout data in a graphics format.
   *****************************************************************/
  @Override
  abstract public void write();

  /*****************************************************************
   * Write graphics layout.
   * @param pSize  Size of output area (e.g., number of pixel).
   *****************************************************************/
  public void writeGraphicsLayout(List<GraphVertex> pVertices,
                                  List<GraphEdge> pEdges, int pSize) {
    Position minPos = Position.min(graph.vertices);
    Position maxPos = Position.max(graph.vertices);
    float lWidth = Position.width(maxPos, minPos);

    Position offset = Position.add(Position.mult(minPos, -1), 0.05f * lWidth);
    // Flip y-coordinate.
    Position scale = new Position(1, -1, 1);
    scale.mult(0.9f * pSize / lWidth);

    // Draw the edges.
    if (Option.showEdges.getBool()) {
      for (GraphEdge e : pEdges) {
        GraphVertex sourceVertex = graph.vertices.get(e.x);
        GraphVertex targetVertex = graph.vertices.get(e.y);
        // Only draw the edge if it and both of its incident vertices are visible
        if (sourceVertex.showVertex && targetVertex.showVertex && !e.auxiliary) {
          Position u = Position.add(graph.vertices.get(e.x).pos, offset);
          u.mult(scale);
          // Flip y-coordinate.
          u.y += pSize;
          Position v = Position.add(graph.vertices.get(e.y).pos, offset);
          v.mult(scale);
          // Flip y-coordinate.
          v.y += pSize;
          writeEdge(e, (int) u.x, (int) u.y, (int) u.z, (int) v.x, (int) v.y,
              (int) v.z);
        }
      }
    }

    // Draw the vertices.
    // First draw the vertices that are not annotated (put them to background).
    for (GraphVertex lCurrVertex : pVertices) {
      if (lCurrVertex.showVertex
          && !(Option.hideSource.getBool() && lCurrVertex.isSource)
          && !lCurrVertex.auxiliary && !lCurrVertex.showName) {
        int radius =
                     (int) Math.max(Math.pow(lCurrVertex.degree, 0.5)
                                    * Option.minVert.getFloat(), Option.minVert
                         .getFloat());
        Position u = Position.add(lCurrVertex.pos, offset);
        u.mult(scale);
        // Flip y-coordinate.
        u.y += pSize;
        writeVertex(lCurrVertex, (int) u.x, (int) u.y, (int) u.z, radius);
      }
    }

    // Draw the annotated vertices.
    // Second draw the annotated vertices (put them to foreground).
    for (GraphVertex lCurrVertex : pVertices) {
      if (lCurrVertex.showVertex
          && !(Option.hideSource.getBool() && lCurrVertex.isSource)
          && !lCurrVertex.auxiliary && lCurrVertex.showName) {
        int radius =
                     (int) Math.max(Math.pow(lCurrVertex.degree, 0.5)
                                    * Option.minVert.getFloat(), Option.minVert
                         .getFloat());
        Position u = Position.add(lCurrVertex.pos, offset);
        u.mult(scale);
        // Flip y-coordinate.
        u.y += pSize;
        writeVertex(lCurrVertex, (int) u.x, (int) u.y, (int) u.z, radius);
      }
    }

  }

  /**
   * Writes a vertex.
   * @param curVertex  The vertex object, to access vertex attributes.
   * @param xPos       x coordinate of the vertex.
   * @param yPos       y coordinate of the vertex.
   * @param zPos       z coordinate of the vertex.
   * @param radius     Radius of the vertex.
   */
  abstract public void writeVertex(GraphVertex curVertex, int xPos, int yPos,
                                   int zPos, int radius);

  /**
   * Writes an edge.
   * @param edge        an edge in graph.edges
   * @param xPos1       x coordinate of the first point.
   * @param yPos1       y coordinate of the first point.
   * @param zPos1       z coordinate of the first point.
   * @param xPos2       x coordinate of the second point.
   * @param yPos2       y coordinate of the second point.
   * @param zPos2       z coordinate of the second point.
   */
  abstract public void writeEdge(GraphEdge edge, int xPos1, int yPos1,
                                 int zPos1, int xPos2, int yPos2, int zPos2);

  /**
   * Calculates the two angled lines that form the arrow head.
   */
  protected int[] paintArrow(int x0, int y0, int x1, int y1) {

    int[] arrowPoints = new int[8];

    double angleFactor = 0.5;
    double tipLocPercentage = 0.9 - Option.minVert.getFloat() * 0.02;
    double xMid = x0 + tipLocPercentage * (x1 - x0);
    double yMid = y0 + tipLocPercentage * (y1 - y0);

    double deltaX = xMid - x0;
    double deltaY = yMid - y0;
    deltaX *= angleFactor;
    deltaY *= angleFactor;
    double diaLength =
                       (Math.sqrt(deltaX * deltaX + deltaY * deltaY) * Math
                           .sqrt(2));
    double arrowTipSizeFactor = 5;

    // (deltaY, -deltaX)
    double x2 = (x0 + deltaY - xMid) / diaLength;
    double y2 = (y0 - deltaX - yMid) / diaLength;

    // (-deltaY, deltaX)
    double x3 = (x0 - deltaY - xMid) / diaLength;
    double y3 = (y0 + deltaX - yMid) / diaLength;

    double xt2 = xMid + arrowTipSizeFactor * x2;
    double yt2 = yMid + arrowTipSizeFactor * y2;
    double xt3 = xMid + arrowTipSizeFactor * x3;
    double yt3 = yMid + arrowTipSizeFactor * y3;

    arrowPoints[0] = (int) Math.round(xMid);
    arrowPoints[1] = (int) Math.round(yMid);

    arrowPoints[2] = (int) Math.round(xt2);
    arrowPoints[3] = (int) Math.round(yt2);

    arrowPoints[4] = (int) Math.round(xMid);
    arrowPoints[5] = (int) Math.round(yMid);

    arrowPoints[6] = (int) Math.round(xt3);
    arrowPoints[7] = (int) Math.round(yt3);

    return arrowPoints;
  }

};
