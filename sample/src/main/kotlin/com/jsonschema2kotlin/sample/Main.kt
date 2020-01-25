package com.jsonschema2kotlin.sample

import com.jsonschema2kotlin.parser.parseSchema
import java.io.FileNotFoundException

class Main

fun main() {
    val stream = Main::class.java.classLoader.getResourceAsStream("sample.schema.json")
        ?: throw FileNotFoundException("Schema not found")
    val jsonSchema = parseSchema(stream)
    jsonSchema.properties
    println(jsonSchema)
}
