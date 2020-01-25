package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.jsonadapter.PropertyKeys.Companion.ordinalToPropertyKey
import com.jsonschema2kotlin.parser.model.Properties
import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Type
import com.jsonschema2kotlin.parser.model.buildProperty
import com.jsonschema2kotlin.parser.readObject
import com.jsonschema2kotlin.parser.skipNameAndValue
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.adapters.EnumJsonAdapter

private val enumJsonAdapter = EnumJsonAdapter.create(Type::class.java)

internal class PropertiesJsonAdapter : JsonAdapter<Properties>() {
    override fun fromJson(reader: JsonReader): Properties {
        return readProperties(reader)
    }

    private fun readProperties(reader: JsonReader): Properties {
        val propertyMap = mutableMapOf<String, Property>()
        reader.readObject {
            propertyMap[reader.nextName()] = readProperty(reader)
        }
        return Properties(propertyMap)
    }

    private fun readProperty(reader: JsonReader): Property {
        var type: String? = null
        var title: String? = null
        var description: String? = null
        var subProperties: Properties? = null
        reader.readObject {
            when (reader.selectName(PropertyKeys.jsonReaderOptions()).ordinalToPropertyKey()) {
                PropertyKeys.TYPE -> type = reader.nextString()
                PropertyKeys.TITLE -> title = reader.nextString()
                PropertyKeys.DESCRIPTION -> description = reader.nextString()
                PropertyKeys.PROPERTIES -> subProperties = readProperties(reader)
                null -> reader.skipNameAndValue()
            }
        }
        val typeEnum = enumJsonAdapter.fromJsonValue(type) ?: throw IllegalStateException("Unexpected null enum value")
        return typeEnum.buildProperty(
            title = title,
            description = description,
            subProperties = subProperties
        )
    }

    override fun toJson(writer: JsonWriter, value: Properties?) {
        TODO("not implemented")
    }
}