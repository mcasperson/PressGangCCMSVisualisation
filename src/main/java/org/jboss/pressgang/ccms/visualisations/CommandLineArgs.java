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
}
