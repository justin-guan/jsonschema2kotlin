package com.jsonschema2kotlin.sample

import com.jsonschema2kotlin.parser.toJsonSchema
import java.io.File
import java.io.FileNotFoundException

class Main

fun main() {
    val resourceUrl = Main::class.java.classLoader.getResource("sample.schema.json")
        ?: throw FileNotFoundException("Schema not found")
    val file = File(resourceUrl.toURI())
    val jsonSchema = file.toJsonSchema()
    println(jsonSchema)
//    println(jsonSchema.toJsonString())
}
