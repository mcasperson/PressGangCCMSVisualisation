package org.jboss.pressgang.ccms.visualisations;

import com.beust.jcommander.Parameter;

/**
 * JCommander annotated class describing the command line args
 */
public class CommandLineArgs {
    @Parameter(names = "--pressgangServer", description = "The URL of the PressGang CCMS REST server that the data will be pulled from.")
    public String pressgangServer;
}
