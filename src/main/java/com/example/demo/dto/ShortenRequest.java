package com.example.demo.dto;

import com.example.demo.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;

// What the CLIENT sends us. Deliberately its own class, separate from Url --
// this shape is the public contract, independent of anything in the database.
public class ShortenRequest {

    @NotBlank(message = "url must not be blank")
    @ValidUrl
    private String url;

    public ShortenRequest() {}   // Jackson needs a no-arg constructor to deserialize JSON

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
