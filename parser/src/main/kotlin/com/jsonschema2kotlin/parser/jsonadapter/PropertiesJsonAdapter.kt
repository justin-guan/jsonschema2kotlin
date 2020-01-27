package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.jsonadapter.PropertyKeys.Companion.ordinalToPropertyKey
import com.jsonschema2kotlin.parser.model.*
import com.jsonschema2kotlin.parser.readArray
import com.jsonschema2kotlin.parser.readObject
import com.jsonschema2kotlin.parser.skipNameAndValue
import com.jsonschema2kotlin.parser.utils.Do
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
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
    var enums: Set<Any?>? = null
    val propertyHolder = PropertyHolder()
    this.readObject {
        Do exhaustive when (this.selectName(PropertyKeys.jsonReaderOptions()).ordinalToPropertyKey()) {
            PropertyKeys.TYPE -> type = this.nextString()
            PropertyKeys.TITLE -> propertyHolder.title = this.nextString()
            PropertyKeys.DESCRIPTION -> propertyHolder.description = this.nextString()
            PropertyKeys.REQUIRED -> propertyHolder.required = this.readRequiredArray()
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
            PropertyKeys.ITEMS -> propertyHolder.items = this.readArrayItems()
            PropertyKeys.UNIQUE_ITEMS -> propertyHolder.uniqueItems = this.nextBoolean()
            PropertyKeys.ENUM -> enums = this.readEnum()
            null -> this.skipNameAndValue()
        }
    }
    val t = requireNotNull(type) { "type must be specified for every property in schema" }
    val typeEnum = enumJsonAdapter.fromJsonValue(t) ?: throw IllegalStateException("Unexpected null enum value")
    propertyHolder.enums = enums?.toEnumeratedType(typeEnum)
    return typeEnum.buildProperty(propertyHolder)
}

private fun JsonReader.readEnum(): Set<Any?> {
    val enums = mutableSetOf<Any?>()
    readArray {
        val value = when (this.peek()) {
            JsonReader.Token.STRING -> this.nextString()
            JsonReader.Token.NUMBER -> this.nextDouble()
            JsonReader.Token.BOOLEAN -> this.nextBoolean()
            JsonReader.Token.NULL -> this.nextNull()
            else -> throw JsonDataException("Malformed enum declaration in schema")
        }
        enums.add(value)
    }
    return enums
}

private fun JsonReader.readRequiredArray(): List<String> {
    val requiredList = mutableListOf<String>()
    this.readArray { requiredList.add(this.nextString()) }
    return requiredList
}

private fun JsonReader.readArrayItems(): List<Property> {
    val nextToken = this.peek()
    val itemList = mutableListOf<Property>()
    when (nextToken) {
        JsonReader.Token.BEGIN_OBJECT -> itemList.add(this.readProperty())
        JsonReader.Token.BEGIN_ARRAY -> this.readArray { itemList.add(this.readProperty()) }
        else -> throw JsonDataException("Array item should be defined as a property or array of properties")
    }
    return itemList
}

private fun JsonWriter.writeProperties(value: Properties) {
    beginObject()
    value.let {
        it.forEach { propName, propValue ->
            writeKeyAndProperty(propName, propValue)
        }
    }
    endObject()
}

private fun JsonWriter.writeKeyAndProperty(propertyName: String, property: Property) {
    name(propertyName)
    writeProperty(property)
}

private fun JsonWriter.writeProperty(property: Property) {
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
    name(PropertyKeys.PROPERTIES.value)
    writeProperties(property.properties)
}

private fun JsonWriter.writeArrayProperty(property: Property.ArrayProperty) {
    writeType(Type.ARRAY)
    writeKeyValue(PropertyKeys.MIN_ITEMS, property.minItems)
    writeKeyValue(PropertyKeys.MAX_ITEMS, property.maxItems)
    writeKeyValue(PropertyKeys.UNIQUE_ITEMS, property.uniqueItems)
    writePropertyArray(PropertyKeys.ITEMS, property.items)
}

private fun JsonWriter.writePropertyArray(key: PropertyKeys, properties: List<Property>) {
    if (properties.isNotEmpty()) {
        name(key.value)
        beginArray()
        properties.forEach { writeProperty(it) }
        endArray()
    }
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

private fun JsonWriter.writeKeyValue(key: PropertyKeys, value: Boolean?) {
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
