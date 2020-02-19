package com.jsonschema2kotlin.sample

import com.jsonschema2kotlin.codegen.generateKotlinFiles
import java.io.File

fun main() {
    val file = File("sample/src/main/resources")
    file.generateKotlinFiles("com.jsonschema.generated")
}
