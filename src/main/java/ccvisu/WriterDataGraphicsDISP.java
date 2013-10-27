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
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListModel;

import ccvisu.Options.Option;

/*****************************************************************
 * Writer for displaying the layout on the screen device.
 * @version  $Revision: 1.70 $; $Date: 2009-01-20 22:30:37 $
 * @author   Dirk Beyer
 *****************************************************************/
public class WriterDataGraphicsDISP extends WriterDataGraphics {

  private final FrameDisplay             display;
  private Vector<Set<String>>            xMap;
  private Vector<Set<String>>            yMap;
  private Vector<Vector<Set<GraphEdge>>> edgeMap;

  // List of groups.
  private final DefaultListModel         clusters   = new DefaultListModel();

  //write-color (computed once and stored)
  private Color                          frontColor;

  // Temporarily associated during callback from ScreenDisplay.
  private Graphics                       mGraphics;
  private int                            insetleft;
  private int                            insetbottom;
  private int                            xSize      = 0;
  private int                            ySize      = 0;

  // Ashgan added the following data field, which is used to 
  // filter the display of vertices and edges
  private DisplayCriteria                dispFilter = null;

  /**
   * Constructor.
   * @param graph       Graph representation, contains the positions of the vertices.
   */
  public WriterDataGraphicsDISP(PrintWriter out, GraphData graph, Options opt) {
    super(out, graph, opt);

    // Adjust frontColor.
    adjustFrontColor();

    setGraphData(graph);

    display = new FrameDisplay(this, opt.frame);
    if (display == null) {
      System.err.println("Runtime error: Could not open ScreenDisplay.");
      System.exit(1);
    }

    // Ashgan added the following line
    dispFilter = new DisplayCriteria(graph);
  }

  /*****************************************************************
   * Nothing to do here.
   * The constructor initializes the ScreenDisplay (frame and canvas), 
   * and that calls back to the methods below 
   * (writeDISP, writeLAY, toggleVertexNames, getVertexNames).
   *****************************************************************/
  @Override
  public void write() {
  }

  /**
   * Writes the layout on the screen device (DISP output format).
   * Call-back method, invoked from <code>ScreenDisplay</code>.
   * @param size         Size of the output drawing square.
   * @param pGraphics         The drawing area of the canvas.
   * @param xCanvasSize  Width of the canvas.
   * @param yCanvasSize  Height of the canvas. 
   * @param insetleft    Left inset of the drawing frame.
   * @param insetbottom  Bottom inset of the drawing frame.
   */
  public void writeDISP(int size, Graphics pGraphics, int xCanvasSize,
                        int yCanvasSize, int insetleft, int insetbottom) {
    this.mGraphics = pGraphics;
    this.insetbottom = insetbottom;
    this.insetleft = insetleft;

    // Maps for getting the vertices at mouse positions.
    xMap = new Vector<Set<String>>(xCanvasSize);
    yMap = new Vector<Set<String>>(yCanvasSize);
    for (int i = 0; i < xCanvasSize; ++i) {
      xMap.add(new TreeSet<String>());
    }
    for (int i = 0; i < yCanvasSize; ++i) {
      yMap.add(new TreeSet<String>());
    }
    //Maps for getting the edges at mouse positions.
    if (Option.showEdges.getBool()) {
      if (xSize == xCanvasSize && ySize == yCanvasSize) {
        for (int x = 0; x < xCanvasSize; ++x) {
          Vector<Set<GraphEdge>> lVec = edgeMap.get(x);
          for (int y = 0; y < yCanvasSize; ++y) {
            lVec.get(y).clear();
          }
        }
      } else {
        edgeMap = new Vector<Vector<Set<GraphEdge>>>(xCanvasSize);
        for (int x = 0; x < xCanvasSize; ++x) {
          Vector<Set<GraphEdge>> lVec = new Vector<Set<GraphEdge>>(yCanvasSize);
          edgeMap.add(lVec);
          for (int y = 0; y < yCanvasSize; ++y) {
            lVec.add(new TreeSet<GraphEdge>());
          }
        }
        xSize = xCanvasSize;
        ySize = yCanvasSize;
      }
    }

    List<GraphVertex> lEmptyVertices = new ArrayList<GraphVertex>();
    List<GraphEdge> lEmptyEdges = new ArrayList<GraphEdge>();
    // Write edges only
    writeGraphicsLayout(lEmptyVertices, graph.edges, size);

    // Draw the vertices, cluster by cluster, clusters on top of normal vertices.
    for (int i = clusters.size() - 1; i >= 0; --i) {
      Group lGroup = (Group) clusters.get(i);
      if (lGroup.visible) {
        writeGraphicsLayout(lGroup.getNodes(), lEmptyEdges, size);
      }
    }

    Position minPos = Position.min(graph.vertices);
    Position maxPos = Position.max(graph.vertices);
    float lWidth = Position.width(maxPos, minPos);

    Position offset = Position.add(Position.mult(minPos, -1), 0.05f * lWidth);
    // Flip y-coordinate.
    Position scale = new Position(1, -1, 1);
    scale.mult(0.9f * size / lWidth);

    //draw the cluster specific information (except default cluster)
    for (int i = 1; i < clusters.size(); ++i) {//set begin to 1, 0 only for test
      Group clt = (Group) clusters.get(i);
      if (clt.visible && clt.info) {
        int x = (int) ((clt.getX() + offset.x) * scale.x + insetleft);
        int y = (int) ((clt.getY() + offset.y) * scale.y + size - insetbottom);
        int l = x - 5;
        int r = x + 5;
        int u = y - 5;
        int b = y + 5;
        pGraphics.setColor(Color.BLACK);
        pGraphics.drawLine(l, u, r, b);
        pGraphics.drawLine(r, u, l, b);
        pGraphics.setColor(clt.getColor().get());
        pGraphics.drawLine(l, y, r, y);
        pGraphics.drawLine(x, u, x, b);
        int radius = (int) (clt.getAverageRadius() * scale.x);
        int diam = (radius + radius);
        pGraphics.drawOval(x - radius, y - radius, diam, diam);
        pGraphics.setColor(Color.BLACK);
      }
    }
  }

  /**
   * Writes a vertex on screen.
   * @param curVertex  The vertex object, to access vertex attributes.
   * @param xPos       x coordinate of the vertex.
   * @param yPos       y coordinate of the vertex.
   * @param zPos       z coordinate of the vertex.
   * @param radius     Radius of the vertex.
   */
  @Override
  public void writeVertex(GraphVertex curVertex, int xPos, int yPos, int zPos,
                          int radius) {
    assert (curVertex.showVertex);
    assert (!curVertex.auxiliary);

    // Correction for inset.left.
    xPos = xPos + insetleft;
    //  Correction for inset.bottom.
    yPos = yPos - insetbottom;

    int startX = xPos - radius;
    int startY = yPos - radius;

    // Draw the vertex.
    int diam = 2 * radius;
    mGraphics.setColor(curVertex.color);
    mGraphics.fillOval(startX, startY, diam, diam);
    if (Option.blackCircle.getBool()) {
      mGraphics.setColor(frontColor);
      mGraphics.drawOval(startX, startY, diam, diam);
      // Draw a little loop if the vertex wishes to have a self-loop.
      if (curVertex.hasSelfLoop && Option.showEdges.getBool()) {
        mGraphics.drawOval(startX, startY, (int) ((1.8) * diam),
            (int) ((1.8) * diam + 4));
      }
    }

    if (curVertex.showName) {
      // Draw annotation.
      // Use inverted background color for the annotation.
      mGraphics.setColor(frontColor);
      String lVertexName = curVertex.name;
      if (Option.shortNames.getBool()) {
        lVertexName = NameHandler.shortenName(lVertexName);
      }
      mGraphics.drawString(lVertexName, xPos + radius + 3, yPos + 3);
    }

    try {
      // For interactive annotation: Store vertex names at their positions in the maps.
      int endX = Math.min(xPos + radius, xMap.size() - 1);
      for (int pos = Math.max(startX, 0); pos <= endX; ++pos) {
        xMap.get(pos).add(curVertex.name);
      }
      int endY = Math.min(yPos + radius, yMap.size() - 1);
      for (int pos = Math.max(startY, 0); pos <= endY; ++pos) {
        yMap.get(pos).add(curVertex.name);
      }
    } catch (Exception e) {
      // Tolerate exception (it's because of the concurrent thread).
    }

  }

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
  @Override
  public void writeEdge(GraphEdge edge, int xPos1, int yPos1, int zPos1,
                        int xPos2, int yPos2, int zPos2) {
    assert (!edge.auxiliary);

    //reflexive edges are not allowed by specification
    if (xPos1 == xPos2 && yPos1 == yPos2) { return; }

    //Correction for inset.left
    xPos1 = xPos1 + insetleft;
    xPos2 = xPos2 + insetleft;
    // Correction for inset.bottom
    yPos1 = yPos1 - insetbottom;
    yPos2 = yPos2 - insetbottom;

    //////
    //Draw
    //////
    mGraphics.setColor(edge.color);
    // area.drawLine(xPos1, yPos1, xPos2, yPos2);
    paintArrow(mGraphics, xPos1, yPos1, xPos2, yPos2);
    //Draw the annotation
    if (edge.showName) {
      int xPos = (xPos1 + xPos2 + Option.fontSize.getInt()) / 2;
      int yPos = (yPos1 + yPos2 + Option.fontSize.getInt()) / 2;
      mGraphics.drawString(edge.relName, xPos, yPos);
    }
    ///////////////////////////////////////////////////
    // For interactive annotation: Store edges at their positions in the maps.
    // Using Bresenham's line algorithm.
    boolean steep = Math.abs(yPos2 - yPos1) > Math.abs(xPos2 - xPos1);
    if (steep) {
      int tmp = xPos1;
      xPos1 = yPos1;
      yPos1 = tmp;
      tmp = xPos2;
      xPos2 = yPos2;
      yPos2 = tmp;
    }
    if (xPos1 > xPos2) {
      int tmp = xPos1;
      xPos1 = xPos2;
      xPos2 = tmp;
      tmp = yPos1;
      yPos1 = yPos2;
      yPos2 = tmp;
    }
    int deltax = xPos2 - xPos1;
    int deltay = Math.abs(yPos2 - yPos1);
    float error = 0;
    float deltaerr = ((float) deltay) / deltax;
    int y = yPos1;
    int ystep;
    if (yPos1 < yPos2) {
      ystep = 1;
    } else {
      ystep = -1;
    }
    for (int x = xPos1; x <= xPos2; ++x) {
      try {
        if (steep) {
          if (y >= 0 && y < xSize && x >= 0 && x < ySize) {
            edgeMap.get(y).get(x).add(edge);
          }
        } else {
          if (x >= 0 && x < xSize && y >= 0 && y < ySize) {
            edgeMap.get(x).get(y).add(edge);
          }
        }
        error += deltaerr;
        if (error >= 0.5) {
          y += ystep;
          error -= 1.0;
        }
      } catch (Exception e) {
        // Tolerate exception (it's because of the concurrent thread).
      }

    }
  }

  /*****************************************************************
   * Writes layout to file using an implementation of class <code>WriterData</code>.
   * Call-back method, invoked from within ScreenDisplay.
   * @param fileName     Name of the output file to write the layout to.
   *****************************************************************/
  public void writeFileLayout(String fileName) {
    try {
      PrintWriter out =
                        new PrintWriter(new BufferedWriter(new FileWriter(
                            fileName)));
      WriterData dataWriter = new WriterDataLAY(out, graph); // Default, also .lay.
      if (fileName.endsWith(".svg")) {
        dataWriter = new WriterDataGraphicsSVG(out, graph, options);
      } else if (fileName.endsWith(".wrl")) {
        dataWriter = new WriterDataGraphicsVRML(out, graph, options);
      }
      dataWriter.write();
      out.flush();
      out.close();
      System.err.println("Wrote layout to output file '" + fileName + "'.");
    } catch (Exception e) {
      System.err.println("Exception while writing file '" + fileName + "': ");
      System.err.println(e);
    }
  }

  /*****************************************************************
   * Marks all vertices whose node names match the given regular expression.
   * Call-back method, invoked from within ScreenDisplay.
   * @param regEx     Regular expression.
   *****************************************************************/
  public void markVertices(String regEx) {
    Color color = Color.red;
    for (GraphVertex curVertex : graph.vertices) {
      if (curVertex.name.matches(regEx)) {
        curVertex.color = color;
        curVertex.showName = true;
      }
    }
  }

  /****************************************************************************
   * Toggle the showName flag of the vertices and edges at the given position.
   * Call-back method, invoked from within ScreenDisplay.
   * @param p       coordinates of the vertex.
   * @return number of names toggled
   ***************************************************************************/
  public int toggleNames(Point p) {
    int xPos = (int) p.getX();
    int yPos = (int) p.getY();
    Set<String> tmp = new TreeSet<String>(xMap.get(xPos));
    tmp.retainAll(yMap.get(yPos));
    int nb = 0;
    for (String name : tmp) {
      ++nb;
      GraphVertex curVertex = graph.nameToVertex.get(name);
      curVertex.showName = !curVertex.showName;
    }

    //edges
    if (Option.showEdges.getBool()) {
      Set<GraphEdge> edgesIndex = edgeMap.get(xPos).get(yPos);
      for (GraphEdge it : edgesIndex) {
        ++nb;
        it.showName = !it.showName;
      }
    }

    return nb;
  }

  /************************************************************************
   * Compute list of names of the vertices and edges at the given position.
   * Call-back method, invoked from within ScreenDisplay.
   * @param p       coordinates.
   ***********************************************************************/
  public Set<String> getNames(Point p) {
    int xPos = (int) p.getX();
    int yPos = (int) p.getY();
    Set<String> tmp = new TreeSet<String>();
    try {
      if (xPos >= xMap.size() || yPos >= yMap.size()) { return tmp; }
      tmp.addAll(xMap.get(xPos));
      tmp.retainAll(yMap.get(yPos));
    } catch (Exception e) {
      // Tolerate exception (it's because of the concurrent thread).
    }
    //edges
    if (Option.showEdges.getBool()) {
      Set<GraphEdge> edgesIndex = edgeMap.get(xPos).get(yPos);
      for (GraphEdge it : edgesIndex) {
        tmp.add(it.relName);
      }
    }
    return tmp;
  }

  /*****************************************************************
   * Restrict the set of vertices displayed on the screen to
   * the vertices within the given rectangular (i.e., zoom).
   * Call-back method, invoked from within ScreenDisplay.
   * @param pTopLeft      coordinates of the top left corner of the rectangular.
   * @param pBottomRight  coordinates of the bottom right corner of the rectangular.
   *****************************************************************/
  public void restrictShowedVertices(Point pTopLeft, Point pBottomRight) {
    int end = (int) Math.min(pBottomRight.getX(), xMap.size());
    Set<String> xNodes = new TreeSet<String>();
    for (int i = (int) pTopLeft.getX(); i < end; ++i) {
      xNodes.addAll(xMap.get(i));
    }
    end = (int) Math.min(pBottomRight.getY(), yMap.size());
    Set<String> yNodes = new TreeSet<String>();
    for (int i = (int) pTopLeft.getY(); i < end; ++i) {
      yNodes.addAll(yMap.get(i));
    }
    Set<String> lNodesToKeep = xNodes;
    lNodesToKeep.retainAll(yNodes);
    for (int i = 0; i < graph.vertices.size(); ++i) {
      GraphVertex curVertex = graph.vertices.get(i);
      if (!lNodesToKeep.contains(curVertex.name)) {
        curVertex.showVertex = false;
      }
    }
    // Ashgan starts
    // Every time the canvas view changes, the 
    // undo list in the associated display filter instance
    // should also be updated
    this.dispFilter.resetUndoList(graph.vertices);
    // Ashgan ends
  }

  /*****************************************************************
   * Reset vertex restriction that was set by restrictShowedVertices.
   * Call-back method, invoked from within ScreenDisplay.
   *****************************************************************/
  public void resetRestriction() {
    // Handle vertex options.
    for (GraphVertex curVertex : graph.vertices) {
      // hideSource (do not show vertex if it is source of an edge).
      if (Option.hideSource.getBool() && curVertex.isSource) {
        curVertex.showVertex = false;
      } else {
        curVertex.showVertex = true;
      }
    }
    // Ashgan starts
    // Every time the canvas view changes, the 
    // undo list in the associated display filter instance
    // should also be updated
    this.dispFilter.resetUndoList(graph.vertices);
    // Ashgan ends
  }

  /*****************************************************************
   * Gets the local graph representation (layout).
   * Call-back method, invoked from within ScreenDisplay.
   * @return   Graph/layout representation to switch to.
   *****************************************************************/
  public GraphData getGraphData() {
    return graph;
  }

  /*****************************************************************
  * Sets the local graph representation (layout) to a new value.
  * Call-back method, invoked from within ScreenDisplay.
  * @param layout     Graph/layout representation to switch to.
  *****************************************************************/
  public void setGraphData(GraphData layout) {
    this.graph = layout;

    //create default cluster with all the nodes in it
    Group.init(this, graph);
    Group defaultCluster = new Group("Group 'Unassigned'", Colors.GREEN);
    this.clusters.removeAllElements();
    addCluster(defaultCluster);
    for (GraphVertex curVertex : graph.vertices) {
      defaultCluster.addNode_WO_COLOR(curVertex);
    }
    Option.showEdges.set(Option.showEdges.getBool() && !graph.edges.isEmpty());
    //edges annotation
    for (GraphEdge e : graph.edges) {
      e.showName = false;
    }
  }

  /**
   * get showEdges
   */
  public boolean isEdgesAvailable() {
    return !graph.edges.isEmpty();
  }

  /**
   * adjust frontColor
   */
  public void adjustFrontColor() {
    frontColor = new Color(0xffffffff - options.backColor.get().getRGB());
    //problem when using gray: colors too close => hard to read
    //ignore alpha
    if (Math.abs(frontColor.getRed() - options.backColor.get().getRed()) < 10
        && Math.abs(frontColor.getBlue() - options.backColor.get().getBlue()) < 10
        && Math.abs(frontColor.getGreen() - options.backColor.get().getGreen()) < 10) {
      frontColor = Color.BLACK;
    }
  }

  /**
   * the color of the text
   * @return the color of the text
   */
  public Color getWriteColor() {
    return frontColor;
  }

  /**
   * add a new cluster in the list
   * @param clt
   */
  public void addCluster(Group clt) {
    clusters.add(0, clt);
  }

  /**
   * remove the cluster at the specified index
   * @param index
   */
  public void removeCluster(int index) {
    assert (index != 0);
    Group clt = (Group) clusters.get(index);
    Group defaultClt = (Group) clusters.get(0);
    for (GraphVertex lCurrVertex : clt) {
      defaultClt.addNode(lCurrVertex);
    }
    clusters.remove(index);
  }

  /**
   * return the cluster at the specified index
   * @param index
   * @return the cluster at the specified index
   */
  public Group getCluster(int index) {
    if (index >= 0 && index < clusters.size()) {
      return (Group) clusters.get(index);
    } else {
      return null;
    }
  }

  /**
   * return the cluster with the specified name
   * @param name
   * @return the cluster with the specified name
   */
  public Group getCluster(String name) {
    for (int i = 0; i < clusters.size(); ++i) {
      Group lGroup = (Group) clusters.get(i);
      if (lGroup.getName().equals(name)) { return lGroup; }
    }
    return null;
  }

  public DefaultListModel getClusters() {
    return clusters;
  }

  /**
   * get the number of cluster
   * @return return the number of cluster
   */
  public int getNbOfCluster() {
    return clusters.size();
  }

  /**
   * move the cluster at index one place higher in the list
   * => cluster drawn sooner
   * @param index
   */
  public void moveClusterUp(int index) {
    if (index > 1) {
      Group tmp = (Group) clusters.get(index);
      clusters.remove(index);
      clusters.insertElementAt(tmp, index - 1);
    }
  }

  /**
   * move the cluster at index one place lower in the list
   * => drawn later (more on top)
   * @param index
   */
  public void moveClusterDown(int index) {
    if (index < clusters.size() - 1 && index > 0) {
      Group tmp = (Group) clusters.get(index);
      clusters.remove(index);
      clusters.insertElementAt(tmp, index + 1);
    }
  }

  /**
   * tells the cluster that the graph has changed => recompute some data
   *
   */
  public void refreshClusters() {
    for (int i = 0; i < clusters.size(); ++i) {
      Group lGroup = (Group) clusters.get(i);
      lGroup.graphchanged();
    }
  }

  /**
   * Open the name of what is under the cursor as if it is an URL.
   * @param p   Coordinates
   */
  public void openURL(Point p) {
    // ToDo: Simplify!
    String targets = getNames(p).toString();
    int lght = targets.length();
    if (lght <= 2) { return; }
    StringTokenizer st = new StringTokenizer(targets.substring(1, lght - 1));
    if (st.hasMoreTokens()) {
      String URL = st.nextToken();
      if (URL.startsWith("\"") && URL.endsWith("\"")) {
        URL = URL.substring(1, URL.length() - 1);
      }
      if (Option.browser.getString().equals("")) {
        if (!guessBrowser(URL)) {
          System.err.println("Unable to find browser");
          return;
        }
      } else {
        String cmd[] = { Option.browser.getString(), URL };

        System.err.println("opening: " + URL);

        try {
          Runtime rt = Runtime.getRuntime();
          rt.exec(cmd);
        } catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }
  }

  private boolean guessBrowser(String URL) {

    String[] possibility =
                           { "firefox", "mozilla", "opera", "safari",
                               "iexplorer", "epiphany", "konqueror" };

    for (int i = 0; i < possibility.length; ++i) {
      Option.browser.set(possibility[i]);

      String cmd[] = { Option.browser.getString(), URL };

      try {
        Runtime rt = Runtime.getRuntime();
        rt.exec(cmd);
        return true;
      } catch (Throwable t) {
      }
    }
    Option.browser.set("");
    return false;
  }

  private void paintArrow(Graphics g, int x0, int y0, int x1, int y1) {
    // Draw line.
    g.drawLine(x0, y0, x1, y1);
    // Draw arrow head.
    int[] aps = paintArrow(x0, y0, x1, y1);
    g.drawLine(aps[0], aps[1], aps[2], aps[3]);
    g.drawLine(aps[4], aps[5], aps[6], aps[7]);
  }

  /**
   * @return the display
   */
  public FrameDisplay getDisplay() {
    return display;
  }

  // Ashgan starts
  /**
   * @param filterType <br>
   * 0: Display unchanged vertices<br>
   * 1: Display removed vertices<br>
   * 2: Display added vertices
   */
  public void filterVertices(int filterType) {
    dispFilter.filterVertices(graph, filterType, true);
  }

  /**
   * @param filterType <br>
   * 0: Display unchanged edges<br>
   * 1: Display removed edges<br>
   * 2: Display added edges
   */
  public void filterEdges(int filterType) {
    dispFilter.filterEdges(graph, filterType, true);
  }

  public void filterOff() {
    dispFilter.resetView(graph.vertices);
  }
  // Ashgan ends
};
