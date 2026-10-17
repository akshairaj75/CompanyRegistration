package com.backend.companyapp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Country {
    USA("United States"),
    KSA("Saudi Arabia"),
    IND("India"),
    UAE("United Arab Emirates"),
    UK("United Kingdom"),
    CAN("Canada"),
    AUS("Australia"),
    DEU("Germany"),
    FRA("France"),
    SGP("Singapore"),
    JPN("Japan"),
    NLD("Netherlands"),
    CHE("Switzerland"),
    QAT("Qatar"),
    MYS("Malaysia"),
    CHN("China"),
    ESP("Spain"),
    ITA("Italy"),
    BRA("Brazil"),
    ZAF("South Africa"),
    NZL("New Zealand"),
    IRL("Ireland"),
    SWE("Sweden"),
    NOR("Norway"),
    DNK("Denmark"),
    FIN("Finland"),
    KWT("Kuwait"),
    OMN("Oman"),
    BHR("Bahrain"),
    OTHER("Other");

    private final String displayName;

    Country(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonValue
    public String getCode() {
        return this.name();
    }

    @JsonCreator
    public static Country fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String clean = value.trim();

        // 1. Direct match on enum name
        for (Country c : Country.values()) {
            if (c.name().equalsIgnoreCase(clean)) {
                return c;
            }
        }

        // 2. Match on displayName
        for (Country c : Country.values()) {
            if (c.getDisplayName().equalsIgnoreCase(clean)) {
                return c;
            }
        }

        // 3. Common aliases
        String lower = clean.toLowerCase();
        switch (lower) {
            case "us":
            case "united states of america":
                return USA;
            case "saudi":
            case "kingdom of saudi arabia":
                return KSA;
            case "in":
            case "bharat":
                return IND;
            case "united arab emirates":
            case "emirates":
            case "dubai":
            case "abu dhabi":
                return UAE;
            case "united kingdom":
            case "great britain":
            case "britain":
            case "england":
            case "gb":
            case "gbr":
                return UK;
            case "germany":
            case "deutschland":
            case "ger":
                return DEU;
            default:
                return OTHER;
        }
    }
}
