package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Definition(
    override val title: String?,
    override val description: String?,
    override val minLength: Int?,
    override val maxLength: Int?,
    override val minimum: Double?,
    override val maximum: Double?,
    override val exclusiveMinimum: Double?,
    override val exclusiveMaximum: Double?,
    override val minItems: Int?,
    override val maxItems: Int?,
    override val items: List<Property<*>>?,
    override val uniqueItems: Boolean?,
    override val required: List<String>?,
    override val properties: Map<String, Property<*>>?,
    override val minProperties: Int?,
    override val maxProperties: Int?,
    override val enum: Set<Any?>?,
    val type: Type?
) : INullProperty,
    IBooleanProperty,
    IStringProperty,
    INumberProperty<Double>,
    IArrayProperty,
    IObjectProperty,
    Enumerated<Any?>
