package org.neo4j.graphalgo;

import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.heavyweight.HeavyGraph;
import org.neo4j.graphalgo.core.heavyweight.HeavyGraphFactory;
import org.neo4j.graphalgo.impl.ComputeAllMetaPaths;
import org.neo4j.graphalgo.results.ComputeAllMetaPathsResult;
import org.neo4j.kernel.api.KernelTransaction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.HashSet;
import java.util.stream.Stream;

public class ComputeAllMetaPathsProc {


    @Context
    public GraphDatabaseAPI api;

    @Context
    public Log log;

    @Context
    public KernelTransaction transaction;

    @Procedure("algo.calculateAllMetaPaths")
    @Description("CALL algo.computeAllMetaPaths(length:int) YIELD length")

    public Stream<ComputeAllMetaPathsResult> calculateAllMetaPaths(
            @Name(value = "length", defaultValue = "5") String lengthString) throws Exception{
        int length = Integer.valueOf(lengthString);
        System.out.println("Given length is: " + String.valueOf(length));

        final ComputeAllMetaPathsResult.Builder builder = ComputeAllMetaPathsResult.builder();

        final HeavyGraph graph;

        graph = (HeavyGraph) new GraphLoader(api)
                .asUndirected(true)
                .withLabelAsProperty(true)
                .load(HeavyGraphFactory.class);


        final ComputeAllMetaPaths algo = new ComputeAllMetaPaths(graph, graph, graph, graph, length);
        HashSet<String> metaPaths;
        metaPaths = algo.compute().getFinalMetaPaths();
        builder.setMetaPaths(metaPaths);
        graph.release();
        //return algo.resultStream();
        //System.out.println(Stream.of(builder.build()));
        return Stream.of(builder.build());
    }

}