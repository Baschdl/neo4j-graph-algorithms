package org.neo4j.graphalgo.results;

import java.util.HashMap;

public class FilterMetaPathsResult {

    public final String filteredMetaPathsDict;

    private FilterMetaPathsResult(HashMap<String, Integer> filteredMetaPathsDict) {
        this.filteredMetaPathsDict = "";
    }

    public static FilterMetaPathsResult.Builder builder() {
        return new FilterMetaPathsResult.Builder();
    }

    public static class Builder extends AbstractResultBuilder<FilterMetaPathsResult> {

        private HashMap<String, Integer> filteredMetaPathsDict;

        public void setFilteredMetaPathsDict(HashMap<String, Integer> filteredMetaPathsDict) {
            // this.metaPaths =  metaPaths.toArray(new String[metaPaths.size()]);
            this.filteredMetaPathsDict = filteredMetaPathsDict;
        }

        public FilterMetaPathsResult build() {
            return new FilterMetaPathsResult(filteredMetaPathsDict);

        }
    }
}