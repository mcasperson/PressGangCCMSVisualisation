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

import java.util.Collection;
import java.util.List;

import ccvisu.Options.Verbosity;

/**
 * @author stadlerb
 * 
 * Reads tuples into a GraphData object using another ReaderData object and 
 * removes all tuples that don't belong to a relation from a given whitelist.
 */
public class ReaderDataGraphFilter extends ReaderDataGraph {

  private ReaderDataGraph    base;
  private Collection<String> whitelist;

  public ReaderDataGraphFilter(ReaderDataGraph base, Verbosity pVerbosity,
                               Collection<String> whitelist) {
    super(null, pVerbosity);

    assert (base != null && whitelist != null);

    this.base = base;
    this.whitelist = whitelist;
  }

  @Override
  public Relation readTuples() {
    Relation lBaseTuples = base.readTuples();

    Relation lNewTuples = new Relation();
    for (List<String> itTuple : lBaseTuples) {
      if (itTuple.size() > 0 && whitelist.contains(itTuple.get(0))) {
        lNewTuples.add(itTuple);
      }
    }

    return lNewTuples;

    /* This has bad time complexity if the list is an ArrayList:
      for (ListIterator<List<String>> it = result.listIterator(); it.hasNext();) {
        List<String> tuple = it.next();
        if (tuple.size() == 0 || !whitelist.contains(tuple.get(0))) {
          it.remove();
        }
      }
      return result;
    */
  }
}
