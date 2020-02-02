package com.jsonschema2kotlin.parser

internal interface IProperty<T> {
    val title: String?
    val description: String?
    val enum: Set<T?>?
}

internal interface INullProperty : IProperty<Nothing>

internal interface IBooleanProperty : IProperty<Boolean>

internal interface IStringProperty : IProperty<String> {
    val minLength: Int?
    val maxLength: Int?
}

internal interface INumberProperty<T : Number> : IProperty<T> {
    val minimum: T?
    val maximum: T?
    val exclusiveMinimum: T?
    val exclusiveMaximum: T?
}

internal interface IArrayProperty : IProperty<List<Any?>> {
    val minItems: Int?
    val maxItems: Int?
    val items: List<Property<*>>?
    val uniqueItems: Boolean?
}

internal interface IObjectProperty : IProperty<Map<String, Any?>> {
    val required: List<String>?
    val properties: Map<String, Property<*>>?
    val minProperties: Int?
    val maxProperties: Int?
}
