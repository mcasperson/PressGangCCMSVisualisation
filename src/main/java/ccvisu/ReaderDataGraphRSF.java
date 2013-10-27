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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ccvisu.Options.Verbosity;

/*****************************************************************
 * Reader for co-change graphs in RSF format.
 * @version  $Revision: 1.30 $; $Date: 2007/12/15 01:20:50 $
 * @author   Dirk Beyer
 *****************************************************************/
public class ReaderDataGraphRSF extends ReaderDataGraph {

  /**
   * Constructor.
   * @param in  Stream reader object.
   */
  public ReaderDataGraphRSF(BufferedReader in, Verbosity pVerbosity) {
    super(in, pVerbosity);
  }

  /*****************************************************************
   * Reads the edges of a graph in RSF (relational standard format)
   * from stream reader <code>in</code>, 
   * and stores them in a list (of <code>GraphEdgeString</code> elements).
   * @return List of string edges.
   *****************************************************************/
  @Override
  public Relation readTuples() {
    Relation result = new Relation();
    int lineno = 1;
    String lLine = "";
    try {
      while ((lLine = mIn.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(lLine);
        if (st.hasMoreTokens() && lLine.charAt(0) != '#') {
          List<String> newTuple = new ArrayList<String>();
          while (st.hasMoreTokens()) {
            newTuple.add(readEntry(st));
          }
          result.add(newTuple);
          /*
          int conf = Integer.parseInt(st.nextToken());
          if (conf >300) {
            result.add(edge);
          }
          */
        }
        ++lineno;
      }
    } catch (Exception e) {
      System.err.println("Runtime error: Input Exception while reading "
                         + "a graph edge, at line " + lineno + ":");
      System.err.println(lLine);
      System.err.println(e);
    }

    return result;
  }

};
