package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Definition
import com.jsonschema2kotlin.parser.model.Property

internal fun Map<String, Property>.mergeDefinitions(definitions: Map<String, Definition>): Map<String, Property> =
    this.mapValues { (_, property) -> property.mergeDefinition(definitions) }

private fun Property.mergeDefinition(definitions: Map<String, Definition>): Property {
    val definition = findDefinition(definitions)
    return when (this) {
        is Property.NullProperty -> mergeNullPropertyDefinition(definition)
        is Property.StringProperty -> mergeStringPropertyDefinition(definition)
        is Property.NumberProperty -> mergeNumberPropertyDefinition(definition)
        is Property.IntegerProperty -> mergeIntegerPropertyDefinition(definition)
        is Property.BooleanProperty -> mergeBooleanPropertyDefinition(definition)
        is Property.ArrayProperty -> mergeArrayPropertyDefinition(definition)
        is Property.ObjectProperty -> mergeObjectPropertyDefinition(definition, definitions)
    }
}

private fun Property.findDefinition(definitions: Map<String, Definition>): Definition? = ref?.let {
    definitions[it.removePrefix("#/definitions/")]
        ?: throw IllegalArgumentException("Reference is referencing a definition that doesn't exist")
}

private fun Property.NullProperty.mergeNullPropertyDefinition(definition: Definition?): Property.NullProperty =
    this.copy(
        title = this.title ?: definition?.title,
        description = this.description ?: definition?.description,
        enum = this.enum, // enum cannot possibly inherit anything meaningful here
        ref = this.ref
    )

private fun Property.StringProperty.mergeStringPropertyDefinition(definition: Definition?): Property.StringProperty =
    this.copy(
        title = this.title ?: definition?.title,
        description = this.description ?: definition?.description,
        minLength = this.minLength ?: definition?.minLength,
        maxLength = this.maxLength ?: definition?.maxLength,
        enum = this.enum ?: definition?.enum?.inferType(),
        ref = this.ref
    )

private fun Property.NumberProperty.mergeNumberPropertyDefinition(definition: Definition?): Property.NumberProperty =
    this.copy(
        title = this.title ?: definition?.title,
        description = this.description ?: definition?.description,
        minimum = this.minimum ?: definition?.minimum,
        maximum = this.maximum ?: definition?.maximum,
        exclusiveMinimum = this.exclusiveMinimum ?: definition?.exclusiveMinimum,
        exclusiveMaximum = this.exclusiveMaximum ?: definition?.exclusiveMaximum,
        enum = this.enum ?: definition?.enum?.inferType<Number, Double> { it.toDouble() },
        ref = this.ref
    )

private fun Property.IntegerProperty.mergeIntegerPropertyDefinition(definition: Definition?): Property.IntegerProperty =
    this.copy(
        title = this.title ?: definition?.title,
        description = this.description ?: definition?.description,
        minimum = this.minimum ?: definition?.minimum?.toLong(),
        maximum = this.maximum ?: definition?.maximum?.toLong(),
        exclusiveMinimum = this.exclusiveMinimum ?: definition?.exclusiveMinimum?.toLong(),
        exclusiveMaximum = this.exclusiveMaximum ?: definition?.exclusiveMaximum?.toLong(),
        enum = this.enum ?: definition?.enum?.inferType<Number, Long> { it.toLong() },
        ref = this.ref
    )

private fun Property.BooleanProperty.mergeBooleanPropertyDefinition(definition: Definition?): Property.BooleanProperty =
    this.copy(
        title = this.title ?: definition?.title,
        description = this.description ?: definition?.description,
        enum = this.enum ?: definition?.enum?.inferType(),
        ref = this.ref
    )

private fun Property.ArrayProperty.mergeArrayPropertyDefinition(definition: Definition?): Property.ArrayProperty =
    this.copy(
        title = this.title ?: definition?.title,
        description = this.description ?: definition?.description,
        minItems = this.minItems ?: definition?.minItems,
        maxItems = this.maxItems ?: definition?.maxItems,
        items = this.items ?: definition?.items,
        uniqueItems = this.uniqueItems ?: definition?.uniqueItems,
        enum = this.enum ?: definition?.enum?.inferType(),
        ref = this.ref
    )

private fun Property.ObjectProperty.mergeObjectPropertyDefinition(
    definition: Definition?,
    definitions: Map<String, Definition>
): Property.ObjectProperty =
    this.copy(
        title = this.title ?: definition?.title,
        description = this.description ?: definition?.description,
        required = this.required ?: definition?.required,
        properties = this.properties?.mergeDefinitions(definitions)
            ?: definition?.properties?.mergeDefinitions(definitions),
        minProperties = this.minProperties ?: definition?.minProperties,
        maxProperties = this.maxProperties ?: definition?.maxProperties,
        enum = this.enum ?: definition?.enum?.inferType(),
        ref = this.ref
    )

private inline fun <reified T, reified R> Set<Any?>.inferType(mapper: (T) -> R) =
    this.filterIsInstance<T>()
        .map(mapper)
        .toSet()
        .takeIf { this.size == it.size }
        ?: throw IllegalArgumentException("Enum arguments are not all of type ${R::class.java}")

private inline fun <reified T> Set<Any?>.inferType() = this.inferType<T, T> { it }
