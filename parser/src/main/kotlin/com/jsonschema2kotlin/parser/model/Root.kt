package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Root(
    @Json(name = "\$id")
    val id: String? = null,
    @Json(name = "\$schema")
    val schema: String,
    @Json(name = "title")
    val title: String? = null,
    @Json(name = "type")
    val type: RootType,
    @Json(name = "properties")
    val properties: Properties = Properties(emptyMap())
)

/**
 * Only object is supported as the root type, otherwise the schema would be equal to a primitive type or array
 */
enum class RootType {
    @Json(name = "object")
    OBJECT
}
