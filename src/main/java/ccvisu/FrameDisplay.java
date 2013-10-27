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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MouseInputAdapter;

import ccvisu.Options.InFormat;
import ccvisu.Options.Option;
import ccvisu.Options.OutFormat;

/*****************************************************************
 * Frame implementation for displaying the layout on the screen device.
 * Used by WriterDataGraphicsDISP.
 * @version  $Revision: 1.37 $; $Date: 2009-01-20 22:30:37 $
 * @author   Dirk Beyer
 *****************************************************************/
public class FrameDisplay implements GraphEventListener {

  private final Frame      mFrame;

  /** Canvas for graphics.*/
  private final MyCanvas   canvas;

  /** part in charge of managing the clusters*/
  private final FrameGroup clusters;

  private final JCheckBox  hideSource;
  private final JCheckBox  edgesCheck;

  final JTextArea          vertexName;

  // Coordinates for zooming rectangle.
  private Point            rectTopLeft     = new Point(0, 0);
  private Point            rectBottomRight = new Point(0, 0);
  private boolean          rectShow        = false;

  // Coordinates of the mouse when MOUSE_PRESSED.
  private int              mouseX;
  private int              mouseY;
  private int              tolerance;

  // Node id when MOUSE_PRESSED.
  private String           draggingVertex;

  enum LoadDirection {
    NEXT, PREV
  };

  /*****************************************************************
   * Canvas implementation for displaying the layout on the screen.
   * @author   Dirk Beyer
   *****************************************************************/
  private class MyCanvas extends Canvas {
    private static final long            serialVersionUID = 200510192212L;

    private final Frame                  parent;
    private final WriterDataGraphicsDISP writer;

    // image used for off-screen work
    private BufferedImage                img;
    // dimension of the image used
    private Dimension                    size             = new Dimension(0, 0);

    /**
     * Constructor.
     * @param parent The parent frame.
     * @param writer The writer that uses this object to draw on.
     *               The painting is delegated to the writer object.
     */
    private MyCanvas(final FrameDisplay parent,
                     final WriterDataGraphicsDISP writer) {
      this.parent = mFrame;
      this.writer = writer;
      setBackground(writer.options.backColor.get());

      // Adds MouseMotionListener for mouse event ''Mouse moved on vertex''.
      // Show the name(s) of the vertex(vertices) in the vertexNameDialog.
      addMouseMotionListener(new MouseInputAdapter() {
        @Override
        public void mouseMoved(MouseEvent evt) {
          // Names in vertexNameDialog.
          parent.vertexName.setText(writer.getNames(evt.getPoint()).toString());
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
          if (rectShow) {
            // Zooming rectangle, set end corner.
            rectBottomRight.setLocation(evt.getPoint());
            repaint();
          }
          // Vertex dragging.
          if (draggingVertex != null) {
            int size = Math.min(getSize().width, getSize().height);

            Position minPos = Position.min(writer.getGraphData().vertices);
            Position maxPos = Position.max(writer.getGraphData().vertices);
            float lWidth = Position.width(maxPos, minPos);

            // offset = -minPos + 0.05f * lWidth;
            Position offset =
                              Position.add(Position.mult(minPos, -1),
                                  0.05f * lWidth);
            Position scale = new Position(1, 1, 1);
            scale.mult(0.9f * size / lWidth);
            // Flip y-coordinate.
            scale.y *= -1;

            //int xPos = (int) ((graph.pos[index][0] + xOffset) * scale);
            //int yPos = (int) ((graph.pos[index][1] + yOffset) * -scale + size);

            float x = evt.getPoint().x / scale.x - offset.x;
            float y = (evt.getPoint().y - size) / scale.y - offset.y;
            GraphVertex lDraggingVertex =
                                          writer.getGraphData().nameToVertex
                                              .get(draggingVertex);
            lDraggingVertex.pos.x = x;
            lDraggingVertex.pos.y = y;
            updateAndPaint();
          }
        }
      });

      // Adds MouseListener for mouse event ''Mouse clicked on vertex''.
      // Draw the name(s) of the vertex(vertices) as annotation on the canvas.
      addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent evt) {
          if (evt.getButton() == MouseEvent.BUTTON1) {
            mouseX = evt.getX();
            mouseY = evt.getY();
            tolerance = Math.max(getHeight(), getWidth());
            tolerance /= 300;
            Set<String> tmp = writer.getNames(evt.getPoint());
            if (tmp.isEmpty()) {
              rectTopLeft.setLocation(evt.getPoint());
              repaint();
              rectShow = true;

              draggingVertex = null;
            } else {
              // Vertex dragging.
              draggingVertex = tmp.iterator().next();
            }
          }
        }

        @Override
        public void mouseReleased(MouseEvent evt) {
          if (evt.getButton() == MouseEvent.BUTTON1) {
            int x = evt.getX();
            int y = evt.getY();
            if (Math.abs(x - mouseX) > tolerance
                || Math.abs(y - mouseY) > tolerance) {
              if (rectShow) {
                rectShow = false;
                rectBottomRight.setLocation(evt.getPoint());

                // Switch coordinates if top-left is not top-left.
                int xTl = Math.min(rectTopLeft.x, rectBottomRight.x);
                int xBr = Math.max(rectTopLeft.x, rectBottomRight.x);
                int yTl = Math.min(rectTopLeft.y, rectBottomRight.y);
                int yBr = Math.max(rectTopLeft.y, rectBottomRight.y);
                rectTopLeft.setLocation(xTl, yTl);
                rectBottomRight.setLocation(xBr, yBr);

                writer.restrictShowedVertices(rectTopLeft, rectBottomRight);
                updateAndPaint();
              }
            } else {
              rectShow = false;
              if (Option.openURL.getBool()
                  && (evt.getModifiersEx() == InputEvent.CTRL_DOWN_MASK)) {
                writer.openURL(evt.getPoint());
              } else if (writer.toggleNames(evt.getPoint()) > 0) {
                //if something changed then recompute the img
                updateAndPaint();
              }
            }

          }
        }
      });

    }

    /*****************************************************************
     * Draws the layout on the screen.
     *****************************************************************/
    @Override
    public void repaint() {
      paint(this.getGraphics());
    }

    /*****************************************************************
     * Draws the layout on the screen.
     * @param area  The graphics area for drawing.
     *****************************************************************/
    @Override
    public void update(Graphics area) {
      paint(area);
    }

    /*****************************************************************
     * Draws the layout on the screen.
     * @param area  The graphics area for drawing.
     *****************************************************************/
    @Override
    public void paint(Graphics area) {
      // Size info.
      setSize(parent.getSize());
      int xSize =
                  getSize().width - mFrame.getInsets().left
                      - mFrame.getInsets().right;
      int ySize =
                  getSize().height - mFrame.getInsets().top
                      - mFrame.getInsets().bottom;

      if (xSize != size.width || ySize != size.height || img == null) {
        update();
      }

      //draw img on area
      area.drawImage(img, 0, 0, null);

      // Zooming rectangle.
      if (rectShow) {
        int x = (int) rectTopLeft.getX();
        int y = (int) rectTopLeft.getY();
        int width = (int) (rectBottomRight.getX() - rectTopLeft.getX());
        int height = (int) (rectBottomRight.getY() - rectTopLeft.getY());
        if (width < 0) {
          width = Math.abs(width);
          x = (int) rectBottomRight.getX();
        }
        if (height < 0) {
          height = Math.abs(height);
          y = (int) rectBottomRight.getY();
        }
        area.drawRect(x, y, width, height);
      }
    } // method paint

    /**
     * update the image used to refresh the screen
     */
    public void update() {
      setSize(parent.getSize());
      int xSize =
                  getSize().width - mFrame.getInsets().left
                      - mFrame.getInsets().right;
      int ySize =
                  getSize().height - mFrame.getInsets().top
                      - mFrame.getInsets().bottom;

      refresh();

      if (xSize != size.width || ySize != size.height || img == null) {
        size = new Dimension(xSize, ySize);
        img =
              new BufferedImage(size.width, size.height,
                  BufferedImage.TYPE_INT_RGB);
      }

      //WARNING GCJ: Graphics2D needs  gcc 4
      Graphics2D lGraphics = (Graphics2D) img.getGraphics();

      //set some rendering preferences
      lGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      lGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

      //set the font
      lGraphics.setFont(new Font("SansSerif", Font.BOLD, Option.fontSize
          .getInt()));

      //fill background
      lGraphics.setColor(this.getBackground());
      lGraphics.fillRect(0, 0, size.width, size.height);
      lGraphics.setColor(Color.BLACK);

      writer.writeDISP(Math.min(xSize, ySize), lGraphics, size.width,
          size.height, mFrame.getInsets().left, mFrame.getInsets().bottom);

      //free some resources
      lGraphics.dispose();
    }//method update

    /**
     * to use when changes are done and you want to display them
     */
    public void updateAndPaint() {
      // write "Refreshing..." in the center of the canvas
      Graphics g = this.getGraphics();
      g.setFont(new Font("SansSerif", Font.ITALIC, 30));
      //computing position
      int x = this.getWidth() / 2 - 70;
      int y = this.getHeight() / 2;
      //drawing
      g.setColor(canvas.writer.getWriteColor());
      g.drawString("Refreshing...", x, y);

      this.update();
      this.paint(g);
    }

  }; // class MyCanvas

  /**
   * Constructor.
   * @param writer      The writer that uses this frame to display the layout.
   */
  public FrameDisplay(final WriterDataGraphicsDISP writer, Frame pFrame) {

    // The frame.
    mFrame = pFrame;
    mFrame.setTitle("Visualization " + writer.options.inputName); // Set window title.
    mFrame.setLocation(0, 275); // Set initial window position.
    // Use size of default screen
    Dimension lDim = Toolkit.getDefaultToolkit().getScreenSize();
    int lSize = Math.min(lDim.width, lDim.height);
    mFrame.setSize(lSize, lSize); // Set windows size.
    //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // The graphics canvas.
    canvas = new MyCanvas(this, writer);
    // Add canvas to the frames content pane.
    mFrame.add(canvas);

    // The dialog (extra window), for the vertex names, etc.
    final JDialog controlPanelDialog = new JDialog(mFrame);
    controlPanelDialog.setTitle("Visualization control panel");
    controlPanelDialog.setLocation(0, 0);
    controlPanelDialog.setResizable(true);

    GridBagLayout lay = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    controlPanelDialog.setLayout(lay);

    constraints.gridheight = 1;
    constraints.gridwidth = 1;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.gridx = 0;
    int y = 0;

    constraints.gridy = y;
    constraints.anchor = GridBagConstraints.NORTH;
    //constraints.fill = GridBagConstraints.HORIZONTAL;
    JPanel buttonPanel = new JPanel();
    controlPanelDialog.add(buttonPanel, constraints);

    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.WEST;
    //constraints.fill = GridBagConstraints.NONE;
    JPanel sizePanel = new JPanel();
    controlPanelDialog.add(sizePanel, constraints);

    constraints.gridy = ++y;
    JPanel renderPanel = new JPanel();
    controlPanelDialog.add(renderPanel, constraints);

    constraints.gridy = ++y;
    JPanel colorPanel = new JPanel();
    controlPanelDialog.add(colorPanel, constraints);

    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.NORTH;
    //constraints.fill = GridBagConstraints.HORIZONTAL;
    JPanel fileButtonPanel = new JPanel();
    controlPanelDialog.add(fileButtonPanel, constraints);

    // Ashgan starts
    JPanel vertexFilterButtonPanel = new JPanel();
    JPanel edgeFilterButtonPanel = new JPanel();
    // Vertex Change Filter Panel
    final JButton newVerticesButton = new JButton("Added Nodes");
    final JButton oldVerticesButton = new JButton("Removed Nodes");
    final JButton unchangedVerticesButton = new JButton("Common Nodes");
    final JButton filterOffButton = new JButton("Show All");
    // Edge Change Filter Panel
    final JButton oldEdgesButton = new JButton("Removed Edges");
    final JButton newEdgesButton = new JButton("Added Edges");
    final JButton unchangedEdgesButton = new JButton("Common Edges");

    if (Option.dispFilter.getBool()) {
      constraints.gridy = ++y;
      constraints.anchor = GridBagConstraints.NORTH;
      //constraints.fill = GridBagConstraints.HORIZONTAL;
      controlPanelDialog.add(vertexFilterButtonPanel, constraints);
      vertexFilterButtonPanel.add(newVerticesButton);
      vertexFilterButtonPanel.add(oldVerticesButton);
      vertexFilterButtonPanel.add(unchangedVerticesButton);
      vertexFilterButtonPanel.add(filterOffButton);

      constraints.gridy = ++y;
      constraints.anchor = GridBagConstraints.NORTH;
      //constraints.fill = GridBagConstraints.HORIZONTAL;
      controlPanelDialog.add(edgeFilterButtonPanel, constraints);
      edgeFilterButtonPanel.add(newEdgesButton);
      edgeFilterButtonPanel.add(oldEdgesButton);
      edgeFilterButtonPanel.add(unchangedEdgesButton);
    }
    // Ashgan ends

    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.WEST;
    //constraints.fill = GridBagConstraints.NONE;
    JPanel searchPanel = new JPanel();
    controlPanelDialog.add(searchPanel, constraints);

    //Button Panel
    JButton resetZoomButton = new JButton("Reset zoom selection");
    buttonPanel.add(resetZoomButton);

    //Size Panel
    final JCheckBox circleCheck =
                                  new JCheckBox("Black Circle",
                                      Option.blackCircle.getBool());
    sizePanel.add(circleCheck);
    final JComboBox fontSizeChoice = new JComboBox();
    fontSizeChoice.addItem("" + Option.fontSize.getInt());
    fontSizeChoice.addItem("6");
    fontSizeChoice.addItem("8");
    fontSizeChoice.addItem("10");
    fontSizeChoice.addItem("12");
    fontSizeChoice.addItem("14");
    fontSizeChoice.addItem("16");
    fontSizeChoice.addItem("18");
    fontSizeChoice.addItem("20");
    fontSizeChoice.addItem("22");
    fontSizeChoice.addItem("24");
    fontSizeChoice.addItem("26");
    fontSizeChoice.addItem("28");
    fontSizeChoice.addItem("30");
    sizePanel.add(new JLabel("   Font size:"));
    sizePanel.add(fontSizeChoice);
    final JComboBox minVertChoice = new JComboBox();
    minVertChoice.addItem("" + Option.minVert.getFloat());
    minVertChoice.addItem("1");
    minVertChoice.addItem("2");
    minVertChoice.addItem("3");
    minVertChoice.addItem("4");
    minVertChoice.addItem("5");
    minVertChoice.addItem("6");
    minVertChoice.addItem("7");
    minVertChoice.addItem("8");
    minVertChoice.addItem("9");
    minVertChoice.addItem("10");
    minVertChoice.addItem("15");
    minVertChoice.addItem("20");
    sizePanel.add(new JLabel("   Min vertex size:"));
    sizePanel.add(minVertChoice);
    // Just a bit of space ...
    sizePanel.add(new JLabel("   "));

    //Render Panel
    hideSource =
                 new JCheckBox("Hide Source Vertices", Option.hideSource
                     .getBool());
    hideSource.setEnabled(writer.isEdgesAvailable());
    renderPanel.add(hideSource);
    edgesCheck = new JCheckBox("Show Edges", Option.showEdges.getBool());
    edgesCheck.setEnabled(writer.isEdgesAvailable());
    renderPanel.add(edgesCheck);
    final JButton showAllLabelsButton = new JButton("Show all labels");
    renderPanel.add(showAllLabelsButton);
    final JButton hideAllLabelsButton = new JButton("Hide all labels");
    renderPanel.add(hideAllLabelsButton);

    // Color Panel
    JComboBox colorChoice = new JComboBox();
    for (Colors c : Colors.values()) {
      colorChoice.addItem(c.toString().toLowerCase());
    }
    colorChoice.setName("normal");
    colorChoice.setSelectedItem("green");
    colorPanel.add(new JLabel("Vertex color:"));
    colorPanel.add(colorChoice);

    JComboBox backGroundColor = new JComboBox();
    backGroundColor.setName("back");
    backGroundColor.addItem("white");
    backGroundColor.addItem("lightgray");
    backGroundColor.addItem("gray");
    backGroundColor.addItem("darkgray");
    backGroundColor.addItem("black");
    colorPanel.add(new JLabel("   Background color:"));
    colorPanel.add(backGroundColor);

    // File Panel
    final JButton saveButton = new JButton("Save layout");
    fileButtonPanel.add(saveButton);

    final JButton loadButton = new JButton("Load layout");
    fileButtonPanel.add(loadButton);

    JButton prevButton = new JButton("Prev layout");
    fileButtonPanel.add(prevButton);

    JButton nextButton = new JButton("Next layout");
    fileButtonPanel.add(nextButton);

    // Search Panel
    JLabel spaceLabel = new JLabel("Search:");
    searchPanel.add(spaceLabel);

    final JTextField markerRegExTextField = new JTextField("", 30);
    searchPanel.add(markerRegExTextField);

    JButton stopButton = new JButton("Stop minimizing");
    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        // Better idea: Set nrIterations to the value of 'step'.
        writer.options.nrIterations = 0;
      }
    });
    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.WEST;
    controlPanelDialog.add(stopButton, constraints);

    /*
        JButton contButton = new JButton("Cont minimizing");
        contButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            // Better idea: Set nrIterations to the value of 'step'.
            writer.options.nrIterations = 0;
          }
        });
        constraints.gridy = ++y;
        constraints.anchor = GridBagConstraints.WEST;
        controlPanelDialog.add(contButton, constraints);
    */

    JButton advanced = new JButton("Advanced");
    constraints.gridy = y;
    constraints.anchor = GridBagConstraints.EAST;
    controlPanelDialog.add(advanced, constraints);

    constraints.gridy = ++y;
    constraints.anchor = GridBagConstraints.WEST;
    controlPanelDialog.add(new JLabel(
        "Click on vertex to annote with name, click again to remove."),
        constraints);

    constraints.gridy = ++y;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    vertexName =
                 new JTextArea("Vertex names appear here when mouse is moved.",
                     3, 2);
    JScrollPane vNameScrollPane = new JScrollPane(vertexName);
    vNameScrollPane
        .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    vNameScrollPane
        .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    vertexName.setEditable(false);
    vertexName.setLineWrap(true);
    controlPanelDialog.add(vNameScrollPane, constraints);
    controlPanelDialog.pack();
    controlPanelDialog.setVisible(true);

    clusters = new FrameGroup(this, writer);

    // Show canvas.
    mFrame.setVisible(true);

    // Adds ActionListener for action event ''Reset Zoom button pressed''.
    // Resets the visibility of all vertices.
    resetZoomButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.resetRestriction();
        canvas.updateAndPaint();
      }
    });

    // Adds ActionListener for action event ''Show All Labels button pressed''.
    // Display the labels of all vertices.
    showAllLabelsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        for (GraphVertex v : writer.options.graph.vertices) {
          if (v.showVertex) {
            v.showName = true;
          }
        }
        for (GraphEdge e : writer.options.graph.edges) {
          GraphVertex sourceVertex = writer.options.graph.vertices.get(e.x);
          GraphVertex targetVertex = writer.options.graph.vertices.get(e.y);
          if (sourceVertex.showVertex && targetVertex.showVertex) {
            e.showName = true;
          }
        }
        canvas.updateAndPaint();
      }
    });

    // Adds ActionListener for action event ''Hide All Labels button pressed''.
    // Hide the labels of all vertices.
    hideAllLabelsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        for (GraphVertex v : writer.options.graph.vertices) {
          if (v.showVertex) {
            v.showName = false;
          }
        }
        for (GraphEdge e : writer.options.graph.edges) {
          GraphVertex sourceVertex = writer.options.graph.vertices.get(e.x);
          GraphVertex targetVertex = writer.options.graph.vertices.get(e.y);
          if (sourceVertex.showVertex && targetVertex.showVertex) {
            e.showName = false;
          }
        }
        canvas.updateAndPaint();
      }
    });

    // Adds ItemListener for item event.
    // Repaint the layout.
    minVertChoice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        Option.minVert.set(Float.parseFloat(minVertChoice.getSelectedItem()
            .toString()));
        canvas.updateAndPaint();
      }
    });

    // Adds ItemListener for item event.
    // Repaint the layout.
    fontSizeChoice.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        Option.fontSize.set(Integer.parseInt(fontSizeChoice.getSelectedItem()
            .toString()));
        canvas.updateAndPaint();
      }
    });

    //Adds actionListener for action event "Advanced pressed".
    //show the clusterManager
    advanced.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if (!clusters.isShowing()) {
          int y = controlPanelDialog.getY();
          y += controlPanelDialog.getHeight();
          y += 30;
          clusters.setLocation(controlPanelDialog.getX(), y);
          clusters.setVisible(true);
        }
      }
    });

    //Adds ItemListener for item event.
    // change chosenColor 
    ItemListener chooseColor = new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        JComboBox ch = (JComboBox) evt.getSource();
        //string (not index)
        //possible to change the order without changing this part
        Colors tmp = Colors.valueOfUpper(ch.getSelectedItem().toString());
        assert (tmp != null);
        String name = ch.getName();
        if (name.equals("normal")) {
          writer.getCluster(0).setColor(tmp);
        } else if (name.equals("back")) {
          writer.options.backColor = tmp;
          writer.adjustFrontColor();
          canvas.setBackground(tmp.get());
        } else {
          System.err
              .println("error: unknown event source (ScreenDisplay, itemlistener)");
        }
        canvas.updateAndPaint();
      }
    };

    backGroundColor.addItemListener(chooseColor);
    colorChoice.addItemListener(chooseColor);
    //canvas.colorHighLightChoice.addItemListener(chooseColor);

    // Adds ItenListener for item event "circleCheck"
    circleCheck.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        Option.blackCircle.set(circleCheck.isSelected());
        canvas.updateAndPaint();
      }
    });

    // Adds ItenListener for item event "hideSource"
    hideSource.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        // Hide source (do not show vertex if it is source of an edge).
        Option.hideSource.set(hideSource.isSelected());
        canvas.updateAndPaint();
      }
    });

    //Adds ItenListener for item event "edgesCheck"
    edgesCheck.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        Option.showEdges.set(edgesCheck.isSelected());
        canvas.updateAndPaint();
      }
    });

    // Adds ActionListener for action event ''Save button pressed''.
    // Opens a dialog for choosing the file to which the layout is saved.
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JFileChooser lFileDialog = new JFileChooser(".");
        lFileDialog.setFileFilter(ReaderData
            .mkExtensionFileFilter(OutFormat.LAY));
        int lOutcome = lFileDialog.showSaveDialog(canvas.parent);
        if (lOutcome == JFileChooser.APPROVE_OPTION) {
          assert (lFileDialog.getCurrentDirectory() != null);
          assert (lFileDialog.getSelectedFile() != null);
          String lFileName =
                             lFileDialog.getCurrentDirectory().toString()
                                 + File.separator
                                 + lFileDialog.getSelectedFile().getName();
          writer.writeFileLayout(lFileName);
        } else if (lOutcome == JFileChooser.CANCEL_OPTION) {
          // Do nothing.
        }
      }
    });

    // Adds ActionListener for action event ''Load button pressed''.
    // Opens a dialog for choosing the file from which the next layout is loaded.
    loadButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        JFileChooser lFileDialog = new JFileChooser(".");
        lFileDialog.setFileFilter(ReaderData
            .mkExtensionFileFilter(InFormat.LAY));
        int lOutcome = lFileDialog.showOpenDialog(canvas.parent);
        if (lOutcome == JFileChooser.APPROVE_OPTION) {
          assert (lFileDialog.getCurrentDirectory() != null);
          assert (lFileDialog.getSelectedFile() != null);
          String lFileName =
                             lFileDialog.getCurrentDirectory().toString()
                                 + File.separator
                                 + lFileDialog.getSelectedFile().getName();
          loadFile(writer, lFileName);
        } else if (lOutcome == JFileChooser.CANCEL_OPTION) {
          // Do nothing.
        }
      }
    });

    // Adds ActionListener for action event ''Next button pressed''.
    // Loads next layout from the directory.
    nextButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent pE) {
        loadOtherFile(LoadDirection.NEXT);
      }
    });
    // Adds ActionListener for action event ''Prev button pressed''.
    // Loads previous layout from the directory.
    prevButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent pE) {
        loadOtherFile(LoadDirection.PREV);
      }
    });

    // Ashgan starts
    newVerticesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.filterVertices(2);
        canvas.updateAndPaint();
      }
    });
    oldVerticesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.filterVertices(1);
        canvas.updateAndPaint();
      }
    });
    unchangedVerticesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.filterVertices(0);
        canvas.updateAndPaint();
      }
    });
    filterOffButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.filterOff();
        canvas.updateAndPaint();
      }
    });
    newEdgesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.filterEdges(2);
        canvas.updateAndPaint();
      }
    });
    oldEdgesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.filterEdges(1);
        canvas.updateAndPaint();
      }
    });
    unchangedEdgesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        writer.filterEdges(0);
        canvas.updateAndPaint();
      }
    });
    // Ashgan ends

    // New KeyListener class for loading the next or previous layout from the directory.
    final class MyKeyAdapter extends KeyAdapter {
      @Override
      public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_O || key == KeyEvent.VK_LEFT) {
          loadOtherFile(LoadDirection.PREV);
        } else if (key == KeyEvent.VK_P || key == KeyEvent.VK_RIGHT) {
          loadOtherFile(LoadDirection.NEXT);
        } else if (key == KeyEvent.VK_S) {
          saveButton.dispatchEvent(new ActionEvent(this,
              ActionEvent.ACTION_PERFORMED, "Save"));
        } else if (key == KeyEvent.VK_L) {
          loadButton.dispatchEvent(new ActionEvent(this,
              ActionEvent.ACTION_PERFORMED, "Load"));
        }
      }
    }
    ;
    mFrame.addKeyListener(new MyKeyAdapter());
    canvas.addKeyListener(new MyKeyAdapter());

    // Adds KeyListener for action event ''Search text field filled''.
    // Marks the vertices according to the expression.
    markerRegExTextField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          canvas.writer.markVertices(".*" + markerRegExTextField.getText()
                                     + ".*");
          canvas.updateAndPaint();
        }
      }
    });

    // Dialog: Adds WindowListener for window event ''Close''.
    controlPanelDialog.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        controlPanelDialog.dispose();
      }
    });

    // Frame: Adds WindowListener for window event ''Close''.
    mFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        // Close the control dialog as well.
        controlPanelDialog.dispose();
        // Close this frame.
        mFrame.dispose();

        // Wake up the calling program in order to continue, or return.
        synchronized (writer.options) {
          writer.options.notify();
        }
      }
    });

  } // constructor

  /**
   * Repaint the drawing on the frame, i.e., delegate to canvas.
   */
  /*  This might still be needed.
  public void repaint() {
    this.canvas.repaint();
  } // method repaint
  */

  /**
   * Load next/previous layout from file, 
   * to leaf through the directory file by file.
   * @param pDirection  determines whether next or previous file 
   *                    should be loaded.
   */
  private void loadOtherFile(LoadDirection pDirection) {
    final String lFileName = canvas.writer.options.inputName;
    String lPath = ".";
    if (lFileName.lastIndexOf(File.separator) != -1) {
      lPath = lFileName.substring(0, lFileName.lastIndexOf(File.separator));
    }

    String[] fileList = (new File(lPath)).list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        if (name.endsWith(".lay")) { return true; }
        return false;
      }
    });
    if (fileList.length == 0) {
      System.err
          .println("No layout (.lay) file available in current directory.");
      return;
    }
    Arrays.sort(fileList);
    int fileCurrent =
                      Arrays.binarySearch(fileList, new File(lFileName)
                          .getName());
    if (fileCurrent < 0) { // File not found in current directory.
      fileCurrent = 0;
    } else if (pDirection == LoadDirection.NEXT
               && (fileCurrent < fileList.length - 1)) {
      fileCurrent++;
    } else if (pDirection == LoadDirection.PREV && (fileCurrent > 0)) {
      fileCurrent--;
    }
    canvas.writer.options.inputName =
                                      lPath + File.separator
                                          + fileList[fileCurrent];
    // Load next layout.
    loadFile(canvas.writer, canvas.writer.options.inputName);
  }

  private void loadFile(WriterDataGraphicsDISP pWriter, String pFileName) {
    mFrame.setTitle("Visualization " + pFileName);
    BufferedReader lIn = null;
    GraphData layout = new GraphData();
    try {
      lIn = new BufferedReader(new FileReader(pFileName));
    } catch (Exception e) {
      System.err.println("Exception while opening file '" + pFileName
                         + "' for reading: ");
      System.err.println(e);
    }
    // Read layout from file.
    (new ReaderDataLAY(lIn, canvas.writer.options.verbosity)).read(layout);
    pWriter.setGraphData(layout);
    canvas.updateAndPaint();
    // Close the input file.
    try {
      lIn.close();
    } catch (Exception e) {
      System.err.println("Exception while closing input file: ");
      System.err.println(e);
    }
  }

  /**
   * repaint when the graph change
   * @param evt a GraphEvent
   */
  public void onGraphEvent(GraphEvent evt) {
    canvas.writer.refreshClusters();
    this.canvas.updateAndPaint();
  }

  /**
   * method invoked when the layout changes
   * tells the clusterManager to refresh its list of clusters and ...
   */
  public void refresh() {
    edgesCheck.setSelected(Option.showEdges.getBool());
    edgesCheck.setEnabled(canvas.writer.isEdgesAvailable());
    // We enable the 'hide source' button only if edges are available, too.
    hideSource.setEnabled(canvas.writer.isEdgesAvailable());
    this.clusters.refresh();
  }

}; // class frame
