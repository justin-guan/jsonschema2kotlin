package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.model.Properties
import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Type
import com.jsonschema2kotlin.parser.readObject
import com.jsonschema2kotlin.parser.skipNameAndValue
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.adapters.EnumJsonAdapter

private val enumJsonAdapter = EnumJsonAdapter.create(Type::class.java)

internal class PropertiesJsonAdapter : JsonAdapter<Properties>() {
    override fun fromJson(reader: JsonReader): Properties {
        val propertyMap = mutableMapOf<String, Property>()
        reader.readObject {
            val name = reader.nextName()
            var type: String? = null
            var description: String? = null
            reader.readObject {
                when (reader.selectName(PropertiesKeyNames.jsonReaderOptions())) {
                    PropertiesKeyNames.TYPE.ordinal -> type = reader.nextString()
                    PropertiesKeyNames.DESCRIPTION.ordinal -> description = reader.nextString()
                    else -> reader.skipNameAndValue()
                }
            }
            val t = enumJsonAdapter.fromJsonValue(type)
            propertyMap[name] = Property(t!!, description!!)
        }
        return Properties(propertyMap)
    }

    override fun toJson(writer: JsonWriter, value: Properties?) {
        TODO("not implemented")
    }
}