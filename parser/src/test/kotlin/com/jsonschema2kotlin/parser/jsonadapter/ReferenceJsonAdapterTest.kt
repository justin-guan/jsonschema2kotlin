package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.model.Reference
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as Is

class ReferenceJsonAdapterTest {

    private val adapter = ReferenceJsonAdapter()

    @Test
    fun `should read reference from string`() {
        val expected = "reference"

        val reference = adapter.fromJson(expected.wrapQuotes())

        val expectedReference = Reference(expected)
        assertThat(reference, Is(expectedReference))
    }

    @Test
    fun `should write reference to string`() {
        val expected = "reference"
        val reference = Reference(expected)

        val expectedReference = adapter.toJson(reference)

        assertThat(expectedReference, Is(expected.wrapQuotes()))
    }
}

private fun String.wrapQuotes() = "\"$this\""
