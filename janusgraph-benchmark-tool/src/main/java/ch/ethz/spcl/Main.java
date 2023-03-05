package ch.ethz.spcl;

import org.apache.commons.cli.*;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphIndex;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.core.schema.SchemaAction;
import org.janusgraph.diskstorage.BackendException;
import org.janusgraph.graphdb.database.management.ManagementSystem;
import org.janusgraph.hadoop.MapReduceIndexManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    final static Option help = Option.builder()
            .longOpt("help")
            .optionalArg(true)
            .desc("Print this help message and exit.")
            .build();

    final static Option config = Option.builder()
            .longOpt("config")
            .required()
            .argName("path")
            .hasArg()
            .desc("Location of the configuration file for Janusgraph instance.")
            .build();

    final static Option tasks = Option.builder()
            .option("n")
            .longOpt("tasks")
            .argName("num")
            .hasArg()
            .desc("Number of tasks (i.e. queries) to create per thread.")
            .build();
    final static Option threads = Option.builder()
            .option("t")
            .longOpt("threads")
            .argName("num")
            .hasArg()
            .desc("Number of threads to spawn. The overall number of tasks scales with the number of threads.")
            .required()
            .build();

    final static Option aggregate = Option.builder()
            .optionalArg(true)
            .option("a")
            .longOpt("aggregate")
            .desc("This flag determines whether we output the aggregated benchmark results and individual query latencies.")
            .build();

    final static Option nodeID = Option.builder()
            .longOpt("node-id")
            .argName("<num>")
            .hasArg()
            .desc("Prefix sum of all the query types before object get query and the weight of object get [default: 129]")
            .build();

    final static Option threadsPerNode = Option.builder()
            .longOpt("threads-per-node")
            .argName("<num>")
            .hasArg()
            .desc("Number of threads a node runs.")
            .build();

    final static Option objectGet = Option.builder()
            .longOpt("object-get")
            .argName("%")
            .hasArg()
            .desc("Prefix sum of all the query types before object get query and the weight of object get [default: 129]")
            .build();

    final static Option objectInsert = Option.builder()
            .longOpt("object-insert")
            .argName("%")
            .hasArg()
            .desc("Prefix sum of all the query types before object insert query and the weight of object insert  [default: 155]")
            .build();

    final static Option objectDelete = Option.builder()
            .longOpt("object-delete")
            .argName("%")
            .hasArg()
            .desc("Prefix sum of all the query types before object delete query and the weight of object delete  [default: 165]")
            .build();

    final static Option objectUpdate = Option.builder()
            .longOpt("object-update")
            .argName("%")
            .hasArg()
            .desc("Prefix sum of all the query types before object update query and the weight of object update  [default: 239]")
            .build();

    final static Option assocCount = Option.builder()
            .longOpt("assoc-count")
            .argName("%")
            .hasArg()
            .desc("Prefix sum of all the query types before association count query and the weight of association count  [default: 288]")
            .build();

    final static Option assocRange = Option.builder()
            .longOpt("assoc-range")
            .argName("%")
            .hasArg()
            .desc("Prefix sum of all the query types before association range query and the weight of association range [default: 800]")
            .build();

    final static Option assocInsert = Option.builder()
            .longOpt("assoc-insert")
            .argName("%")
            .hasArg()
            .desc("Prefix sum of all the query types before association insert query and the weight of association insert [default: 1000]")
            .build();

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        CommandLine cli;

        Options options = new Options();

        options.addOption(help);
        options.addOption(config);
        options.addOption(tasks);
        options.addOption(threads);
        options.addOption(aggregate);
        options.addOption(objectGet);
        options.addOption(objectInsert);
        options.addOption(objectDelete);
        options.addOption(objectUpdate);
        options.addOption(assocCount);
        options.addOption(assocRange);
        options.addOption(assocInsert);

        options.addOption(nodeID);
        options.addOption(threadsPerNode);

        try {
            cli = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp(options);
            return;
        }

        if (cli.hasOption(help)) {
            printHelp(options);
            return;
        }

        int taskCount = Integer.parseInt(cli.getOptionValue(tasks));
        int threadCount = Integer.parseInt(cli.getOptionValue(threads));
        String configFile = cli.getOptionValue(config);
        boolean isAggregate = cli.hasOption(aggregate);
        int id = Integer.parseInt(cli.getOptionValue(nodeID, "0"));
        int threadPerNode = Integer.parseInt(cli.getOptionValue(threadsPerNode, "36"));
        List<Integer> probabilities = new ArrayList<>();
        probabilities.add(Integer.parseInt(cli.getOptionValue(objectGet, "129")));
        probabilities.add(Integer.parseInt(cli.getOptionValue(objectInsert, "155")));
        probabilities.add(Integer.parseInt(cli.getOptionValue(objectDelete, "165")));
        probabilities.add(Integer.parseInt(cli.getOptionValue(objectUpdate, "239")));
        probabilities.add(Integer.parseInt(cli.getOptionValue(assocCount, "288")));
        probabilities.add(Integer.parseInt(cli.getOptionValue(assocRange, "800")));
        probabilities.add(Integer.parseInt(cli.getOptionValue(assocInsert, "1000")));

        // Connect to JanusGraph instance
        JanusGraph graph;
        GraphTraversalSource source;
        try {
            graph = initializeGraph(configFile);
            source = graph.traversal();
        } catch (BackendException e) {
            LOG.info("Connection failed...");
            return;
        }

        // Unfortunately, the JanusGraph csv loader did not assign the id column as primary key
        // to vertices. In order to query vertices by id, we need to create an index for it.
        createIndex(graph);

        LOG.info("Starting executing queries...");
        ExecutionController.runBenchmark(
                graph,
                source,
                taskCount,
                threadCount,
                probabilities,
                isAggregate,
                id,
                threadPerNode
        );
        LOG.info("Benchmark ran to completion...");

        closeGraph(graph, source);
    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("janusgraph-bench --config <PATH> --tasks <num> --threads <num> "
            + "[--node-id <num>] [--threads-per-node <num>]"
            + "[--aggregate] [--object-get <%>] [--object-insert <%>] [--object-delete <%>] "
            + "[--object-update <%>] [--assoc-count <%>] [--assoc-range <%>] [--object-insert <%>]",
    System.lineSeparator()
            + "Benchmarking tool for janusgraph. "
            + System.lineSeparator()
            + "Note that the percentages are not checked, so if they add up to 110%, "
            + System.lineSeparator()
            + "then 110% queries (based on #threads and #tasks) will be issued."
            + System.lineSeparator()
            + System.lineSeparator()
            + "options: ",
            options,
            ""
        );
    }

    private static JanusGraph initializeGraph(String configFile) throws BackendException {
        LOG.info("Establishing connection to Janusgraph instance...");
        JanusGraph graph = JanusGraphFactory.open(configFile);
        LOG.info("Connected to Janusgraph.");

        return graph;
    }

    private static void createIndex(JanusGraph graph) {
        try {
            JanusGraphManagement management = graph.openManagement();
            if (!management.containsGraphIndex("byIdComposite")) {
                LOG.info("Index does not exist, creating a new index for id field.");

                PropertyKey id = management.getPropertyKey("id");
                management.buildIndex("byIdComposite", Vertex.class).addKey(id).buildCompositeIndex();
                management.commit();
                ManagementSystem.awaitGraphIndexStatus(graph, "byIdComposite").call();

                // reindex the existing data - JanusGraphManagement
                management = graph.openManagement();
                management.updateIndex(management.getGraphIndex("byIdComposite"), SchemaAction.REINDEX).get();
                management.commit();

                // reindex the existing data - MapReduce
                /*
                management = graph.openManagement();
                MapReduceIndexManagement mr = new MapReduceIndexManagement(graph);
                mr.updateIndex(management.getGraphIndex("byIdComposite"), SchemaAction.REINDEX).get();
                management.commit();
                */

                LOG.info("Completed creating index, moving on...");
            } else {
                LOG.info("Index already exists, skipping index creation...");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private static void closeGraph(JanusGraph graph, GraphTraversalSource source) {
        try {
            if (source != null) {
                source.close();
            }
            if (graph != null) {
                graph.close();
            }
        } catch (Exception e) {
          System.out.println(e.getMessage());
        } finally {
            source = null;
            graph = null;
        }
    }
}