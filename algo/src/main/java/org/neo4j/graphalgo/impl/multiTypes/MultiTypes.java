package org.neo4j.graphalgo.impl.multiTypes;

import org.neo4j.graphalgo.core.heavyweight.HeavyGraph;
import org.neo4j.graphalgo.impl.Algorithm;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MultiTypes extends Algorithm<MultiTypes> {

    private HeavyGraph graph;
    private String edgeType;
    private String typeLabel;
    private GraphDatabaseService db;
    private Map<Integer, Label> nodeLabelMap;

    private static final String LABEL_NAME_PROPERTY = "name";

    public MultiTypes(HeavyGraph graph,
                      GraphDatabaseService db,
                      String edgeType,
                      String typeLabel) throws IOException {
        this.graph = graph;
        this.edgeType = edgeType;
        this.typeLabel = typeLabel;
        this.nodeLabelMap = new HashMap<>();

    }

    public void compute() {
        long startTime = System.nanoTime();
        graph.forEachNode(this::updateNode);


        System.out.println("Finished calculation in seconds " + String.valueOf(System.nanoTime()-startTime));
    }

    private boolean updateNode(int nodeId) {
        if (isTypeNode(nodeId))
            return true;

        Node nodeInstance = db.getNodeById(((Number)nodeId).longValue());

        for (int neighborId : graph.getAdjacentNodes(nodeId)) {
            if (isTypeNode(neighborId)) {
                nodeInstance.addLabel(getOrCreateLabel(neighborId));
            }
        }
        return true;
    }

    private boolean isTypeNode(int node) {
        return graph.getLabelIdToNameDict().get(graph.getLabel(node)).equals(typeLabel);
    }

    private Label getOrCreateLabel(int labelNodeId) {
        if (!nodeLabelMap.containsKey(labelNodeId))
            createLabel(labelNodeId);

        return nodeLabelMap.get(labelNodeId);
    }

    private void createLabel(int labelNodeId) {
        Node labelNodeInstance = db.getNodeById(((Number)labelNodeId).longValue());
        String name = Integer.toString(labelNodeId);
        if (labelNodeInstance.hasProperty(LABEL_NAME_PROPERTY))
            name = (String) labelNodeInstance.getProperty(LABEL_NAME_PROPERTY);

        nodeLabelMap.put(labelNodeId, Label.label(name));
    }



    /* Things I don't understand */
    @Override
    public MultiTypes me() { return this; }

    @Override
    public MultiTypes release() {
        return null;
    }
}
