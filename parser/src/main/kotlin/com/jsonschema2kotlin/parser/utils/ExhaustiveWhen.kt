package com.jsonschema2kotlin.parser.utils

internal object Do {
    inline infix fun <reified T> exhaustive(any: T?) = any
}
