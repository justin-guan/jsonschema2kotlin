package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.jsonadapter.PropertyKeys.Companion.ordinalToPropertyKey
import com.jsonschema2kotlin.parser.model.*
import com.jsonschema2kotlin.parser.readArray
import com.jsonschema2kotlin.parser.readObject
import com.jsonschema2kotlin.parser.skipNameAndValue
import com.jsonschema2kotlin.parser.utils.Do
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
        val propertyHolder = PropertyHolder()
        reader.readObject {
            Do exhaustive when (reader.selectName(PropertyKeys.jsonReaderOptions()).ordinalToPropertyKey()) {
                PropertyKeys.TYPE -> type = reader.nextString()
                PropertyKeys.TITLE -> propertyHolder.title = reader.nextString()
                PropertyKeys.DESCRIPTION -> propertyHolder.description = reader.nextString()
                PropertyKeys.REQUIRED -> reader.readArray { propertyHolder.required.add(reader.nextString()) }
                PropertyKeys.PROPERTIES -> propertyHolder.properties = readProperties(reader)
                PropertyKeys.MINIMUM -> propertyHolder.minimum = reader.nextDouble()
                PropertyKeys.MAXIMUM -> propertyHolder.maximum = reader.nextDouble()
                PropertyKeys.EXCLUSIVE_MIN -> propertyHolder.exclusiveMinimum = reader.nextDouble()
                PropertyKeys.EXCLUSIVE_MAX -> propertyHolder.exclusiveMaximum = reader.nextDouble()
                PropertyKeys.MIN_LENGTH -> propertyHolder.minLength = reader.nextInt()
                PropertyKeys.MAX_LENGTH -> propertyHolder.maxLength = reader.nextInt()
                PropertyKeys.MIN_ITEMS -> propertyHolder.minItems = reader.nextInt()
                PropertyKeys.MAX_ITEMS -> propertyHolder.maxItems = reader.nextInt()
                PropertyKeys.MIN_PROPERTIES -> propertyHolder.minProperties = reader.nextInt()
                PropertyKeys.MAX_PROPERTIES -> propertyHolder.maxProperties = reader.nextInt()
                null -> reader.skipNameAndValue()
            }
        }
        val typeEnum = enumJsonAdapter.fromJsonValue(type) ?: throw IllegalStateException("Unexpected null enum value")
        return typeEnum.buildProperty(propertyHolder)
    }

    override fun toJson(writer: JsonWriter, value: Properties?) {
        TODO("not implemented")
    }
}
