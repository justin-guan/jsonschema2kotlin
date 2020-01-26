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
        return reader.readProperties()
    }

    override fun toJson(writer: JsonWriter, value: Properties?) {
        value?.let { writer.writeProperties(it) }
    }
}

private fun JsonReader.readProperties(): Properties {
    val propertyMap = mutableMapOf<String, Property>()
    this.readObject {
        propertyMap[this.nextName()] = readProperty()
    }
    return Properties(propertyMap)
}

private fun JsonReader.readProperty(): Property {
    var type: String? = null
    val propertyHolder = PropertyHolder()
    this.readObject {
        Do exhaustive when (this.selectName(PropertyKeys.jsonReaderOptions()).ordinalToPropertyKey()) {
            PropertyKeys.TYPE -> type = this.nextString()
            PropertyKeys.TITLE -> propertyHolder.title = this.nextString()
            PropertyKeys.DESCRIPTION -> propertyHolder.description = this.nextString()
            PropertyKeys.REQUIRED -> this.readArray { propertyHolder.required.add(this.nextString()) }
            PropertyKeys.PROPERTIES -> propertyHolder.properties = readProperties()
            PropertyKeys.MINIMUM -> propertyHolder.minimum = this.nextDouble()
            PropertyKeys.MAXIMUM -> propertyHolder.maximum = this.nextDouble()
            PropertyKeys.EXCLUSIVE_MIN -> propertyHolder.exclusiveMinimum = this.nextDouble()
            PropertyKeys.EXCLUSIVE_MAX -> propertyHolder.exclusiveMaximum = this.nextDouble()
            PropertyKeys.MIN_LENGTH -> propertyHolder.minLength = this.nextInt()
            PropertyKeys.MAX_LENGTH -> propertyHolder.maxLength = this.nextInt()
            PropertyKeys.MIN_ITEMS -> propertyHolder.minItems = this.nextInt()
            PropertyKeys.MAX_ITEMS -> propertyHolder.maxItems = this.nextInt()
            PropertyKeys.MIN_PROPERTIES -> propertyHolder.minProperties = this.nextInt()
            PropertyKeys.MAX_PROPERTIES -> propertyHolder.maxProperties = this.nextInt()
            null -> this.skipNameAndValue()
        }
    }
    val typeEnum = enumJsonAdapter.fromJsonValue(type) ?: throw IllegalStateException("Unexpected null enum value")
    return typeEnum.buildProperty(propertyHolder)
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
    Do exhaustive when (property) {
        is Property.StringProperty -> this.writeStringProperty(property)
        is Property.NumberProperty -> this.writeNumberProperty(property)
        is Property.IntegerProperty -> this.writeIntegerProperty(property)
        is Property.ObjectProperty -> this.writeObjectProperty(property)
        is Property.ArrayProperty -> this.writeArrayProperty(property)
        is Property.BooleanProperty -> this.writeBooleanProperty()
        is Property.NullProperty -> this.writeNullProperty()
    }
    endObject()
}

private fun JsonWriter.writeType(type: Type) {
    writeKeyValue(PropertyKeys.TYPE, type.value)
}

private fun JsonWriter.writeStringProperty(property: Property.StringProperty) {
    writeType(Type.STRING)
    writeKeyValue(PropertyKeys.MIN_LENGTH, property.minLength)
    writeKeyValue(PropertyKeys.MAX_LENGTH, property.maxLength)
}

private fun JsonWriter.writeNumberProperty(property: Property.NumberProperty) {
    writeType(Type.NUMBER)
    writeKeyValue(PropertyKeys.MINIMUM, property.minimum)
    writeKeyValue(PropertyKeys.MAXIMUM, property.maximum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MIN, property.exclusiveMinimum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MAX, property.exclusiveMaximum)
}

private fun JsonWriter.writeIntegerProperty(property: Property.IntegerProperty) {
    writeType(Type.INTEGER)
    writeKeyValue(PropertyKeys.MINIMUM, property.minimum)
    writeKeyValue(PropertyKeys.MAXIMUM, property.maximum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MIN, property.exclusiveMinimum)
    writeKeyValue(PropertyKeys.EXCLUSIVE_MAX, property.exclusiveMaximum)
}

private fun JsonWriter.writeObjectProperty(property: Property.ObjectProperty) {
    writeType(Type.OBJECT)
    writeKeyValue(PropertyKeys.REQUIRED, property.required)
    writeKeyValue(PropertyKeys.MIN_PROPERTIES, property.minProperties)
    writeKeyValue(PropertyKeys.MAX_PROPERTIES, property.maxProperties)
    name("properties")
    writeProperties(property.properties)
}

private fun JsonWriter.writeArrayProperty(property: Property.ArrayProperty) {
    writeType(Type.ARRAY)
    writeKeyValue(PropertyKeys.MIN_ITEMS, property.minItems)
    writeKeyValue(PropertyKeys.MAX_ITEMS, property.maxItems)
}

private fun JsonWriter.writeBooleanProperty() {
    writeType(Type.BOOLEAN)
}

private fun JsonWriter.writeNullProperty() {
    writeType(Type.NULL)
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
