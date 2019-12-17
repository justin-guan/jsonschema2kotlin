package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.jsonadapter.PropertiesJsonAdapter
import com.jsonschema2kotlin.parser.model.Properties
import com.jsonschema2kotlin.parser.model.Root
import com.jsonschema2kotlin.parser.model.RootType
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStream

private val moshi = Moshi.Builder()
    .add(Properties::class.java, PropertiesJsonAdapter())
    .add(RootType::class.java, EnumJsonAdapter.create(RootType::class.java))
    .add(KotlinJsonAdapterFactory())
    .build()

fun parseSchema(schema: InputStream): Root {
    val adapter: JsonAdapter<Root> = moshi.adapter(Root::class.java)
    val json = schema.bufferedReader().use { it.readText() }
    return adapter.fromJson(json)!!
}