package org.neo4j.graphalgo.api;

import java.util.Collection;
import java.util.HashMap;

public interface ArrayGraphInterface {

    int[] getAdjacentNodes(int nodeId);

    int[] getOutgoingNodes(int nodeId);

    int[] getIncomingNodes(int nodeId);

    int getLabel(int nodeId);

    Integer[] getLabels(int nodeId);

    int getEdgeLabel(Integer nodeId1, Integer nodeId2);

    Collection<Integer> getAllEdgeLabels();

    Collection<Integer> getAllLabels();

    HashMap<Integer, String> getLabelIdToNameDict();

    HashMap<Integer, String> getEdgeLabelIdToNameDict();
}
