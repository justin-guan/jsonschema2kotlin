package com.jsonschema2kotlin.parser.model

private const val DELIMITER = '#'
private const val DEFINITIONS_PATH = "/definitions/"

internal interface ReferenceHolder {
    val ref: Reference?
}

data class Reference(val ref: String) {
    internal val definitionPath = this.ref.takeLastWhile { it != DELIMITER }.removePrefix(DEFINITIONS_PATH)

    internal fun fileName(localReferenceLocation: String): String {
        return if (this.ref.startsWith(DELIMITER)) {
            localReferenceLocation
        } else {
            this.ref.takeWhile { it != DELIMITER }
        }
    }
}
