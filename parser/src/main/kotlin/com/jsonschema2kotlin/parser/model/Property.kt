package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.Json

sealed class Property : IProperty {
    data class StringProperty internal constructor(
        private val property: IStringProperty
    ) : Property(), IStringProperty by property

    data class NumberProperty internal constructor(
        private val property: INumberProperty<Double>
    ) : Property(), INumberProperty<Double> by property

    data class IntegerProperty internal constructor(
        private val property: INumberProperty<Long>
    ) : Property(), INumberProperty<Long> by property

    data class ObjectProperty internal constructor(
        private val property: IObjectProperty
    ) : Property(), IObjectProperty by property

    data class ArrayProperty internal constructor(
        private val property: IArrayProperty
    ) : Property(), IArrayProperty by property

    data class BooleanProperty internal constructor(
        private val property: IBooleanProperty
    ) : Property(), IBooleanProperty by property

    data class NullProperty internal constructor(
        private val property: INullProperty
    ) : Property(), INullProperty by property
}

private const val TYPE_STRING = "string"
private const val TYPE_NUMBER = "number"
private const val TYPE_INTEGER = "integer"
private const val TYPE_OBJECT = "object"
private const val TYPE_ARRAY = "array"
private const val TYPE_BOOLEAN = "boolean"
private const val TYPE_NULL = "null"

internal enum class Type(val value: String) {
    @Json(name = TYPE_STRING)
    STRING(TYPE_STRING),
    @Json(name = TYPE_NUMBER)
    NUMBER(TYPE_NUMBER),
    @Json(name = TYPE_INTEGER)
    INTEGER(TYPE_INTEGER),
    @Json(name = TYPE_OBJECT)
    OBJECT(TYPE_OBJECT),
    @Json(name = TYPE_ARRAY)
    ARRAY(TYPE_ARRAY),
    @Json(name = TYPE_BOOLEAN)
    BOOLEAN(TYPE_BOOLEAN),
    @Json(name = TYPE_NULL)
    NULL(TYPE_NULL)
}
