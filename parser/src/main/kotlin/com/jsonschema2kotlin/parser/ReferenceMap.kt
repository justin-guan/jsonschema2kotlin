package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Definition
import com.jsonschema2kotlin.parser.model.Reference

class ReferenceMap(private val definitions: Map<String, Map<String, Definition>?>) {
    fun getDefinition(reference: Reference?): Definition? = reference?.let { ref ->
        val referenceFile = ref.fileName
        val definitions = definitions[referenceFile]
            ?: throw NoSuchElementException("Reference is referencing a file that does not exist")
        val referenceKey = ref.definitionPath
        return definitions[referenceKey]
            ?: throw NoSuchElementException("Reference is referencing a definition that doesn't exist")
    }
}
