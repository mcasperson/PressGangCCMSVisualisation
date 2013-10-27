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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * a GUI to manage the clusters define new, remove, ...
 * @version $Revision: 1.15 $; $Date: 2007/12/15 01:20:51 $
 * @author Damien Zufferey
 */
public class FrameGroup extends JFrame {

  private static final long      serialVersionUID = 5669096380733602612L;

  /** what display the cluster's names */
  private JList                  lst;

  /** where the clusters are stocked */
  private WriterDataGraphicsDISP parent;

  /** notify when significant changes are done */
  private FrameDisplay           display;

  // temporarily used
  private JDialog                diag             = null;
  private JDialog                diag2;
  /** cluster curently edited */
  private Group                  curClt;
  // used to get some input
  private JTextField             txt;

  // nodes list from a specific cluster
  private List                   cltNodes;
  // list from all nodes in the graph
  private List                   allNodes;

  // to select the way the pattern handled
  private JComboBox              mode;                                   // equals,contains,...
  private JComboBox              mode2;                                  // keep, remove

  /** display informations */
  private JLabel                 numberOfNode;
  /** display informations */
  private JLabel                 radius;

  /** to choose the color to display */
  private JComboBox              color;
  /** to choose if the cluster should be diplayed */
  private final JCheckBox        visible;
  /** to choose if the default cluster should be diplayed */
  private final JCheckBox        defaultVisible;
  /** to choose if the cluster's info should be diplayed */
  private final JCheckBox        infoVisible;

  /**
   * Constructor
   * @param pparent - WriterDataGraphicsDISP containing the clusters
   * @param disp - a GraphEventListener charged of the rendering
   */
  public FrameGroup(final FrameDisplay disp, WriterDataGraphicsDISP pparent) {
    super("Group highlighting");
    this.parent = pparent;
    this.display = disp;
    this.setLayout(new BorderLayout());

    /** save clusters */
    JButton save = new JButton("Save as...");
    /** load clusters */
    JButton load = new JButton("Load ...");

    JPanel north = new JPanel();
    north.add(save);
    north.add(load);
    add(north, BorderLayout.NORTH);

    lst = new JList(parent.getClusters());
    //lst.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    //lst.setLayoutOrientation(JList.VERTICAL);
    //lst.setVisibleRowCount(-1);

    add(lst, BorderLayout.CENTER);

    radius = new JLabel("        ", SwingConstants.RIGHT);
    numberOfNode = new JLabel("        ", SwingConstants.RIGHT);

    visible = new JCheckBox("Show group", true);
    infoVisible = new JCheckBox("Graphic informations", false);
    defaultVisible = new JCheckBox("Show group-free vertices", true);

    color = new JComboBox();
    for (Colors c : Colors.values()) {
      color.addItem(c.toString().toLowerCase());
    }
    color.setSelectedItem("red");

    /** to add a new cluster */
    JButton newCluster = new JButton("New");
    /** to remove a cluster */
    JButton removeCluster = new JButton("Del");
    /** to edit a cluster */
    JButton editCluster = new JButton("Edit");
    /** to change the position of a cluster in the rendering order */
    JButton up = new JButton("up");
    /** to change the position of a cluster in the rendering order */
    JButton down = new JButton("down");
    /** to choose if the cluster's nodes should be diplayed */
    JButton showLabel = new JButton("Show labels");
    /** to choose if the cluster's nodes should be hidden */
    JButton hideLabel = new JButton("Hide labels");

    //Panel info = new Panel(new GridLayout(12,1));
    JPanel info = new JPanel(new GridLayout(10, 1));
    info.add(new JPanel().add(defaultVisible));
    info.add(new JPanel().add(visible));
    info.add(new JPanel().add(infoVisible));
    info.add(new JPanel().add(showLabel));
    info.add(new JPanel().add(hideLabel));

    JPanel p1 = new JPanel();
    p1.add(new JLabel("Color:"));
    p1.add(color);
    info.add(p1);

    info.add(new JLabel("Number of Vertices:"));
    info.add(numberOfNode);

    info.add(new JLabel("Average radius:"));
    info.add(radius);

    add(info, BorderLayout.EAST);

    JPanel buttons = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.weightx = 0.5;
    c.weighty = 0.5;

    int y = 0;
    c.gridy = y;
    buttons.add(newCluster, c);
    c.gridy = ++y;
    buttons.add(editCluster, c);
    c.gridy = ++y;
    buttons.add(removeCluster, c);
    c.gridy = ++y;
    buttons.add(up, c);
    c.gridy = ++y;
    buttons.add(down, c);

    add(buttons, BorderLayout.WEST);

    setLocation(900, 150);
    pack();
    //setVisible(true);

    //construct the choices for a later usage
    //it always use the same object => it "remembers" last choice
    mode = new JComboBox();
    mode.addItem("Equals");
    mode.addItem("Contains");
    mode.addItem("Starts with");
    mode.addItem("Ends with");

    mode2 = new JComboBox();
    mode2.addItem("Keep");
    mode2.addItem("Remove");

    // Listeners

    // Adds ActionListener for action event ''Save button pressed''.
    // Opens a dialog for choosing the file to which the group definition is saved.
    save.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        // Create a fileDialog and save the info in the selected file
        JFileChooser lFileDialog = new JFileChooser(".");
        lFileDialog.setFileFilter(ReaderData.mkExtensionFileFilter(".grp",
            "Group Files"));
        int lOutcome = lFileDialog.showSaveDialog((Frame) null);
        if (lOutcome == JFileChooser.APPROVE_OPTION) {
          assert (lFileDialog.getCurrentDirectory() != null);
          assert (lFileDialog.getSelectedFile() != null);
          String lFileName =
                             lFileDialog.getCurrentDirectory().toString()
                                 + File.separator
                                 + lFileDialog.getSelectedFile().getName();
          try {
            PrintWriter lOut =
                               new PrintWriter(new BufferedWriter(
                                   new FileWriter(lFileName)));
            ReaderWriterGroup.write(lOut, parent);
            System.err.println("Wrote groups informations to output '"
                               + lFileName + "'.");
            lOut.close();
          } catch (IOException e) {
            System.err.println("error while writing (ClusterManager.saveClt):");
            e.printStackTrace();
          }
        } else if (lOutcome == JFileChooser.CANCEL_OPTION) {
          // Do nothing.
        }
      }
    });

    // Adds ActionListener for action event ''Load button pressed''.
    // Opens a dialog for choosing the file from which the group definition is loaded.
    load.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        // Create a fileDialog and try to load the info from the selected file
        JFileChooser lFileDialog = new JFileChooser(".");
        lFileDialog.setFileFilter(ReaderData.mkExtensionFileFilter(".grp",
            "Group Files"));
        int lOutcome = lFileDialog.showOpenDialog((Frame) null);
        if (lOutcome == JFileChooser.APPROVE_OPTION) {
          assert (lFileDialog.getCurrentDirectory() != null);
          assert (lFileDialog.getSelectedFile() != null);
          String lFileName =
                             lFileDialog.getCurrentDirectory().toString()
                                 + File.separator
                                 + lFileDialog.getSelectedFile().getName();
          // Read group definition from file.
          BufferedReader lIn = null;
          try {
            lIn = new BufferedReader(new FileReader(lFileName));
            ReaderWriterGroup.read(lIn, parent);
            display.onGraphEvent(new GraphEvent(this));
            //  Close the input file.
            lIn.close();
          } catch (Exception e) {
            System.err.println("Exception while reading from file '"
                               + lFileName + "'.");
            System.err.println(e);
          }
        } else if (lOutcome == JFileChooser.CANCEL_OPTION) {
          // Do nothing.
        }
      }
    });

    // set the selected color to the selected cluster
    color.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        JComboBox ch = (JComboBox) evt.getSource();
        // string (not index)
        // possible to change the order without changing this part
        Group clt = getClusterFromListIndex(lst.getSelectedIndex());
        if (clt != null) {
          Colors tmp = Colors.valueOfUpper(ch.getSelectedItem().toString());
          assert (tmp != null);
          clt.setColor(tmp);
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    // toggle the visible attribute of the selected cluster
    visible.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        Group clt = getClusterFromListIndex(lst.getSelectedIndex());
        if (clt != null) {
          clt.visible = visible.isSelected();
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    // toggle the visible attribute of the selected cluster
    infoVisible.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        Group clt = getClusterFromListIndex(lst.getSelectedIndex());
        if (clt != null) {
          clt.info = infoVisible.isSelected();
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    // toggle the visible attribute of the default cluster
    defaultVisible.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        parent.getCluster(0).visible = defaultVisible.isSelected();
        display.onGraphEvent(new GraphEvent(this));

      }
    });

    // call the method in charge of creating a new cluster
    newCluster.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        if (diag != null) {
          diag.setVisible(false);
          diag.dispose();
        }

        // create a dialog that asks the user for a name and create a new cluster
        diag = new JDialog((Frame) null, "Name of the new group:", true);
        txt = new JTextField(30);
        diag.add(txt, BorderLayout.CENTER);
        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        JPanel btt = new JPanel();
        btt.add(ok);
        btt.add(cancel);
        diag.add(btt, BorderLayout.SOUTH);
        ok.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            String name = txt.getText();
            if (name != null) {
              if (parent.getCluster(name) == null) {
                parent.addCluster(new Group(name));
              }
            }
            txt = null;
            diag.setVisible(false);
            diag.dispose();
            diag = null;
          }
        });

        txt.addKeyListener(new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              String name = txt.getText();
              if (name != null) {
                if (parent.getCluster(name) == null) {
                  parent.addCluster(new Group(name));
                }
              }
              txt = null;
              diag.setVisible(false);
              diag.dispose();
              diag = null;
            }
          }
        });

        cancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            ;
            diag.setVisible(false);
            diag.dispose();
            diag = null;
          }
        });
        diag.pack();
        diag.setLocationRelativeTo(null);// center of screen
        diag.setVisible(true);
      }
    });

    // open a dialog to edit the nodes of the selected cluster
    editCluster.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        Group clt = getClusterFromListIndex(lst.getSelectedIndex());
        if (clt != null) {
          if (diag != null) {
            diag.setVisible(false);
            diag.dispose();
          }
          editDialog(clt);
        }
      }
    });

    // remove the selected cluster
    removeCluster.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int index = lst.getSelectedIndex();
        if (index < parent.getNbOfCluster() - 1) {
          parent.removeCluster(index);
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    // put the selected cluster one rank higher in the rendering list
    up.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int index = lst.getSelectedIndex();
        if (index < parent.getNbOfCluster() - 1) {
          parent.moveClusterDown(index);
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    // put the selected cluster one rank lower in the rendering list
    down.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int index = lst.getSelectedIndex();
        if (index < parent.getNbOfCluster() - 1) {
          parent.moveClusterUp(index);
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    //diplays the label of the nodes in the cluster
    showLabel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        Group clt = getClusterFromListIndex(lst.getSelectedIndex());
        if (clt != null) {
          for (GraphVertex lCurrVertex : clt) {
            lCurrVertex.showName = true;
          }
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    //hides the label of the nodes in the cluster
    hideLabel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        Group clt = getClusterFromListIndex(lst.getSelectedIndex());
        if (clt != null) {
          for (GraphVertex lCurrVertex : clt) {
            lCurrVertex.showName = false;
          }
          display.onGraphEvent(new GraphEvent(this));
        }
      }
    });

    /* Not necessary for JList
    // refresh displayed attributs
    lst.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        refreshInfo();
      }
    });
    */

    // process the windowClosing event
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        setVisible(false);
      }
    });
  }

  /**
   * return a cluster in function of an integer
   * 
   * @param index
   *            the index of the selected list item
   * @return the selected cluster
   */
  private Group getClusterFromListIndex(int index) {
    //int end = parent.getNbOfCluster() - 1;
    if (index < parent.getNbOfCluster() - 1) {
      //return parent.getCluster(end - index);
      return parent.getCluster(index);
    } else {
      return null;
    }
  }

  /**
   * refresh the list that contains the nodes of the current cluster
   */
  private void refreshCltNodes() {
    cltNodes.removeAll();
    for (GraphVertex lCurrVertex : curClt) {
      cltNodes.add(lCurrVertex.name);
    }
  }

  /**
   * create a dialog to edit the cluster
   * @param clt - the cluster to edit
   */
  private void editDialog(Group clt) {
    curClt = clt;

    cltNodes = new List(40);
    refreshCltNodes();
    cltNodes.setMultipleMode(true);

    allNodes = new List(40);
    allNodes.setMultipleMode(true);
    for (GraphVertex lCurrVertex : parent.graph.vertices) {
      allNodes.add(lCurrVertex.name);
    }

    diag = new JDialog(this, clt.getName());

    JButton addNode = new JButton("Add");
    JButton addPattern = new JButton("Add (pattern)");
    JButton filter = new JButton("filter (pattern)");
    JButton remove = new JButton("Remove");

    JPanel up = new JPanel(new GridLayout(1, 2, 20, 10));
    up.add(new JLabel("Vertices of " + clt.getName()));
    up.add(new JLabel("List of all vertices"));
    diag.add(up, BorderLayout.NORTH);

    JPanel center = new JPanel(new GridLayout(1, 2, 20, 10));
    center.add(cltNodes);
    center.add(allNodes);
    diag.add(center, BorderLayout.CENTER);
    JPanel buttons = new JPanel();
    buttons.add(addNode);
    buttons.add(addPattern);
    buttons.add(filter);
    buttons.add(remove);
    diag.add(buttons, BorderLayout.SOUTH);

    diag.setSize(330, 300);
    diag.setLocationRelativeTo(this);// center of screen
    diag.setVisible(true);

    diag.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent evt) {
        diag.setVisible(false);
        diag.dispose(); // Close.
        diag = null;
      }
    });

    addNode.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        int selected[] = allNodes.getSelectedIndexes();
        for (int i = 0; i < selected.length; ++i) {
          curClt.addNode(parent.graph.vertices.get(selected[i]));
        }
        refreshCltNodes();
        display.onGraphEvent(new GraphEvent(this));
        refreshInfo();
      }
    });

    remove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        // affect selected node to the default cluster
        int selected[] = cltNodes.getSelectedIndexes();
        GraphVertex vertex[] = new GraphVertex[selected.length];
        for (int i = 0; i < selected.length; ++i) {
          vertex[i] = curClt.getNodes().get(selected[i]);
        }
        for (int i = 0; i < selected.length; ++i) {
          (parent.getCluster(0)).addNode(vertex[i]);
        }
        refreshCltNodes();
        display.onGraphEvent(new GraphEvent(this));
        refreshInfo();
      }
    });

    addPattern.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        diag2 = new JDialog(diag, "Enter a pattern", true);

        txt = new JTextField(30);
        JPanel center = new JPanel(new GridLayout(2, 1));
        center.add(txt);
        center.add(mode);
        diag2.add(center, BorderLayout.CENTER);

        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        JPanel btt = new JPanel();
        btt.add(ok);
        btt.add(cancel);
        diag2.add(btt, BorderLayout.SOUTH);

        AddPatternOk listener = new AddPatternOk();

        txt.addKeyListener(listener);
        ok.addActionListener(listener);

        cancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            ;
            diag2.setVisible(false);
            diag2.dispose();
          }
        });

        diag2.pack();
        diag2.setLocationRelativeTo(null);// center of screen
        diag2.setVisible(true);
      }
    });

    filter.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        diag2 = new JDialog(diag, "Enter a pattern", true);

        txt = new JTextField(30);
        JPanel center = new JPanel(new GridLayout(3, 1));
        center.add(txt);
        center.add(mode);
        center.add(mode2);
        diag2.add(center, BorderLayout.CENTER);

        JButton ok = new JButton("Ok");
        JButton cancel = new JButton("Cancel");
        JPanel btt = new JPanel();
        btt.add(ok);
        btt.add(cancel);
        diag2.add(btt, BorderLayout.SOUTH);

        FilterPatternOk listener = new FilterPatternOk();
        txt.addKeyListener(listener);
        ok.addActionListener(listener);

        cancel.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            ;
            diag2.setVisible(false);
            diag2.dispose();
          }
        });
        diag2.pack();
        diag2.setLocationRelativeTo(null);// center of screen
        diag2.setVisible(true);
      }
    });

  }

  /**
   * refresh the list of clusters and closes the dialogs (edit,new,...) 
   * method invoqued by ScreenDisplay
   */
  public void refresh() {
    if (diag != null) {
      diag.setVisible(false);
      diag.dispose();
      diag = null;
    }
    if (diag2 != null) {
      diag2.setVisible(false);
      diag2.dispose();
      diag2 = null;
    }
  }

  /**
   * refresh the informations of the selected cluster
   *
   */
  private void refreshInfo() {
    int index = lst.getSelectedIndex();
    int end = parent.getNbOfCluster() - 1;
    Group clt = parent.getCluster(end - index);
    if (clt != null) {
      // refresh the display with the correct informations
      radius.setText(Float.toString(clt.getAverageRadius()));
      numberOfNode.setText(Integer.toString(clt.getNodes().size()));
      visible.setSelected(clt.visible);
      infoVisible.setSelected(clt.info);
      // select matching color
      String tmp = clt.getColor().toString();
      assert (tmp != null);
      color.setSelectedItem(tmp);
    }
  }

  private class AddPatternOk extends KeyAdapter implements ActionListener {

    //ok button event
    //same effect as Enter key
    public void actionPerformed(ActionEvent arg0) {
      process();
    }

    //Enter key
    //same effect as ok button
    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        process();
      }
    }

    private void process() {
      String pattern = txt.getText();
      if (pattern != null) {
        curClt.addPattern(pattern, mode.getSelectedIndex());
        refreshCltNodes();
        display.onGraphEvent(new GraphEvent(this));
        refreshInfo();
      }
      txt = null;
      diag2.setVisible(false);
      diag2.dispose();
    }
  }

  private class FilterPatternOk extends KeyAdapter implements ActionListener {

    //ok button event
    //same effect as Enter key
    public void actionPerformed(ActionEvent arg0) {
      process();
    }

    //Enter key
    //same effect as ok button
    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        process();
      }
    }

    private void process() {
      String pattern = txt.getText();
      if (pattern != null) {
        boolean keep = true;
        if (mode2.getSelectedIndex() == 1) {
          keep = false;
        }
        curClt.filter(pattern, mode.getSelectedIndex(), keep);
        refreshCltNodes();
        display.onGraphEvent(new GraphEvent(this));
        refreshInfo();
      }
      txt = null;
      diag2.setVisible(false);
      diag2.dispose();
    }
  }
}
