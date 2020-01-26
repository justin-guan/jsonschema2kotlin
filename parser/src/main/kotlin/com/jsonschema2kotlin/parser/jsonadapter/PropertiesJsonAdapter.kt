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
private typealias NoOp = Unit

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
        value?.let { writer.writeProperties(it) }
    }
}

private fun JsonWriter.writeProperties(value: Properties) {
    beginObject()
    value.let {
        it.forEach { propName, propValue ->
            writeProperty(propName, propValue)
        }
    }
    endObject()
}

private fun JsonWriter.writeProperty(propertyName: String, property: Property) {
    name(propertyName)
    beginObject()
    writeBaseProperty(property)
    Do exhaustive when(property) {
        is Property.StringProperty -> this.writeProperty(property)
        is Property.NumberProperty -> this.writeProperty(property)
        is Property.IntegerProperty -> this.writeProperty(property)
        is Property.ObjectProperty -> this.writeProperty(property)
        is Property.ArrayProperty -> this.writeProperty(property)
        is Property.BooleanProperty -> NoOp
        is Property.NullProperty -> NoOp
    }
    endObject()
}

private fun JsonWriter.writeType(type: Type) {
    writeKeyValue(PropertyKeys.TYPE, type.value)
}

private fun JsonWriter.writeProperty(property: Property.StringProperty) {
    writeType(Type.STRING)
    writeKeyValue(PropertyKeys.MIN_LENGTH, property.minLength)
    writeKeyValue(PropertyKeys.MAX_LENGTH, property.maxLength)
}

private fun JsonWriter.writeProperty(property: Property.NumberProperty) {
    writeType(Type.NUMBER)
    writeKeyValue(PropertyKeys.MINIMUM, property.minimum)
    writeKeyValue(PropertyKeys.MAXIMUM, property.maximum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MIN, property.exclusiveMinimum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MAX, property.exclusiveMaximum)
}

private fun JsonWriter.writeProperty(property: Property.IntegerProperty) {
    writeType(Type.INTEGER)
    writeKeyValue(PropertyKeys.MINIMUM, property.minimum)
    writeKeyValue(PropertyKeys.MAXIMUM, property.maximum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MIN, property.exclusiveMinimum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MAX, property.exclusiveMaximum)
}

private fun JsonWriter.writeProperty(property: Property.ObjectProperty) {
    writeType(Type.OBJECT)
    writeKeyValue(PropertyKeys.REQUIRED, property.required)
    writeKeyValue(PropertyKeys.MIN_PROPERTIES, property.minProperties)
    writeKeyValue(PropertyKeys.MAX_PROPERTIES, property.maxProperties)
    name("properties")
    writeProperties(property.properties)
}

private fun JsonWriter.writeProperty(property: Property.ArrayProperty) {
    writeType(Type.ARRAY)
    writeKeyValue(PropertyKeys.MIN_ITEMS, property.minItems)
    writeKeyValue(PropertyKeys.MAX_ITEMS, property.maxItems)
}

private fun JsonWriter.writeBaseProperty(property: IProperty) {
    writeKeyValue(PropertyKeys.TITLE, property.title)
    writeKeyValue(PropertyKeys.DESCRIPTION, property.description)
}

private fun JsonWriter.writeKeyValue(key: PropertyKeys, value: String?) {
    value?.run {
        name(key.value)
        value(value)
    }
}

private fun JsonWriter.writeKeyValue(key: PropertyKeys, value: Int?) {
    value?.run {
        name(key.value)
        value(value)
    }
}

private fun JsonWriter.writeKeyValue(key: PropertyKeys, value: Double?) {
    value?.run {
        name(key.value)
        value(value)
    }
}

private fun JsonWriter.writeKeyValue(key: PropertyKeys, value: Long?) {
    value?.run {
        name(key.value)
        value(value)
    }
}

private fun JsonWriter.writeKeyValue(key: PropertyKeys, value: List<String>) {
    if (value.isNotEmpty()) {
        name(key.value)
        beginArray()
        value.forEach { value(it) }
        endArray()
    }
}

