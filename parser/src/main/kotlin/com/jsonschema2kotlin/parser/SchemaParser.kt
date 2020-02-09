package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.JsonSchema
import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Type
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.InputStream

private val moshi = Moshi.Builder()
    .add(
        PolymorphicJsonAdapterFactory.of(Property::class.java, "type")
            .withSubtype(Property.NullProperty::class.java, Type.NULL.value)
            .withSubtype(Property.StringProperty::class.java, Type.STRING.value)
            .withSubtype(Property.NumberProperty::class.java, Type.NUMBER.value)
            .withSubtype(Property.IntegerProperty::class.java, Type.INTEGER.value)
            .withSubtype(Property.BooleanProperty::class.java, Type.BOOLEAN.value)
            .withSubtype(Property.ArrayProperty::class.java, Type.ARRAY.value)
            .withSubtype(Property.ObjectProperty::class.java, Type.OBJECT.value)
    )
    .add(Type::class.java, EnumJsonAdapter.create(Type::class.java).nullSafe())
    .add(KotlinJsonAdapterFactory())
    .build()

fun InputStream.toJsonSchema(): JsonSchema {
    val adapter: JsonAdapter<JsonSchema> = moshi.adapter(JsonSchema::class.java)
    val json = this.bufferedReader().use { it.readText() }
    return adapter.fromJson(json)?.mergeDefinitionsIntoProperties() ?: throw JsonDataException("Error parsing json")
}

fun JsonSchema.mergeDefinitionsIntoProperties(): JsonSchema {
    return mergeDefinitions(MergeStrategy.Merge)
}

fun File.toJsonSchema(): Map<String, JsonSchema> {
    val schemas = mutableMapOf<String, JsonSchema>()
    if (this.isDirectory) {
        walk().forEach {
            val jsonSchema = it.inputStream().toJsonSchema()
            schemas[jsonSchema.id] = jsonSchema
        }
    } else {
        val jsonSchema = this.inputStream().toJsonSchema()
        schemas[jsonSchema.id] = jsonSchema
    }
    return schemas
}

fun JsonSchema.toJsonString(): String {
    val adapter: JsonAdapter<JsonSchema> = moshi.adapter(JsonSchema::class.java)
    return adapter.toJson(this.unmergeDefinitionsFromProperties())
}

fun Map<String, JsonSchema>.toJsonString(): String {
    val adapter: JsonAdapter<Map<String, JsonSchema>> = moshi.adapter<Map<String, JsonSchema>>(Map::class.java)
    return adapter.toJson(this.mapValues { it.value.unmergeDefinitionsFromProperties() })
}

fun JsonSchema.unmergeDefinitionsFromProperties(): JsonSchema {
    return mergeDefinitions(MergeStrategy.Unmerge)
}

private fun JsonSchema.mergeDefinitions(mergeStrategy: MergeStrategy): JsonSchema {
    val definitions = definitions ?: emptyMap()
    return copy(properties = properties?.mergeDefinitions(definitions, mergeStrategy))
}
