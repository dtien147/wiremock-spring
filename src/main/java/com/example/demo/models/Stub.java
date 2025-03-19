package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Stub {
    private String method;
    private int status;
    private List<Predicate> predicates;
    private String responseTemplate;
    private List<DataMapping> data;

    @JsonProperty("response") // ✅ Map 'response' field from YAML
    private ResponseMetadata response;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public List<Predicate> getPredicates() { return predicates; }
    public void setPredicates(List<Predicate> predicates) { this.predicates = predicates; }

    public String getResponseTemplate() { return responseTemplate; }
    public void setResponseTemplate(String responseTemplate) { this.responseTemplate = responseTemplate; }

    public List<DataMapping> getData() { return data; }
    public void setData(List<DataMapping> data) { this.data = data; }

    public ResponseMetadata getResponse() { return response; }
    public void setResponse(ResponseMetadata response) { this.response = response; }
}
