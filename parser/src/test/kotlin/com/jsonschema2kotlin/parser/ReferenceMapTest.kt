package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Definition
import com.jsonschema2kotlin.parser.model.Reference
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.hamcrest.CoreMatchers.`is` as Is

class ReferenceMapTest {

    private val fileName = "filename.file"
    private val referenceKey = "refkey"
    private val expectedDefinition = mockk<Definition>()
    private val definitions = mapOf(fileName to mapOf(referenceKey to expectedDefinition))
    private val referenceMap = ReferenceMap(definitions)

    @Test
    fun `should find a definition that exists in a file`() {
        val reference = buildReference(fileName, referenceKey)

        val definition = referenceMap.getDefinition(reference)

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
        val reference = buildReference("", referenceKey)
        assertThrows<NoSuchElementException> {
            referenceMap.getDefinition(reference)
        }
    }

    private fun buildReference(fileName: String, referenceKey: String) =
        Reference("$fileName#/definitions/$referenceKey")
}
