package org.neo4j.graphalgo.metaPathComputationProcs;

import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.ProcedureConfiguration;
import org.neo4j.graphalgo.core.heavyweight.HeavyGraph;
import org.neo4j.graphalgo.core.utils.Pools;
import org.neo4j.graphalgo.impl.metaPathComputation.ComputeAllMetaPathsStartingAtInstances;
import org.neo4j.graphalgo.impl.metaPathComputation.ComputeAllMetaPathsStartingAtInstancesEdgeList;
import org.neo4j.graphalgo.impl.metaPathComputation.ComputeAllMetaPathsStartingAtInstancesWholeGraph;
import org.neo4j.graphalgo.results.metaPathComputationResults.ComputeAllMetaPathsResult;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ComputeAllMetaPathsStartingAtInstancesProc {

	@Context public GraphDatabaseAPI api;

	@Context public Log log;

	@Context public KernelTransaction transaction;

	@Procedure("algo.computeAllMetaPathsStartingAtInstances") @Description(
			"CALL algo.computeAllMetaPathsStartingAtInstances(length:int, nodeSkipProbability:float, edgeSkipProbability:float, "
					+ "{graph: 'my-graph', startNodeID: 5, endNodeID: 3000, limit: 10}) YIELD length: \n"
					+ "Precomputes meta paths between all nodes connected by a edge up to a metapath-length given by 'length' and saves them to a file for each node pair."
					+ "'nodePairSkipProbability' specifies the probability of skipping one pair of directly connected nodes and 'edgeSkipProbability' specifies the probability to skip an "
					+ "edge in the recursive search for matching meta-paths. One can limit the nodes where the algorithm starts with 'startNodeID' and 'endNodeID'."
					+ "If an edgelist is provided, one can specify the maximal number of meta-paths which should be mined.\n")

	public Stream<ComputeAllMetaPathsResult> computeAllMetaPathsStartingAtInstances(@Name(value = "length", defaultValue = "5") Long length,
			@Name(value = "nodeSkipProbability", defaultValue = "0") Double nodeSkipProbability,
			@Name(value = "edgeSkipProbability", defaultValue = "0") Double edgeSkipProbability, @Name(value = "config", defaultValue = "{}") Map<String, Object> config)
			throws Exception {

		ProcedureConfiguration configuration = ProcedureConfiguration.create(config);

		final ComputeAllMetaPathsResult.Builder builder = ComputeAllMetaPathsResult.builder();

		final HeavyGraph graph;

		log.info("Loading the graph...");
		graph = (HeavyGraph) new GraphLoader(api, Pools.DEFAULT).init(log, null, null, configuration).asUndirected(true).withLabelAsProperty(true)
				.load(configuration.getGraphImpl());
		log.info("Graph loaded.");

		Optional<String> edgelistFilepath = configuration.getString("edgelistFilepath");

		final ComputeAllMetaPathsStartingAtInstances algo;
		if (!edgelistFilepath.isPresent()) {
			algo = new ComputeAllMetaPathsStartingAtInstancesWholeGraph(graph, length.intValue(), log, nodeSkipProbability.floatValue(), edgeSkipProbability.floatValue(),
					configuration.getInt("startNodeID", Integer.MIN_VALUE), configuration.getInt("endNodeID", Integer.MAX_VALUE));
		} else {
			ArrayList<Integer[]> edgelist = new ArrayList<>();
			try (BufferedReader reader = new BufferedReader(new FileReader(edgelistFilepath.get()))) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] splittedLine = line.split(" ");
					edgelist.add(new Integer[] { Integer.parseInt(splittedLine[0]), Integer.parseInt(splittedLine[1]) });
				}
			} catch (IOException x) {
				System.err.format("IOException: %s%n", x);
			}
			algo = new ComputeAllMetaPathsStartingAtInstancesEdgeList(graph, length.intValue(), log, nodeSkipProbability.floatValue(), edgeSkipProbability.floatValue(),
					configuration.getInt("startNodeID", Integer.MIN_VALUE), configuration.getInt("endNodeID", Integer.MAX_VALUE), edgelist, configuration.getInt("limit", -1));
		}
		log.info("Starting meta-path computation...");
		algo.compute();
		log.info("Finished meta-path computation.");
		graph.release();
		return Stream.of(builder.build());
	}
}