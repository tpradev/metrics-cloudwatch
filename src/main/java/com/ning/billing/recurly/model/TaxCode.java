package com.ning.billing.recurly.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Created by farza on 4/21/17.
 */
public enum TaxCode {

    UNKNOWN("unknown"),
    PHYSICAL("physical"),
    DIGITAL("digital");

    private final String type;

    private TaxCode(final String type) {
        this.type = type;
    }

    @JsonValue
    public String getType() {
        return type;
    }
}
