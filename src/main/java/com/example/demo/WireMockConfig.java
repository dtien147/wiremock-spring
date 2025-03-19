package com.example.demo;

import com.example.demo.models.DataMapping;
import com.example.demo.models.Stub;
import com.example.demo.models.StubConfiguration;
import com.example.demo.transformers.CsvLookupResponseTransformer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.io.*;
import java.nio.file.Paths;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
public class WireMockConfig {
    private final CsvLookupResponseTransformer csvLookupResponseTransformer = new CsvLookupResponseTransformer();

    @Bean
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .port(8081) // WireMock on port 8081
                        .usingFilesUnderDirectory(Paths.get("src/main/resources").toAbsolutePath().toString()) // Ensure correct path
                        .extensions(csvLookupResponseTransformer)
        );

        wireMockServer.start();
        return wireMockServer;
    }

    @Bean
    public CommandLineRunner setupStubs(WireMockServer wireMockServer) {
        return args -> {
            try {
                // Load YAML from resources
                ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
                InputStream yamlStream = getClass().getClassLoader().getResourceAsStream("stubs.yaml");

                if (yamlStream == null) {
                    throw new RuntimeException("❌ stubs.yaml file not found in resources folder!");
                }

                StubConfiguration config = objectMapper.readValue(yamlStream, StubConfiguration.class);

                // Register each stub
                for (Stub stub : config.getResponses()) {
                    UrlPathPattern urlPattern = new UrlPathPattern(new RegexPattern(stub.getPredicates().get(0).getMatches().getPath()), true);

                    // Create response mapping
                    ResponseDefinitionBuilder response = aResponse()
                            .withStatus(stub.getStatus())
                            .withHeader(ContentTypeHeader.KEY, MediaType.APPLICATION_JSON_VALUE);

                    // ✅ Process CSV data injection
                    if (stub.getData() != null && !stub.getData().isEmpty()) {
                        for (DataMapping dataMapping : stub.getData()) {
                            if (dataMapping.getFromDataSource() != null && dataMapping.getFromDataSource().getCsv() != null) {
                                response = response
                                        .withTransformers(csvLookupResponseTransformer.getName())
                                        .withTransformerParameter("stubConfig", stub);
                            }
                        }
                    }

                    // Register stub in WireMock
                    wireMockServer.stubFor(
                            WireMock.request(stub.getMethod(), urlPattern)
                                    .willReturn(response)
                    );
                }
                System.out.println("✅ WireMock stubs loaded successfully with CSV support!");
            } catch (Exception e) {
                System.err.println("❌ Error loading stubs: " + e.getMessage());
            }
        };
    }


}
