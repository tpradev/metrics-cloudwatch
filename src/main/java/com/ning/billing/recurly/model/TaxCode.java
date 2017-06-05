package com.ning.billing.recurly.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Currency
 *
 * @author Farza Shereef (farza.pulikkalakath@tp-link.com)
 * @version 1.0
 * @since 1.1
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
