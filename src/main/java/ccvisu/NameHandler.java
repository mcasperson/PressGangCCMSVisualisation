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

/**
 * @author ashgan
 *
 */
public class NameHandler {

  public static String shortenName(String artifactName) {
    // if artifact has a '(' character in its name it's
    // a method, otherwise treat as a class (or package)
    if (artifactName.lastIndexOf('(') != -1) { return NameHandler
        .getMethodBriefName(artifactName); }
    return NameHandler.getTypeBriefName(artifactName);
  }

  private static String getMethodBriefName(String meth) {
    String[] methNameParts = meth.split("\\(");
    if (methNameParts != null) {
      if (methNameParts[0] != "") {
        String methBriefSig = methNameParts[0];
        int lastDotIndex = methNameParts[0].lastIndexOf('.');

        // extracting the method ID 
        // (immediate parent class name + method name)
        if (lastDotIndex != -1) {
          String methID = methNameParts[0].substring(lastDotIndex + 1);
          String classID =
                           NameHandler.getTypeBriefName(methNameParts[0]
                               .substring(0, lastDotIndex));

          methBriefSig = classID + "." + methID;
        }
        // refining argument names
        String[] argList = methNameParts[1].split(",");
        String argSig = "(";
        for (String arg : argList) {
          argSig += NameHandler.getTypeBriefName(arg) + ", ";
        }

        if (argList.length > 0) {
          argSig = argSig.substring(0, argSig.lastIndexOf(','));
        }

        //        argSig += ")";
        methBriefSig += argSig;
        return methBriefSig;
      } else {
        return "ErrorMethodID_" + meth;
      }
    }

    return "ErrorBriefName_" + meth;
  }

  private static String getTypeBriefName(String className) {
    String classID = className;
    if (classID.lastIndexOf('.') != -1) {
      classID = classID.substring(classID.lastIndexOf('.') + 1);
    }
    return classID;
  }

}
