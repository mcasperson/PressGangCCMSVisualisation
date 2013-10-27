package org.jboss.pressgang.ccms.visualisations;

import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTContentSpecCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTextContentSpecCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTCSNodeCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTContentSpecCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.constants.CommonFilterConstants;
import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTCSNodeTypeV1;
import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTBaseInterfaceV1;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.*;

/**
 * This class polls the PressGang REST endpoint for information on topics and content specs. It then
 * generates various graphs that will then be further processed.
 */
public class GraphGenerator {
    /**
     * The required expansion details for the content specs.
     */
    private static final String CONTENT_SPEC_EXPANSION =
            "{\"branches\":[{\"trunk\":{\"name\": \"" + RESTv1Constants.CONTENT_SPEC_EXPANSION_NAME + "\"}}]}";

    /**
     * The required expansion details for the content spec node.
     */
    private static final String CSNODE_EXPAND_WITH_CONTENT_SPEC =
            "{\"branches\":[" +
                "{\"trunk\":{\"name\": \"" + RESTv1Constants.CONTENT_SPEC_NODE_EXPANSION_NAME + "\"}, \"branches\":[" +
                    "{\"trunk\":{\"name\": \"" + RESTCSNodeV1.CONTENT_SPEC_NAME + "\"}}" +
                "]}" +
            "]}";

    /**
     * The RSF graph name
     */
    private static final String RSF_GRAPH_NAME = "PRESSGANG";

    /**
     * A graph in the RSF format (http://ccvisu.sosy-lab.org/manual/main.html#sec:input-rsf) used by CCVisu.
     */
    private final StringBuilder rsfGraph = new StringBuilder();
    /**
     * A graph in the SIF format (http://wiki.cytoscape.org/Cytoscape_User_Manual/Network_Formats#SIF_Format) used by
     * Cytoscape.
     */
    private String sifGraph;
    /**
     * A graph in a plain text format (http://wiki.cytoscape.org/Cytoscape_User_Manual/Creating_Networks).
     */
    private String delimitedGraph;

    public GraphGenerator(@NotNull final CommandLineArgs commandLineArgs) {
        final ResteasyClient client = new ResteasyClientBuilder().build();
        final ResteasyWebTarget target = client.target(commandLineArgs.pressgangServer);
        final RESTBaseInterfaceV1 pressgangRest = target.proxy(RESTBaseInterfaceV1.class);

        try {
            // get a list of the specs references by the spec nodes, and create a database of the spec
            // details like product, title and version
            final Map<Integer, SpecDetails> specDetailsList = new HashMap<Integer, SpecDetails>();
            final Set<Integer> foundSpecs = new HashSet<Integer>();
            final StringBuilder contentSpecIDs = new StringBuilder();
            final RESTCSNodeCollectionV1 contentSpecNodes = pressgangRest.getJSONContentSpecNodes(CSNODE_EXPAND_WITH_CONTENT_SPEC);
            for (final RESTCSNodeCollectionItemV1 contentSpecNode : contentSpecNodes.getItems()) {
                final Integer specId = contentSpecNode.getItem().getContentSpec().getId();

                if (!foundSpecs.contains(specId)) {
                    foundSpecs.add(specId);
                    if (contentSpecIDs.length() != 0) {
                        contentSpecIDs.append(",");
                    }
                    contentSpecIDs.append(specId);
                }

                if (!specDetailsList.containsKey(specId)) {
                    specDetailsList.put(specId, new SpecDetails());
                }

                if (contentSpecNode.getItem().getNodeType() == RESTCSNodeTypeV1.META_DATA) {
                    if ("Product".equalsIgnoreCase(contentSpecNode.getItem().getTitle())) {
                        specDetailsList.get(specId).setProduct(contentSpecNode.getItem().getAdditionalText());
                    } else if ("Version".equalsIgnoreCase(contentSpecNode.getItem().getTitle())) {
                        specDetailsList.get(specId).setVersion(contentSpecNode.getItem().getAdditionalText());
                    } else if ("Title".equalsIgnoreCase(contentSpecNode.getItem().getTitle())) {
                        specDetailsList.get(specId).setTitle(contentSpecNode.getItem().getAdditionalText());
                    }
                }
            }

            final String query = "query;" + CommonFilterConstants.TOPIC_IS_INCLUDED_IN_SPEC + "=" + contentSpecIDs.toString();
            final RESTTopicCollectionV1 topics = pressgangRest.getJSONTopicsWithQuery(new PathSegmentImpl(query, false), "{\"branches\":[" + RESTv1Constants.TOPICS_EXPANSION_NAME + "]}");

            buildRsfGraph(specDetailsList, contentSpecNodes, topics);


        } catch (@NotNull final Exception ex) {
            ex.printStackTrace();
        }
    }

    private void buildRsfGraph(@NotNull final Map<Integer, SpecDetails> specDetailsList,
                               @NotNull final RESTCSNodeCollectionV1 contentSpecNodes,
                               @NotNull final RESTTopicCollectionV1 topics) {
        for (final RESTCSNodeCollectionItemV1 contentSpecNode : contentSpecNodes.getItems()) {
            if (contentSpecNode.getItem().getNodeType() == RESTCSNodeTypeV1.TOPIC) {
                if (rsfGraph.length() != 0) {
                    rsfGraph.append("\n");
                }

                final Integer specId = contentSpecNode.getItem().getContentSpec().getId();
                final Integer topicId = contentSpecNode.getItem().getEntityId();

                rsfGraph.append(RSF_GRAPH_NAME + " " + specDetailsList.get(specId).getFixedProduct() + " " + topicId + " 1.0");
            }
        }
    }

    private void buildDelimitedGraph(@NotNull final RESTCSNodeCollectionV1 contentSpecNodes, @NotNull final RESTTopicCollectionV1 topics) {

    }

}
