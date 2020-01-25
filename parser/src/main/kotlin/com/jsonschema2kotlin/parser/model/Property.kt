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

internal enum class Type {
    @Json(name = "string")
    STRING,
    @Json(name = "number")
    NUMBER,
    @Json(name = "integer")
    INTEGER,
    @Json(name = "object")
    OBJECT,
    @Json(name = "array")
    ARRAY,
    @Json(name = "boolean")
    BOOLEAN,
    @Json(name = "null")
    NULL
}
