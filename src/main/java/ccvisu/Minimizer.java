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

import java.util.Vector;

/*****************************************************************
 * Minimizer for a given energy model, which is set by the constructor
 * of the concrete minimizer implementation.
 * @version  $Revision: 1.13 $; $Date: 2007/12/15 02:03:35 $
 * @author   Dirk Beyer
 *****************************************************************/
public abstract class Minimizer {

  /**when chages occur in the graph*/
  protected Vector<GraphEventListener> listeners =
                                                   new Vector<GraphEventListener>();

  /**
   * Constructor
   * @param listener a GraphEventListener
   */
  public void addGraphEventListener(GraphEventListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Minimizes iteratively the energy using the Barnes-Hut algorithm.
   * Starts from the layout given by the positions in <code>pos</code>, 
   * and stores the computed layout as positions in <code>pos</code>.
   */
  public abstract void minimizeEnergy();

};
