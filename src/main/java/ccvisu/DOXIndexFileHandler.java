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
public class DOXIndexFileHandler extends DefaultHandler {

  // COMPOUND kind values: class, struct, union, interface, protocol, category,
  //                       exception, file, namespace, group, page, example, dir
  // MEMBER kind   values: define, property, event, variable, typedef,
  //                       enum, enumvalue, function, signal, prototype, 
  //                       friend, dcop, slot

  private enum TagType {
    UNKNOWN, COMPOUND, MEMBER
  };

  private class Entry {
    private int    level = 1;
    private String kind  = "";
    private String id    = "";
    private String name  = "";

    Entry(int level, String kind, String id) {
      assert (level > 0);
      this.level = level;
      this.kind = kind;
      this.id = id;
    }
  }

  private int          xmlLevel               = 0;
  private TagType      detectingNameOfTagType = TagType.UNKNOWN;
  private Entry        currentCompound        = new Entry(1, "", "");
  private Entry        currentMember          = new Entry(1, "", "");
  private List<String> xmlFileIds             = new ArrayList<String>();

  private Relation     mRelations;

  /**
   * Constructor.
   */
  public DOXIndexFileHandler(Relation pRelations) {
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

    detectingNameOfTagType = TagType.UNKNOWN;

    // COMPOUND and MEMBER
    if (qName.toLowerCase().equals("compound")
        || qName.toLowerCase().equals("member")) {
      assert (attrs != null);
      String lId = attrs.getValue("refid");
      assert (lId != null);
      String lKind = attrs.getValue("kind");
      assert (lKind != null);
      if (qName.toLowerCase().equals("compound")) {
        currentCompound = new Entry(xmlLevel, lKind, lId);
      } else {
        currentMember = new Entry(xmlLevel, lKind, lId);
      }
    }
    // NAME
    else if (qName.toLowerCase().equals("name")) {
      if (xmlLevel == currentCompound.level + 1) {
        detectingNameOfTagType = TagType.COMPOUND;
      } else {
        assert (xmlLevel == currentCompound.level + 2);
        assert (xmlLevel == currentMember.level + 1);
        detectingNameOfTagType = TagType.MEMBER;
      }
    }
    // DOXYGENINDEX
    else if (qName.toLowerCase().equals("doxygenindex")) {
      // Not needed.
    } else {
      assert (false);
    }
  }

  /**
   * 
   */
  @Override
  public void endElement(String uri, String localName, String qName)
                                                                    throws SAXException {

    detectingNameOfTagType = TagType.UNKNOWN;

    // COMPOUND
    if (qName.toLowerCase().equals("compound")) {
      assert (xmlLevel == currentCompound.level);
      if (currentCompound.name.equals("")) {
        System.err.println("Runtime Error: No name found for compound "
                           + currentCompound.id + ".");
        System.exit(1);
      }
      List<String> lTuple = new ArrayList<String>();
      lTuple.add("COMPOUND");
      lTuple.add(currentCompound.kind);
      lTuple.add(currentCompound.id);
      lTuple.add('"' + currentCompound.name + '"');
      mRelations.add(lTuple);
    }
    // MEMBER  
    else if (qName.toLowerCase().equals("member")) {
      assert (xmlLevel == currentCompound.level + 1);
      assert (xmlLevel == currentMember.level);
      if (currentMember.name.equals("")) {
        System.err.println("Runtime Error: No name found for member "
                           + currentMember.id + ".");
        System.exit(1);
      }
      {
        List<String> newTuple = new ArrayList<String>();
        newTuple.add("MEMBER");
        newTuple.add(currentMember.kind);
        newTuple.add(currentMember.id);
        newTuple.add('"' + currentCompound.name + "." + currentMember.name
                     + '"');
        mRelations.add(newTuple);
      }
      {
        List<String> newTuple = new ArrayList<String>();
        newTuple.add("CONTAINEDIN");
        newTuple.add(currentMember.id);
        newTuple.add(currentCompound.id);
        mRelations.add(newTuple);
      }
    }

    xmlLevel--;
  }

  /**
   * 
   */
  @Override
  public void characters(char[] ch, int start, int len) throws SAXException {
    String lName = new String(ch, start, len);
    if (lName.trim().equals("")) { return; }

    // COMPOUND name
    if (detectingNameOfTagType == TagType.COMPOUND) {
      xmlFileIds.add(currentCompound.id);
      // Concatenate the two strings (e.g. "MyClass<T" + ">").
      currentCompound.name += lName;
    }
    // MEMBER name
    else if (detectingNameOfTagType == TagType.MEMBER) {
      // Concatenate the two strings (e.g. "operator" + "<=").
      currentMember.name += lName;
    }
  }

  /**
   * 
   * @return       list of file names to process.
   */
  public List<String> getXmlFileIds() {
    return xmlFileIds;
  }
}