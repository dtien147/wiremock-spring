package com.example.demo.transformers;

import com.example.demo.models.DataMapping;
import com.example.demo.models.Stub;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

public class FieldMappingResponseTransformer extends ResponseTransformer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "field-mapping-transformer";
    }

    private void mapFields(JsonNode templateJson, JsonNode responseBody) {
        Iterator<Map.Entry<String, JsonNode>> fields = templateJson.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();

            if (responseBody.has(key)) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) templateJson).put(key, responseBody.get(key).asText());
            }

            // Recursively map nested objects
            if (field.getValue().isObject() && responseBody.has(key)) {
                mapFields(field.getValue(), responseBody.get(key));
            }
        }
    }

    @Override
    public Response transform(Request request, Response response, FileSource fileSource, Parameters parameters) {
        try {
            // ✅ Load the response JSON template from file
            Stub stub = (Stub) parameters.get("stubConfig");
            File templateFile = new File(Paths.get("src/main/resources/" + stub.getResponseTemplate()).toAbsolutePath().toString());

            if (!templateFile.exists()) {
                System.err.println("❌ Error: JSON template file not found: " + templateFile);
                return response;
            }

            JsonNode templateJson = objectMapper.readTree(templateFile);
            JsonNode responseBody = objectMapper.readTree(response.getBodyAsString());

            // ✅ Map response fields to template fields
            mapFields(templateJson, responseBody);

            // ✅ Convert updated JSON to byte array
            byte[] updatedJsonBytes = objectMapper.writeValueAsBytes(templateJson);

            return Response.Builder.like(response)
                    .but().body(objectMapper.writeValueAsString(responseBody))
                    .body(updatedJsonBytes)
                    .build();

        } catch (IOException e) {
            System.err.println("❌ Error transforming response: " + e.getMessage());
        }

        return response;
    }
}
