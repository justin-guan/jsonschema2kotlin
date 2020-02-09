package com.jsonschema2kotlin.sample

import com.jsonschema2kotlin.parser.toJsonSchema
import com.jsonschema2kotlin.parser.toJsonString
import java.io.File

fun main() {
    val file = File("sample/src/main/resources")
    val jsonSchema = file.toJsonSchema()
    println(jsonSchema)
    println(jsonSchema.toJsonString())
}
