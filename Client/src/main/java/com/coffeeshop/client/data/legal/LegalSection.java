package com.coffeeshop.client.data.legal;

public class LegalSection {
    private final String heading;
    private final String body;

    public LegalSection(String heading, String body) {
        this.heading = heading;
        this.body = body;
    }

    public String getHeading() {
        return heading;
    }

    public String getBody() {
        return body;
    }
}
