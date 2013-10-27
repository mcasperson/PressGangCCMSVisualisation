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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

// Command-line options.
public class Options {

  // This enum construction is not yet used everywhere.
  public enum Option {

    // For layout output.
    /** Hide source vertices (first vertex of an edge). */
    hideSource("Boolean", "false",
               "draw only vertices that are not source of an edge."
                   + CCVisu.endl + Options.helpOptionNameIndent("")
                   + "In co-change graphs, all change-transaction vertices"
                   + CCVisu.endl + Options.helpOptionNameIndent("")
                   + "are source vertices"),
    // Scale circles for vertices in the layout.
    minVert("Float", "2.0f", "size of the smallest vertex; diameter"),

    fontSize("Integer", "14", "font size of vertex annotations"),
    // Draw black circles around the filled circles for vertices (strokes).
    blackCircle("Boolean", "true", ""),
    // Show the Edges
    showEdges("Boolean", "false",
              "show the edges of the graph; available only for CVS and RFS inFomat"),
    // Options for VRML writer.
    // Scale positions in the layout.
    scalePos("Float", "1.0f",
             "scaling factor for the layout to adjust; VRML and SVG only"),

    // Only for DISP writer.
    /** If true, the layout is already displayed while the minimizer is still improving it,
        and a simple mouse click on the canvas updates the current layout on the screen.
        If false, the layout is displayed only after minimization is completed. */
    anim("Boolean", "true", ""),

    // For all writers.
    annotAll("Boolean", "false", "annotate each vertex with its name"),

    annotNone("Boolean", "false", "annotate no vertex"),

    /** Heuristically shorten vertex labels. */
    shortNames("Boolean", "false", "shorten vertex labels"),

    dispFilter("Boolean", "false", "show extra controls for display filter"),

    openURL(
            "Boolean",
            "false",
            "the vertex names can be considered as URL and opened in a web broswer."
                + CCVisu.endl
                + Options.helpOptionNameIndent("")
                + "This option used with DISP output requires to hold CTRL KEY while clicking"),

    browser("String", "",
            "browser <str> will be invoked; if empty, CCVisu will try to guess");

    private final String mType;
    private final String mDefault;
    private final String mDescription;

    private Boolean      valueBoolean;
    private Integer      valueInteger;
    private Float        valueFloat;
    private String       valueString;

    Option(String pType, String pDefault, String pDescription) {
      mType = pType;
      mDefault = pDefault;
      mDescription = pDescription;
      if (mType.equals("Boolean")) {
        valueBoolean = new Boolean(mDefault);
      }
      if (mType.equals("Integer")) {
        valueInteger = new Integer(mDefault);
      }
      if (mType.equals("Float")) {
        valueFloat = new Float(mDefault);
      }
      if (mType.equals("String")) {
        valueString = new String(mDefault);
      }
    }

    String getType() {
      return mType;
    }

    public String getDefault() {
      return mDefault;
    }

    public String getDescription() {
      return mDescription;
    }

    public boolean getBool() {
      assert (valueBoolean != null);
      return valueBoolean;
    }

    public void set(boolean pValue) {
      assert (valueBoolean != null);
      valueBoolean = new Boolean(pValue);
    }

    public int getInt() {
      assert (valueInteger != null);
      return valueInteger;
    }

    public void set(int pValue) {
      assert (valueInteger != null);
      valueInteger = new Integer(pValue);
    }

    public float getFloat() {
      assert (valueFloat != null);
      return valueFloat;
    }

    public void set(float pValue) {
      assert (valueFloat != null);
      valueFloat = new Float(pValue);
    }

    public String getString() {
      assert (valueString != null);
      return valueString;
    }

    public void set(String pValue) {
      assert (valueString != null);
      valueString = new String(pValue);
    }

    public String getValue() {
      String lValue = "";
      if (mType.equals("Boolean")) {
        lValue = valueBoolean.toString();
      }
      if (mType.equals("Integer")) {
        lValue = valueInteger.toString();
      }
      if (mType.equals("Float")) {
        lValue = valueFloat.toString();
      }
      if (mType.equals("String")) {
        lValue = "'" + valueString.toString() + "'";
      }
      return lValue;
    }

    public static Option getOption(String pOptionName) {
      for (Option o : Option.values()) {
        if (pOptionName.equalsIgnoreCase("-" + o.toString())) { return o; }
      }
      return null;
    }
  }

  // Input formats: CVS, SVN, DOX, RSF, LAY
  public enum InFormat {
    /** CVS log format (only input). */
    CVS("log", "CVS Log Files"),
    /** SVN log format (only input). */
    SVN("xml", "SVN Log Files in XML"),
    /** Doxygen XML dump (only input). */
    DOX("xml", "Doxygen Output as XML Files"),
    /** Graph (relation) in relational standard format. */
    RSF("rsf", "Relational Standard Files"),
    /** Graph is passed as data structure from a third-party client. */
    AUX("AUX", "Auxiliary Graph Format"),
    /** Graph layout in textual format. */
    LAY("lay", "Layout Files");

    private String mFileExtension = "";
    private String mDescription   = "";

    InFormat(String pFileExtension, String pDescription) {
      mFileExtension = pFileExtension;
      mDescription = pDescription;
    }

    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
      return '.' + this.mFileExtension;
    }

    /**
     * @return the formatName
     */
    public String getDescription() {
      return this.mDescription;
    }

  }

  // Output formats: RSF, LAY, VRML, SVG, DISP
  public enum OutFormat {
    /** Graph (relation) in relational standard format. */
    RSF("rsf", "Relational Standard Files"),
    /** Graph layout in textual format. */
    LAY("lay", "Layout Files"),
    /** Graph layout in VRML format (only output). */
    VRML("wrl", "VRML Files"),
    /** Graph layout in SVG format (only output). */
    SVG("svg", "SVG Files"),
    /** Display graph layout on screen (only output). */
    DISP("not", "No Files, but Screen Output");

    private String mFileExtension = "";
    private String mDescription   = "";

    OutFormat(String pFileExtension, String pDescription) {
      mFileExtension = pFileExtension;
      mDescription = pDescription;
    }

    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
      return '.' + this.mFileExtension;
    }

    /**
     * @return the formatName
     */
    public String getDescription() {
      return this.mDescription;
    }
  }

  // Verbosity levels.
  public enum Verbosity {
    /** Don't say much, only report errors. */
    QUIET(0),
    /** Print warnings as well. */
    WARNING(1),
    /** Print statistics and other information. */
    VERBOSE(2),
    /** Print debugging information as well. */
    DEBUG(3);

    private int mLevel;

    Verbosity(int pLevel) {
      mLevel = pLevel;
    }

    boolean isAtLeast(Verbosity pVerbosity) {
      return mLevel >= pVerbosity.mLevel;
    }
  }

  boolean            guiMode       = false;

  // Input format
  InFormat           inFormat      = InFormat.RSF;
  String             inputName     = "stdin";
  // Output format.
  OutFormat          outFormat     = OutFormat.DISP;
  String             outputName    = "stdout";

  // Filter (whitelist) for relations to be selected for visualization.
  Collection<String> relSelection  = new ArrayList<String>();

  // For CVS reader. Time constant for sliding window.
  int                timeWindow    = 180000;
  boolean            sliding       = false;

  // For layout.
  int                nrDim         = 2;
  /* Number of iterations of the minimizer.
   * Choose appropriate values by observing the convergence of energy. */
  public int         nrIterations  = 100;
  String             initialLayStr = "";
  public GraphData   initialLayout = null;
  boolean            fixedInitPos  = false;

  // For energy model.
  /* Exponent of the Euclidian distance in the attraction 
   * term of the energy (default: 1). */
  float              attrExponent  = 1.0f;
  // Exponent of the Euclidian distance in the repulsion term of the energy (default: 0).
  float              repuExponent  = 0.0f;
  /* Use vertex repulsion instead of edge repulsion,
   *   true for vertex repulsion, false for edge repulsion
   *   (default: edge repulsion). */
  boolean            vertRepu      = false;
  /* Use unweighted model by ignoring the edge weights,
   *   true for unweighted, false for weighted (default: weighted). */
  boolean            noWeight      = false;
  /* Gravitation factor for the Barnes-Hut-procedure,
   *   attraction to the barycenter (default: 0.001). */
  float              gravitation   = 0.001f;

  // For layout output.
  Colors             backColor     = Colors.WHITE;

  public Frame       frame         = null;

  /* In/Out parameter representing the graph. */
  public GraphData   graph         = null;

  public Verbosity   verbosity     = Verbosity.QUIET;

  /*****************************************************************
   * Parses command-line options.
   * @param args    Array of command-line options.
   *****************************************************************/
  public void parseCmdLine(String[] args) {
    List<String> argsList = Arrays.asList(args);

    if (argsList.size() == 0) {
      guiMode = true;
    }

    for (ListIterator<String> it = argsList.listIterator(); it.hasNext();) {
      String arg = Options.getNext(it);

      // General options without argument.
      // Help.
      if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")) {
        Options.printHelp();
        System.exit(0);
      }
      // Version.
      else if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("--version")) {
        Options.printVersion();
        System.exit(0);
      }
      // Quiet.
      else if (arg.equalsIgnoreCase("-q")
               || arg.equalsIgnoreCase("--nowarnings")) {
        verbosity = Verbosity.QUIET;
      }
      // Warnings on.
      else if (arg.equalsIgnoreCase("-w") || arg.equalsIgnoreCase("--warnings")) {
        verbosity = Verbosity.WARNING;
      }
      // Verbose.
      else if (arg.equalsIgnoreCase("-verbose")) {
        verbosity = Verbosity.VERBOSE;
      }
      // GUI mode.
      else if (arg.equalsIgnoreCase("-gui")) {
        guiMode = true;
      }
      // Check if asserts are switched on.
      else if (arg.equalsIgnoreCase("-assert")) {
        boolean assertsEnabled = false;
        assert (assertsEnabled = true); // Intentional side effect!!!
        if (assertsEnabled) {
          System.out.println("Assertions are enabled.");
        } else {
          System.out.println("Assertions are disabled.");
        }
        System.exit(0);
      }

      // General options with argument.
      // Change input reader.
      else if (arg.equalsIgnoreCase("-i")) {
        arg = Options.getNext(it);
        inputName = arg;
      }
      // Change output writer.
      else if (arg.equalsIgnoreCase("-o")) {
        arg = Options.getNext(it);
        outputName = arg;
      }
      // Input format.
      else if (arg.equalsIgnoreCase("-inFormat")) {
        arg = Options.getNext(it);
        inFormat = Options.getInFormat(arg);
      }
      // Output format.
      else if (arg.equalsIgnoreCase("-outFormat")) {
        arg = Options.getNext(it);
        outFormat = Options.getOutFormat(arg);
      }

      // Options for DOX reader.
      // Relations that should be used for computing layout.
      else if (arg.equalsIgnoreCase("-relSelect")) {
        arg = Options.getNext(it);
        relSelection.add(arg);
      }

      // Options for CVS reader.
      // Time-constant of sliding-window for change transaction recovery 
      //   (in milli-seconds).
      else if (arg.equalsIgnoreCase("-timeWindow")) {
        arg = Options.getNext(it);
        timeWindow = Integer.parseInt(arg);
      }
      // sliding/fixed-window for change transaction recovery 
      else if (arg.equalsIgnoreCase("-slidingTW")) {
        sliding = true;
      }

      // Options for layout.
      // Number of dimensions (up to 3).
      else if (arg.equalsIgnoreCase("-dim")) {
        arg = Options.getNext(it);
        nrDim = Integer.parseInt(arg);
      }
      // Number of iterations for minimization.
      else if (arg.equalsIgnoreCase("-iter")) {
        arg = Options.getNext(it);
        nrIterations = Integer.parseInt(arg);
      }
      // Initial layout.
      else if (arg.equalsIgnoreCase("-initLayout")) {
        arg = Options.getNext(it);
        initialLayStr = arg;
      }
      // Fixed positions for nodes in the initial layout given by option -initLayout.
      else if (arg.equalsIgnoreCase("-fixedInitPos")) {
        fixedInitPos = true;
      }

      // Energy model.
      // Attraction exponent.
      else if (arg.equalsIgnoreCase("-attrExp")) {
        arg = Options.getNext(it);
        attrExponent = Float.parseFloat(arg);
      }
      // Repulsion exponent.
      else if (arg.equalsIgnoreCase("-repuExp")) {
        arg = Options.getNext(it);
        repuExponent = Float.parseFloat(arg);
      }
      // Node repulsion.
      else if (arg.equalsIgnoreCase("-vertRepu")) {
        vertRepu = true;
      }
      // No weights.
      else if (arg.equalsIgnoreCase("-noWeight")) {
        noWeight = true;
      }
      // Gravitation factor.
      else if (arg.equalsIgnoreCase("-grav")) {
        arg = Options.getNext(it);
        gravitation = Float.parseFloat(arg);
      }

      // TODO: Remove/convert to new the following:

      // Options for output writers.
      // Avoid black circles around the filled circles for vertices (strokes).
      else if (arg.equalsIgnoreCase("-noBlackCircle")) {
        Option.blackCircle.set(false);
      }
      // Background color.
      else if (arg.equalsIgnoreCase("-backcolor")) {
        arg = Options.getNext(it);
        backColor = Colors.valueOfUpper(arg);
      }

      // Only for display writer.
      // Animation of layout during minimization, if outFormat is DISP.
      else if (arg.equalsIgnoreCase("-noAnim")) {
        Option.anim.set(false);
      }

      // Switch on user-defined marker to emphasize certain vertices.
      else if (arg.equalsIgnoreCase("-markScript")) {
        arg = Options.getNext(it);
        try {
          CCVisu.marker =
                          new MarkerScript(new BufferedReader(new FileReader(
                              arg)), verbosity);
        } catch (FileNotFoundException e) {
          System.err.println("Error reading file -- file not found: " + arg);
          CCVisu.marker = new Marker();
        }
      }

      // 'Automatic' option.
      else if (Option.getOption(arg) != null) {
        Option lOption = Option.getOption(arg);
        if (lOption.getType().equals("Boolean")) {
          lOption.set(true);
        } else if (lOption.getType().equals("Integer")) {
          arg = Options.getNext(it);
          lOption.set(Integer.parseInt(arg));
        } else if (lOption.getType().equals("Float")) {
          arg = Options.getNext(it);
          lOption.set(Float.parseFloat(arg));
        } else if (lOption.getType().equals("String")) {
          arg = Options.getNext(it);
          lOption.set(arg);
        }
      }

      // Unknown option.
      else {
        System.err.println("Usage error: Option '" + arg + "' unknown.");
        System.exit(1);
      }
    } // for parsing command-line 
  }

  /*****************************************************************
   * Checks whether the current command-line argument a parameter.
   * If there is no follower argument, it exits the program.
   * @param it    String list iterator that points to the current argument.
   *****************************************************************/
  private static String getNext(ListIterator<String> it) {
    if (!it.hasNext()) {
      System.err.println("Usage error: Option '" + it.previous()
                         + "' requires an argument (file).");
      System.exit(1);
    }
    return it.next();
  }

  /*****************************************************************
   * Prints version information.
   *****************************************************************/
  private static void printVersion() {
    System.out
        .println(Options.toolVersion()
                 + "."
                 + CCVisu.endl
                 + "Copyright (C) 2005-2010  Dirk Beyer (Uni Passau, Germany). "
                 + CCVisu.endl
                 + "CCVisu is free software, released under the GNU LGPL. ");
  }

  /*****************************************************************
   * Returns the tool name with version info.
   *****************************************************************/
  public static String toolVersion() {
    return "CCVisu 3.0, 2010-02-04";
  }

  /*****************************************************************
   * Returns a brief tool description.
   *****************************************************************/
  public static String toolDescription() {
    return "CCVisu, a tool for visual graph clustering and "
           + "general force-directed graph layout.";
  }

  /*****************************************************************
   * Returns data and time in ISO format.
   *****************************************************************/
  public static String currentDateTime() {
    return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
  }

  /*****************************************************************
   * Prints usage information.
   *****************************************************************/
  private static void printHelp() {
    // Usage and info message.
    System.out
        .print(CCVisu.endl
               + "This is "
               + Options.toolDescription()
               + CCVisu.endl
               + "   "
               + CCVisu.endl
               + "Usage: java ccvisu.CCVisu [OPTION]... "
               + CCVisu.endl
               + "Compute a layout for a given (co-change) graph (or convert). "
               + CCVisu.endl
               + "   "
               + CCVisu.endl
               + "Options: "
               + CCVisu.endl
               + "General options: "
               + CCVisu.endl
               + "   -h  --help        display this help message and exit. "
               + CCVisu.endl
               + "   -v  --version     print version information and exit. "
               + CCVisu.endl
               + "   -q  --nowarnings  quiet mode (default). "
               + CCVisu.endl
               + "   -w  --warnings    enable warnings. "
               + CCVisu.endl
               + "   -verbose          verbose mode. "
               + CCVisu.endl
               + "   -assert           check if assertions are enabled. "
               + CCVisu.endl
               + "   -gui              GUI mode (provides a window to set options). "
               + CCVisu.endl
               + "   -i <file>         read input data from given file (default: stdin). "
               + CCVisu.endl
               + "   -o <file>         write output data to given file (default: stdout). "
               + CCVisu.endl
               + "   -inFormat FORMAT  read input data in format FORMAT (default: RSF, see below). "
               + CCVisu.endl
               + "   -outFormat FORMAT write output data in format FORMAT (default: DISP, see below). "
               + CCVisu.endl
               + "   "
               + CCVisu.endl
               + "Layouting options: "
               + CCVisu.endl
               + "   -dim <int>        number of dimensions of the layout (2 or 3, default: 2). "
               + CCVisu.endl
               + "   -iter <int>       number of iterations of the minimizer (default: 100). "
               + CCVisu.endl
               + "   -initLayout <file>  use layout from file (LAY format) as initial layout "
               + CCVisu.endl
               + "                     (default: random layout). "
               + CCVisu.endl
               + "   "
               + CCVisu.endl
               + "Energy model options: "
               + CCVisu.endl
               + "   -attrExp <int>    exponent for the distance in the attraction term "
               + CCVisu.endl
               + "                     (default: 1). "
               + CCVisu.endl
               + "   -repuExp <int>    exponent for the distance in the repulsion term "
               + CCVisu.endl
               + "                     (default: 0). "
               + CCVisu.endl
               + "   -vertRepu         use vertex repulsion instead of edge repulsion "
               + CCVisu.endl
               + "                     (default: edge repulsion). "
               + CCVisu.endl
               + "   -noWeight         use unweighted model (default: weighted). "
               + CCVisu.endl
               + "   -grav <float>     gravitation factor for the Barnes-Hut-procedure "
               + CCVisu.endl
               + "                     (default: 0.001). "
               + CCVisu.endl
               + "   "
               + CCVisu.endl
               + "DOX reader option: "
               + CCVisu.endl
               + "   -relSelect <rel>  selects a relation for visualization"
               + CCVisu.endl
               + "                     (default: REFFILE). "
               + CCVisu.endl
               + "   "
               + CCVisu.endl
               + "CVS reader options: "
               + CCVisu.endl
               + "   -timeWindow <int> time window for transaction recovery, in milli-seconds "
               + CCVisu.endl
               + "                     (default: 180'000). "
               + CCVisu.endl
               + "   -slidingTW        the time window 'slides': a new commit node is created"
               + CCVisu.endl
               + "                     when the time difference between two commited files is bigger"
               + CCVisu.endl
               + "                     than the time window (default: fixed time window)."
               + CCVisu.endl
               + "   "
               + CCVisu.endl
               + "Layout writer options: "
               + CCVisu.endl
               + Options.helpForOption(Option.hideSource)
               + Options.helpForOption(Option.minVert)
               + Options.helpForOption(Option.fontSize)
               + "   -backColor COLOR  background color (default: WHITE). "
               + CCVisu.endl
               + "                     Colors: BLACK, GRAY, LIGHTGRAY, WHITE."
               + CCVisu.endl
               + "   -noBlackCircle    no black circle around each vertex (default: with). "
               + CCVisu.endl
               + Options.helpForOption(Option.showEdges)
               + Options.helpForOption(Option.scalePos)
               + "   -noAnim           layout not shown while minimizer is still improving it "
               + CCVisu.endl
               + "                     (default: show). "
               + CCVisu.endl
               + Options.helpForOption(Option.annotAll)
               + Options.helpForOption(Option.annotNone)
               + Options.helpForOption(Option.shortNames)
               + Options.helpForOption(Option.dispFilter)
               + "   -markScript <file> highlight vertices using group information parsed from file "
               + CCVisu.endl
               + "                     (see file marker_script.example to see how it works)."
               + CCVisu.endl
               + Options.helpForOption(Option.openURL)
               + CCVisu.endl
               + "DISP specific option"
               + CCVisu.endl
               + Options.helpForOption(Option.browser)
               + "   "
               + CCVisu.endl
               + "Formats: "
               + CCVisu.endl
               + "   CVS               CVS log format (for input only; produce with 'cvs log -Nb')."
               + CCVisu.endl
               + "   SVN               SVN log format (for input only; produce with 'svn log -v --xml')."
               + CCVisu.endl
               + "   DOX               Doxygen XML format (for input only; produce with 'doxygen')."
               + CCVisu.endl
               + "   RSF               graph in relational standard format."
               + CCVisu.endl
               + "   LAY               graph layout in textual format."
               + CCVisu.endl
               + "   VRML              graph layout in VRML format (for output only)."
               + CCVisu.endl
               + "   SVG               graph layout in SVG format (for output only)."
               + CCVisu.endl
               + "   DISP              display gaph layout on screen (only output)."
               + CCVisu.endl + "   " + CCVisu.endl
               + "http://www.cs.sfu.ca/~dbeyer/CCVisu/ " + CCVisu.endl + "   "
               + CCVisu.endl
               + "Report bugs to Dirk Beyer <firstname.lastname@sfu.ca>. "
               + CCVisu.endl + "   " + CCVisu.endl);
  }

  private static String helpForOption(Option pOption) {
    String lArgText = "";
    if (pOption.getType().equals("Integer")) {
      lArgText = " <int>";
    }
    if (pOption.getType().equals("Float")) {
      lArgText = " <float>";
    }
    if (pOption.getType().equals("String")) {
      lArgText = " <str>";
    }
    return Options.helpOptionNameIndent("-" + pOption.toString() + lArgText)
           + pOption.getDescription() + " (current value: "
           + pOption.getValue() + "). " + CCVisu.endl;
    /*
    return "   -" + pOption.toString() + "         " + pOption.getDescription()
           + " (default value: " + pOption.getDefault() + "). " + CCVisu.endl;
    */
  }

  private static String helpOptionNameIndent(String pString) {
    String lIndent = "                  ";
    if (pString.length() >= lIndent.length()) { return pString; }
    return "   " + pString
           + lIndent.substring(pString.length(), lIndent.length());
  }

  /*****************************************************************
   * Transforms the format given as a string into the appropriate enum.
   * @param formatStr  File format string to be converted to enum.
   * @return           File format identifier.
   *****************************************************************/
  private static InFormat getInFormat(String formatStr) {
    InFormat result = InFormat.RSF;
    try {
      result = InFormat.valueOf(formatStr.toUpperCase());
    } catch (Exception e) {
      Options.handleFormatException(formatStr);
    }
    return result;
  }

  /*****************************************************************
   * Transforms the format given as a string into the appropriate enum.
   * @param formatStr  File format string to be converted to enum.
   * @return           File format identifier.
   *****************************************************************/
  private static OutFormat getOutFormat(String formatStr) {
    OutFormat result = OutFormat.RSF;
    try {
      result = OutFormat.valueOf(formatStr.toUpperCase());
    } catch (Exception e) {
      Options.handleFormatException(formatStr);
    }
    return result;
  }

  /*****************************************************************
   * Handles exceptions of format conversions.
   * @param formatStr    input/output format as string
   *****************************************************************/
  private static void handleFormatException(String formatStr) {
    System.err.println("Usage error: '" + formatStr
                       + "' is not a valid input/output format.");
    System.exit(1);
  }
}
