package com.jsonschema2kotlin.parser

import com.squareup.moshi.JsonReader

internal fun JsonReader.skipNameAndValue() {
    skipName()
    skipValue()
}

internal inline fun JsonReader.readObject(body: () -> Unit) {
    beginObject()
    while (hasNext()) {
        body()
    }
    endObject()
}

internal inline fun JsonReader.readArray(body: () -> Unit) {
    beginArray()
    while (hasNext()) {
        body()
    }
    endArray()
}