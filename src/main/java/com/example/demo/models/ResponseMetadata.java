package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseMetadata {
    @JsonProperty("message")
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
