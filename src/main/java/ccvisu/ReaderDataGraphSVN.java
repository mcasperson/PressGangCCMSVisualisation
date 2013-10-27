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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ccvisu.Options.Verbosity;

/*****************************************************************
 * Reader for SVN log files. 
 * Extracts the co-change graph from the SVN log info.
 * @version  $Revision: 1.1 $
 * @author   Dirk Beyer
 *****************************************************************/
public class ReaderDataGraphSVN extends ReaderDataGraph {

  public class SVNXmlFileHandler extends DefaultHandler {

    private String   mCurrentRevision = "";
    private String   mCurrentPath     = "";
    private Relation mRelations;

    public SVNXmlFileHandler(Relation pRelations) {
      super();
      mRelations = pRelations;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attrs) throws SAXException {
      // LOGENTRY
      if (qName.toLowerCase().equals("logentry")) {
        assert (attrs != null);
        mCurrentRevision = attrs.getValue("revision");
        assert (mCurrentRevision != null);
      }
      // PATH
      else if (qName.toLowerCase().equals("path")) {
        // Reset path.
        mCurrentPath = "";
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
                                                                      throws SAXException {
      if (qName.toLowerCase().equals("path")) {
        if (mCurrentRevision.equals("")) {
          System.err.println("Runtime Error: No path found for revision "
                             + mCurrentRevision + ".");
          return;
        }
        List<String> lTuple = new ArrayList<String>();
        lTuple.add("CO-CHANGE");
        lTuple.add(mCurrentRevision);
        lTuple.add('"' + mCurrentPath + '"');
        mRelations.add(lTuple);
      }
    }

    @Override
    public void characters(char[] ch, int start, int len) throws SAXException {
      String lName = new String(ch, start, len);
      if (lName.trim().equals("")) { return; }
      // Concatenate the existing and the new string.
      mCurrentPath += lName;
    }
  }

  /**
   * Constructor.
   * @param in          Stream reader object.
   */
  public ReaderDataGraphSVN(BufferedReader in, Verbosity pVerbosity) {
    super(in, pVerbosity);
  }

  /*****************************************************************
   * Reads the edges of a graph in SVN log format
   * from stream reader <code>in</code>, 
   * and stores them in a list (of <code>GraphEdgeString</code> elements).
   * @see ReaderDataGraph#readTuples()
   * @return List of string edges.
   */
  @Override
  public Relation readTuples() {
    // This is where we store the relations.
    Relation lRelations = new Relation();

    XMLReader xmlReader = null;
    SVNXmlFileHandler svnFileHandler = null;
    try {
      // initialize xmlReader
      xmlReader = XMLReaderFactory.createXMLReader();

      // update xmlReader for SVN-file parsing
      svnFileHandler = new SVNXmlFileHandler(lRelations);
      xmlReader.setContentHandler(svnFileHandler);
      xmlReader.setErrorHandler(svnFileHandler);

      // parse SVN log file
      xmlReader.parse(new InputSource(mIn));
    } catch (SAXException e) {
      System.err.println("Runtime error: A SAX-error occured.");
      System.err.println(e.getMessage());
    } catch (IOException e) {
      System.err.println("Runtime error: Error while opening a file.");
      System.err.println(e.getMessage());
      System.exit(1);
    }

    return lRelations;
  }

};
