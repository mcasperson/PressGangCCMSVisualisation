package org.jboss.pressgang.ccms.visualisations;

import com.beust.jcommander.JCommander;
import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jetbrains.annotations.NotNull;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * The entry point to the application.
 */
@Singleton
public class Main {
    @Inject
    private CommandLineArgs commandLineArgs;

    @Inject
    private GraphGenerator graphGenerator;

    public void main(@NotNull @Observes final ContainerInitialized event,
                     @NotNull @Parameters final List<String> parameters) {
        final String[] args = parameters.toArray(new String[0]);
        new JCommander(commandLineArgs, args);

        graphGenerator.generateGraphs();

        System.out.print(graphGenerator.getLayGraph());
    }
}
