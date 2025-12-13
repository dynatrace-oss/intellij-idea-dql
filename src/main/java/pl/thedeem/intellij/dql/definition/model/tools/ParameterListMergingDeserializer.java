package pl.thedeem.intellij.dql.definition.model.tools;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParameterListMergingDeserializer extends JsonDeserializer<List<Parameter>> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public List<Parameter> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        return mapper.readValue(parser, new TypeReference<>() {
        });
    }

    @Override
    public List<Parameter> deserialize(JsonParser p, DeserializationContext ctx, List<Parameter> intoValue) throws IOException {
        JsonNode incoming = mapper.readTree(p);
        List<Parameter> result = new ArrayList<>();
        if (incoming.isArray()) {
            for (JsonNode element : incoming) {
                result.add(mapper.readValue(element.toString(), Parameter.class));
            }
        }
        return result;
    }
}
