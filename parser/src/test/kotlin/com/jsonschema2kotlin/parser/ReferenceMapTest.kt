package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Definition
import com.jsonschema2kotlin.parser.model.Reference
import io.mockk.mockk
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.hamcrest.CoreMatchers.`is` as Is

class ReferenceMapTest {

    private val fileName = "filename.file"
    private val referenceKey = "refkey"
    private val expectedDefinition = mockk<Definition>()
    private val definitions = mapOf(fileName to mapOf(referenceKey to expectedDefinition))
    private val referenceMap = ReferenceMap(fileName, definitions)

    @Test
    fun `should find a definition that exists in another file`() {
        val reference = buildReference(fileName, referenceKey)

        val definition = referenceMap.getDefinition(reference)

        assertThat(reference.ref, not(startsWith("#")))
        assertThat(definition, Is(expectedDefinition))
    }

    @Test
    fun `should find a definition with a local file reference`() {
        val reference = buildReference("", referenceKey)

        val definition = referenceMap.getDefinition(reference)

        assertThat(reference.ref, startsWith("#"))
        assertThat(definition, Is(expectedDefinition))
    }

    @Test
    fun `should fail to find a definition due to an unknown reference key`() {
        val reference = buildReference(fileName, "")
        assertThrows<NoSuchElementException> {
            referenceMap.getDefinition(reference)
        }
    }

    @Test
    fun `should fail to find a definition due to an unknown file name`() {
        val reference = buildReference(fileName + "unknown", referenceKey)
        assertThrows<NoSuchElementException> {
            referenceMap.getDefinition(reference)
        }
    }

    private fun buildReference(fileName: String, referenceKey: String) =
        Reference("$fileName#/definitions/$referenceKey")
}
