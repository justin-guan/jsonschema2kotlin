package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.model.Reference
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class ReferenceJsonAdapter : JsonAdapter<Reference>() {
    override fun fromJson(reader: JsonReader): Reference {
        val reference = reader.nextString() ?: throw IllegalArgumentException("Expected reference to not be null")
        return Reference(reference)
    }

    override fun toJson(writer: JsonWriter, value: Reference?) {
        value?.let {
            writer.value(it.ref)
        }
    }
}
