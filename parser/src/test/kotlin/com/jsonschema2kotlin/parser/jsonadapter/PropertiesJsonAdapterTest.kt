package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.model.*
import com.squareup.moshi.JsonDataException
import org.amshove.kluent.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.reflect.KClass

class PropertiesJsonAdapterTest {

    companion object {
        private const val PROPERTY_PREFIX = "property"
        private const val TITLE_SUFFIX = " title"
        private const val DESCRIPTION_SUFFIX = " description"

        @Suppress("UNUSED") // used by junit for parameterized test argument generation
        @JvmStatic
        private fun generatePropertySchemas(): Stream<Arguments> = Stream.of(
            *generateArguments(1),
            *generateArguments(2)
        )

        private fun generateArguments(count: Int) = arrayOf(
            Arguments.of(buildPropertySchema(count, Type.STRING.value), Property.StringProperty::class, count),
            Arguments.of(buildPropertySchema(count, Type.NUMBER.value), Property.NumberProperty::class, count),
            Arguments.of(buildPropertySchema(count, Type.INTEGER.value), Property.IntegerProperty::class, count),
            Arguments.of(buildPropertySchema(count, Type.OBJECT.value), Property.ObjectProperty::class, count),
            Arguments.of(buildPropertySchema(count, Type.ARRAY.value), Property.ArrayProperty::class, count),
            Arguments.of(buildPropertySchema(count, Type.BOOLEAN.value), Property.BooleanProperty::class, count),
            Arguments.of(buildPropertySchema(count, Type.NULL.value), Property.NullProperty::class, count)
        )

        private fun buildPropertySchema(numProperties: Int, type: String) = """
            |{
            |   ${(0 until numProperties).joinToString { buildProperty(it, type) }}
            |}
        """.trimMargin()

        private fun buildProperty(propertyNumber: Int, type: String) = """
            |   "$PROPERTY_PREFIX$propertyNumber": {
            |       "type": "$type"
            |   }
        """.trimMargin()
    }

    private val adapter = PropertiesJsonAdapter()

    @ParameterizedTest(name = "generate {2} properties of type {1} for schema")
    @MethodSource(value = ["generatePropertySchemas"])
    fun `generate properties of type for schema`(propertySchema: String, propertyClass: KClass<*>, count: Int) {
        val stringProperties = adapter.fromJson(propertySchema)

        for (propertyNumber in 0 until count) {
            val expectedPropertyName = "$PROPERTY_PREFIX$propertyNumber"
            val properties = stringProperties.shouldNotBeNull()
            properties shouldHaveKey expectedPropertyName
            val stringProperty = properties[expectedPropertyName].shouldNotBeNull()
            stringProperty shouldBeInstanceOf propertyClass
            stringProperty.title.shouldBeNull()
            stringProperty.description.shouldBeNull()
        }
    }

    @Test
    fun `errors on an invalid type`() {
        val invalidType = "invalid"

        enumValues<Type>().map { it.value } shouldNotContain invalidType
        assertThrows<JsonDataException> {
            adapter.fromJson(buildPropertySchema(1, invalidType))
        }
    }

    @Test
    fun `from json`() {
        val stream = this::class.java.classLoader
            .getResourceAsStream("properties.json")!!
            .bufferedReader()
            .use { it.readText() }
        val properties = adapter.fromJson(stream).shouldNotBeNull()

        properties.assertProperty<Property.StringProperty>("stringProperty") {
            it.minLength shouldBeEqualTo 0
            it.maxLength shouldBeEqualTo 100
        }

        properties.assertProperty<Property.NumberProperty>("numberProperty1") {
            it.minimum shouldBeEqualTo 0.0
            it.maximum shouldBeEqualTo 100.0
            it.exclusiveMinimum.shouldBeNull()
            it.exclusiveMaximum.shouldBeNull()
        }

        properties.assertProperty<Property.NumberProperty>("numberProperty2") {
            it.exclusiveMinimum shouldBeEqualTo 0.0
            it.exclusiveMaximum shouldBeEqualTo 100.0
            it.minimum.shouldBeNull()
            it.maximum.shouldBeNull()
        }

        properties.assertProperty<Property.ArrayProperty>("arrayProperty") {
            it.minItems shouldBeEqualTo 0
            it.maxItems shouldBeEqualTo 100
        }

        properties.assertProperty<Property.ObjectProperty>("objectProperty") {
            it.minProperties shouldBeEqualTo 0
            it.maxProperties shouldBeEqualTo 100
            it.properties.assertProperty<Property.StringProperty>("subProperty")
        }

        properties.assertProperty<Property.BooleanProperty>("booleanProperty")
        properties.assertProperty<Property.NullProperty>("nullProperty")
    }

    @Test
    fun `to and from string property json`() {
        val key = "key"
        val stringProperty = Property.StringProperty(object : IStringProperty {
            override val minLength: Int? = 0
            override val maxLength: Int? = 0
            override val title: String? = "$key title"
            override val description: String? = "$key description"
        })
        toAndFromJson(key, stringProperty) {
            assertPropertiesEqual(
                it,
                stringProperty,
                Property.StringProperty::minLength,
                Property.StringProperty::maxLength
            )
        }
    }

    @Test
    fun `to and from number property json`() {
        val key = "key"
        val numberProperty = Property.NumberProperty(object : INumberProperty<Double> {
            override val title: String? = "$key title"
            override val description: String? = "$key description"
            override val minimum: Double? = 0.0
            override val maximum: Double? = 100.0
            override val exclusiveMinimum: Double? = -1.0
            override val exclusiveMaximum: Double? = 101.0
        })
        toAndFromJson(key, numberProperty) {
            assertPropertiesEqual(
                it,
                numberProperty,
                Property.NumberProperty::minimum,
                Property.NumberProperty::maximum,
                Property.NumberProperty::exclusiveMinimum,
                Property.NumberProperty::exclusiveMaximum
            )
        }
    }

    @Test
    fun `to and from integer property json`() {
        val key = "key"
        val integerProperty = Property.IntegerProperty(object : INumberProperty<Long> {
            override val title: String? = "$key title"
            override val description: String? = "$key description"
            override val minimum: Long? = 0
            override val maximum: Long? = 100
            override val exclusiveMinimum: Long? = -1
            override val exclusiveMaximum: Long? = 101
        })
        toAndFromJson(key, integerProperty) {
            assertPropertiesEqual(
                it,
                integerProperty,
                Property.IntegerProperty::minimum,
                Property.IntegerProperty::maximum,
                Property.IntegerProperty::exclusiveMinimum,
                Property.IntegerProperty::exclusiveMaximum
            )
        }
    }

    @Test
    fun `to and from array property json`() {
        val key = "key"
        val arrayProperty = Property.ArrayProperty(object : IArrayProperty {
            override val title: String? = "$key title"
            override val description: String? = "$key description"
            override val minItems: Int? = 0
            override val maxItems: Int? = 100

        })
        toAndFromJson(key, arrayProperty) {
            assertPropertiesEqual(it, arrayProperty, Property.ArrayProperty::minItems, Property.ArrayProperty::maxItems)
        }
    }

    @Test
    fun `to and from empty object property json`() {
        val key = "key"
        val objectProperty = Property.ObjectProperty(object : IObjectProperty {
            override val title: String? = "$key title"
            override val description: String? = "$key description"
            override val required: List<String> = emptyList()
            override val properties: Properties = Properties(emptyMap())
            override val minProperties: Int? = 0
            override val maxProperties: Int? = 100
        })
        toAndFromJson(key, objectProperty) {
            assertPropertiesEqual(
                it,
                objectProperty,
                Property.ObjectProperty::minProperties,
                Property.ObjectProperty::maxProperties
            )
            it.required shouldContainSame objectProperty.required
            it.properties shouldContainSame objectProperty.properties
        }
    }

    @Test
    fun `to and from non empty object property json`() {
        val key = "key"
        val subPropertyKey = "subPropertyKey"
        val stringProperty = Property.StringProperty(object : IStringProperty {
            override val title: String? = "$subPropertyKey title"
            override val description: String? = "$subPropertyKey description"
            override val minLength: Int? = 0
            override val maxLength: Int? = 100
        })
        val objectProperty = Property.ObjectProperty(object : IObjectProperty {
            override val title: String? = "$key title"
            override val description: String? = "$key description"
            override val required: List<String> = listOf(subPropertyKey)
            override val properties: Properties = Properties(mapOf(subPropertyKey to stringProperty))
            override val minProperties: Int? = 0
            override val maxProperties: Int? = 100
        })
        toAndFromJson(key, objectProperty) {
            assertPropertiesEqual(
                it,
                objectProperty,
                Property.ObjectProperty::minProperties,
                Property.ObjectProperty::maxProperties
            )
            it.required shouldContainSame objectProperty.required
            it.properties.assertProperty<Property.StringProperty>(subPropertyKey) { subProperty ->
                assertPropertiesEqual(
                    subProperty,
                    stringProperty,
                    Property.StringProperty::minLength,
                    Property.StringProperty::maxLength
                )
            }
        }
    }

    @Test
    fun `to and from boolean property json`() {
        val key = "key"
        val booleanProperty = Property.BooleanProperty(object : IBooleanProperty {
            override val title: String? = "$key title"
            override val description: String? = "$key description"
        })
        toAndFromJson(key, booleanProperty)
    }

    @Test
    fun `to and from null property json`() {
        val key = "key"
        val nullProperty = Property.NullProperty(object : INullProperty {
            override val title: String? = "$key title"
            override val description: String? = "$key description"
        })
        toAndFromJson(key, nullProperty)
    }

    private inline fun <reified T : Property> toAndFromJson(key: String, property: T, assertions: (T) -> Unit = {}) {
        val properties = Properties(mapOf(key to property))
        val propertiesString = adapter.toJson(properties)
        val fromJson = adapter.fromJson(propertiesString).shouldNotBeNull()

        fromJson.assertProperty<T>(key) {
            it.title shouldBeEqualTo property.title
            it.description shouldBeEqualTo property.description
            assertions.invoke(it)
        }
    }

    private fun <T> assertPropertiesEqual(first: T, second: T, vararg property: (T) -> Any?) {
        property.forEach {
            it.invoke(first) shouldBeEqualTo it.invoke(second)
        }
    }

    private inline fun <reified T : Property> Properties.assertProperty(key: String, assertions: (T) -> Unit = {}) {
        this shouldHaveKey key
        with(this.getValue(key)) {
            this shouldBeInstanceOf T::class
            with(this as T) {
                this.title shouldBeEqualTo "$key$TITLE_SUFFIX"
                this.description shouldBeEqualTo "$key$DESCRIPTION_SUFFIX"
                assertions.invoke(this)
            }
        }
    }
}
