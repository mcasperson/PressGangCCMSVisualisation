package org.jboss.pressgang.ccms.visualisations;

import com.beust.jcommander.JCommander;
import org.jetbrains.annotations.NotNull;

/**
 * The entry point to the application.
 */
public class Main {
    public static void main(@NotNull final String[] args) {
        final CommandLineArgs commandLineArgs = new CommandLineArgs();
        new JCommander(commandLineArgs, args);
        new GraphGenerator(commandLineArgs);
    }
}
