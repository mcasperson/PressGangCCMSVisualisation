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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ccvisu.Options.Verbosity;

/**
 * Extracts relations in RSF format from a given XML input.
 * BASEDON, COMPOUND, CONTAINEDIN, LOCATEDAT, MEMBER, REFERSTO
 * supported so far.
 */
public class ReaderDataGraphDOX extends ReaderDataGraph {

  private String mInputName;

  /**
   * Constructor.
   * @param pIn          Stream reader object.
   */
  public ReaderDataGraphDOX(BufferedReader pIn, Verbosity pVerbosity,
                            String pInputName) {
    super(pIn, pVerbosity);
    mInputName = pInputName;
  }

  /**
   * @see ReaderDataGraph#readTuples()
   */
  @Override
  public Relation readTuples() {
    // This is where we store the relations.
    Relation lRelations = new Relation();

    XMLReader xmlReader = null;
    DOXIndexFileHandler indexFileHandler = null;
    try {
      // initialize xmlReader
      xmlReader = XMLReaderFactory.createXMLReader();
      // SAXParser saxParser =
      // SAXParserFactory.newInstance().newSAXParser();

      // update xmlReader for index-file parsing
      indexFileHandler = new DOXIndexFileHandler(lRelations);
      xmlReader.setContentHandler(indexFileHandler);
      xmlReader.setErrorHandler(indexFileHandler);

      // parse index file
      xmlReader.parse(new InputSource(mIn));
      // saxParser.parse(new File(filePath + "/" + fileName),
      // filenamesDetector);
    } catch (SAXException e) {
      System.err.println("Runtime error: A SAX-error occured.");
      System.err.println(e.getMessage());
    } catch (IOException e) {
      System.err.println("Runtime error: Error while opening a file.");
      System.err.println(e.getMessage());
      System.exit(1);
    }

    // update xmlReader for xml-file parsing
    DOXXmlFileHandler xmlFileHandler = new DOXXmlFileHandler(lRelations);
    xmlReader.setContentHandler(xmlFileHandler);
    xmlReader.setErrorHandler(xmlFileHandler);

    // parse detected XML files
    List<String> xmlFilenames = indexFileHandler.getXmlFileIds();
    String lPath =
                   mInputName.substring(0, mInputName
                       .lastIndexOf(File.separator));
    for (String lItFileName : xmlFilenames) {
      try {
        xmlReader
            .parse(new InputSource(new FileReader(lPath + File.separator
                                                  + lItFileName + ".xml")));
        // saxParser.parse(new File(filePath + "/" + xmlFilenames.pop()
        // + ".xml"), fileParser);
        // Exception handling
      } catch (SAXException e) {
        System.err.println("Runtime error: A SAX-error occured.");
        System.err.println("  While parsing file '" + lItFileName + "'.");
        System.err.println("  " + e.getMessage());
      } catch (IOException e) {
        System.err.println("Runtime error: Error while opening a file.");
        System.err.println(e.getMessage());
        System.exit(1);
      }
    }

    addExtraTuples(lRelations);

    return lRelations;
  }

  /**
   * @param pRelations
   */
  private void addExtraTuples(Relation pRelations) {
    // Construct mappings id-to-name, -kind, -container name, and -file.
    Map<String, String> lIdToName = new HashMap<String, String>();
    Map<String, String> lIdToKind = new HashMap<String, String>();
    Map<String, String> lIdToContainerId = new HashMap<String, String>();
    Map<String, String> lIdToFile = new HashMap<String, String>();
    for (List<String> itTuple : pRelations) {
      assert (itTuple.size() > 1);
      Iterator<String> it = itTuple.iterator();
      String lRelName = it.next();
      if (lRelName.equals("MEMBER") || lRelName.equals("COMPOUND")) {
        assert (itTuple.size() == 4);
        String lRelKind = it.next();
        String lId = it.next().replace("\"", "");
        String lName = it.next().replace("\"", "");
        if (lRelKind.equalsIgnoreCase("function")) {
          lName += "()";
        }
        lIdToKind.put(lId, lRelKind);
        String lPrev = lIdToName.put(lId, lName);
        // Check for duplicate entries: 
        if (lPrev != null && !lPrev.equals(itTuple.get(3))
            && mVerbosity.isAtLeast(Verbosity.WARNING)) {
          System.err.println("Warning: Multiple names found for id '"
                             + itTuple.get(2) + "':");
          System.err.println("    " + lPrev);
          System.err.println("    " + itTuple.get(3));
        }
      }
      if (lRelName.equals("LOCATEDAT")) {
        assert (itTuple.size() == 4);
        String lPrev = lIdToFile.put(it.next(), it.next());
        // Skip the 'line-no'.
        // Check for duplicate entries: 
        if (lPrev != null && !lPrev.equals(itTuple.get(2))
            && mVerbosity.isAtLeast(Verbosity.WARNING)) {
          System.err.println("Warning: Multiple locations found for id '"
                             + itTuple.get(1) + "':");
          System.err.println("    " + lPrev);
          System.err.println("    " + itTuple.get(2));
        }
      }
    }
    // We must do a second loop over the tuples list, since we have to know all of the classes' names beforehand
    for (List<String> itTuple : pRelations) {
      assert (itTuple.size() > 1);
      Iterator<String> it = itTuple.iterator();
      String lRelName = it.next();
      if (lRelName.equals("CONTAINEDIN")) {
        assert (itTuple.size() == 3);
        String lId = it.next().replace("\"", "");
        String lName = it.next().replace("\"", "");

        String lPrev = lIdToContainerId.put(lId, lName);
        // Check for duplicate entries: 
        if (lPrev != null && !lPrev.equals(itTuple.get(2))
            && mVerbosity.isAtLeast(Verbosity.WARNING)) {
          System.err.println("Warning: Multiple names found for id '"
                             + itTuple.get(1) + "':");
          System.err.println("    " + lPrev);
          System.err.println("    " + itTuple.get(2));
        }
      }
    }

    // Appending derived relations to pRelations.
    // A set for tracking which tuples we already added.
    Set<String> lStoredTuples = new HashSet<String>();
    for (List<String> itTuple : new Relation(pRelations)) {
      assert (itTuple.size() > 1);
      Iterator<String> it = itTuple.iterator();
      String lRelName = it.next();
      if (lRelName.equals("REFERSTO")) {
        String source = it.next();
        String target = it.next();
        pRelations.addTuple("REF" + lIdToKind.get(target), source, target,
            lIdToName, lStoredTuples);
        String sourceContainer = lIdToContainerId.get(source);
        String targetContainer = lIdToContainerId.get(target);
        if (lIdToKind.get(sourceContainer).equals("class")
            && lIdToKind.get(targetContainer).equals("class")) {
          pRelations.addTuple("refClass", sourceContainer, targetContainer,
              lIdToName, lStoredTuples);
        }
        // We need some extra tolerance for file names.
        if (lIdToFile.get(source) == null) {
          System.err.println("Runtime warning: No file location found for "
                             + "entry with id '" + source + "'.");
        } else if (lIdToFile.get(target) != null) {
          // Sometimes we don't get the file location for an id.
          // This happens, e.g., for 'enumvalue'. --- We omit such tuples.
          pRelations.addTuple("refFile", source, target, lIdToFile,
              lStoredTuples);
        }
      }
      if (lRelName.equals("BASEDON")) {
        String source = it.next();
        String target = it.next();
        if (!target.startsWith("UNKNOWN")
            && lIdToKind.get(source).equals("class")
            && lIdToKind.get(target).equals("class")) {
          pRelations.addTuple("inheritClass", source, target, lIdToName,
              lStoredTuples);
        }
      }
    }
  }

}