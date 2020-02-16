package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.model.Reference
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasKey
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileNotFoundException
import org.hamcrest.CoreMatchers.`is` as Is

class SchemaParserTest {

    @Test
    fun `end to end jsonschema reading`() {
        val propertiesSchemaFile = "properties.schema.json"
        val definitionsSchemaFile = "definitions.schema.json"
        val resources = this::class.java.classLoader.getResource(propertiesSchemaFile)
            ?: throw FileNotFoundException("Unable to find resource")
        val resourceFile = File(resources.toURI()).parentFile

        val schemas = resourceFile.toJsonSchema()

        assertThat(schemas.size, Is(2))

        schemas.getValue(propertiesSchemaFile).run {
            assertThat(id, Is(nullValue()))
            assertThat(schema, Is("http://json-schema.org/draft-07/schema#"))
            assertThat(title, Is("Test"))

            assertThat(definitions, hasKey("localReference"))
            definitions!!.getValue("localReference").run {
                assertThat(description, Is("Local Reference"))
            }

            assertThat(properties, hasKey("stringProperty"))
            properties!!.getValue("stringProperty").cast<Property.StringProperty>().run {
                assertThat(title, Is("stringProperty title"))
                assertThat(description, Is("stringProperty description"))
                assertThat(minLength, Is(0))
                assertThat(maxLength, Is(100))
                assertThat(enum, containsInAnyOrder("enum1", "enum2"))

                ref!!.run {
                    assertThat(ref, Is("definitions.schema.json#/definitions/stringLength"))
                    assertThat(definitionPath, Is("stringLength"))
                    assertThat(fileName(propertiesSchemaFile), Is(definitionsSchemaFile))
                }
            }

            assertThat(properties, hasKey("numberProperty1"))
            properties!!.getValue("numberProperty1").cast<Property.NumberProperty>().run {
                assertThat(title, Is("numberProperty1 title"))
                assertThat(description, Is("numberProperty1 description"))
                assertThat(minimum, Is(0.0))
                assertThat(maximum, Is(100.0))

                ref!!.run {
                    assertThat(ref, Is("definitions.schema.json#/definitions/numberRange"))
                    assertThat(definitionPath, Is("numberRange"))
                    assertThat(fileName(propertiesSchemaFile), Is(definitionsSchemaFile))
                }
            }

            assertThat(properties, hasKey("numberProperty2"))
            properties!!.getValue("numberProperty2").cast<Property.NumberProperty>().run {
                assertThat(title, Is("numberProperty2 title"))
                assertThat(description, Is("numberProperty2 description"))
                assertThat(exclusiveMinimum, Is(0.0))
                assertThat(exclusiveMaximum, Is(100.0))

                ref!!.run {
                    assertThat(ref, Is("definitions.schema.json#/definitions/exclusiveNumberRange"))
                    assertThat(definitionPath, Is("exclusiveNumberRange"))
                    assertThat(fileName(propertiesSchemaFile), Is(definitionsSchemaFile))
                }
            }

            assertThat(properties, hasKey("integerProperty1"))
            properties!!.getValue("integerProperty1").cast<Property.IntegerProperty>().run {
                assertThat(title, Is("integerProperty1 title"))
                assertThat(description, Is("integerProperty1 description"))
                assertThat(minimum, Is(0L))
                assertThat(maximum, Is(100L))

                ref!!.run {
                    assertThat(ref, Is("definitions.schema.json#/definitions/numberRange"))
                    assertThat(definitionPath, Is("numberRange"))
                    assertThat(fileName(propertiesSchemaFile), Is(definitionsSchemaFile))
                }
            }

            assertThat(properties, hasKey("integerProperty2"))
            properties!!.getValue("integerProperty2").cast<Property.IntegerProperty>().run {
                assertThat(title, Is("integerProperty2 title"))
                assertThat(description, Is("integerProperty2 description"))
                assertThat(exclusiveMinimum, Is(0L))
                assertThat(exclusiveMaximum, Is(100L))

                ref!!.run {
                    assertThat(ref, Is("definitions.schema.json#/definitions/exclusiveNumberRange"))
                    assertThat(definitionPath, Is("exclusiveNumberRange"))
                    assertThat(fileName(propertiesSchemaFile), Is(definitionsSchemaFile))
                }
            }

            assertThat(properties, hasKey("booleanProperty"))
            properties!!.getValue("booleanProperty").cast<Property.BooleanProperty>().run {
                assertThat(title, Is("booleanProperty title"))
                assertThat(description, Is("booleanProperty description"))
                assertThat(enum, containsInAnyOrder(true, false))

                ref!!.run {
                    assertThat(ref, Is("definitions.schema.json#/definitions/booleanTypes"))
                    assertThat(definitionPath, Is("booleanTypes"))
                    assertThat(fileName(propertiesSchemaFile), Is(definitionsSchemaFile))
                }
            }

            assertThat(properties, hasKey("nullProperty"))
            properties!!.getValue("nullProperty").cast<Property.NullProperty>().run {
                assertThat(title, Is("nullProperty title"))
                assertThat(description, Is("nullProperty description"))
            }

            assertThat(properties, hasKey("arrayProperty"))
            properties!!.getValue("arrayProperty").cast<Property.ArrayProperty>().run {
                assertThat(title, Is("arrayProperty title"))
                assertThat(description, Is("arrayProperty description"))
                assertThat(minItems, Is(0))
                assertThat(maxItems, Is(100))
            }

            assertThat(properties, hasKey("objectProperty"))
            properties!!.getValue("objectProperty").cast<Property.ObjectProperty>().run {
                assertThat(title, Is("objectProperty title"))
                assertThat(minProperties, Is(0))
                assertThat(maxProperties, Is(100))

                fun Property.assertLocalReference(reference: Reference?) {
                    assertThat(description, Is("Local Reference"))

                    reference!!.run {
                        assertThat(ref, Is("#/definitions/localReference"))
                        assertThat(definitionPath, Is("localReference"))
                        assertThat(fileName(propertiesSchemaFile), Is(propertiesSchemaFile))
                    }
                }
                assertLocalReference(ref)

                assertThat(properties, hasKey("subProperty"))
                properties!!.getValue("subProperty").cast<Property.StringProperty>().run {
                    assertThat(title, Is("subProperty title"))
                    assertThat(required, containsInAnyOrder("subProperty"))

                    assertLocalReference(ref)
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T: Property> Property.cast(): T = this as T
