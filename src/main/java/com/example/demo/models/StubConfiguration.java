package com.example.demo.models;

import com.example.demo.models.Stub;

import java.util.List;

public class StubConfiguration {
    private String uri;
    private List<Stub> responses;

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public List<Stub> getResponses() { return responses; }
    public void setResponses(List<Stub> responses) { this.responses = responses; }
}
