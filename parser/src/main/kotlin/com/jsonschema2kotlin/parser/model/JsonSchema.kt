package com.jsonschema2kotlin.parser.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonSchema(
    @Json(name = "\$id")
    val id: String? = null,
    @Json(name = "\$schema")
    val schema: String,
    @Json(name = "type")
    private val type: RootType,
    @Json(name = "title")
    override val title: String? = null,
    @Json(name = "description")
    override val description: String? = null,
    @Json(name = "properties")
    override val properties: Properties = Properties(emptyMap()),
    @Json(name = "required")
    override val required: List<String> = emptyList(),
    @Json(name = "minProperties")
    override val minProperties: Int? = null,
    @Json(name = "maxProperties")
    override val maxProperties: Int? = null
) : IObjectProperty

/**
 * Only object is supported as the root type, otherwise the schema would be equal to a primitive type or array
 */
enum class RootType {
    @Json(name = "object")
    OBJECT
}
