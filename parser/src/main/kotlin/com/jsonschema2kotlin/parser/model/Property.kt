package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.Json

sealed class Property {
    abstract val title: String?
    abstract val description: String?

    data class StringProperty internal constructor(
        override val title: String?,
        override val description: String?
    ) : Property()

    data class NumberProperty internal constructor(
        override val title: String?,
        override val description: String?
    ) : Property()

    data class IntegerProperty internal constructor(
        override val title: String?,
        override val description: String?
    ) : Property()

    data class ObjectProperty internal constructor(
        override val title: String?,
        override val description: String?,
        val properties: Properties
    ) : Property()

    data class ArrayProperty internal constructor(
        override val title: String?,
        override val description: String?
    ) : Property()

    data class BooleanProperty internal constructor(
        override val title: String?,
        override val description: String?
    ) : Property()

    data class NullProperty internal constructor(
        override val title: String?,
        override val description: String?
    ) : Property()
}

enum class Type {
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

internal fun Type.buildProperty(title: String?, description: String?, subProperties: Properties?): Property = when (this) {
    Type.STRING -> Property.StringProperty(
        title = title,
        description = description
    )
    Type.NUMBER -> Property.NumberProperty(
        title = title,
        description = description
    )
    Type.INTEGER -> Property.IntegerProperty(
        title = title,
        description = description
    )
    Type.OBJECT -> Property.ObjectProperty(
        title = title,
        description = description,
        properties = subProperties ?: Properties()
    )
    Type.ARRAY -> Property.ArrayProperty(
        title = title,
        description = description
    )
    Type.BOOLEAN -> Property.BooleanProperty(
        title = title,
        description = description
    )
    Type.NULL -> Property.NullProperty(
        title = title,
        description = description
    )
}
