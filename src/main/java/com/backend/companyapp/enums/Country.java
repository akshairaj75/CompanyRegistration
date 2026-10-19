package com.backend.companyapp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Country {
    // Primary / Most Frequent Business Countries (preserving requested short words)
    USA("United States"),
    KSA("Saudi Arabia"),
    IND("India"),
    UAE("United Arab Emirates"),
    UK("United Kingdom"),

    // Global Countries (ISO 3166-1 alpha-3 codes)
    AFG("Afghanistan"),
    ALB("Albania"),
    DZA("Algeria"),
    AND("Andorra"),
    AGO("Angola"),
    ATG("Antigua and Barbuda"),
    ARG("Argentina"),
    ARM("Armenia"),
    AUS("Australia"),
    AUT("Austria"),
    AZE("Azerbaijan"),
    BHS("Bahamas"),
    BHR("Bahrain"),
    BGD("Bangladesh"),
    BRB("Barbados"),
    BLR("Belarus"),
    BEL("Belgium"),
    BLZ("Belize"),
    BEN("Benin"),
    BTN("Bhutan"),
    BOL("Bolivia"),
    BIH("Bosnia and Herzegovina"),
    BWA("Botswana"),
    BRA("Brazil"),
    BRN("Brunei"),
    BGR("Bulgaria"),
    BFA("Burkina Faso"),
    BDI("Burundi"),
    CPV("Cabo Verde"),
    KHM("Cambodia"),
    CMR("Cameroon"),
    CAN("Canada"),
    CAF("Central African Republic"),
    TCD("Chad"),
    CHL("Chile"),
    CHN("China"),
    COL("Colombia"),
    COM("Comoros"),
    COG("Congo"),
    COD("Congo (DRC)"),
    CRI("Costa Rica"),
    CIV("Cote d'Ivoire"),
    HRV("Croatia"),
    CUB("Cuba"),
    CYP("Cyprus"),
    CZE("Czech Republic"),
    DNK("Denmark"),
    DJI("Djibouti"),
    DMA("Dominica"),
    DOM("Dominican Republic"),
    ECU("Ecuador"),
    EGY("Egypt"),
    SLV("El Salvador"),
    GNQ("Equatorial Guinea"),
    ERI("Eritrea"),
    EST("Estonia"),
    SWZ("Eswatini"),
    ETH("Ethiopia"),
    FJI("Fiji"),
    FIN("Finland"),
    FRA("France"),
    GAB("Gabon"),
    GMB("Gambia"),
    GEO("Georgia"),
    DEU("Germany"),
    GHA("Ghana"),
    GRC("Greece"),
    GRD("Grenada"),
    GTM("Guatemala"),
    GIN("Guinea"),
    GNB("Guinea-Bissau"),
    GUY("Guyana"),
    HTI("Haiti"),
    HND("Honduras"),
    HKG("Hong Kong"),
    HUN("Hungary"),
    ISL("Iceland"),
    IDN("Indonesia"),
    IRN("Iran"),
    IRQ("Iraq"),
    IRL("Ireland"),
    ISR("Israel"),
    ITA("Italy"),
    JAM("Jamaica"),
    JPN("Japan"),
    JOR("Jordan"),
    KAZ("Kazakhstan"),
    KEN("Kenya"),
    KIR("Kiribati"),
    PRK("North Korea"),
    KOR("South Korea"),
    KWT("Kuwait"),
    KGZ("Kyrgyzstan"),
    LAO("Laos"),
    LVA("Latvia"),
    LBN("Lebanon"),
    LSO("Lesotho"),
    LBR("Liberia"),
    LBY("Libya"),
    LIE("Liechtenstein"),
    LTU("Lithuania"),
    LUX("Luxembourg"),
    MAC("Macau"),
    MDG("Madagascar"),
    MWI("Malawi"),
    MYS("Malaysia"),
    MDV("Maldives"),
    MLI("Mali"),
    MLT("Malta"),
    MHL("Marshall Islands"),
    MRT("Mauritania"),
    MUS("Mauritius"),
    MEX("Mexico"),
    FSM("Micronesia"),
    MDA("Moldova"),
    MCO("Monaco"),
    MNG("Mongolia"),
    MNE("Montenegro"),
    MAR("Morocco"),
    MOZ("Mozambique"),
    MMR("Myanmar"),
    NAM("Namibia"),
    NRU("Nauru"),
    NPL("Nepal"),
    NLD("Netherlands"),
    NZL("New Zealand"),
    NIC("Nicaragua"),
    NER("Niger"),
    NGA("Nigeria"),
    MKD("North Macedonia"),
    NOR("Norway"),
    OMN("Oman"),
    PAK("Pakistan"),
    PLW("Palau"),
    PSE("Palestine"),
    PAN("Panama"),
    PNG("Papua New Guinea"),
    PRY("Paraguay"),
    PER("Peru"),
    PHL("Philippines"),
    POL("Poland"),
    PRT("Portugal"),
    QAT("Qatar"),
    ROU("Romania"),
    RUS("Russia"),
    RWA("Rwanda"),
    KNA("Saint Kitts and Nevis"),
    LCA("Saint Lucia"),
    VCT("Saint Vincent and the Grenadines"),
    WSM("Samoa"),
    SMR("San Marino"),
    STP("Sao Tome and Principe"),
    SEN("Senegal"),
    SRB("Serbia"),
    SYC("Seychelles"),
    SLE("Sierra Leone"),
    SGP("Singapore"),
    SVK("Slovakia"),
    SVN("Slovenia"),
    SLB("Solomon Islands"),
    SOM("Somalia"),
    ZAF("South Africa"),
    SSD("South Sudan"),
    ESP("Spain"),
    LKA("Sri Lanka"),
    SDN("Sudan"),
    SUR("Suriname"),
    SWE("Sweden"),
    CHE("Switzerland"),
    SYR("Syria"),
    TWN("Taiwan"),
    TJK("Tajikistan"),
    TZA("Tanzania"),
    THA("Thailand"),
    TLS("Timor-Leste"),
    TGO("Togo"),
    TON("Tonga"),
    TTO("Trinidad and Tobago"),
    TUN("Tunisia"),
    TUR("Turkey"),
    TKM("Turkmenistan"),
    TUV("Tuvalu"),
    UGA("Uganda"),
    UKR("Ukraine"),
    URY("Uruguay"),
    UZB("Uzbekistan"),
    VUT("Vanuatu"),
    VAT("Vatican City"),
    VEN("Venezuela"),
    VNM("Vietnam"),
    YEM("Yemen"),
    ZMB("Zambia"),
    ZWE("Zimbabwe"),
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

        // 2. Direct match on displayName
        for (Country c : Country.values()) {
            if (c.getDisplayName().equalsIgnoreCase(clean)) {
                return c;
            }
        }

        // 3. Common aliases and alternative spellings
        String lower = clean.toLowerCase();
        switch (lower) {
            case "us":
            case "united states of america":
            case "u.s.a.":
            case "u.s.":
                return USA;
            case "saudi":
            case "kingdom of saudi arabia":
            case "k.s.a.":
                return KSA;
            case "in":
            case "bharat":
                return IND;
            case "united arab emirates":
            case "emirates":
            case "dubai":
            case "abu dhabi":
            case "sharjah":
            case "u.a.e.":
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
            case "russia":
            case "russian federation":
                return RUS;
            case "south korea":
            case "korea":
                return KOR;
            case "north korea":
                return PRK;
            case "holland":
                return NLD;
            default:
                return OTHER;
        }
    }
}
