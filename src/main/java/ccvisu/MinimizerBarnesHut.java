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
 * Andreas Noack (an@informatik.tu-cottbus.de)
 * University of Technology at Cottbus, Germany
 * Dirk Beyer    (firstname.lastname@uni-passau.de)
 * University of Passau, Bavaria, Germany
 */

package ccvisu;

import ccvisu.Options.Verbosity;

/*****************************************************************
 * Minimizer for the (weighted) (edge-repulsion) LinLog energy model,
 * based on the Barnes-Hut algorithm.
 * @version  $Revision: 1.31 $; $Date: 2007/12/15 02:03:35 $
 * @author   Andreas Noack and Dirk Beyer
 * Created:  Andreas Noack, 2004-04-01.
 * Changed:  Dirk Beyer:
 *           Extended to edge-repulsion, according to the IWPC 2005 paper.
 *           Data structures changed to achieve O(n log n) space complexity.
 *           Energy model extended to a weighted version.
 *           2006-02-08: Energy model changed to flexible repulsion exponent.
 *                       Some bug fixes from Andreas integrated.
 *****************************************************************/
public class MinimizerBarnesHut extends Minimizer {
  /** Options for the minimizer. */
  private final Options        options;
  /** Number of nodes. */
  private final int            nodeNr;
  /** The graph that this layout is computed for.
   *    node. fixedPos: The minimizer does not change the position
   *    of a node with fixedPos == true.
   *    pos: Position matrix (3 dimensions for every node).
   *    Is not copied and serves as input and output 
   *    of <code>minimizeEnergy</code>.
   *    If the input is two-dimensional (i.e. pos[i][2] == 0
   *    for all i), the output is also two-dimensional.
   *    Random initial positions are appropriate.
   *    Preconditions: dimension at least [nodeNr][3];
   *    no two different nodes have the same position
   *    The input positions should be scaled such that 
   *    the average Euclidean distance between connected nodes 
   *    is roughly 1. */
  private final GraphData      graph;

  /** The following two must be symmetric. */

  /** Node indexes of the similarity list for each node.
   *    Is not copied and not modified by minimizeEnergy.
   *    (attrIndexes[i][k] == j) represents the k-th edge 
   *    of node i, namely (i,j).
   *    Omit edges with weight 0.0 (i.e. non-edges). 
   *    Preconditions:
   *    (attrIndexes[i][k] != i) for all i,k (irreflexive);
   *    (attrIndexes[i][k1] == j) iff (attrIndexes[j][k2] == i) 
   *     for all i, j, exists k1, k2 (symmetric). */
  private final int            attrIndexes[][];
  /** Similarity values of the similarity lists for each node.
   *    Is not copied and not modified by minimizeEnergy.
   *    For each (attrIndexes[i][k] == j), (attrValues[i][k] == w)
   *    represents the weight w of edge (i,j).
   *    For unweighted graphs use only 1.0f as edge weight. 
   *    Preconditions:
   *    exists k1: (attrIndexes[i][k1] == j) and (attrValues[i][k1] == w) iff
   *    exists k2: (attrIndexes[j][k2] == i) and (attrValues[j][k2] == w) 
   *    (symmetric). */
  private final float          attrValues[][];

  /** Repulsion vector (node weights).
   *    Is not copied and not modified by minimizeEnergy.
   *    For for node repulsion use 1.0f for all nodes.
   *    Preconditions: dimension at least [nodeNr];
   *    repu[i] >= 1 for all i. */
  private final float          repu[];

  /** Exponent of the Euclidean distance in the attraction term of the energy model.
   *    1.0f for the LinLog models, 3.0f for the energy
   *    version of the Fruchterman-Reingold model.
   *    If 0.0f, the log of the distance is taken instead of a constant fun.
   *    (Value 0.0f not yet tested.) */
  private float                attrExponent      = 1.0f;

  /** Exponent of the Euclidean distance in the repulsion term of the energy model.
  *     0.0f for the LinLog models, 0.0f for the energy
  *     version of the Fruchterman-Reingold model.
  *     If 0.0f, the log of the distance is taken instead of a constant fun. */
  private float                repuExponent      = 0.0f;

  /** Position of the barycenter of the nodes. */
  private Position             baryCenter        = new Position(); ;

  /** Factor for the gravitation energy (attraction to the barycenter),
   *    0.0f for no gravitation. */
  private float                gravitationFactor = 0.0f;

  /** Factor for repulsion energy that normalizes average distance 
    * between pairs of nodes with maximum similarity to (roughly) 1. */
  private float                repuFactor        = 1.0f;

  /** Factors for the repulsion force for pulsing. */
  private static final float[] repuStrategy      =
                                                   { 0.95f, 0.9f, 0.85f, 0.8f,
      0.75f, 0.8f, 0.85f, 0.9f, 0.95f, 1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f,
      1.4f, 1.3f, 1.2f, 1.1f, 1.0f                };
  /** Octtree for repulsion computation. */
  private OctTree              octTree           = null;

  /**
   * Sets the number of nodes, the similarity matrices (edge weights), 
   *   and the position matrix.
   */
  public MinimizerBarnesHut(Options pOpt) {
    this.options = pOpt;
    this.nodeNr = options.graph.vertices.size();
    this.graph = options.graph;
    this.attrExponent = options.attrExponent;
    this.repuExponent = options.repuExponent;
    this.gravitationFactor = options.gravitation;

    // Create graph layout data structure, allocate memory.
    // Positions are already initialized (given by graph).

    // Initialize repulsions.
    repu = new float[options.graph.vertices.size()];
    for (int i = 0; i < options.graph.vertices.size(); ++i) {
      // Set repulsion according to the energy model.
      if (options.vertRepu) {
        repu[i] = 1.0f;
      } else {
        GraphVertex curVertex = options.graph.vertices.get(i);
        repu[i] = curVertex.degree;
      }
    }

    // Initialize attractions.
    // Vertex indexes of the similarity lists. 
    attrIndexes = new int[options.graph.vertices.size()][];
    // Similarity values of the similarity lists.
    attrValues = new float[options.graph.vertices.size()][];
    {
      // Compute length of row lists.
      int[] attrCounter = new int[options.graph.vertices.size()];
      for (GraphEdge e : options.graph.edges) {
        if (e.x == e.y) {
          GraphVertex curVertex = options.graph.vertices.get(e.x);
          System.err.println("Layout warning: Reflexive edge for vertex '"
                             + curVertex.name + "' found.");
        } else {
          ++attrCounter[e.x];
          ++attrCounter[e.y];
        }
      }

      // Allocate the rows.
      for (int i = 0; i < options.graph.vertices.size(); i++) {
        attrIndexes[i] = new int[attrCounter[i]];
        attrValues[i] = new float[attrCounter[i]];
      }

      // Transfer the edges to the similarity lists.
      attrCounter = new int[options.graph.vertices.size()];
      for (GraphEdge e : options.graph.edges) {
        // We must not add reflexive edges.
        if (e.x != e.y) {
          // Similarity list must be symmetric.
          attrIndexes[e.x][attrCounter[e.x]] = e.y;
          attrIndexes[e.y][attrCounter[e.y]] = e.x;
          // Set similarities according to the energy model.
          if (options.noWeight) {
            attrValues[e.x][attrCounter[e.x]] = 1.0f;
            attrValues[e.y][attrCounter[e.y]] = 1.0f;
          } else {
            attrValues[e.x][attrCounter[e.x]] = e.w;
            attrValues[e.y][attrCounter[e.y]] = e.w;
          }

          ++attrCounter[e.x];
          ++attrCounter[e.y];
        }
      }

    }
  }

  /**
   * Iteratively minimizes energy using the Barnes-Hut algorithm.
   * Starts from the positions in <code>pos</code>, 
   * and stores the computed positions in <code>pos</code>.
   */
  @Override
  public void minimizeEnergy() {
    if (nodeNr <= 1) { return; }

    if (options.verbosity.isAtLeast(Verbosity.WARNING)) {
      System.err.println();
      System.err.println("Note: Minimizer will run " + options.nrIterations
                         + " iterations,");
      System.err
          .println("increase (decrease) this number with option -iter to");
      System.err.println("increase quality of layout (decrease runtime).");
    }

    analyzeDistances();

    final float finalRepuFactor = computeRepuFactor();
    repuFactor = finalRepuFactor;

    // compute initial energy
    buildOctTree();
    float energySum = 0.0f;
    for (int i = 0; i < nodeNr; i++) {
      energySum += getEnergy(i);
    }
    if (options.verbosity.isAtLeast(Verbosity.WARNING)) {
      System.err.println();
      System.err.println("initial   energy " + energySum + "   repulsion "
                         + repuFactor);
    }

    //notify the listeners
    GraphEvent evt = new GraphEvent(this);
    for (GraphEventListener l : listeners) {
      l.onGraphEvent(evt);
    }

    // minimize energy
    for (int step = 1; step <= options.nrIterations; step++) {

      computeBaryCenter();
      buildOctTree();

      // except in the last 20 iterations, vary the repulsion factor
      // according to repuStrategy
      if (step < (options.nrIterations - 20)) {
        repuFactor =
                     finalRepuFactor
                         * (float) Math
                             .pow(
                                 MinimizerBarnesHut.repuStrategy[step
                                                                 % MinimizerBarnesHut.repuStrategy.length],
                                 attrExponent - repuExponent);
      } else {
        repuFactor = finalRepuFactor;
      }

      // for all non-fixed nodes: minimize energy, i.e., move each node
      energySum = 0.0f;
      for (int i = 0; i < nodeNr; i++) {
        GraphVertex lCurrVertex = graph.vertices.get(i);
        if (!lCurrVertex.fixedPos) {

          // compute direction of the move of the node
          Position bestDir = new Position();
          getDirection(i, bestDir);

          // line search: compute length of the move
          Position oldPos = new Position(lCurrVertex.pos);
          float oldEnergy = getEnergy(i);
          float bestEnergy = oldEnergy;
          int bestMultiple = 0;
          bestDir.div(32);
          for (int multiple = 32; multiple >= 1
                                  && (bestMultiple == 0 || bestMultiple / 2 == multiple); multiple /=
                                                                                                      2) {
            // lCurrPos = oldPos + bestDir * multiple;
            lCurrVertex.pos =
                              Position.add(oldPos, Position.mult(bestDir,
                                  multiple));
            float curEnergy = getEnergy(i);
            if (curEnergy < bestEnergy) {
              bestEnergy = curEnergy;
              bestMultiple = multiple;
            }
          }

          for (int multiple = 64; multiple <= 128
                                  && bestMultiple == multiple / 2; multiple *=
                                                                               2) {
            // lCurrPos = oldPos + bestDir * multiple;
            lCurrVertex.pos =
                              Position.add(oldPos, Position.mult(bestDir,
                                  multiple));
            float curEnergy = getEnergy(i);
            if (curEnergy < bestEnergy) {
              bestEnergy = curEnergy;
              bestMultiple = multiple;
            }
          }

          // lCurrVertex.pos = oldPos + bestDir * bestMultiple;
          lCurrVertex.pos =
                            Position.add(oldPos, Position.mult(bestDir,
                                bestMultiple));
          if (bestMultiple > 0) {
            octTree.moveNode(oldPos, lCurrVertex.pos, repu[i]); //1.0f);
          }
          energySum += bestEnergy;
        }
      } // for
      if (options.verbosity.isAtLeast(Verbosity.WARNING)) {
        System.err.println("iteration " + step + "   energy " + energySum
                           + "   repulsion " + repuFactor);
      }

      //notify the listeners
      for (GraphEventListener l : listeners) {
        try {
          l.onGraphEvent(evt);
        } catch (Exception s) {
          // The listener (i.e., the drawing frame) was closed.
          System.exit(0);
        }

      }

    }
    analyzeDistances();
    //new JTreeFrame(octTree);
  }

  /**
   * Returns the Euclidean distance between the specified positions.
   * @return Euclidean distance between the specified positions.
   */
  private float getDist(Position pos1, Position pos2) {
    Position lDiff = Position.subtract(pos1, pos2);
    return (float) Math.sqrt(lDiff.x * lDiff.x + lDiff.y * lDiff.y + lDiff.z
                             * lDiff.z);
  }

  /** 
   * Returns the repulsion energy between the node with the specified index
   * and the nodes in the octtree.
   * 
   * @param index Index of the repulsing node.
   * @param tree  Octtree containing repulsing nodes.
   * @return Repulsion energy between the node with the specified index
   *         and the nodes in the octtree.
   */
  private float getRepulsionEnergy(final int index, OctTree tree) {
    if (tree == null || tree.index == index || index >= repu.length) { return 0.0f; }

    float dist = getDist(graph.vertices.get(index).pos, tree.position);
    if (tree.index < 0 && dist < 2.0f * tree.width()) {
      float energy = 0.0f;
      for (OctTree lElement : tree.children) {
        energy += getRepulsionEnergy(index, lElement);
      }
      return energy;
    }

    if (repuExponent == 0.0f) {
      return -repuFactor * tree.weight * (float) Math.log(dist) * repu[index];
    } else {
      return -repuFactor * tree.weight * (float) Math.pow(dist, repuExponent)
             / repuExponent * repu[index];
    }
  }

  /**
   * Returns the energy of the specified node.
   * @param   index   Index of a node.
   * @return  Energy of the node.
  */
  private float getEnergy(final int index) {
    Position lVertexIndexPos = graph.vertices.get(index).pos;

    // repulsion energy
    float energy = getRepulsionEnergy(index, octTree);

    // attraction energy
    for (int i = 0; i < attrIndexes[index].length; i++) {
      if (attrIndexes[index][i] != index) {
        float dist =
                     getDist(graph.vertices.get(attrIndexes[index][i]).pos,
                         lVertexIndexPos);
        if (attrExponent == 0.0f) {
          energy += attrValues[index][i] * (float) Math.log(dist);
        } else {
          energy +=
                    attrValues[index][i] * (float) Math.pow(dist, attrExponent)
                        / attrExponent;
        }
      }
    }

    // gravitation energy
    float dist = getDist(lVertexIndexPos, baryCenter);
    if (attrExponent == 0.0f) {
      energy +=
                gravitationFactor * repuFactor * repu[index]
                    * (float) Math.log(dist);
    } else {
      energy +=
                gravitationFactor * repuFactor * repu[index]
                    * (float) Math.pow(dist, attrExponent) / attrExponent;
    }
    return energy;
  }

  /**
   * Computes the direction of the repulsion force from the tree 
   *     on the specified node.
   * @param  index   Index of the repulsed node.
   * @param  tree    Repulsing octtree.
   * @param  dir     Direction of the repulsion force acting on the node
   *                 is added to this variable (output parameter).
   * @return Approximate second derivation of the repulsion energy.
   */
  private float addRepulsionDir(final int index, OctTree tree, Position dir) {
    GraphVertex lVertexIndex = graph.vertices.get(index);

    if (tree == null || tree.index == index) { return 0.0f; }

    float dist = getDist(lVertexIndex.pos, tree.position);
    if (tree.index < 0 && dist < tree.width()) {
      float dir2 = 0.0f;
      for (OctTree lElement : tree.children) {
        dir2 += addRepulsionDir(index, lElement, dir);
      }
      return dir2;
    }

    if (dist != 0.0) {
      float tmp =
                  repuFactor * tree.weight * repu[index]
                      * (float) Math.pow(dist, repuExponent - 2);
      // dir -= (tree.position - lVertexIndex) * tmp;
      dir.subtract(Position.mult(Position.subtract(tree.position,
          lVertexIndex.pos), tmp));
      return tmp * Math.abs(repuExponent - 1);
    }

    return 0.0f;
  }

  /**
   * Computes the direction of the total force acting on the specified node.
   * @param  index   Index of a node.
   * @param  dir     Direction of the total force acting on the node
   *                 (output parameter).
   */
  private void getDirection(final int index, Position dir) {
    Position lVertexIndexPos = graph.vertices.get(index).pos;
    dir.x = 0.0f;
    dir.y = 0.0f;
    dir.z = 0.0f;

    // compute repulsion force vector        
    float dir2 = addRepulsionDir(index, octTree, dir);

    // compute attraction force vector
    for (int i = 0; i < attrIndexes[index].length; i++) {
      if (attrIndexes[index][i] != index) {

        float dist =
                     getDist(graph.vertices.get(attrIndexes[index][i]).pos,
                         lVertexIndexPos);
        float tmp =
                    attrValues[index][i]
                        * (float) Math.pow(dist, attrExponent - 2);
        dir2 += tmp * Math.abs(attrExponent - 1);

        // dir += (graph.vertices.get(attrIndexes[index][i]).pos - lVertexIndexPos)
        //         * tmp;
        dir.add(Position.mult(Position.subtract(graph.vertices
            .get(attrIndexes[index][i]).pos, lVertexIndexPos), tmp));
      }
    }

    // compute gravitation force vector      
    float dist = getDist(lVertexIndexPos, baryCenter);
    dir2 +=
            gravitationFactor * repuFactor * repu[index]
                * (float) Math.pow(dist, attrExponent - 2)
                * Math.abs(attrExponent - 1);

    // dir +=  gravitationFactor * repuFactor * repu[index]
    //             * (float) Math.pow(dist, attrExponent - 2)
    //             * (baryCenter - lVertexIndexPos);
    dir.add(Position.mult(Position.subtract(baryCenter, lVertexIndexPos),
        gravitationFactor * repuFactor * repu[index]
            * (float) Math.pow(dist, attrExponent - 2)));

    // normalize force vector with second derivation of energy
    dir.div(dir2);

    // ensure that the length of dir is at most 1/8
    // of the maximum Euclidean distance between nodes
    float length =
                   (float) Math.sqrt(dir.x * dir.x + dir.y * dir.y + dir.z
                                     * dir.z);
    if (length > octTree.width() / 8) {
      length /= octTree.width() / 8;
      dir.div(length);
    }
  }

  /**
   * Builds the octtree.
   */
  private void buildOctTree() {
    // compute minima and maxima of positions in each dimension
    Position minPos = Position.min(graph.vertices);
    Position maxPos = Position.max(graph.vertices);

    // add nodes to the octtree
    octTree =
              new OctTree(0, graph.vertices.get(0).pos, repu[0], minPos, maxPos);
    for (int i = 1; i < nodeNr; i++) {
      octTree.addNode(i, graph.vertices.get(i).pos, repu[i]); // 1.0f);
    }
  }

  /**
   * Computes the factor for repulsion forces <code>repuFactor</code>
   * such that in the energy minimum the average Euclidean distance
   * between pairs of nodes with similarity 1.0 is approximately 1.
   */
  private float computeRepuFactor() {
    float attrSum = 0.0f;
    for (int i = 1; i < nodeNr; i++) {
      for (int j = 0; j < attrValues[i].length; j++) {
        attrSum += attrValues[i][j];
      }
    }

    float repuSum = 0.0f;
    for (int i = 0; i < nodeNr; i++) {
      repuSum += repu[i];
    }
    if (repuSum > 0 && attrSum > 0) { return attrSum
                                             / (repuSum * repuSum)
                                             * (float) Math
                                                 .pow(
                                                     repuSum,
                                                     0.5f * (attrExponent - repuExponent)); }
    return 1.0f;
  }

  /** 
   * Computes the position of the barycenter <code>baryCenter</code>
   * of all nodes.
   */
  private void computeBaryCenter() {
    // Reset.
    baryCenter = new Position();
    for (GraphVertex lCurrVertex : graph.vertices) {
      baryCenter.add(lCurrVertex.pos);
    }
    baryCenter.div(nodeNr);
  }

  /**
   * Computes and outputs some statistics. 
   */
  private void analyzeDistances() {
    float edgeLengthSum = 0.0f;
    float edgeLengthLogSum = 0.0f;
    float attrSum = 0.0f;

    for (int i = 0; i < nodeNr; i++) {
      GraphVertex lVertexIndex = graph.vertices.get(i);
      for (int j = 0; j < attrValues[i].length; j++) {
        float dist =
                     getDist(lVertexIndex.pos, graph.vertices
                         .get(attrIndexes[i][j]).pos);
        float distLog = (float) Math.log(dist);
        edgeLengthSum += attrValues[i][j] * dist;
        edgeLengthLogSum += attrValues[i][j] * distLog;
        attrSum += attrValues[i][j];
      }
    }
    edgeLengthSum /= 2;
    edgeLengthLogSum /= 2;
    attrSum /= 2;
    if (options.verbosity.isAtLeast(Verbosity.WARNING)) {
      System.err.println();
      System.err.println("Number of Nodes: " + nodeNr);
      System.err.println("Overall Attraction: " + attrSum);
      System.err.println("Arithmetic mean of edge lengths: " + edgeLengthSum
                         / attrSum);
      System.err.println("Geometric mean of edge lengths: "
                         + (float) Math.exp(edgeLengthLogSum / attrSum));
    }
  }

  /**
   * Octtree for graph nodes with positions in 3D space.
   * Contains all graph nodes that are located in a given cuboid in 3D space.
   * 
   * @author Andreas Noack
   */
  private class OctTree {
    /** For leafs, the unique index of the graph node; for non-leafs -1. */
    private int             index;
    /** Children of this tree node. */
    private final OctTree[] children = new OctTree[8];
    /** Barycenter of the contained graph nodes. */
    private Position        position;
    /** Total weight of the contained graph nodes. */
    private float           weight;
    /** Minimum coordinates of the cuboid in each of the 3 dimensions. */
    private final Position  minPos;
    /** Maximum coordinates of the cuboid in each of the 3 dimensions. */
    private final Position  maxPos;

    /**
     * Creates an octtree containing one graph node.
     *  
     * @param index    Unique index of the graph node.
     * @param position Position of the graph node.
     * @param weight   Weight of the graph node.
     * @param minPos   Minimum coordinates of the cuboid.
     * @param maxPos   Maximum coordinates of the cuboid.
     */
    private OctTree(int index, Position position, float weight,
                    Position minPos, Position maxPos) {
      this.index = index;
      this.position = new Position(position);
      this.weight = weight;
      this.minPos = minPos;
      this.maxPos = maxPos;
    }

    /**
     * Adds a graph node to the octtree.
     * 
     * @param nodeIndex  Unique index of the graph node.
     * @param nodePos    Position of the graph node.
     * @param nodeWeight Weight of the graph node.
     */
    private void addNode(int nodeIndex, Position nodePos, float nodeWeight) {
      if (nodeWeight == 0.0f) { return; }

      if (index >= 0) {
        addNode2(index, position, weight);
        index = -1;
      }

      // position = (position * weight + nodePos * nodeWeight)
      //                / (weight + nodeWeight)
      position =
                 Position.div(Position.add(Position.mult(position, weight),
                     Position.mult(nodePos, nodeWeight)), weight + nodeWeight);
      weight += nodeWeight;

      addNode2(nodeIndex, nodePos, nodeWeight);
    }

    /**
     * Adds a graph node to the octtree, 
     * without changing the position and weight of the root.
     * 
     * @param nodeIndex  Unique index of the graph node.
     * @param nodePos    Position of the graph node.
     * @param nodeWeight Weight of the graph node.
     */
    private void addNode2(int nodeIndex, Position nodePos, float nodeWeight) {
      int childIndex = 0;
      if (nodePos.x > (minPos.x + maxPos.x) / 2) {
        childIndex += 1 << 0;
      }
      if (nodePos.y > (minPos.y + maxPos.y) / 2) {
        childIndex += 1 << 1;
      }
      if (nodePos.z > (minPos.z + maxPos.z) / 2) {
        childIndex += 1 << 2;
      }

      if (children[childIndex] == null) {
        Position newMinPos = new Position();
        Position newMaxPos = new Position();

        if ((childIndex & 1 << 0) == 0) {
          newMinPos.x = minPos.x;
          newMaxPos.x = (minPos.x + maxPos.x) / 2;
        } else {
          newMinPos.x = (minPos.x + maxPos.x) / 2;
          newMaxPos.x = maxPos.x;
        }
        if ((childIndex & 1 << 1) == 0) {
          newMinPos.y = minPos.y;
          newMaxPos.y = (minPos.y + maxPos.y) / 2;
        } else {
          newMinPos.y = (minPos.y + maxPos.y) / 2;
          newMaxPos.y = maxPos.y;
        }
        if ((childIndex & 1 << 2) == 0) {
          newMinPos.z = minPos.z;
          newMaxPos.z = (minPos.z + maxPos.z) / 2;
        } else {
          newMinPos.z = (minPos.z + maxPos.z) / 2;
          newMaxPos.z = maxPos.z;
        }

        children[childIndex] =
                               new OctTree(nodeIndex, nodePos, nodeWeight,
                                   newMinPos, newMaxPos);
      } else {
        children[childIndex].addNode(nodeIndex, nodePos, nodeWeight);
      }
    }

    /**
     * Updates the positions of the octtree nodes 
     * when the position of a graph node has changed.
     * 
     * @param oldPos     Previous position of the graph node.
     * @param newPos     New position of the graph node.
     * @param nodeWeight Weight of the graph node.
     */
    private void moveNode(Position oldPos, Position newPos, float nodeWeight) {
      //position += (newPos - oldPos) * (nodeWeight / weight);
      position.add(Position.mult(Position.subtract(newPos, oldPos), nodeWeight
                                                                    / weight));

      int childIndex = 0;
      if (oldPos.x > (minPos.x + maxPos.x) / 2) {
        childIndex += 1 << 0;
      }
      if (oldPos.y > (minPos.y + maxPos.y) / 2) {
        childIndex += 1 << 1;
      }
      if (oldPos.z > (minPos.z + maxPos.z) / 2) {
        childIndex += 1 << 2;
      }
      if (children[childIndex] != null) {
        children[childIndex].moveNode(oldPos, newPos, nodeWeight);
      }
    }

    /**
     * Returns the maximum extension of the octtree.
     * 
     * @return Maximum over all dimensions of the extension of the octtree.
     */
    private float width() {
      return Position.width(maxPos, minPos);
    }
  }

}
