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
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileFilter;

import ccvisu.Options.InFormat;
import ccvisu.Options.OutFormat;
import ccvisu.Options.Verbosity;

/*****************************************************************
 * Reader for input data.
 * @version  $Revision: 1.8 $; $Date: 2007/12/15 01:20:50 $
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class ReaderData {

  /** Input stream reader object. */
  protected BufferedReader mIn;
  protected Verbosity      mVerbosity;

  /**
   * Constructor.
   * @param pIn          Stream reader object.
   * @param pVerbosity   level of verbosity.
   */
  public ReaderData(BufferedReader pIn, Verbosity pVerbosity) {
    this.mIn = pIn;
    mVerbosity = pVerbosity;
  }

  /*****************************************************************
   * Reads the graph or layout data from stream reader <code>in</code>.
   * @param graph  <code>GraphData</code> object to store the read graph or layout data in.
   *****************************************************************/
  abstract public void read(GraphData graph);

  // Helper.
  protected String readEntry(StringTokenizer st) {
    String result = st.nextToken();
    if (result.charAt(0) == '"') {
      while (result.charAt(result.length() - 1) != '"') {
        result = result + ' ' + st.nextToken();
      }
      result = result.substring(1, result.length() - 1);
    }
    return result;
  }

  public static FileFilter mkExtensionFileFilter(final InFormat pInFormat) {
    return ReaderData.mkExtensionFileFilter(pInFormat.getFileExtension(),
        pInFormat.getDescription());
  }

  public static FileFilter mkExtensionFileFilter(final OutFormat pOutFormat) {
    return ReaderData.mkExtensionFileFilter(pOutFormat.getFileExtension(),
        pOutFormat.getDescription());
  }

  public static FileFilter mkExtensionFileFilter(final String pExtension,
                                                 final String pDescription) {
    return new FileFilter() {
      @Override
      public boolean accept(File file) {
        if (file.getName().endsWith(pExtension)) { return true; }
        return false;
      }

      @Override
      public String getDescription() {
        return pDescription;
      }
    };
  }

};
