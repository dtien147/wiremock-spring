package com.example.demo.transformers;

import com.example.demo.models.DataMapping;
import com.example.demo.models.Stub;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.*;
import com.github.tomakehurst.wiremock.common.FileSource;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvLookupResponseTransformer extends ResponseTransformer {
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ Use Jackson for JSON conversion

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        try {
            // Extract the application ID using the regex provided in the YAML:
            String url = request.getUrl();
            Stub stub = (Stub) parameters.get("stubConfig");
            var dataMapping = stub.getData().get(0);
            Pattern pattern = Pattern.compile(dataMapping.getKey().getSelector());
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                // ✅ Load JSON response template
                File jsonTemplateFile = new File(Paths.get("src/main/resources/__files/" + stub.getResponseTemplate()).toAbsolutePath().toString());
                if (!jsonTemplateFile.exists()) {
                    System.err.println("❌ JSON template file not found: " + jsonTemplateFile);
                    return response;
                }
                String applicationId = matcher.group(1);
                var csvData = readCSV(dataMapping.getFromDataSource().getCsv().getPath(), applicationId);

                JsonNode jsonResponseTemplate = objectMapper.readTree(jsonTemplateFile);

                // ✅ Inject CSV data into JSON template dynamically
                injectCsvDataIntoJson(jsonResponseTemplate, csvData, dataMapping.getInto());

                return Response.Builder.like(response)
                        .but().body(objectMapper.writeValueAsString(jsonResponseTemplate))
                        .build();
            }
        } catch (Exception e) {

        }
        return response;
    }

    @Override
    public String getName() {
        return "csv-lookup-transformer";
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private Map<String, String> readCSV(String filePath, String key) {
        String delimiter = "|";
        int keyIndex = 0;
        Map<String, String> dataMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/" + filePath))) {
            String headerLine = br.readLine(); // Read the first line (column headers)
            if (headerLine == null) return dataMap;

            String[] headers = headerLine.split("\\" + delimiter); // Split header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\" + delimiter);

                if (values[keyIndex].equals(key)) {
                    var map = new HashMap<String, String>();
                    for (var i = 0; i < values.length; i++) {
                        map.put(headers[i], values[i]);
                    }

                    return map;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading CSV file: " + filePath + " - " + e.getMessage());
        }
        return dataMap;
    }

    private void injectCsvDataIntoJson(JsonNode jsonNode, Map<String, String> csvData, String into) {
        if (jsonNode.has(into) && jsonNode.get(into).isObject()) {
            ObjectNode applicationNode = (ObjectNode) jsonNode.get(into);
            Iterator<Map.Entry<String, JsonNode>> fields = applicationNode.fields();
            List<String> keysToReplace = new ArrayList<>();

            // ✅ Collect keys that contain placeholders
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                String value = field.getValue().asText();

                if (value.contains("{{") && value.contains("}}")) {
                    keysToReplace.add(key);
                }
            }

            // ✅ Replace placeholders with actual CSV values
            for (String key : keysToReplace) {
                String placeholder = applicationNode.get(key).asText();
                String csvKey = placeholder.replace("{{application.", "").replace("}}", "").trim();

                if (csvData.containsKey(csvKey)) {
                    applicationNode.put(key, csvData.get(csvKey));
                } else {
                    System.err.println("⚠️ Placeholder key '" + csvKey + "' not found in CSV data.");
                    applicationNode.put(key, ""); // Default to empty if not found
                }
            }
        } else {
            System.err.println("❌ JSON template does not contain " + into);
        }
    }
}