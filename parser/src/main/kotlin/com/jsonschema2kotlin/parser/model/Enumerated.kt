package com.jsonschema2kotlin.parser.model

internal interface Enumerated<T> {
    val enum: Set<T?>?
}
