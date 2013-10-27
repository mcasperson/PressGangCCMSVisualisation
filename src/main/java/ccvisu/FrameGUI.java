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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ccvisu.Options.InFormat;
import ccvisu.Options.Option;
import ccvisu.Options.OutFormat;

/**
 * GUI for those who don't like command line 
 * @version  $Revision: 1.11 $; $Date: 2007/12/15 01:20:50 $
 * @author Damien Zufferey
 */
public class FrameGUI extends JFrame {

  private static final long serialVersionUID = 200604191040L;

  // Reference to CCVisu's main option settings.
  private Options           options;

  /**a choice for the input format*/
  private JComboBox         inFormat;
  /**a choice for the output format*/
  private JComboBox         outFormat;

  /** textfield to enter the input file*/
  private JTextField        inFile;
  /** textfield to enter the output file (when needed)*/
  private JTextField        outFile;

  private JButton           loadInFile;
  private JButton           saveOutFile;
  /** validate choices and execute*/
  private JButton           exec;

  private JPanel            MinimizerOptions;
  private JPanel            DISPOptions;                     //also VRML and SVG
  private JPanel            CVSOptions;

  //minimizer
  private JComboBox         dim;
  private JTextField        iter;
  private JTextField        initLayout;
  private JTextField        attrExp;
  private JTextField        repuExp;
  private JTextField        grav;
  private JCheckBox         noWeight;
  private JComboBox         vertRepu;
  private JButton           loadInitLayout;

  //DISP: screen vrml svg
  private JCheckBox         hideSource;
  private JCheckBox         blackCircle;
  private JCheckBox         anim;
  private JTextField        minVert;
  private JTextField        fontSize;
  private JTextField        scale;
  private JComboBox         annot;
  private JComboBox         backColor;

  //CVS
  private JTextField        timeWindow;

  /**
   * Constructor with parameters
   * @param  pOptions      Command-line options.
   * @throws java.awt.HeadlessException
   */
  public FrameGUI(Options pOptions) throws HeadlessException {
    super("CCVisu");

    this.options = pOptions;

    //construction of the GUI's widgets
    inFormat = new JComboBox();
    for (InFormat f : InFormat.values()) {
      inFormat.addItem(f);
    }
    inFormat.setSelectedItem(options.inFormat);
    inFormat.setName("in");

    outFormat = new JComboBox();
    for (OutFormat f : OutFormat.values()) {
      outFormat.addItem(f);
    }
    outFormat.setSelectedItem(options.outFormat);
    outFormat.setName("out");

    ItemListener formatListener = new ItemListener() {
      public void itemStateChanged(ItemEvent evt) {
        JComboBox scr = (JComboBox) evt.getSource();
        if (scr.getName().equals("in")) {
          InFormat in = (InFormat) scr.getSelectedItem();
          switch (in) {
          case LAY:
            enableMinimizerOptions(false);
            enableCVSOptions(false);
            break;
          case RSF:
            enableMinimizerOptions(true);
            enableCVSOptions(false);
            break;
          case CVS:
            enableMinimizerOptions(true);
            enableCVSOptions(true);
            break;
          }
          if (in == InFormat.LAY
              && outFormat.getSelectedItem() == OutFormat.RSF) {
            outFormat.setSelectedItem(InFormat.LAY.toString());
          }
        } else if (scr.getName().equals("out")) {
          OutFormat out = (OutFormat) scr.getSelectedItem();
          switch (out) {
          case DISP:
            enableDISPOptions(true);
            outFile.setText("");
            outFile.setEnabled(false);
            saveOutFile.setEnabled(false);
            break;
          case SVG:
          case VRML:
            enableSVGOptions(true);
            outFile.setEnabled(true);
            saveOutFile.setEnabled(true);
            break;
          case LAY:
          case RSF:
            enableDISPOptions(false);
            outFile.setEnabled(true);
            saveOutFile.setEnabled(true);
            break;
          }
          if (out == OutFormat.RSF
              && inFormat.getSelectedItem() == InFormat.LAY) {
            inFormat.setSelectedItem(InFormat.RSF.toString());
          }
        } else {
          System.err.println("PromptGUI.java, itemlistener: unknown source");
        }

      }
    };
    this.outFormat.addItemListener(formatListener);
    this.inFormat.addItemListener(formatListener);

    this.inFile = new JTextField(options.inputName, 30);
    this.outFile = new JTextField(options.outputName, 30);

    this.loadInFile = new JButton("open ...");
    this.loadInFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        String file = loadDialog();
        inFile.setText(file);
      }
    });

    this.saveOutFile = new JButton("save as ...");
    this.saveOutFile.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        String file = saveDialog();
        outFile.setText(file);
      }
    });

    this.exec = new JButton("Go");
    this.exec.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        // Write option settings from GUI into Options object.
        options.inFormat = (InFormat) inFormat.getSelectedItem();
        options.inputName = inFile.getText();
        options.outFormat = (OutFormat) outFormat.getSelectedItem();
        options.outputName = outFile.getText();

        options.timeWindow = Integer.parseInt(timeWindow.getText());
        options.nrDim = ((Integer) dim.getSelectedItem()).intValue();
        options.nrIterations = Integer.parseInt(iter.getText());
        options.initialLayStr = initLayout.getText();
        options.attrExponent = Float.parseFloat(attrExp.getText());
        options.repuExponent = Float.parseFloat(repuExp.getText());
        options.gravitation = Float.parseFloat(grav.getText());
        if (vertRepu.getSelectedIndex() == 1) {
          options.vertRepu = true;
        }
        options.noWeight = noWeight.isSelected();

        Option.minVert.set(Float.parseFloat(minVert.getText()));
        Option.fontSize.set(Integer.parseInt(fontSize.getText()));
        options.backColor = (Colors) backColor.getSelectedItem();
        Option.blackCircle.set(blackCircle.isSelected());
        Option.hideSource.set(hideSource.isSelected());
        if (annot.getSelectedIndex() == 1) { //all
          Option.annotAll.set(true);
        } else if (annot.getSelectedIndex() == 2) { //none
          Option.annotNone.set(true);
        }
        Option.scalePos.set(Float.parseFloat(scale.getText()));
        Option.anim.set(anim.isSelected());

        // Clean up.
        dispose();

        // Wake up the calling program in order to continue, and return.
        synchronized (options) {
          options.notify();
        }
      }
    });

    //minimizer panel
    createMinimizerOptions(options);
    //disp panel
    createDISPOptions(options);
    //CVS panel
    createCVSOptions(options);

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }

    });

    //layout
    JPanel main = new JPanel();
    this.add(main);
    main.setLayout(new GridBagLayout());

    JPanel up = new JPanel();
    up.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();

    c.weightx = 1;
    c.weighty = 1;
    c.fill = GridBagConstraints.NONE;
    c.gridx = 0;
    c.gridy = 0;
    up.add(new JLabel("Input format:"), c);
    c.gridx = 1;
    up.add(this.inFormat, c);
    c.gridx = 2;
    up.add(new JLabel("File:"), c);
    c.gridx = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    up.add(this.inFile, c);
    c.fill = GridBagConstraints.NONE;
    c.gridx = 4;
    up.add(this.loadInFile, c);
    c.gridx = 0;
    c.gridy = 1;
    up.add(new JLabel("Output format:"), c);
    c.gridx = 1;
    up.add(this.outFormat, c);
    c.gridx = 2;
    up.add(new JLabel("File:"), c);
    c.gridx = 3;
    c.fill = GridBagConstraints.HORIZONTAL;
    up.add(this.outFile, c);
    c.fill = GridBagConstraints.NONE;
    c.gridx = 4;
    up.add(this.saveOutFile, c);

    c.gridx = 0;
    c.gridy = 0;
    main.add(up, c);

    JPanel down = new JPanel();
    down.setLayout(new GridBagLayout());
    c.gridx = 0;
    c.gridy = 0;
    c.anchor = GridBagConstraints.NORTH;
    down.add(new JLabel("------- CVS options -------"), c);
    c.gridy = 1;
    c.anchor = GridBagConstraints.WEST;
    down.add(CVSOptions, c);
    c.anchor = GridBagConstraints.NORTH;
    c.gridy = 2;
    down.add(new JLabel("------- Minimizer options -------"), c);
    c.gridy = 3;
    c.anchor = GridBagConstraints.WEST;
    down.add(MinimizerOptions, c);
    c.anchor = GridBagConstraints.NORTH;
    c.gridy = 4;
    down.add(new JLabel("------- Display, SVG and VRML options -------"), c);
    c.gridy = 5;
    c.anchor = GridBagConstraints.WEST;
    down.add(DISPOptions, c);

    c.gridx = 0;
    c.gridy = 1;
    main.add(down, c);
    c.gridy = 2;
    c.anchor = GridBagConstraints.CENTER;
    main.add(exec, c);

    //activate default
    enableMinimizerOptions(true);
    enableCVSOptions(false);
    enableDISPOptions(true);
    outFile.setEnabled(false);
    saveOutFile.setEnabled(false);

    this.pack();
    this.setSize(this.getWidth(), this.getHeight() + 40); //a little bigger
    this.setLocationRelativeTo(null);//center of screen
    this.setVisible(true);
  }

  /**
   * Construct the panel for the minimizer options
   * @param  pOptions      Command-line options.
   */
  private void createMinimizerOptions(Options pOptions) {
    //widgets creation
    this.dim = new JComboBox();
    this.dim.addItem(new Integer(2));
    this.dim.addItem(new Integer(3));
    this.dim.setSelectedItem(new Integer(pOptions.nrDim));

    this.iter = new JTextField(Integer.toString(pOptions.nrIterations), 5);

    this.initLayout = new JTextField(pOptions.initialLayStr, 30);

    this.attrExp = new JTextField(Float.toString(pOptions.attrExponent), 5);
    this.repuExp = new JTextField(Float.toString(pOptions.repuExponent), 5);

    this.grav = new JTextField(Float.toString(pOptions.gravitation));

    this.vertRepu = new JComboBox();
    this.vertRepu.addItem("Edge repulsion");
    this.vertRepu.addItem("Vertex repulsion");
    if (pOptions.vertRepu) {
      this.vertRepu.setSelectedIndex(1);
    } else {
      this.vertRepu.setSelectedIndex(0);
    }

    this.noWeight = new JCheckBox("No weight", pOptions.noWeight);

    this.loadInitLayout = new JButton("open ...");
    this.loadInitLayout.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        String file = loadInitlayDialog();
        initLayout.setText(file);
      }
    });

    //layout
    MinimizerOptions = new JPanel();
    MinimizerOptions.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 1;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 0;
    MinimizerOptions.add(new JLabel("Dimention:"), c);
    c.gridx = 1;
    MinimizerOptions.add(this.dim, c);
    c.gridx = 2;
    MinimizerOptions.add(new JLabel("# of iteration:"), c);
    c.gridx = 3;
    MinimizerOptions.add(this.iter, c);
    c.gridx = 0;
    c.gridy = 1;
    MinimizerOptions.add(new JLabel("Initial layout:"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    MinimizerOptions.add(this.initLayout, c);
    c.gridwidth = 1;
    c.gridx = 3;
    MinimizerOptions.add(this.loadInitLayout, c);
    c.gridx = 0;
    c.gridy = 2;
    MinimizerOptions.add(new JLabel("Attraction:"), c);
    c.gridx = 1;
    MinimizerOptions.add(this.attrExp, c);
    c.gridx = 2;
    MinimizerOptions.add(new JLabel("Repulsion:"), c);
    c.gridx = 3;
    MinimizerOptions.add(this.repuExp, c);
    c.gridx = 0;
    c.gridy = 3;
    MinimizerOptions.add(new JLabel("Gravitation:"), c);
    c.gridx = 1;
    MinimizerOptions.add(this.grav, c);
    c.gridx = 2;
    MinimizerOptions.add(this.noWeight, c);
    c.gridx = 3;
    MinimizerOptions.add(this.vertRepu, c);
  }

  /**
   * construct the panel for the display options 
   * @param  pOptions      Command-line options.
   */
  private void createDISPOptions(Options pOptions) {

    //creations of widgets
    this.anim = new JCheckBox("Animation", Option.anim.getBool());

    this.blackCircle =
                       new JCheckBox("Black circle", Option.blackCircle
                           .getBool());

    this.hideSource =
                      new JCheckBox("Hide source Vertex", Option.hideSource
                          .getBool());

    this.annot = new JComboBox();
    this.annot.addItem("default");
    this.annot.addItem("all");
    this.annot.addItem("none");
    if (Option.annotAll.getBool()) {
      if (Option.annotNone.getBool()) {
        this.annot.setSelectedItem("default");
      } else {
        this.annot.setSelectedItem("all");
      }
    } else {
      if (Option.annotNone.getBool()) {
        this.annot.setSelectedItem("none");
      } else {
        this.annot.setSelectedItem("default");
      }
    }

    this.backColor = new JComboBox();
    this.backColor.addItem(Colors.BLACK);
    this.backColor.addItem(Colors.GRAY);
    this.backColor.addItem(Colors.LIGHTGRAY);
    this.backColor.addItem(Colors.WHITE);
    this.backColor.setSelectedItem(pOptions.backColor);

    this.scale = new JTextField(Float.toString(Option.scalePos.getFloat()));

    this.minVert = new JTextField(Float.toString(Option.minVert.getFloat()));

    this.fontSize = new JTextField(Integer.toString(Option.fontSize.getInt()));

    //layout
    DISPOptions = new JPanel();
    DISPOptions.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 1;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 0;
    DISPOptions.add(this.hideSource, c);
    c.gridx = 1;
    DISPOptions.add(this.blackCircle, c);
    c.gridx = 3;
    DISPOptions.add(this.anim, c);
    c.gridx = 0;
    c.gridy = 1;
    DISPOptions.add(new JLabel("Min vertex size:"), c);
    c.gridx = 1;
    DISPOptions.add(this.minVert, c);
    c.gridx = 2;
    DISPOptions.add(new JLabel("Font size:"), c);
    c.gridx = 3;
    DISPOptions.add(this.fontSize, c);
    c.gridx = 0;
    c.gridy = 2;
    DISPOptions.add(new JLabel("Annotation:"), c);
    c.gridx = 1;
    DISPOptions.add(this.annot, c);
    c.gridx = 2;
    DISPOptions.add(new JLabel("Background:"), c);
    c.gridx = 3;
    DISPOptions.add(this.backColor, c);
    c.gridx = 0;
    c.gridy = 3;
    DISPOptions.add(new JLabel("Scale:"), c);
    c.gridx = 1;
    DISPOptions.add(this.scale, c);
  }

  /**
   * construct the panel for the CVS options
   * @param  pOptions      Command-line options.
   */
  private void createCVSOptions(Options pOptions) {
    //creations of widgets
    this.timeWindow = new JTextField(Integer.toString(pOptions.timeWindow));

    //layout
    CVSOptions = new JPanel();
    CVSOptions.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.weightx = 1;
    c.weighty = 1;
    c.gridx = 0;
    c.gridy = 0;
    CVSOptions.add(new JLabel("Time window:"), c);
    c.gridx = 1;
    CVSOptions.add(this.timeWindow, c);
  }

  /**
   * en/disable the part concerning the minimizer
   * @param b
   */
  private void enableMinimizerOptions(boolean b) {
    dim.setEnabled(b);
    iter.setEnabled(b);
    initLayout.setEnabled(b);
    attrExp.setEnabled(b);
    repuExp.setEnabled(b);
    grav.setEnabled(b);
    noWeight.setEnabled(b);
    vertRepu.setEnabled(b);
    loadInitLayout.setEnabled(b);
  }

  /**
   * en/disable the part concerning CVS
   * @param b
   */
  private void enableCVSOptions(boolean b) {
    timeWindow.setEnabled(b);
  }

  /**
   * en/disable the part concerning the display
   * @param b
   */
  private void enableDISPOptions(boolean b) {
    hideSource.setEnabled(b);
    blackCircle.setEnabled(b);
    anim.setEnabled(b);
    minVert.setEnabled(b);
    fontSize.setEnabled(b);
    scale.setEnabled(!b);
    annot.setEnabled(b);
    backColor.setEnabled(b);
  }

  /**
   * en/disable the part concerning SVG and VRML format
   * @param b
   */
  private void enableSVGOptions(boolean b) {
    hideSource.setEnabled(b);
    blackCircle.setEnabled(b);
    anim.setEnabled(!b);
    minVert.setEnabled(b);
    fontSize.setEnabled(b);
    scale.setEnabled(b);
    annot.setEnabled(b);
    backColor.setEnabled(b);
  }

  /**
   * dialog to select a file to load
   * @return return a String containing the file name
   */
  private String loadDialog() {
    JFileChooser lFileDialog = new JFileChooser(".");
    lFileDialog.setFileFilter(ReaderData
        .mkExtensionFileFilter((InFormat) inFormat.getSelectedItem()));
    String lFileName = "";
    int lOutcome = lFileDialog.showOpenDialog(this);
    if (lOutcome == JFileChooser.APPROVE_OPTION) {
      assert (lFileDialog.getCurrentDirectory() != null);
      assert (lFileDialog.getSelectedFile() != null);
      lFileName =
                  lFileDialog.getCurrentDirectory().toString() + File.separator
                      + lFileDialog.getSelectedFile().getName();
    } else if (lOutcome == JFileChooser.CANCEL_OPTION) {
      // Do nothing.
    }
    return lFileName;
  }

  /**
   * dialog to select the initial layout
   * @return return a String containing the fliename
   */
  private String loadInitlayDialog() {
    JFileChooser lFileDialog = new JFileChooser(".");
    lFileDialog.setFileFilter(ReaderData.mkExtensionFileFilter(InFormat.LAY));
    String lFileName = "";
    int lOutcome = lFileDialog.showOpenDialog(this);
    if (lOutcome == JFileChooser.APPROVE_OPTION) {
      assert (lFileDialog.getCurrentDirectory() != null);
      assert (lFileDialog.getSelectedFile() != null);
      lFileName =
                  lFileDialog.getCurrentDirectory().toString() + File.separator
                      + lFileDialog.getSelectedFile().getName();
    } else if (lOutcome == JFileChooser.CANCEL_OPTION) {
      // Do nothing.
    }
    return lFileName;
  }

  /**
   * dialog to select an output file
   * @return return a String containing the filename
   */
  private String saveDialog() {
    JFileChooser lFileDialog = new JFileChooser(".");
    lFileDialog.setFileFilter(ReaderData
        .mkExtensionFileFilter((OutFormat) outFormat.getSelectedItem()));
    String lFileName = "";
    int lOutcome = lFileDialog.showSaveDialog(this);
    if (lOutcome == JFileChooser.APPROVE_OPTION) {
      assert (lFileDialog.getCurrentDirectory() != null);
      assert (lFileDialog.getSelectedFile() != null);
      lFileName =
                  lFileDialog.getCurrentDirectory().toString() + File.separator
                      + lFileDialog.getSelectedFile().getName();
    } else if (lOutcome == JFileChooser.CANCEL_OPTION) {
      // Do nothing.
    }
    return lFileName;
  }
}
