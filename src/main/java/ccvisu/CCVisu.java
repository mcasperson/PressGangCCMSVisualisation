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

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import ccvisu.Options.InFormat;
import ccvisu.Options.Option;
import ccvisu.Options.OutFormat;
import ccvisu.Options.Verbosity;

/*****************************************************************
 * Main class of the CCVisu package.
 * Contains the main program and some auxiliary methods.
 * @version  $Revision: 1.117 $; $Date: 2009-01-20 23:03:38 $
 * @author   Dirk Beyer
 *****************************************************************/
public class CCVisu {

  /** End of line.*/
  public final static String endl   = System.getProperty("line.separator");

  // Marker.
  // Emphasize, i.e., add annotation of vertex name for some vertices.
  // Option -markScript changes this to a subclass of Marker.
  public static Marker       marker = new Marker();

  /*****************************************************************
   * Main program. Performs the following steps.
   * 1) Parses and handles the command line options.
   * 2) Processes the actual task.
   * @param args  Command line arguments.
   *****************************************************************/
  public static void main(String[] args) {

    // This is where we store all options (configuration settings) for CCVisu.
    Options options = new Options();
    // Set the options that are given via command line.
    options.parseCmdLine(args);

    if (options.guiMode) {
      // Set, in addition, the options as given via GUI dialog.
      new FrameGUI(options);
      synchronized (options) {
        try {
          // Wait for the GUI options dialog to finish.
          options.wait();
        } catch (InterruptedException e) {
        }
      }
    }

    // Create a frame for drawing the layout.
    options.frame = new Frame();
    // Initialize the graph representation.
    options.graph = new GraphData();

    CCVisu.process(options);

    if (options.outFormat == OutFormat.DISP) {
      synchronized (options) {
        try {
          // Wait for the layout frame and control dialog to finish.
          options.wait();
        } catch (InterruptedException e) {
        }
      }
    }
    System.exit(0);
  }

  /*****************************************************************
   * Processing the actual task. Performs the following steps.
   * 1) Creates the appropriate input reader and reads the input.
   * 2) Computes the layout (if necessary).
   * 3) Creates the appropriate output writer and writes the output.
   * @param options  Command-line options, already parsed.
   *****************************************************************/
  public static void process(Options options) {
    assert (options.graph != null);
    assert (options.frame != null);

    // Default I/O.

    // Setup of input reader.
    BufferedReader lIn = null;
    try {
      if (options.inputName.equalsIgnoreCase("stdin")) {
        lIn = new BufferedReader(new InputStreamReader(System.in));
      } else {
        lIn = new BufferedReader(new FileReader(options.inputName));
      }
    } catch (Exception e) {
      System.err.println("Exception while opening file '" + options.inputName
                         + "' for reading.");
      System.exit(1);
    }

    // Setup of output writer.
    PrintWriter lOut = null;
    try {
      if (options.outputName.equalsIgnoreCase("stdout")) {
        lOut =
               new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                   System.out)));
      } else {
        lOut =
               new PrintWriter(new BufferedWriter(new FileWriter(
                   options.outputName)));
      }
    } catch (Exception e) {
      System.err.println("Exception while opening file '" + options.outputName
                         + "' for writing.");
      System.exit(1);
    }

    if (!options.initialLayStr.equalsIgnoreCase("")) {
      // Setup of optional initial layout.
      BufferedReader initialLayoutStream = null;
      options.initialLayout = new GraphData();
      try {
        initialLayoutStream =
                              new BufferedReader(new FileReader(
                                  options.initialLayStr));
      } catch (Exception e) {
        System.err.println("Exception while opening file '"
                           + options.initialLayStr + "' for reading.");
        System.exit(1);
      }
      // Read initial (pre-computed) layout from file.
      (new ReaderDataLAY(initialLayoutStream, options.verbosity))
          .read(options.initialLayout);
      if (options.verbosity.isAtLeast(Verbosity.VERBOSE)) {
        System.err.println("" + options.initialLayout.vertices.size()
                           + " vertices read.");
        System.err.println("Initial layout reading finished.");
      }
      // Close the input file.
      try {
        initialLayoutStream.close();
      } catch (Exception e) {
        System.err.println("Exception while closing input file: ");
        System.err.println(e);
      }
      // Reset vertex degrees, 
      // i.e., use the degrees from the graph and ignore the degree from read layout.
      for (GraphVertex itVertex : options.initialLayout.vertices) {
        itVertex.degree = 0;
      }
    }

    // Set input reader.
    ReaderData graphReader = null;
    switch (options.inFormat) {
    case CVS: // CVS log format.
      graphReader =
                    new ReaderDataGraphCVS(lIn, options.verbosity,
                        options.timeWindow, options.sliding);
      break;
    case SVN: // SVN log format.
      graphReader = new ReaderDataGraphSVN(lIn, options.verbosity);
      break;
    case DOX: // Doxygen XML dump format.
      graphReader =
                    new ReaderDataGraphDOX(lIn, options.verbosity,
                        options.inputName);
      break;
    case RSF:// Graph in RSF format.
      graphReader = new ReaderDataGraphRSF(lIn, options.verbosity);
      break;
    case AUX:// Graph in AUX format.
      // Graph is set by calling client. -- Do nothing.
      graphReader = new ReaderDataGraphAUX(lIn, options.verbosity);
      break;
    case LAY:// Layout in text format LAY.
      graphReader = new ReaderDataLAY(lIn, options.verbosity);
      break;
    default:
      System.err.println("Runtime error: Unexpected input format '"
                         + options.inFormat.toString() + "'.");
      System.exit(1);
    }

    // Set filter for selection of user relations for visualization.
    if (options.relSelection.size() > 0) {
      // The user has given relations to select.
      assert (graphReader instanceof ReaderDataGraph);
      graphReader =
                    new ReaderDataGraphFilter((ReaderDataGraph) graphReader,
                        options.verbosity, options.relSelection);
    } else {
      if (options.inFormat == InFormat.DOX
          && options.outFormat != OutFormat.RSF) {
        // The user has not given any relation name to select,
        //   but wants to actually draw the relation.
        System.err
            .println("Warning: Using default DOX-derived relation refFile.");
        System.err
            .println("Change this with parameter '-relSelect <rel-name>'");
        options.relSelection.add("refFile");
        assert (graphReader instanceof ReaderDataGraph);
        graphReader =
                      new ReaderDataGraphFilter((ReaderDataGraph) graphReader,
                          options.verbosity, options.relSelection);
      }
    }

    // Read the data using the reader (i.e., fill into existing graph structure).
    graphReader.read(options.graph);
    if (options.verbosity.isAtLeast(Verbosity.VERBOSE)) {
      System.err
          .println("" + options.graph.vertices.size() + " vertices read.");
      System.err.println("Graph reading finished.");
    }

    // Close the input file.
    try {
      lIn.close();
    } catch (Exception e) {
      System.err.println("Exception while closing input file: ");
      System.err.println(e);
    }

    // Disable impossible input/output combinations.
    if (options.inFormat == InFormat.LAY && options.outFormat == OutFormat.RSF) {
      System.err
          .println("Usage error: Cannot produce RSF output from LAY input.");
      System.exit(1);
    }

    // Handle vertex options.
    for (GraphVertex curVertex : options.graph.vertices) {
      // annotAll (annotate each vertex with its name).
      if (Option.annotAll.getBool()) {
        curVertex.showName = true;
      }
      // annotNone (annotate no vertex).
      if (Option.annotNone.getBool()) {
        curVertex.showName = false;
      }
    }
    // lVertex.fixedPos == true means that the minimizer does not change 
    //   lVertex's position.
    if (options.fixedInitPos && options.initialLayout != null) {
      for (GraphVertex lCurrVertex : options.graph.vertices) {
        // If the current vertex exists in the read initial layout,
        // then mark its position as fixed.
        if (options.initialLayout.nameToVertex.containsKey(lCurrVertex.name)) {
          lCurrVertex.fixedPos = true;
        }
      }
    }

    // Output writer.
    WriterData dataWriter = null;

    // Determine if we need to compute a layout.
    if (options.inFormat != InFormat.LAY && options.outFormat != OutFormat.RSF) {
      // Initialize layout.
      CCVisu.initializeLayout(options);
      // Set minimizer algorithm. 
      // So far there is only one implemented in CCVisu.
      Minimizer minimizer = new MinimizerBarnesHut(options);
      if (options.outFormat == OutFormat.DISP && Option.anim.getBool()) {
        // Display layout animation during minimization.
        dataWriter = new WriterDataGraphicsDISP(lOut, options.graph, options);
        // Animation  --  add GraphEventListener.
        WriterDataGraphicsDISP displ = (WriterDataGraphicsDISP) dataWriter;
        minimizer.addGraphEventListener(displ.getDisplay());
      }
      // Compute layout for given graph.
      minimizer.minimizeEnergy();
    }

    // Set output writer.
    switch (options.outFormat) {
    case RSF: // Co-change graph in RSF.
      dataWriter = new WriterDataRSF(lOut, options.graph);
      break;
    case LAY: // Layout in text format LAY.
      dataWriter = new WriterDataLAY(lOut, options.graph);
      break;
    case VRML: // Layout in VRML format.
      dataWriter = new WriterDataGraphicsVRML(lOut, options.graph, options);
      break;
    case SVG: // Layout in SVG format.
      dataWriter = new WriterDataGraphicsSVG(lOut, options.graph, options);
      break;
    case DISP: // Display layout on screen.
      if (dataWriter == null) {
        // ... if the view is not already there, i.e. animation is not activated.
        dataWriter = new WriterDataGraphicsDISP(lOut, options.graph, options);
      }
      break;
    default:
      System.err.println("Runtime error: Unexpected output format '"
                         + options.outFormat.toString() + "'.");
      System.exit(1);
    }

    // Write the data using the writer.
    dataWriter.write();

    // Close the output file.
    lOut.flush();
    lOut.close();
  }

  /*****************************************************************
   * Compute randomized initial layout for a given graph 
   * with the given number of dimensions.
   *****************************************************************/
  public static void initializeLayout(Options options) {
    // Initialize with random positions.
    for (GraphVertex lCurrVertex : options.graph.vertices) {
      lCurrVertex.pos.x = 2 * (float) Math.random() - 1;

      if (options.nrDim >= 2) {
        lCurrVertex.pos.y = 2 * (float) Math.random() - 1;
      } else {
        lCurrVertex.pos.y = 0;
      }

      if (options.nrDim == 3) {
        lCurrVertex.pos.z = 2 * (float) Math.random() - 1;
      } else {
        lCurrVertex.pos.z = 0;
      }
    }

    // Copy positions and properties from the initial layout 
    //   that was read from file.
    if (options.initialLayout != null) {
      for (GraphVertex lCurrVertex : options.graph.vertices) {
        GraphVertex oldVertex =
                                options.initialLayout.nameToVertex
                                    .get(lCurrVertex.name);
        if (oldVertex != null) {
          lCurrVertex.color = oldVertex.color;
          lCurrVertex.showName = oldVertex.showName;
          lCurrVertex.pos = new Position(oldVertex.pos);
        }
      }
    }

    // Highlight certain vertices (according to Marker m) by marking.
    for (GraphVertex curVertex : options.graph.vertices) {
      CCVisu.marker.mark(curVertex);
    }
  }
};
