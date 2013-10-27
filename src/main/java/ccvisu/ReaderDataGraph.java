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

import java.io.BufferedReader;
import java.util.List;

import ccvisu.Options.Verbosity;

/*****************************************************************
 * Reader for input graphs.
 * Different concrete graph readers return what they read in String format 
 * (list of edges of type <code>GraphEdgeString</code>)
 * when <code>readEdges()</code> is called.
 * One single transformation method (<code>readGraph()</code> of this class)
 * transforms the string representation into the final format 
 * (<code>GraphData</code> object with edges of type <code>GraphEdgeInt</code>).
 * @version  $Revision: 1.43 $; $Date: 2007/12/15 02:03:35 $
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class ReaderDataGraph extends ReaderData {
  /** End of line.*/
  protected final static String endl = CCVisu.endl;

  /**
   * Constructor.
   * @param in  Stream reader object.
   */
  public ReaderDataGraph(BufferedReader in, Verbosity pVerbosity) {
    super(in, pVerbosity);
  }

  /*****************************************************************
   * Reads the graph data from stream reader <code>in</code>.
   * @param pGraph  <code>GraphData</code> object to store the graph data in.
   *****************************************************************/
  @Override
  public void read(GraphData pGraph) {
    pGraph.mTuples = readTuples();
    // Initialize graph representation.
    initializeGraph(pGraph);
  }

  /*****************************************************************
   * Reads the graph data from list of string edges (see class comment).
   *****************************************************************/
  private void initializeGraph(GraphData pGraph) {
    int lLineNo = 0;
    for (List<String> itTuple : pGraph.mTuples) {
      ++lLineNo;
      if (!itTuple.isEmpty() && itTuple.get(0).charAt(0) != '#') {
        if (itTuple.size() < 3) {
          System.err.println("Runtime error: Exception while reading "
                             + "a graph edge, at line " + lLineNo + ":");
          System.err.println("Input graph file needs to follow the RSF format");
          System.err.println("'<graph-name> <source-node> <target-node>'.");
        }
        // Relation name.
        String lEdgeRelName = itTuple.get(0);
        // Source vertex.
        String lEdgeSourceVertex = itTuple.get(1);
        // Target vertex.
        String lEdgeTargetVertex = itTuple.get(2);
        // Edge weight.
        String lEdgeWeight = "1.0";
        if (itTuple.size() > 3) {
          lEdgeWeight = itTuple.get(3);
        }

        // Source vertex.
        GraphVertex x = new GraphVertex();
        x.name = lEdgeSourceVertex;
        // Target vertex.
        GraphVertex y = new GraphVertex();
        y.name = lEdgeTargetVertex;

        // Insert x-vertex to graph.
        if (!pGraph.nameToVertex.containsKey(x.name)) {
          x.id = pGraph.vertices.size();
          pGraph.vertices.add(x);
          pGraph.nameToVertex.put(x.name, x);
        }
        // Use existing vertex, if not the same (id field).
        x = pGraph.nameToVertex.get(x.name);
        x.isSource = true;

        // Insert y-vertex to graph.
        if (!pGraph.nameToVertex.containsKey(y.name)) {
          y.id = pGraph.vertices.size();
          pGraph.vertices.add(y);
          pGraph.nameToVertex.put(y.name, y);
        }
        // Use existing vertex, if not the same (id field).
        y = pGraph.nameToVertex.get(y.name);
        y.isSource = false;

        // Insert edge to graph.
        GraphEdge edge = new GraphEdge();
        edge.relName = lEdgeRelName;
        edge.x = x.id;
        edge.y = y.id;
        // (Detection of reflexive edges is done by CCVisu.computeLayout().)
        try {
          edge.w = Math.abs(Float.parseFloat(lEdgeWeight));
        } catch (Exception e) {
          if (mVerbosity.isAtLeast(Verbosity.WARNING)) {
            System.err.println("RSF warning: Float expected for relation '"
                               + lEdgeRelName + "' at '" + lEdgeWeight
                               + "', at line " + lLineNo + ".");
          }
          edge.w = 1.0f;
        }
        pGraph.edges.add(edge);
        // Adjust degrees of the vertices.
        x.degree += edge.w;
        y.degree += edge.w;
        if (x.degree < 0 || y.degree < 0) {
          System.err.println("Invalid graph: edge {" + x.name + "," + y.name
                             + "} " + "has weight: " + edge.w + ".");
        }
      }
    }
    return;
  }

  /*****************************************************************
   * Reads the edges of a graph from stream reader <code>in</code>, 
   * and stores them in a list (of <code>GraphEdgeString</code> elements).
   * @return List of string edges.
   *****************************************************************/
  abstract public Relation readTuples();

};
