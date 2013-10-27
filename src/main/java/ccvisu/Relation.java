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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Relation extends ArrayList<List<String>> {
  private static final long serialVersionUID = 20100124113500L;

  Relation() {
    super();
  }

  Relation(Relation pRelation) {
    super(pRelation);
  }

  public void addTuple(String pRelName, String pSourceName, String pTargetName,
                       Set<String> pStoredTuples) {
    assert (pSourceName != null && pTargetName != null);
    List<String> newTuple = new ArrayList<String>();
    newTuple.add(pRelName);
    newTuple.add(pSourceName);
    newTuple.add(pTargetName);
    // We don't need reflexive edges.
    if (!pSourceName.equals(pTargetName)) {
      boolean isNew =
                      pStoredTuples.add(pRelName + '\t' + pSourceName + '\t'
                                        + pTargetName);
      if (isNew) {
        this.add(newTuple);
      }
    }
  }

  public void addTuple(String pRelName, String pSourceId, String pTargetId,
                       Map<String, String> pNameMap, Set<String> pStoredTuples) {
    assert (pSourceId != null && pTargetId != null);
    String lSourceName = pNameMap.get(pSourceId);
    String lTargetName = pNameMap.get(pTargetId);
    addTuple(pRelName, lSourceName, lTargetName, pStoredTuples);
  }
}
