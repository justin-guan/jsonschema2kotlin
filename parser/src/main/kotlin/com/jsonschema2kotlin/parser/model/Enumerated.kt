package com.jsonschema2kotlin.parser.model

internal interface Enumerated<T> {
    val enums: Set<T?>
}

internal fun Set<Any?>.toStringSet(): Set<String?> = this.safeCastToType()

internal fun Set<Any?>.toNumberSet(): Set<Double?> = this.safeCastAndMap<Number?, Double?> { it?.toDouble() }

internal fun Set<Any?>.toIntegerSet(): Set<Long?> = this.safeCastAndMap<Number?, Long?> { it?.toLong() }

internal fun Set<Any?>.toBooleanSet(): Set<Boolean?> = this.safeCastToType()

private inline fun <reified T, reified R> Set<Any?>.safeCastAndMap(mapper: (T) -> R): Set<R> =
    this.filterIsInstance<T>()
        .map(mapper)
        .toSet()

private inline fun <reified T> Set<Any?>.safeCastToType(): Set<T> = this.safeCastAndMap<T, T> { it }
