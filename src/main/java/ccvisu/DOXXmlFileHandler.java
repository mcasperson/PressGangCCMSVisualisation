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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
public class DOXXmlFileHandler extends DefaultHandler {

  private int      xmlLevel       = 0;

  private int      mMemberLevel   = 1;
  private String   mMemberId      = "";
  private int      mCompoundLevel = 1;
  private String   mCompoundId    = "";

  private Relation mRelations;

  /**
    * Constructor.
    */
  public DOXXmlFileHandler(Relation pRelations) {
    super();
    mRelations = pRelations;
  }

  /**
   * 
   */
  @Override
  public void startElement(String uri, String localName, String qName,
                           Attributes attrs) throws SAXException {
    xmlLevel++;

    assert (attrs != null);

    // COMPOUNDDEF and MEMBERDEF
    if (qName.toLowerCase().equals("memberdef")
        || qName.toLowerCase().equals("compounddef")) {
      // Update current entry.
      String lId = attrs.getValue("id");
      assert (lId != null);
      if (qName.toLowerCase().equals("memberdef")) {
        mMemberLevel = xmlLevel;
        mMemberId = lId;
      } else {
        mCompoundLevel = xmlLevel;
        mCompoundId = lId;
      }
    }
    // BASECOMPOUNDREF
    if (qName.toLowerCase().equals("basecompoundref")) {
      // Output this reference.
      assert (xmlLevel == mCompoundLevel + 1);
      // Super class that we inherit from.
      String lBaseCompoundId = attrs.getValue("refid");
      if (lBaseCompoundId == null) {
        lBaseCompoundId = "UNKNOWN-EXTERNAL";
      }
      // Protection.
      String lProtection = attrs.getValue("prot");
      assert (lProtection != null);
      // Virtual.
      String lVirtual = attrs.getValue("virt");
      assert (lVirtual != null);
      List<String> newTuple = new ArrayList<String>();
      newTuple.add("BASEDON");
      newTuple.add(mCompoundId);
      newTuple.add(lBaseCompoundId);
      newTuple.add(lProtection);
      newTuple.add(lVirtual);
      mRelations.add(newTuple);
    }
    // REFERENCES
    else if (qName.toLowerCase().equals("references")) {
      // Output this reference.
      assert (xmlLevel == mMemberLevel + 1);
      String lRefId = attrs.getValue("refid");
      assert (lRefId != null);
      List<String> newTuple = new ArrayList<String>();
      newTuple.add("REFERSTO");
      newTuple.add(mMemberId);
      newTuple.add(lRefId);
      mRelations.add(newTuple);
    }
    // LOCATION
    else if (qName.toLowerCase().equals("location")) {
      // Output the location.
      String lFile = attrs.getValue("file");
      assert (lFile != null);
      String lLineNo = attrs.getValue("line");
      if (lLineNo == null) {
        lLineNo = "UNKNOWN";
      }
      if (xmlLevel == mCompoundLevel + 1) {
        List<String> newTuple = new ArrayList<String>();
        newTuple.add("LOCATEDAT");
        newTuple.add(mCompoundId);
        newTuple.add('"' + lFile + '"');
        newTuple.add(lLineNo);
        mRelations.add(newTuple);
      } else {
        assert (xmlLevel == mMemberLevel + 1);
        assert (xmlLevel == mCompoundLevel + 3);
        List<String> newTuple = new ArrayList<String>();
        newTuple.add("LOCATEDAT");
        newTuple.add(mMemberId);
        newTuple.add('"' + lFile + '"');
        newTuple.add(lLineNo);
        mRelations.add(newTuple);
      }
    }
  }

  /**
   * 
   */
  @Override
  public void endElement(String uri, String localName, String qName)
                                                                    throws SAXException {
    xmlLevel--;
  }
}
