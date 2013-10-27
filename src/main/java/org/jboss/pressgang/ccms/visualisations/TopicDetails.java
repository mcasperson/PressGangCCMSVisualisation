package org.jboss.pressgang.ccms.visualisations;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The extra data associated with topics.
 */
public class TopicDetails {
    private final Set<String> products = new HashSet<String>();

    @NotNull
    public Set<String> getProducts() {
        return products;
    }
}
