package com.jsonschema2kotlin.parser.jsonadapter

import com.squareup.moshi.JsonReader

internal enum class PropertyKeys(val value: String) {
    TYPE("type"),
    TITLE("title"),
    DESCRIPTION("description"),
    PROPERTIES("properties");

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