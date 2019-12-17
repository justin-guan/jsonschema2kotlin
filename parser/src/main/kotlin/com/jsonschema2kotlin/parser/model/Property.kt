package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.Json

data class Property(
    val type: Type?,
    val description: String?
)

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