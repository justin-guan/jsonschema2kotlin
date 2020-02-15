package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Definition
import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Reference
import io.mockk.mockk
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import org.hamcrest.CoreMatchers.`is` as Is

class PropertyDefinitionMergerKtTest {

    companion object {
        private const val referenceFile = "references.schema.json"
        private const val referenceKey = "refkey"
        private val testReference = Reference("$referenceFile#/definitions/$referenceKey")
        private val nullProperty = Property.NullProperty(
            title = null,
            description = null,
            enum = null,
            ref = null
        )
        private val stringProperty = Property.StringProperty(
            title = null,
            description = null,
            minLength = null,
            maxLength = null,
            enum = null,
            ref = null
        )
        private val numberProperty = Property.NumberProperty(
            title = null,
            description = null,
            enum = null,
            minimum = null,
            maximum = null,
            exclusiveMinimum = null,
            exclusiveMaximum = null,
            ref = null
        )
        private val integerProperty = Property.IntegerProperty(
            title = null,
            description = null,
            enum = null,
            minimum = null,
            maximum = null,
            exclusiveMinimum = null,
            exclusiveMaximum = null,
            ref = null
        )
        private val booleanProperty = Property.BooleanProperty(
            title = null,
            description = null,
            enum = null,
            ref = null
        )
        private val arrayProperty = Property.ArrayProperty(
            title = null,
            description = null,
            enum = null,
            minItems = null,
            maxItems = null,
            items = null,
            uniqueItems = null,
            ref = null
        )
        private val objectProperty = Property.ObjectProperty(
            title = null,
            description = null,
            enum = null,
            minProperties = null,
            maxProperties = null,
            required = null,
            properties = null,
            ref = null
        )
        private val properties =
            listOf<Property>(
                nullProperty,
                stringProperty,
                numberProperty,
                integerProperty,
                booleanProperty,
                arrayProperty
            )
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
            items = listOf(mockk(), mockk(), mockk()),
            uniqueItems = true,
            required = properties.map { it::class.simpleName!! },
            properties = properties.associateBy { it::class.simpleName!! },
            minProperties = 0,
            maxProperties = 100,
            enum = null,
            type = null
        )

        private fun buildReferenceMap(def: Definition) = ReferenceMap(
            referenceFile,
            mapOf(referenceFile to mapOf(referenceKey to def))
        )

        @JvmStatic
        private fun generateUnmergeTestArguments(): Stream<Arguments> {
            val originalProperties: List<Property> = listOf(
                nullProperty.copy(ref = testReference),
                stringProperty.copy(ref = testReference),
                numberProperty.copy(ref = testReference),
                integerProperty.copy(ref = testReference),
                booleanProperty.copy(ref = testReference),
                arrayProperty.copy(ref = testReference),
                objectProperty.copy(ref = testReference)
            )
            val mergedProperties = originalProperties.map { it.mergeReferences(definition, MergeStrategy.Merge) }
            return originalProperties.zip(mergedProperties) { original, merged -> Arguments.of(original, merged) }
                .toTypedArray()
                .let { Stream.of(*it) }
        }

        private fun Property.mergeReferences(def: Definition, mergeStrategy: MergeStrategy): Property {
            val key = "key"
            val map = mapOf(key to this).mergeDefinitions(buildReferenceMap(def), mergeStrategy)
            return map[key] ?: error("Merged property not found")
        }
    }

    @Test
    fun `should merge null property with definition`() {
        val property = nullProperty.copy(ref = testReference)
        val definition = definition.copy()
        val merged = testPropertyMergeAndBaseAssertions(property, definition)

        assertThat(merged.enum, Is(definition.enum))
    }

    @Test
    fun `should merge string property with definition`() {
        val property = stringProperty.copy(ref = testReference)
        val definition = definition.copy(enum = setOf("a", "b", "c"))
        val merged = testPropertyMergeAndBaseAssertions(property, definition)

        assertThat(merged.enum, Is(definition.enum))
        assertThat(merged.minLength, Is(definition.minLength))
        assertThat(merged.maxLength, Is(definition.maxLength))
    }

    @Test
    fun `should merge number property with definition`() {
        val property = numberProperty.copy(ref = testReference)
        val definition = definition.copy(enum = setOf(0.0, 1.0, 2.0))
        val merged = testPropertyMergeAndBaseAssertions(property, definition)

        assertThat(merged.enum, Is(definition.enum))
        assertThat(merged.minimum, Is(definition.minimum))
        assertThat(merged.maximum, Is(definition.maximum))
        assertThat(merged.exclusiveMinimum, Is(definition.exclusiveMinimum))
        assertThat(merged.exclusiveMaximum, Is(definition.exclusiveMaximum))
    }

    @Test
    fun `should merge integer property with definition`() {
        val property = integerProperty.copy(ref = testReference)
        val definition = definition.copy(enum = setOf(0L, 1L, 2L))
        val merged = testPropertyMergeAndBaseAssertions(property, definition)

        assertThat(merged.enum, containsInAnyOrder(*definition.enum!!.toTypedArray()))
        assertThat(merged.minimum, Is(definition.minimum!!.toLong()))
        assertThat(merged.maximum, Is(definition.maximum!!.toLong()))
        assertThat(merged.exclusiveMinimum, Is(definition.exclusiveMinimum!!.toLong()))
        assertThat(merged.exclusiveMaximum, Is(definition.exclusiveMaximum!!.toLong()))
    }

    @Test
    fun `should merge boolean property with definition`() {
        val property = booleanProperty.copy(ref = testReference)
        val definition = definition.copy(enum = setOf(true, false))
        val merged = testPropertyMergeAndBaseAssertions(property, definition)

        assertThat(merged.enum, Is(definition.enum))
    }

    @Test
    fun `should merge array property with definition`() {
        val property = arrayProperty.copy(ref = testReference)
        val definition = definition.copy(enum = setOf(listOf(), listOf("")))
        val merged = testPropertyMergeAndBaseAssertions(property, definition)

        assertThat(merged.enum, Is(definition.enum))
        assertThat(merged.minItems, Is(definition.minItems))
        assertThat(merged.maxItems, Is(definition.maxItems))
        assertThat(merged.items, containsInAnyOrder(*definition.items!!.toTypedArray()))
        assertThat(merged.uniqueItems, Is(definition.uniqueItems))
    }

    @Test
    fun `should merge object property with definition`() {
        val property = objectProperty.copy(ref = testReference)
        val definition = definition.copy()
        val merged = testPropertyMergeAndBaseAssertions(property, definition)

        assertThat(merged.enum, Is(definition.enum))
        assertThat(merged.minProperties, Is(definition.minProperties))
        assertThat(merged.maxProperties, Is(definition.maxProperties))
        assertThat(merged.required, containsInAnyOrder(*definition.required!!.toTypedArray()))
        assertThat(merged.properties, not(nullValue()))
        assertThat(merged.properties!!.size, Is(definition.properties!!.size))
        assertThat(
            merged.properties!!.entries,
            containsInAnyOrder(*properties.associateBy { it::class.simpleName }.entries.toTypedArray())
        )
    }

    @ParameterizedTest
    @MethodSource("generateUnmergeTestArguments")
    fun `should unmerge property from definition`(originalProperty: Property, mergedProperty: Property) {
        val unmerged = mergedProperty.mergeReferences(definition, MergeStrategy.Unmerge)

        // Equality check is sufficient because these are data class comparisons
        assertThat(unmerged, Is<Property>(originalProperty))
    }

    private inline fun <reified T : Property> testPropertyMergeAndBaseAssertions(property: T, def: Definition): T {
        val merged = property.mergeReferences(def, MergeStrategy.Merge)

        assertThat(merged, instanceOf(T::class.java))
        assertThat(merged.title, Is(def.title))
        assertThat(merged.description, Is(def.description))
        assertThat(merged.ref, Is(property.ref))
        return merged as T
    }
}
