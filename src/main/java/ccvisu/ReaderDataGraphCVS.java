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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import ccvisu.Options.Verbosity;

/*****************************************************************
 * Reader for CVS log files. 
 * Extracts the co-change graph from the CVS log info.
 * @version  $Revision: 1.35 $; $Date: 2007/12/15 02:03:35 $
 * @author   Dirk Beyer
 *****************************************************************/
public class ReaderDataGraphCVS extends ReaderDataGraph {

  /** Time-window constant for transaction recovery, in milli-seconds.*/
  private final int     timeWindow;

  private final boolean sliding;

  /**
   * Constructor.
   * @param in          Stream reader object.
   * @param timeWindow  Time window for transaction recovery, in milli-seconds
   *                    (default: 180'000).
   * @param sliding     sliding or fixed time window
   */
  public ReaderDataGraphCVS(BufferedReader in, Verbosity pVerbosity,
                            int timeWindow, boolean sliding) {
    super(in, pVerbosity);
    this.timeWindow = timeWindow;
    this.sliding = sliding;
  }

  /** 
   * Represents a CVS revision entry (an abstraction of it).
   * @author Dirk Beyer
   */
  private class Revision implements Comparable<Revision> {
    String relName;
    String filename;
    Long   time;
    String user;
    String logmsg;
    /** The internal number (id) of the change transaction.*/
    int    transaction;

    /*****************************************************************
     * Compares this revision with the specified object for order.  
     * Returns a negative integer, zero, or a positive integer as this object 
     * is less than, equal to, or greater than the specified object.<p>
     *
     * @param   rev revision to be compared for order with this revision.
     * @return  a negative integer, zero, or a positive integer as this object
     *              is less than, equal to, or greater than the specified object.
     *****************************************************************/
    public int compareTo(Revision rev) {
      if (this.time.compareTo(rev.time) == 0) {
        return this.hashCode() - rev.hashCode();
      } else {
        return this.time.compareTo(rev.time);
      }
    }

    /*****************************************************************
     * Compares the specified object with this revision for equality.
     * Returns <tt>true</tt> if the specified object is identical with this object. 
     * The method is based on <tt>compareTo</tt> to make the ordering 
     * <i>consistent with equals</i>.<p>
     * 
     * @param o  Object to be compared for equality with this revision.
     * @return <tt>true</tt> if the specified Object is equal to this revision.
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
      Revision rev = (Revision) o;
      if (rev == null) { // Either o is null or o is not of class Revision.
        return false;
      }
      return (this.compareTo(rev) == 0);
    }
  };

  /*****************************************************************
   * Reads the edges of a graph in CVS log format
   * from stream reader <code>in</code>, 
   * and stores them in a list (of <code>GraphEdgeString</code> elements).
   * @return List of string edges.
   *****************************************************************/
  @Override
  public Relation readTuples() {
    Relation result = new Relation();

    List<Revision> revisionList = readRevisionList();
    SortedMap<Long, Collection<SortedSet<Revision>>> transMap =
                                                                recoverTransactions(revisionList);

    Set<Long> timeSet = transMap.keySet();
    for (Long time : timeSet) {
      Collection<SortedSet<Revision>> transColl = transMap.get(time);
      for (SortedSet<Revision> revSet : transColl) {
        for (Revision revision : revSet) {
          List<String> newTuple = new ArrayList<String>();
          newTuple.add(revision.relName);
          newTuple.add(Integer.toString(revision.transaction));
          newTuple.add(revision.filename);
          result.add(newTuple);

          // Print revision entry with timestamp and user of the changes to stdout.
          if (mVerbosity.isAtLeast(Verbosity.WARNING)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(revision.time.longValue());
            System.out.println("REV  \t"
                               + Integer.toString(revision.transaction) + "\t"
                               + "\"" + cal.getTime() + "\"" + "\t"
                               + revision.user + "\t" + revision.filename
                               + "\t" + revision.relName);
          }
        }
      }
    }
    return result;
  }

  /*****************************************************************
   * Parses the date entry.
   * @param dateStr   The CVS date entry string.
   * @return  Long value of the date, or 
   *          <code>null</code> if <code>dateStr</code> 
   *          is not a valid date entry.
   *****************************************************************/
  private Long parseDate(String dateStr) {
    // Delimiter for year/month/day.
    char delim = '/';
    int posEnd = dateStr.indexOf(delim);
    if (posEnd < 0 || posEnd > 9) {
      delim = '-';
      posEnd = dateStr.indexOf(delim);
      if (posEnd < 0 || posEnd > 9) { return null; }
    }
    int posBegin = 0;
    int year = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(delim, posBegin);

    int month = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(' ', posBegin);

    int day = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(':', posBegin);

    int hour = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = dateStr.indexOf(':', posBegin);

    int min = Integer.parseInt(dateStr.substring(posBegin, posEnd));
    posBegin = posEnd + 1;
    posEnd = posBegin + 2;
    int sec = Integer.parseInt(dateStr.substring(posBegin, posEnd));

    Calendar cal = Calendar.getInstance();
    cal.clear(); // Erase the milli secs.
    cal.set(year, month - 1, day, hour, min, sec);

    return new Long(cal.getTimeInMillis());
  }

  /*****************************************************************
   * Parses the CVS log data and extracts revisions.
   * @return  List of revisions.
   *****************************************************************/
  private List<Revision> readRevisionList() {
    List<Revision> result = new ArrayList<Revision>();

    String lLine = "";
    //String relName = "CO-CHANGE";
    String filename = null;
    Long time;
    String user;
    String logmsg;

    int lineno = 1;
    try {
      while ((lLine = mIn.readLine()) != null) {
        // New working file.
        if (lLine.startsWith("Working file: ")) {
          // Set name of the current working file, 
          //   for which we pasre the revisions.
          filename = lLine.substring(14);
        }

        // New revision.
        if (lLine.startsWith("date: ")) {
          // Set date, author, and logmsg of the current revision.

          // Parse date. Start right after "date: ".
          time = parseDate(lLine.substring(6, lLine.indexOf("author: ")));
          if (time == null) {
            System.err
                .print("Error while reading the CVS date info for file: ");
            System.err.println(filename + ".");
          }

          // Parse author. Start right after "author: ".
          int posBegin = lLine.indexOf("author: ") + 8;
          int posEnd = lLine.indexOf(';', posBegin);
          user = lLine.substring(posBegin, posEnd);

          // Parse logmsg. Start on next line the date/author line.
          logmsg = "";
          ++lineno;
          while (((lLine = mIn.readLine()) != null)
                 && !lLine.startsWith("----") && !lLine.startsWith("====")) {
            if (!lLine.startsWith("branches: ")) {
              logmsg += lLine + ReaderDataGraph.endl;
            }
            ++lineno;
          }

          // Create revision and add revision to resulting list.
          Revision revision = new Revision();
          //revision.relName = relName.replace(' ', '_');  // Replace blanks by underline.
          revision.filename = filename.replace(' ', '_'); // Replace blanks by underline.
          revision.time = time;
          revision.user = user;
          revision.logmsg = logmsg;
          result.add(revision);

          //System.out.print("Relation: "+ relName +
          //                 " File: " + filename +
          //                 " Time: " + time.toString() +
          //                 " User: " + user + " LogMsg: " + logmsg);

        }
        ++lineno;
      } // while
    } catch (Exception e) {
      System.err.println("Exception while reading the CVS log at line "
                         + lineno + ":");
      System.err.println(e);
      System.err.print("Read line: ");
      System.err.println(lLine);
    }
    return result;
  }

  /*****************************************************************
   * Recovers the change transactions for the co-change graph 
   *   from the revision information, i.e., it assigns
   *   the transaction ids for the revisions.
   * @param   revisionList is a list of revisions.
   * @return  Sorted map that maps timestamps to collections of transactions,
   *          where transactions are sets of revisions.
   *****************************************************************/
  private SortedMap<Long, Collection<SortedSet<Revision>>> recoverTransactions(
                                                                               List<Revision> revisionList) {

    // Step 1: Transform the list of revisions to a sorted data structure.

    // A map user -> msg-entry.
    Map<String, Map<String, SortedMap<Long, SortedSet<String>>>> userMap =
                                                                           new HashMap<String, Map<String, SortedMap<Long, SortedSet<String>>>>();
    // A map logmsg -> time-entry.
    Map<String, SortedMap<Long, SortedSet<String>>> msgMap;
    // A map time -> file.
    SortedMap<Long, SortedSet<String>> timeMap;
    // A set of files.
    SortedSet<String> fileSet;

    for (Revision revision : revisionList) {
      // A map logmsg -> time-entry.
      msgMap = userMap.get(revision.user);
      if (msgMap == null) {
        msgMap = new HashMap<String, SortedMap<Long, SortedSet<String>>>();
        userMap.put(revision.user, msgMap);
      }

      // A map time -> file.
      timeMap = msgMap.get(revision.logmsg);
      if (timeMap == null) {
        timeMap = new TreeMap<Long, SortedSet<String>>();
        msgMap.put(revision.logmsg, timeMap);
      }

      // A set of files.
      fileSet = timeMap.get(revision.time);
      if (fileSet == null) {
        fileSet = new TreeSet<String>();
        timeMap.put(revision.time, fileSet);
      }

      // Add file to set.
      fileSet.add(revision.filename);
    }

    // Step 2: Create the result, which is
    // a map timestamp -> set of transactions (Long -> SortedSet), 
    // where one transaction is a set of revisions.
    SortedMap<Long, Collection<SortedSet<Revision>>> result =
                                                              new TreeMap<Long, Collection<SortedSet<Revision>>>();

    int transaction = 0;
    Set<String> userSet = userMap.keySet();
    for (String user : userSet) {
      msgMap = userMap.get(user);

      Set<String> msgSet = msgMap.keySet();
      for (String logmsg : msgSet) {
        timeMap = msgMap.get(logmsg);

        Set<Long> timeSet = timeMap.keySet();

        long firstTime = 0;
        Set<String> tmpFilesSeen = new TreeSet<String>(); // Detect a time window that is too long.
        Collection<SortedSet<Revision>> transColl; // Collection of transactions.
        SortedSet<Revision> revSet = null; // Transaction, i.e., set of revisions.
        for (Long time : timeSet) {
          if (time.longValue() - firstTime > timeWindow) {
            // Start new transaction.
            ++transaction;
            firstTime = time.longValue();
            tmpFilesSeen.clear();
            // Retrieve (or create new) set of transactions for the timestamp.
            transColl = result.get(time);
            if (transColl == null) {
              transColl = new ArrayList<SortedSet<Revision>>();
              result.put(time, transColl);
            }
            // New transaction (set of revisions).
            revSet = new TreeSet<Revision>();
            transColl.add(revSet);
          } else if (sliding) {
            // The time window 'slides' with the files.
            firstTime = time.longValue();
          }

          fileSet = timeMap.get(time);
          for (String filename : fileSet) {
            // Detect a time window that is too long.
            if (mVerbosity.isAtLeast(Verbosity.WARNING)
                && tmpFilesSeen.contains(filename)) {
              System.err
                  .println("Transaction-recovery warning: Time window might be to wide "
                           + ReaderDataGraph.endl
                           + "(currently '"
                           + timeWindow
                           + "' milli-seconds). "
                           + ReaderDataGraph.endl
                           + "File '"
                           + filename
                           + "' already contained in current transaction.");
            }
            tmpFilesSeen.add(filename);

            // Create revision and add revision to resulting list.
            Revision revision = new Revision();
            revision.relName = "CO-CHANGE";
            revision.filename = filename;
            revision.time = time;
            revision.user = user;
            revision.logmsg = logmsg;
            revision.transaction = transaction;
            revSet.add(revision);
          }
        }
      }
      //    }
    }
    return result;
  }
};
