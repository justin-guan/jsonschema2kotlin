package com.jsonschema2kotlin.parser.model

internal interface IProperty {
    val title: String?
    val description: String?
}

internal interface INullProperty : IProperty

internal interface IBooleanProperty : IProperty

internal interface IStringProperty : IProperty {
    val minLength: Int?
    val maxLength: Int?
}

internal interface INumberProperty<T : Number> : IProperty {
    val minimum: T?
    val maximum: T?
    val exclusiveMinimum: T?
    val exclusiveMaximum: T?
}

internal interface IArrayProperty : IProperty {
    val minItems: Int?
    val maxItems: Int?
    val items: List<Property>?
    val uniqueItems: Boolean?
}

internal interface IObjectProperty : IProperty {
    val required: List<String>?
    val properties: Map<String, Property>?
    val minProperties: Int?
    val maxProperties: Int?
}
