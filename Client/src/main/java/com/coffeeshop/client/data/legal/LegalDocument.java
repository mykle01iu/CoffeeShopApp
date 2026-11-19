package com.coffeeshop.client.data.legal;

import java.util.List;

public class LegalDocument {
    private final LegalType type;
    private final String title;
    private final String lastUpdated;
    private final String version;
    private final List<LegalSection> sections;

    public LegalDocument(LegalType type, String title, String lastUpdated, String version, List<LegalSection> sections) {
        this.type = type;
        this.title = title;
        this.lastUpdated = lastUpdated;
        this.version = version;
        this.sections = sections;
    }

    public LegalType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getVersion() {
        return version;
    }

    public List<LegalSection> getSections() {
        return sections;
    }
}
