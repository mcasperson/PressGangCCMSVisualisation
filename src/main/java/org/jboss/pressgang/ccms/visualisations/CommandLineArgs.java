package org.jboss.pressgang.ccms.visualisations;

import com.beust.jcommander.Parameter;

import javax.inject.Singleton;

/**
 * JCommander annotated class describing the command line args
 */
@Singleton
public class CommandLineArgs {
    @Parameter(names = "--pressgangServer", description = "The URL of the PressGang CCMS REST server that the data will be pulled from.")
    public String pressgangServer;

    @Parameter(names = "--topicDatabaseFile", description = "The name of the file that will contain the topic's additional data.")
    public String topicDatabaseFile;

    @Parameter(names = "--topicGraphFile", description = "The name of the file that will contain the topic graph.")
    public String topicGraphFile;
}
