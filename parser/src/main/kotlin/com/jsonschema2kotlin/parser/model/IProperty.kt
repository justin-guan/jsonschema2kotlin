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

internal interface INumberProperty<T : Number> : IProperty {
    val minimum: T?
    val maximum: T?
    val exclusiveMinimum: T?
    val exclusiveMaximum: T?
}

internal interface IStringProperty : IProperty {
    val minLength: Int?
    val maxLength: Int?
}

internal interface IArrayProperty : IProperty {
    val minItems: Int?
    val maxItems: Int?
}

internal interface IBooleanProperty : IProperty

internal interface INullProperty : IProperty
