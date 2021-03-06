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

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/*****************************************************************
 * Writer for co-change graphs in RSF format.
 * @version  $Revision: 1.23 $; $Date: 2007/12/15 01:20:50 $
 * @author   Dirk Beyer
 *****************************************************************/
public class WriterDataRSF extends WriterData {

  public WriterDataRSF(PrintWriter out, GraphData graph) {
    super(out, graph);
  }

  /*****************************************************************
   * Writes the graph data in RSF (relational standard format).
   *****************************************************************/
  @Override
  public void write() {
    assert (graph.mTuples != null);
    out.println("# Generated by " + Options.toolDescription() + WriterData.endl
                + "# " + Options.currentDateTime() + WriterData.endl
                + "# Relations are printed in RSF Format:" + WriterData.endl
                + "# RELNAME <element_1> <element_2> ...");
    for (List<String> itTuple : graph.mTuples) {
      assert (itTuple.size() > 1);
      Iterator<String> it = itTuple.iterator();
      // Relation name.                                                    
      out.print(mkQuoted(it.next()));
      // Tuple elements.                                                   
      while (it.hasNext()) {
        out.print('\t' + mkQuoted(it.next()));
      }
      out.println();
    }
    out.flush();
  }
};
