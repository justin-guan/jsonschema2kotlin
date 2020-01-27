package com.jsonschema2kotlin.parser.model

interface Enum {
    val enums: EnumeratedType?
}

sealed class EnumeratedType {
    class NumericalType<T : Number>(set: Set<T?>) : EnumeratedType(), Set<T?> by set
    class StringType(set: Set<String?>) : EnumeratedType(), Set<String?> by set
    class BooleanType(set: Set<Boolean?>) : EnumeratedType(), Set<Boolean?> by set
}

internal fun Set<Any?>.toEnumeratedType(type: Type): EnumeratedType {
    return try {
        when (type) {
            Type.STRING -> EnumeratedType.StringType(this.safeCastToType())
            Type.NUMBER -> EnumeratedType.NumericalType(this.safeCastAndMap<Number?, Double?> { it?.toDouble() })
            Type.INTEGER -> EnumeratedType.NumericalType(this.safeCastAndMap<Number?, Long?> { it?.toLong() })
            Type.BOOLEAN -> EnumeratedType.BooleanType(this.safeCastToType())
            Type.OBJECT -> throw UnsupportedOperationException("Enum objects are not supported")
            Type.ARRAY -> throw UnsupportedOperationException("Enum arrays are not supported")
            Type.NULL -> throw UnsupportedOperationException("Enums for null types are not supported")
        }
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Expected enum definition to contain the same type as the declared type", e)
    }
}

private inline fun <reified T, reified R> Set<Any?>.safeCastAndMap(mapper: (T) -> R): Set<R> =
    this.filterIsInstance<T>()
        .map(mapper)
        .toSet()
        .takeIf { it.size == size }
        ?: throw IllegalArgumentException("Set contains elements that are not type ${T::class}")

private inline fun <reified T> Set<Any?>.safeCastToType(): Set<T> = this.safeCastAndMap<T, T> { it }
