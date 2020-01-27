package com.jsonschema2kotlin.parser.model

internal interface IProperty {
    val title: String?
    val description: String?
}

internal interface IObjectProperty : IProperty {
    val required: List<String>
    val properties: Properties
    val minProperties: Int?
    val maxProperties: Int?
}

internal interface INumberProperty<T : Number> : IProperty, Enum {
    val minimum: T?
    val maximum: T?
    val exclusiveMinimum: T?
    val exclusiveMaximum: T?
}

internal interface IStringProperty : IProperty, Enum {
    val minLength: Int?
    val maxLength: Int?
}

internal interface IArrayProperty : IProperty {
    val minItems: Int?
    val maxItems: Int?
    val items: List<Property>
    val uniqueItems: Boolean?
}

internal interface IBooleanProperty : IProperty, Enum

internal interface INullProperty : IProperty
