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

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A class with a list of nodes that compute some informations on them
 * 
 * @version  $Revision: 1.14 $; $Date: 2007/12/15 02:03:35 $
 * @author Damien Zufferey, Dirk Beyer
 */
public class Group implements Iterable<GraphVertex> {

  /**used as mode for the method addPattern*/
  public static final int               EQUALS   = 0;
  /**used as mode for the method addPattern*/
  public static final int               CONTAINS = 1;
  /**used as mode for the method addPattern*/
  public static final int               STARTS   = 2;
  /**used as mode for the method addPattern*/
  public static final int               ENDS     = 3;

  /**contain the name of node's cluster*/
  private static String[]               indexToCltName;
  /**name of the cluster*/
  private String                        name;
  /**list of int representing the index of nodes in the GraphData*/
  private final List<GraphVertex>       nodes    = new Vector<GraphVertex>();
  /**color of the cluster*/
  private Colors                        color    = Colors.RED;

  /**x-coordinate of the barycenter*/
  private float                         x;
  /**y-coordinate of the barycenter*/
  private float                         y;
  /**z-coordinate of the barycenter*/
  private float                         z;

  private float                         averageRadius;

  /** pointer to the data*/
  private static GraphData              graph;
  private static WriterDataGraphicsDISP writer;

  /**used to know if the cluster should be drawn*/
  public boolean                        visible  = true;
  /**used to know if the circle and cross should be drawn */
  public boolean                        info     = false;
  /** true if needs to recompute radius, center,... */
  private boolean                       changed  = true;

  /**
   * Constructor
   * @param name the cluster's name
   */
  public Group(String name) {
    this.name = name;
  }

  /**
   * Constructor
   * @param name the cluster's name
   * @param color the cluster's color
   */
  public Group(String name, Colors color) {
    this.name = name;
    this.color = color;
  }

  /**
   * add the given node to the cluster
   * @param vertex
   */
  public void addNode(GraphVertex vertex) {
    nodes.add(vertex);
    vertex.color = color.get();
    changed = true;

    Group clt = Group.writer.getCluster(Group.indexToCltName[vertex.id]);
    if (clt != null) {
      clt.removeNode(vertex);
    }
    Group.indexToCltName[vertex.id] = name;
  }

  /**
   * add the node that corresponds to the index-th node in graph(GraphData)
   * without changing his color
   * function used only to assign to default cluster at begining
   * @param vertex    vertex to process
   */
  public void addNode_WO_COLOR(GraphVertex vertex) {
    nodes.add(vertex);
    changed = true;

    Group clt = Group.writer.getCluster(Group.indexToCltName[vertex.id]);
    if (clt != null) {
      clt.removeNode(vertex);
    }
    Group.indexToCltName[vertex.id] = name;
  }

  /**
   * remove from cluster the given node
   * @param vertex
   */
  public void removeNode(GraphVertex vertex) {
    nodes.remove(vertex);
    changed = true;
  }

  /**
   * return an iterator on the index of the cluster's nodes
   * @return iterator
   */
  public Iterator<GraphVertex> iterator() {
    return nodes.iterator();
  }

  /**
   * adds nodes to a cluster in function of a given pattern 
   * @param pattern 
   * @param mode the way of using the pattern.
   */
  public void addPattern(String pattern, int mode) {
    for (GraphVertex curVertex : Group.graph.vertices) {
      if (mode == Group.EQUALS) {
        if (curVertex.name.equals(pattern)) {
          addNode(curVertex);
        }
      } else if (mode == Group.CONTAINS) {
        if (curVertex.name.matches(".*" + pattern + ".*")) {
          addNode(curVertex);
        }
      } else if (mode == Group.STARTS) {
        if (curVertex.name.startsWith(pattern)) {
          addNode(curVertex);
        }
      } else if (mode == Group.ENDS) {
        if (curVertex.name.endsWith(pattern)) {
          addNode(curVertex);
        }
      }
    }
  }

  public void filter(String pattern, int mode, boolean keep) {
    int end = nodes.size();
    boolean match[] = new boolean[end];
    for (int i = 0; i < end; ++i) {
      match[i] = !keep;
      GraphVertex curVertex = nodes.get(i);
      if (mode == Group.EQUALS) {
        if (curVertex.name.equals(pattern)) {
          match[i] = keep;
        }
      } else if (mode == Group.CONTAINS) {
        if (curVertex.name.matches(".*" + pattern + ".*")) {
          match[i] = keep;
        }
      } else if (mode == Group.STARTS) {
        if (curVertex.name.startsWith(pattern)) {
          match[i] = keep;
        }
      } else if (mode == Group.ENDS) {
        if (curVertex.name.endsWith(pattern)) {
          match[i] = keep;
        }
      }
    }
    GraphVertex selected[] = new GraphVertex[end];
    for (int i = 0; i < end; ++i) {
      if (!match[i]) {
        selected[i] = nodes.get(i);
      }
    }
    Group defaultClt = Group.writer.getCluster(0);
    for (int i = 0; i < end; ++i) {
      if (selected[i] != null) {
        defaultClt.addNode(selected[i]);
      }
    }
  }

  public List<GraphVertex> getNodes() {
    return nodes;
  }

  /**
   * @return return the color of the cluster.
   */
  public Colors getColor() {
    return color;
  }

  /**
   * @param color color to define.
   */
  public void setColor(Colors color) {
    this.color = color;
    for (GraphVertex curVertex : nodes) {
      curVertex.color = color.get();
    }
  }

  /**
   * @return return the name of the cluster.
   */
  public String getName() {
    return name;
  }

  /**
   * @return return the name of the cluster.
   */
  @Override
  public String toString() {
    return name;
  }

  /**
   * @return return the averageRadius.
   */
  public float getAverageRadius() {
    if (changed) {
      compute();
    }
    return averageRadius;
  }

  /**
   * return the x-coordinate of the barycenter
   * @return x
   */
  public float getX() {
    if (changed) {
      compute();
    }
    return x;
  }

  /**
   * return the y-coordinate of the barycenter
   * @return y
   */
  public float getY() {
    if (changed) {
      compute();
    }
    return y;
  }

  /**
   * return the z-coordinate of the barycenter
   * @return z
   */
  public float getZ() {
    if (changed) {
      compute();
    }
    return z;
  }

  /**
   * compute the informations provided by the cluster
   */
  private void compute() {
    int nbr = nodes.size();
    //barycenter
    x = 0;
    y = 0;
    z = 0;
    for (int i = 0; i < nbr; ++i) {
      GraphVertex lCurrVertex = nodes.get(i);
      x += lCurrVertex.pos.x;
      y += lCurrVertex.pos.y;
      z += lCurrVertex.pos.z;
    }
    x /= nbr;
    y /= nbr;
    z /= nbr;
    //radius
    averageRadius = 0;
    for (int i = 0; i < nbr; ++i) {
      GraphVertex lCurrVertex = nodes.get(i);
      float delta_x = (float) Math.pow(lCurrVertex.pos.x - x, 2);
      float delta_y = (float) Math.pow(lCurrVertex.pos.y - y, 2);
      float delta_z = (float) Math.pow(lCurrVertex.pos.z - z, 2);
      averageRadius += Math.sqrt(delta_x + delta_y + delta_z);
    }
    averageRadius /= nbr;

    changed = false;
  }

  /**
   * initialize Data common to all clusters
   * @param writer
   * @param graph
   */
  public static void init(WriterDataGraphicsDISP writer, GraphData graph) {
    Group.writer = writer;
    Group.graph = graph;
    Group.indexToCltName = new String[graph.vertices.size()];
  }

  /**
   * tells the cluster to recompute its informations
   */
  public void graphchanged() {
    this.changed = true;
  }

}
