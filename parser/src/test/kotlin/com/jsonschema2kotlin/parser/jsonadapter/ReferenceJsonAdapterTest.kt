package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.model.Reference
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as Is

class ReferenceJsonAdapterTest {

    private val adapter = ReferenceJsonAdapter()

    @Test
    fun `should read local reference from string`() {
        val expected = "#/definitions/reference"

        val reference = adapter.fromJson(expected.wrapQuotes())

        val expectedReference = Reference(expected)
        assertThat(reference, Is(expectedReference))
    }

    @Test
    fun `should read file reference from string`() {
        val expected = "file.schema.json#/definitions/reference"

        val reference = adapter.fromJson(expected.wrapQuotes())

        val expectedReference = Reference(expected)
        assertThat(reference, Is(expectedReference))
    }

    @Test
    fun `should write local reference to string`() {
        val expected = "#/definitions/reference"
        val reference = Reference(expected)

        val actual = adapter.toJson(reference)

        assertThat(actual, Is(expected.wrapQuotes()))
    }

    @Test
    fun `should write file reference to string`() {
        val expected = "file.schema.json#/definitions/reference"
        val reference = Reference(expected)

        val actual = adapter.toJson(reference)

        assertThat(actual, Is(expected.wrapQuotes()))
    }
}

private fun String.wrapQuotes() = "\"$this\""
