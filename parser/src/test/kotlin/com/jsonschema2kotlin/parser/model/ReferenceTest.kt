package com.jsonschema2kotlin.parser.model

import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as Is

class ReferenceTest {
    @Test
    fun `should find the definition path for a local reference`() {
        val defPath = "definitionPath"
        val reference = Reference("#/definitions/$defPath")

        assertThat(reference.definitionPath, Is(defPath))
    }

    @Test
    fun `should find the definition path for a file reference`() {
        val defPath = "definitionPath"
        val reference = Reference("file.schema.json#/definitions/$defPath")

        assertThat(reference.definitionPath, Is(defPath))
    }

    @Test
    fun `should find the file name if reference refers to another file`() {
        val referenceLocation = "file.schema.json"
        val reference = Reference("$referenceLocation#/definitions/definitionPath")

        val fileName = reference.fileName("")

        assertThat(fileName, Is(referenceLocation))
    }

    @Test
    fun `should default to a fallback value if reference refers to the local file`() {
        val localReferenceLocation = "localRefFile.schema.json"
        val reference = Reference("#/definitions/definitionPath")

        val fileName = reference.fileName(localReferenceLocation)

        assertThat(fileName, Is(localReferenceLocation))
    }
}
