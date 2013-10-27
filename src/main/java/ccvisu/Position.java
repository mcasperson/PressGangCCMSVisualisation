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

/**
 * @author dbeyer
 *
 */
public class Position {
  public float x;
  public float y;
  public float z;

  Position() {
    this.x = 0.0f;
    this.y = 0.0f;
    this.z = 0.0f;
  }

  Position(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  Position(Position pPos) {
    this.x = pPos.x;
    this.y = pPos.y;
    this.z = pPos.z;
  }

  public boolean equals(Position pPos) {
    if (this.x == pPos.x && this.y == pPos.y && this.z == pPos.z) {
      return true;
    } else {
      return false;
    }
  }

  public boolean lesseq(Position pPos) {
    if (this.x <= pPos.x && this.y <= pPos.y && this.z <= pPos.z) {
      return true;
    } else {
      return false;
    }
  }

  public void add(Position pPos) {
    this.x += pPos.x;
    this.y += pPos.y;
    this.z += pPos.z;
  }

  public void subtract(Position pPos) {
    this.x -= pPos.x;
    this.y -= pPos.y;
    this.z -= pPos.z;
  }

  public void mult(Position pPos) {
    this.x *= pPos.x;
    this.y *= pPos.y;
    this.z *= pPos.z;
  }

  public void div(Position pPos) {
    this.x /= pPos.x;
    this.y /= pPos.y;
    this.z /= pPos.z;
  }

  public void mult(float pScalar) {
    this.x *= pScalar;
    this.y *= pScalar;
    this.z *= pScalar;
  }

  public void div(float pScalar) {
    this.x /= pScalar;
    this.y /= pScalar;
    this.z /= pScalar;
  }

  public static Position add(Position pPos, float pScalar) {
    Position result = new Position(pPos);
    result.x += pScalar;
    result.y += pScalar;
    result.z += pScalar;
    return result;
  }

  public static Position subtract(Position pPos, float pScalar) {
    Position result = new Position(pPos);
    result.x -= pScalar;
    result.y -= pScalar;
    result.z -= pScalar;
    return result;
  }

  public static Position mult(Position pPos, float pScalar) {
    Position result = new Position(pPos);
    result.x *= pScalar;
    result.y *= pScalar;
    result.z *= pScalar;
    return result;
  }

  public static Position div(Position pPos, float pScalar) {
    Position result = new Position(pPos);
    result.x /= pScalar;
    result.y /= pScalar;
    result.z /= pScalar;
    return result;
  }

  public static Position add(Position pPos1, Position pPos2) {
    Position result = new Position(pPos1);
    result.x += pPos2.x;
    result.y += pPos2.y;
    result.z += pPos2.z;
    return result;
  }

  public static Position subtract(Position pPos1, Position pPos2) {
    Position result = new Position(pPos1);
    result.x -= pPos2.x;
    result.y -= pPos2.y;
    result.z -= pPos2.z;
    return result;
  }

  public static Position mult(Position pPos1, Position pPos2) {
    Position result = new Position(pPos1);
    result.x *= pPos2.x;
    result.y *= pPos2.y;
    result.z *= pPos2.z;
    return result;
  }

  public static Position div(Position pPos1, Position pPos2) {
    Position result = new Position(pPos1);
    result.x /= pPos2.x;
    result.y /= pPos2.y;
    result.z /= pPos2.z;
    return result;
  }

  public static Position min(Collection<GraphVertex> pCollection) {
    Position minPos =
                      new Position(Float.MAX_VALUE, Float.MAX_VALUE,
                          Float.MAX_VALUE);
    for (GraphVertex lCurrVertex : pCollection) {
      if (lCurrVertex.showVertex) {
        minPos.x = Math.min(minPos.x, lCurrVertex.pos.x);
        minPos.y = Math.min(minPos.y, lCurrVertex.pos.y);
        minPos.z = Math.min(minPos.z, lCurrVertex.pos.z);
      }
    }
    return minPos;
  }

  public static Position max(Collection<GraphVertex> pCollection) {
    Position maxPos =
                      new Position(-Float.MAX_VALUE, -Float.MAX_VALUE,
                          -Float.MAX_VALUE);
    for (GraphVertex lCurrVertex : pCollection) {
      if (lCurrVertex.showVertex) {
        maxPos.x = Math.max(maxPos.x, lCurrVertex.pos.x);
        maxPos.y = Math.max(maxPos.y, lCurrVertex.pos.y);
        maxPos.z = Math.max(maxPos.z, lCurrVertex.pos.z);
      }
    }
    return maxPos;
  }

  /**
   * Returns the maximum over each dimension of the width of two points.
   * 
   * @return Maximum over each dimension of the width of two points.
   */
  public static float width(Position p1, Position p2) {
    Position lDiffPos = Position.subtract(p1, p2);
    float lWidth = Math.max(lDiffPos.x, lDiffPos.y);
    lWidth = Math.max(lDiffPos.z, lWidth);
    return lWidth;
  }

}
