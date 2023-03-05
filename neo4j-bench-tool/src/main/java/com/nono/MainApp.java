package com.nono;

import ch.qos.logback.classic.Level;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp {

    final static Option help = Option.builder()
            .longOpt("help")
            .desc("Print this help message and exit.")
            .build();

    final static Option tasks = Option.builder()
            .option("n")
            .longOpt("tasks")
            .argName("num")
            .hasArg()
            .desc("Number of tasks (i.e. queries) to create per thread.")
            .required()
            .build();

    final static Option threads = Option.builder()
            .option("t")
            .longOpt("threads")
            .argName("num")
            .hasArg()
            .desc("Number of threads to spawn. The overall number of tasks scales with the number of threads.")
            .required()
            .build();

    final static Option startTime = Option.builder()
            .longOpt("start-time")
            .argName("time")
            .hasArg()
            .desc("Unix timestamp of when to start. The program will wait till before connecting to neo4j. " +
                    "If not specified the benchmark will start immediately.")
            .build();

    final static Option database = Option.builder()
            .option("d")
            .longOpt("database")
            .argName("name")
            .hasArg()
            .desc("Database name.")
            .required()
            .build();

    final static Option hostname = Option.builder()
            .option("h")
            .longOpt("hostname")
            .argName("host")
            .hasArg()
            .desc("Neo4j hostname (without neo4j:// prefix).")
            .required()
            .build();

    final static Option username = Option.builder()
            .option("u")
            .longOpt("username")
            .argName("user")
            .hasArg()
            .desc("Neo4j username. [default: neo4j]")
            .build();

    final static Option password = Option.builder()
            .option("p")
            .longOpt("password")
            .argName("password")
            .hasArg()
            .desc("Neo4j password. [default: test]")
            .build();

    final static Option objectGet = Option.builder()
            .longOpt("object-get")
            .argName("%")
            .hasArg()
            .desc("Percentage of object get queries. [default: 129]")
            .build();

    final static Option objectInsert = Option.builder()
            .longOpt("object-insert")
            .argName("%")
            .hasArg()
            .desc("Percentage of object insert queries. [default: 155]")
            .build();

    final static Option objectDelete = Option.builder()
            .longOpt("object-delete")
            .argName("%")
            .hasArg()
            .desc("Percentage of object delete queries. [default: 165]")
            .build();

    final static Option objectUpdate = Option.builder()
            .longOpt("object-update")
            .argName("%")
            .hasArg()
            .desc("Percentage of object update queries. [default: 239]")
            .build();

    final static Option assocCount = Option.builder()
            .longOpt("assoc-count")
            .argName("%")
            .hasArg()
            .desc("Percentage of association count queries. [default: 288]")
            .build();

    final static Option assocRange = Option.builder()
            .longOpt("assoc-range")
            .argName("%")
            .hasArg()
            .desc("Percentage of association range queries. [default: 800]")
            .build();

    final static Option assocInsert = Option.builder()
            .longOpt("assoc-insert")
            .argName("%")
            .hasArg()
            .desc("Percentage of association insert queries. [default: 1000]")
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

    final static Option throughput = Option.builder()
            .optionalArg(true)
            .option("a")
            .longOpt("aggregate")
            .desc("This flag determines whether we output the aggregated benchmark results and individual query latencies.")
            .build();

    public static void main(String[] args) {

        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO);

        Options options = new Options();

        options.addOption(help);
        options.addOption(tasks);
        options.addOption(threads);
        options.addOption(startTime);
        options.addOption(database);
        options.addOption(hostname);
        options.addOption(username);
        options.addOption(password);
        options.addOption(objectGet);
        options.addOption(objectInsert);
        options.addOption(objectDelete);
        options.addOption(objectUpdate);
        options.addOption(assocCount);
        options.addOption(assocRange);
        options.addOption(assocInsert);

        options.addOption(nodeID);
        options.addOption(threadsPerNode);
        options.addOption(throughput);

        CommandLineParser parser = new DefaultParser();
        CommandLine cli;

        try {
             cli = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp(options);
            return;
        }

        if (cli.hasOption(help)) {
            printHelp(options);
            return;
        }

        ExecutionController.runBenchmark(
                cli.getOptionValue(hostname),
                cli.getOptionValue(username, "neo4j"),
                cli.getOptionValue(password, "test"),
                Integer.parseInt(cli.getOptionValue(tasks)),
                Integer.parseInt(cli.getOptionValue(threads)),
                Long.parseLong(cli.getOptionValue(startTime, "0")),
                cli.getOptionValue(database),
                Integer.parseInt(cli.getOptionValue(objectGet, "129")),
                Integer.parseInt(cli.getOptionValue(objectInsert, "155")),
                Integer.parseInt(cli.getOptionValue(objectDelete, "165")),
                Integer.parseInt(cli.getOptionValue(objectUpdate, "239")),
                Integer.parseInt(cli.getOptionValue(assocCount, "288")),
                Integer.parseInt(cli.getOptionValue(assocRange, "800")),
                Integer.parseInt(cli.getOptionValue(assocInsert, "1000")),
                Integer.parseInt(cli.getOptionValue(nodeID, "0")),
                Integer.parseInt(cli.getOptionValue(threadsPerNode, "36")),
                cli.hasOption(throughput)
        );

    }

    private static void printHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                "neo4j-bench",
                "\nBenchmarking tool for neo4j. Note that the percentages are not checked, so if they " +
                        "add up to 110%, then 110% queries (based on #threads and #tasks) will be issues.\n" +
                        "\n" +
                        "options:",
                options,
                "\nSource at https://github.com/JanKleine/neo4j-benchmark-tool/",
                true
        );
    }
}
