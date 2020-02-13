package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Definition
import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Reference
import io.mockk.mockk
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.hamcrest.CoreMatchers.`is` as Is

class PropertyDefinitionMergerKtTest {

    private val referenceFile = "references.schema.json"
    private val referenceKey = "refkey"
    private val definition = Definition(
        title = "title",
        description = "description",
        minLength = 0,
        maxLength = 100,
        minimum = 0.0,
        maximum = 100.0,
        exclusiveMinimum = 0.0,
        exclusiveMaximum = 100.0,
        minItems = 0,
        maxItems = 0,
        items = listOf(mockk()),
        uniqueItems = true,
        required = listOf("required"),
        properties = mapOf("required" to mockk()),
        minProperties = 0,
        maxProperties = 100,
        enum = null,
        type = null
    )
    private val referenceMap = ReferenceMap(
        referenceFile,
        mapOf(referenceFile to mapOf(referenceKey to definition))
    )

    @Test
    fun `should merge null property with definition`() {
        val key = ""
        val property = Property.NullProperty(
            title = null,
            description = null,
            enum = null,
            ref = Reference("$referenceFile#/definitions/$referenceKey")
        )

        val properties = mapOf(key to property).mergeDefinitions(referenceMap, MergeStrategy.Merge)

        assertThat(properties.size, Is(1))
        val merged = properties[key] ?: error("Merged property not found")
        assertThat(merged, instanceOf(Property.NullProperty::class.java))
        merged as Property.NullProperty
        assertThat(merged.title, Is(definition.title))
        assertThat(merged.description, Is(definition.description))
        assertThat(merged.enum, Is(definition.enum))
        assertThat(merged.ref, Is(property.ref))
    }
}
