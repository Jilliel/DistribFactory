package fr.tp.inf112.projects.robotsim.app;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;

public class BasicVertexDeserializer extends JsonDeserializer<BasicVertex> {
    @Override
    public BasicVertex deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        int x = node.get("xCoordinate").asInt();
        int y = node.get("yCoordinate").asInt();
        return new BasicVertex(x, y);
    }
}