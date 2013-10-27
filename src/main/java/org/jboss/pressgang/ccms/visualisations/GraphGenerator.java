package org.jboss.pressgang.ccms.visualisations;

import ccvisu.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTTopicCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTCSNodeCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.constants.CommonFilterConstants;
import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTCSNodeTypeV1;
import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTBaseInterfaceV1;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class polls the PressGang REST endpoint for information on topics and content specs. It then
 * generates various graphs that will then be further processed.
 */
public class GraphGenerator {
    /**
     * The logger
     */
    private static final Logger LOGGER = Logger.getLogger(GraphGenerator.class.getName());

    /**
     * The required expansion details for the content specs.
     */
    private static final String CONTENT_SPEC_EXPANSION =
            "{\"branches\":[{\"trunk\":{\"name\": \"" + RESTv1Constants.CONTENT_SPEC_EXPANSION_NAME + "\"}}]}";

    /**
     * The required expansion details for the content specs.
     */
    private static final String TOPICS_EXPANSION =
            "{\"branches\":[{\"trunk\":{\"name\": \"" + RESTv1Constants.TOPICS_EXPANSION_NAME + "\"}}]}";

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

    private final StringBuilder rsfGraph = new StringBuilder();
    private final StringBuilder layGraph = new StringBuilder();
    /**
     * A graph in the SIF format (http://wiki.cytoscape.org/Cytoscape_User_Manual/Network_Formats#SIF_Format) used by
     * Cytoscape.
     */
    private String sifGraph;
    /**
     * A graph in a plain text format (http://wiki.cytoscape.org/Cytoscape_User_Manual/Creating_Networks).
     */
    private String delimitedGraph;

    /**
     * The parsed command line args
     */
    @Inject
    private CommandLineArgs commandLineArgs;


    public GraphGenerator() {

    }

    /**
     * Gets the data from the server and builds the various graphs supported by this application.
     */
    public void generateGraphs() {
        LOGGER.info("Constructing RESTEasy Client");

        final ResteasyClient client = new ResteasyClientBuilder().build();
        final ResteasyWebTarget target = client.target(commandLineArgs.pressgangServer);
        final RESTBaseInterfaceV1 pressgangRest = target.proxy(RESTBaseInterfaceV1.class);

        try {
            LOGGER.info("Getting Content Spec Nodes");

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

            LOGGER.info("Getting Topics");

            final String query = "query;" + CommonFilterConstants.TOPIC_IS_INCLUDED_IN_SPEC + "=" + contentSpecIDs.toString();
            final RESTTopicCollectionV1 topics = pressgangRest.getJSONTopicsWithQuery(new PathSegmentImpl(query, false), TOPICS_EXPANSION);

            buildExtraDataFile(specDetailsList, contentSpecNodes, topics);
            buildRsfGraph(specDetailsList, contentSpecNodes, topics);


        } catch (@NotNull final Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Builds a JSON database that includes extra attributes for topics.
     * @param specDetailsList The collection of spec details
     * @param contentSpecNodes The collection of spec nodes
     * @param topics The collection of topics
     */
    private void buildExtraDataFile(@NotNull final Map<Integer, SpecDetails> specDetailsList,
                               @NotNull final RESTCSNodeCollectionV1 contentSpecNodes,
                               @NotNull final RESTTopicCollectionV1 topics) {

        LOGGER.info("Building Topic Database");

        final Map<Integer, TopicDetails> integerTopicDetailsMap = new HashMap<Integer, TopicDetails>();

        for (final RESTCSNodeCollectionItemV1 contentSpecNode : contentSpecNodes.getItems()) {
            if (contentSpecNode.getItem().getNodeType() == RESTCSNodeTypeV1.TOPIC) {
                final Integer specId = contentSpecNode.getItem().getContentSpec().getId();
                final Integer topicId = contentSpecNode.getItem().getEntityId();

                if (specDetailsList.containsKey(specId)) {

                    if (!integerTopicDetailsMap.containsKey(topicId)) {
                        integerTopicDetailsMap.put(topicId, new TopicDetails());
                    }

                    integerTopicDetailsMap.get(topicId).getProducts().add(specDetailsList.get(specId).getProduct());
                }
            }
        }

        final ObjectWriter writer = new ObjectMapper().writer();
        try {
            final StringWriter stringWriter = new StringWriter();
            writer.writeValue(stringWriter, integerTopicDetailsMap);

            final StringBuilder jsonDatabase = new StringBuilder();
            jsonDatabase.append("topicDatabase = ");
            jsonDatabase.append(stringWriter.toString());

            FileWriter output = null;
            try {
                output = new FileWriter(commandLineArgs.topicDatabaseFile);
                output.write(jsonDatabase.toString());
            } finally {
                output.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a CCVisu RSF graph.
     * @param specDetailsList The collection of spec details
     * @param contentSpecNodes The collection of spec nodes
     * @param topics The collection of topics
     */
    private void buildRsfGraph(@NotNull final Map<Integer, SpecDetails> specDetailsList,
                               @NotNull final RESTCSNodeCollectionV1 contentSpecNodes,
                               @NotNull final RESTTopicCollectionV1 topics) {

        LOGGER.info("Building Topic Graph");

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

        buildLayGraph();
    }

    /**
     * Create a LAY file from the RSF file. This is a copy of the CCVisu.process() method,
     * cut down to work with just the RSF input and LAY output.
     */
    private void buildLayGraph() {

        LOGGER.info("Building Topic Layout Graph");

        BufferedReader input = null;
        PrintWriter output = null;

        try {
            input = new BufferedReader(new StringReader(getRsfGraph().toString()));
            output = new PrintWriter(new BufferedWriter(new FileWriter(commandLineArgs.topicGraphFile)));

            final Options options = new Options();

            // Initialize the graph representation.
            options.graph = new GraphData();
            // The output should be in three dimensions
            options.nrDim = 3;
            // Use 100 iterations
            options.nrIterations = 100;

            /*
                The table below shows the various settings that can be applied to the CCVisu options
                object to emulate some common energy models. (http://ccvisu.sosy-lab.org/manual/main.html#htoc26)

                Energy model	                attrExponent a (1)   repuExponent r (0)	vertRepu e (false)
                Fruchterman Reingold	        3	                0	                0
                Vertex-repulsion LinLog	        1	                0	                0
                Edge-repulsion LinLog	        1	                0	                1
                Weighted edge-repulsion LinLog	1	                0	                1
            */

            // Fruchterman Reingold
            options.attrExponent = 3;
            options.repuExponent = 0;
            options.vertRepu = false;

            // Vertex-repulsion LinLog
            /*options.attrExponent = 1;
            options.repuExponent = 0;
            options.vertRepu = false;*/

            // Edge-repulsion LinLog
            /*options.attrExponent = 1;
            options.repuExponent = 0;
            options.vertRepu = true;*/


            final ReaderData graphReader = new ReaderDataGraphRSF(input, options.verbosity);
            graphReader.read(options.graph);

            // Handle vertex options.
            for (GraphVertex curVertex : options.graph.vertices) {
                // annotAll (annotate each vertex with its name).
                if (Options.Option.annotAll.getBool()) {
                    curVertex.showName = true;
                }
                // annotNone (annotate no vertex).
                if (Options.Option.annotNone.getBool()) {
                    curVertex.showName = false;
                }
            }

            // lVertex.fixedPos == true means that the minimizer does not change
            //   lVertex's position.
            if (options.fixedInitPos && options.initialLayout != null) {
                for (GraphVertex lCurrVertex : options.graph.vertices) {
                    // If the current vertex exists in the read initial layout,
                    // then mark its position as fixed.
                    if (options.initialLayout.nameToVertex.containsKey(lCurrVertex.name)) {
                        lCurrVertex.fixedPos = true;
                    }
                }
            }

            // Initialize layout.
            CCVisu.initializeLayout(options);
            // Set minimizer algorithm.
            // So far there is only one implemented in CCVisu.
            final Minimizer minimizer = new MinimizerBarnesHut(options);
            // Compute layout for given graph.
            minimizer.minimizeEnergy();

            // Output writer.
            final WriterData dataWriter = new WriterDataLAY(output, options.graph);

            // Write the data using the writer.
            dataWriter.write();

            // Close the output file.
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the input file.
            try {
                input.close();
            } catch (Exception e) {
                System.err.println("Exception while closing input file: ");
                System.err.println(e);
            }

            try {
                output.close();
            } catch (Exception e) {
                System.err.println("Exception while closing output file: ");
                System.err.println(e);
            }
        }

    }

    private void buildDelimitedGraph(@NotNull final RESTCSNodeCollectionV1 contentSpecNodes, @NotNull final RESTTopicCollectionV1 topics) {

    }


    /**
     * A graph in the RSF format (http://ccvisu.sosy-lab.org/manual/main.html#sec:input-rsf) used by CCVisu.
     */
    @NotNull
    public String getRsfGraph() {
        return rsfGraph.toString();
    }

    /**
     * A graph in the LAY format (http://ccvisu.sosy-lab.org/manual/main.html) used by CCVisu.
     */
    @NotNull
    public String getLayGraph() {
        return layGraph.toString();
    }
}
