package com.jsonschema2kotlin.parser.model

class Properties(properties: Map<String, Property> = emptyMap()) : Map<String, Property> by properties {
    override fun toString(): String {
        return "{" +
                this.asSequence()
                    .map { (key, value) -> "$key: $value" }
                    .joinToString(", ") +
                "}"
    }
}
