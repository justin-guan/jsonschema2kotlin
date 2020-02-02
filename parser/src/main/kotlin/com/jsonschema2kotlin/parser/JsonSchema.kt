package com.jsonschema2kotlin.parser

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonSchema(
    @Json(name = "\$id")
    val id: String? = null,
    @Json(name = "\$schema")
    val schema: String,
    private val type: RootType,
    override val title: String?,
    override val description: String?,
    override val required: List<String>,
    override val properties: Map<String, Property<*>>,
    override val minProperties: Int?,
    override val maxProperties: Int?,
    override val enum: Set<Map<String, Any?>?>?
) : IObjectProperty

/**
 * Only object is supported as the root type, otherwise the schema would be equal to a primitive type or array
 */
enum class RootType {
    @Json(name = "object")
    OBJECT
}

sealed class Property<T>(@Json(name = "type") val type: Type) : IProperty<T> {
    data class NullProperty(
        override val title: String?,
        override val description: String?,
        override val enum: Set<Nothing?>?
    ) : Property<Nothing>(Type.NULL), INullProperty

    data class StringProperty(
        override val description: String?,
        override val title: String?,
        override val minLength: Int?,
        override val maxLength: Int?,
        override val enum: Set<String?>?
    ) : Property<String>(Type.STRING), IStringProperty

    data class NumberProperty(
        override val description: String?,
        override val title: String?,
        override val minimum: Double?,
        override val maximum: Double?,
        override val exclusiveMinimum: Double?,
        override val exclusiveMaximum: Double?,
        override val enum: Set<Double?>?
    ) : Property<Double>(Type.NUMBER), INumberProperty<Double>

    data class IntegerProperty(
        override val description: String?,
        override val title: String?,
        override val minimum: Long?,
        override val maximum: Long?,
        override val exclusiveMinimum: Long?,
        override val exclusiveMaximum: Long?,
        override val enum: Set<Long?>?
    ) : Property<Long>(Type.INTEGER), INumberProperty<Long>

    data class BooleanProperty(
        override val description: String?,
        override val title: String?,
        override val enum: Set<Boolean?>?
    ) : Property<Boolean>(Type.BOOLEAN), IBooleanProperty

    data class ArrayProperty(
        override val description: String?,
        override val title: String?,
        override val minItems: Int?,
        override val maxItems: Int?,
        override val items: List<Property<*>>?,
        override val uniqueItems: Boolean?,
        override val enum: Set<List<Any?>?>?
    ) : Property<List<Any?>>(Type.ARRAY), IArrayProperty

    data class ObjectProperty(
        override val description: String?,
        override val title: String?,
        override val required: List<String>?,
        override val properties: Map<String, Property<*>>?,
        override val minProperties: Int?,
        override val maxProperties: Int?,
        override val enum: Set<Map<String, Any?>?>?
    ) : Property<Map<String, Any?>>(Type.OBJECT), IObjectProperty
}

enum class Type(val value: String) {
    NULL("null"),
    STRING("string"),
    NUMBER("number"),
    INTEGER("integer"),
    BOOLEAN("boolean"),
    ARRAY("array"),
    OBJECT("object")
}

