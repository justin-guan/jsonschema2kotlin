package com.jsonschema2kotlin.parser.jsonadapter

import com.squareup.moshi.JsonReader

internal enum class PropertyKeys(val value: String) {
    TYPE("type"),
    TITLE("title"),
    DESCRIPTION("description"),
    REQUIRED("required"),
    PROPERTIES("properties"),
    MINIMUM("minimum"),
    MAXIMUM("maximum"),
    EXCLUSIVE_MIN("exclusiveMinimum"),
    EXCLUSIVE_MAX("exclusiveMaximum"),
    MIN_LENGTH("minLength"),
    MAX_LENGTH("maxLength"),
    MIN_ITEMS("minItems"),
    MAX_ITEMS("maxItems"),
    MIN_PROPERTIES("minProperties"),
    MAX_PROPERTIES("maxProperties");

    companion object {
        fun jsonReaderOptions(): JsonReader.Options =
            values().map { it.value }
                .toTypedArray()
                .let { JsonReader.Options.of(*it) }

        fun Int.ordinalToPropertyKey(): PropertyKeys? =
            this.takeIf { it in values().indices }
                ?.let { values()[it] }
    }
}
