package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.jsonadapter.ReferenceJsonAdapter
import com.jsonschema2kotlin.parser.model.JsonSchema
import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Reference
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
    .add(Reference::class.java, ReferenceJsonAdapter())
    .add(KotlinJsonAdapterFactory())
    .build()

fun File.toJsonSchema(): Map<String, JsonSchema> {
    val schemas: Map<String, JsonSchema> = walk()
        .filter { !it.isDirectory }
        .associate { it.name to it.inputStream().toJsonSchema() }
    return schemas.mergeDefinitionsIntoProperties()
}

fun InputStream.toJsonSchema(): JsonSchema {
    val adapter: JsonAdapter<JsonSchema> = moshi.adapter(JsonSchema::class.java)
    val json = this.bufferedReader().use { it.readText() }
    return adapter.fromJson(json) ?: throw JsonDataException("Error parsing json")
}

fun Map<String, JsonSchema>.mergeDefinitionsIntoProperties(): Map<String, JsonSchema> {
    return mergeDefinitionsIntoProperties(MergeStrategy.Merge)
}

fun Map<String, JsonSchema>.toJsonString(): String {
    val adapter: JsonAdapter<Map<String, JsonSchema>> = moshi.adapter<Map<String, JsonSchema>>(Map::class.java)
    return adapter.toJson(this.unmergeDefinitionsIntoProperties())
}

fun Map<String, JsonSchema>.unmergeDefinitionsIntoProperties(): Map<String, JsonSchema> {
    return mergeDefinitionsIntoProperties(MergeStrategy.Unmerge)
}

private fun Map<String, JsonSchema>.mergeDefinitionsIntoProperties(
    mergeStrategy: MergeStrategy
): Map<String, JsonSchema> {
    val definitions = this.mapValues { it.value.definitions }
    return this.mapValues { (filename, jsonSchema) ->
        val referenceMap = ReferenceMap(filename, definitions)
        jsonSchema.copy(properties = jsonSchema.properties?.mergeDefinitions(referenceMap, mergeStrategy))
    }
}
