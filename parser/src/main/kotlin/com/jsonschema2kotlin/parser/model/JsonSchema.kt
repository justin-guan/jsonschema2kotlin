package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonSchema(
    @Json(name = "\$id")
    val id: String? = null,
    @Json(name = "\$schema")
    val schema: String,
    private val type: RootType,
    val definitions: Map<String, Definition>?,
    override val title: String?,
    override val description: String?,
    override val required: List<String>,
    override val properties: Map<String, Property<*>>,
    override val minProperties: Int?,
    override val maxProperties: Int?
) : IObjectProperty

/**
 * Only object is supported as the root type, otherwise the schema would be equal to a primitive type or array
 */
enum class RootType {
    @Json(name = "object")
    OBJECT
}

sealed class Property<T>(@Json(name = "type") val type: Type) : IProperty, Reference {

    data class NullProperty(
        override val title: String?,
        override val description: String?,
        override val enum: Set<Nothing?>?,
        @Json(name = "\$ref")
        override val ref: String?
    ) : Property<Nothing>(Type.NULL), INullProperty, Enumerated<Nothing>

    data class StringProperty(
        override val title: String?,
        override val description: String?,
        override val minLength: Int?,
        override val maxLength: Int?,
        override val enum: Set<String?>?,
        @Json(name = "\$ref")
        override val ref: String?
    ) : Property<String>(Type.STRING), IStringProperty, Enumerated<String>

    data class NumberProperty(
        override val title: String?,
        override val description: String?,
        override val minimum: Double?,
        override val maximum: Double?,
        override val exclusiveMinimum: Double?,
        override val exclusiveMaximum: Double?,
        override val enum: Set<Double?>?,
        @Json(name = "\$ref")
        override val ref: String?
    ) : Property<Double>(Type.NUMBER), INumberProperty<Double>, Enumerated<Double>

    data class IntegerProperty(
        override val title: String?,
        override val description: String?,
        override val minimum: Long?,
        override val maximum: Long?,
        override val exclusiveMinimum: Long?,
        override val exclusiveMaximum: Long?,
        override val enum: Set<Long?>?,
        @Json(name = "\$ref")
        override val ref: String?
    ) : Property<Long>(Type.INTEGER), INumberProperty<Long>, Enumerated<Long>

    data class BooleanProperty(
        override val title: String?,
        override val description: String?,
        override val enum: Set<Boolean?>?,
        @Json(name = "\$ref")
        override val ref: String?
    ) : Property<Boolean>(Type.BOOLEAN), IBooleanProperty, Enumerated<Boolean>

    data class ArrayProperty(
        override val title: String?,
        override val description: String?,
        override val minItems: Int?,
        override val maxItems: Int?,
        override val items: List<Property<*>>?,
        override val uniqueItems: Boolean?,
        override val enum: Set<List<Any?>?>?,
        @Json(name = "\$ref")
        override val ref: String?
    ) : Property<List<Any?>>(Type.ARRAY), IArrayProperty, Enumerated<List<Any?>>

    data class ObjectProperty(
        override val title: String?,
        override val description: String?,
        override val required: List<String>?,
        override val properties: Map<String, Property<*>>?,
        override val minProperties: Int?,
        override val maxProperties: Int?,
        override val enum: Set<Map<String, Any?>?>?,
        @Json(name = "\$ref")
        override val ref: String?
    ) : Property<Map<String, Any?>>(Type.OBJECT), IObjectProperty, Enumerated<Map<String, Any?>>
}
