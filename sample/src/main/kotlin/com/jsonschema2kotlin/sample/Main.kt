package com.jsonschema2kotlin.sample

import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.toJsonSchema
import com.jsonschema2kotlin.parser.toJsonString
import java.io.FileNotFoundException

class Main

fun main() {
    val stream = Main::class.java.classLoader.getResourceAsStream("sample.schema.json")
        ?: throw FileNotFoundException("Schema not found")
    val jsonSchema = stream.toJsonSchema()
    println(jsonSchema)
    println(jsonSchema.toJsonString())

    val property = jsonSchema.properties["property"] as Property.IntegerProperty
    property.enums.forEach { println(it) }
}
