package com.jsonschema2kotlin.parser

import com.jsonschema2kotlin.parser.model.Definition
import com.jsonschema2kotlin.parser.model.Property

internal fun Map<String, Property>.mergeDefinitions(
    definitions: ReferenceMap,
    mergeStrategy: MergeStrategy
): Map<String, Property> =
    this.mapValues { (_, property) -> property.mergeDefinition(definitions, mergeStrategy) }

private fun Property.mergeDefinition(
    definitions: ReferenceMap,
    mergeStrategy: MergeStrategy
): Property {
    val definition = findDefinition(definitions)
    return when (this) {
        is Property.NullProperty -> mergeNullPropertyDefinition(definition, mergeStrategy)
        is Property.StringProperty -> mergeStringPropertyDefinition(definition, mergeStrategy)
        is Property.NumberProperty -> mergeNumberPropertyDefinition(definition, mergeStrategy)
        is Property.IntegerProperty -> mergeIntegerPropertyDefinition(definition, mergeStrategy)
        is Property.BooleanProperty -> mergeBooleanPropertyDefinition(definition, mergeStrategy)
        is Property.ArrayProperty -> mergeArrayPropertyDefinition(definition, mergeStrategy)
        is Property.ObjectProperty -> mergeObjectPropertyDefinition(definition, definitions, mergeStrategy)
    }
}

private fun Property.findDefinition(referenceMap: ReferenceMap): Definition? {
    return referenceMap.getDefinition(this.ref)
}

private fun Property.NullProperty.mergeNullPropertyDefinition(
    definition: Definition?,
    mergeStrategy: MergeStrategy
): Property.NullProperty =
    this.copy(
        title = mergeStrategy.execute(this.title) { definition?.title },
        description = mergeStrategy.execute(this.description) { definition?.description },
        enum = this.enum // cannot possibly be transformed into anything meaningful
    )

private fun Property.StringProperty.mergeStringPropertyDefinition(
    definition: Definition?,
    mergeStrategy: MergeStrategy
): Property.StringProperty =
    this.copy(
        title = mergeStrategy.execute(this.title) { definition?.title },
        description = mergeStrategy.execute(this.description) { definition?.description },
        minLength = mergeStrategy.execute(this.minLength) { definition?.minLength },
        maxLength = mergeStrategy.execute(this.maxLength) { definition?.maxLength },
        enum = mergeStrategy.execute(this.enum) { definition?.enum?.inferType() }
    )

private fun Property.NumberProperty.mergeNumberPropertyDefinition(
    definition: Definition?,
    mergeStrategy: MergeStrategy
): Property.NumberProperty =
    this.copy(
        title = mergeStrategy.execute(this.title) { definition?.title },
        description = mergeStrategy.execute(this.description) { definition?.description },
        minimum = mergeStrategy.execute(this.minimum) { definition?.minimum },
        maximum = mergeStrategy.execute(this.maximum) { definition?.maximum },
        exclusiveMinimum = mergeStrategy.execute(this.exclusiveMinimum) { definition?.exclusiveMinimum },
        exclusiveMaximum = mergeStrategy.execute(this.exclusiveMaximum) { definition?.exclusiveMaximum },
        enum = mergeStrategy.execute(this.enum) { definition?.enum?.inferType<Number, Double> { it.toDouble() } }
    )

private fun Property.IntegerProperty.mergeIntegerPropertyDefinition(
    definition: Definition?,
    mergeStrategy: MergeStrategy
): Property.IntegerProperty =
    this.copy(
        title = mergeStrategy.execute(this.title) { definition?.title },
        description = mergeStrategy.execute(this.description) { definition?.description },
        minimum = mergeStrategy.execute(this.minimum) { definition?.minimum?.toLong() },
        maximum = mergeStrategy.execute(this.maximum) { definition?.maximum?.toLong() },
        exclusiveMinimum = mergeStrategy.execute(this.exclusiveMinimum) { definition?.exclusiveMinimum?.toLong() },
        exclusiveMaximum = mergeStrategy.execute(this.exclusiveMaximum) { definition?.exclusiveMaximum?.toLong() },
        enum = mergeStrategy.execute(this.enum) { definition?.enum?.inferType<Number, Long> { it.toLong() } }
    )

private fun Property.BooleanProperty.mergeBooleanPropertyDefinition(
    definition: Definition?,
    mergeStrategy: MergeStrategy
): Property.BooleanProperty =
    this.copy(
        title = mergeStrategy.execute(this.title) { definition?.title },
        description = mergeStrategy.execute(this.description) { definition?.description },
        enum = mergeStrategy.execute(this.enum) { definition?.enum?.inferType() }
    )

private fun Property.ArrayProperty.mergeArrayPropertyDefinition(
    definition: Definition?,
    mergeStrategy: MergeStrategy
): Property.ArrayProperty =
    this.copy(
        title = mergeStrategy.execute(this.title) { definition?.title },
        description = mergeStrategy.execute(this.description) { definition?.description },
        minItems = mergeStrategy.execute(this.minItems) { definition?.minItems },
        maxItems = mergeStrategy.execute(this.maxItems) { definition?.maxItems },
        items = mergeStrategy.execute(this.items) { definition?.items },
        uniqueItems = mergeStrategy.execute(this.uniqueItems) { definition?.uniqueItems },
        enum = mergeStrategy.execute(this.enum) { definition?.enum?.inferType() }
    )

private fun Property.ObjectProperty.mergeObjectPropertyDefinition(
    definition: Definition?,
    definitions: ReferenceMap,
    mergeStrategy: MergeStrategy
): Property.ObjectProperty =
    this.copy(
        title = mergeStrategy.execute(this.title) { definition?.title },
        description = mergeStrategy.execute(this.description) { definition?.description },
        required = mergeStrategy.execute(this.required) { definition?.required },
        properties = mergeStrategy.execute(
            property = this.properties?.mergeDefinitions(definitions, mergeStrategy),
            definition = { definition?.properties?.mergeDefinitions(definitions, mergeStrategy) }
        ),
        minProperties = mergeStrategy.execute(this.minProperties) { definition?.minProperties },
        maxProperties = mergeStrategy.execute(this.maxProperties) { definition?.maxProperties },
        enum = mergeStrategy.execute(this.enum) { definition?.enum?.inferType() }
    )

private inline fun <reified T, reified R> Set<Any?>.inferType(mapper: (T) -> R) =
    this.filterIsInstance<T>()
        .map(mapper)
        .toSet()
        .takeIf { this.size == it.size }
        ?: throw IllegalArgumentException("Enum arguments are not all of type ${R::class.java}")

private inline fun <reified T> Set<Any?>.inferType() = this.inferType<T, T> { it }

internal sealed class MergeStrategy {
    abstract fun <T> execute(property: T?, definition: () -> T?): T?

    object Merge : MergeStrategy() {
        override fun <T> execute(property: T?, definition: () -> T?): T? {
            return property ?: definition.invoke()
        }
    }

    object Unmerge : MergeStrategy() {
        override fun <T> execute(property: T?, definition: () -> T?): T? {
            return property?.takeIf { it != definition.invoke() }
        }
    }
}
