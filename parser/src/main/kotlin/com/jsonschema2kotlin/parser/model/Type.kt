package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.Json

private const val TYPE_NULL = "null"
private const val TYPE_STRING = "string"
private const val TYPE_NUMBER = "number"
private const val TYPE_INTEGER = "integer"
private const val TYPE_BOOLEAN = "boolean"
private const val TYPE_ARRAY = "array"
private const val TYPE_OBJECT = "object"

enum class Type(val value: String?) {
    @Json(name = TYPE_NULL)
    NULL(TYPE_NULL),
    @Json(name = TYPE_STRING)
    STRING(TYPE_STRING),
    @Json(name = TYPE_NUMBER)
    NUMBER(TYPE_NUMBER),
    @Json(name = TYPE_INTEGER)
    INTEGER(TYPE_INTEGER),
    @Json(name = TYPE_BOOLEAN)
    BOOLEAN(TYPE_BOOLEAN),
    @Json(name = TYPE_ARRAY)
    ARRAY(TYPE_ARRAY),
    @Json(name = TYPE_OBJECT)
    OBJECT(TYPE_OBJECT)
}

interface Typed {
    val type: Type
}
