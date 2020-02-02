package com.jsonschema2kotlin.parser

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
            .withSubtype(Property.ObjectProperty::class.java, Type.OBJECT.value))
    .add(Type::class.java, EnumJsonAdapter.create(Type::class.java)
        .nullSafe())
    .add(KotlinJsonAdapterFactory())
    .build()

fun InputStream.toJsonSchema(): JsonSchema {
    val adapter: JsonAdapter<JsonSchema> = moshi.adapter(JsonSchema::class.java)
    val json = this.bufferedReader().use { it.readText() }
    return adapter.fromJson(json)!!
}

fun JsonSchema.toJsonString(): String {
    val adapter: JsonAdapter<JsonSchema> = moshi.adapter(JsonSchema::class.java)
    return adapter.toJson(this)
}
