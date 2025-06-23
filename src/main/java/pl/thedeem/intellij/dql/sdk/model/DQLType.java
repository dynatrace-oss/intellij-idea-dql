package pl.thedeem.intellij.dql.sdk.model;

import java.util.List;
import java.util.Map;

public class DQLType {
    List<Integer> indexRange;
    Map<String, DQLFieldType> mappings;

    public List<Integer> getIndexRange() {
        return indexRange;
    }

    public Map<String, DQLFieldType> getMappings() {
        return mappings;
    }

    public static class DQLFieldType {
        String type;
        List<DQLType> types;

        public String getType() {
            return type;
        }

        public List<DQLType> getTypes() {
            return types;
        }
    }
}
