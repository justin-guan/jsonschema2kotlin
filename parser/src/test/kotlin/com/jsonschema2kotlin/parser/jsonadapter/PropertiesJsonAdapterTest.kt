package com.jsonschema2kotlin.parser.jsonadapter

import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Type
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
}
