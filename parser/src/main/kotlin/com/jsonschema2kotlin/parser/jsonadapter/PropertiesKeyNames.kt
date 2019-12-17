package com.jsonschema2kotlin.parser.jsonadapter

import com.squareup.moshi.JsonReader

internal enum class PropertiesKeyNames(val value: String) {
    TYPE("type"),
    DESCRIPTION("description");

    companion object {
        fun jsonReaderOptions(): JsonReader.Options =
            enumValues<PropertiesKeyNames>()
                .map { it.value }
                .toTypedArray()
                .let { JsonReader.Options.of(*it) }
    }
}