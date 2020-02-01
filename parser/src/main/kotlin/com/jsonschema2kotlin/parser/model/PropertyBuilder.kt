package com.jsonschema2kotlin.parser.model

internal data class PropertyHolder(
    override var title: String? = null,
    override var description: String? = null,
    override var required: List<String> = emptyList(),
    override var properties: Properties = Properties(emptyMap()),
    override var minimum: Number? = null,
    override var maximum: Number? = null,
    override var exclusiveMinimum: Number? = null,
    override var exclusiveMaximum: Number? = null,
    override var minLength: Int? = null,
    override var maxLength: Int? = null,
    override var minItems: Int? = null,
    override var maxItems: Int? = null,
    override var minProperties: Int? = null,
    override var maxProperties: Int? = null,
    override var items: List<Property> = emptyList(),
    override var uniqueItems: Boolean? = null,
    override var enums: Set<Any?> = emptySet()
) : IObjectProperty,
    INumberProperty<Number>,
    IStringProperty,
    IArrayProperty,
    IBooleanProperty,
    INullProperty,
    Enumerated<Any>

internal fun Type.buildProperty(propertyHolder: PropertyHolder): Property =
    when (this) {
        Type.STRING -> Property.StringProperty(propertyHolder, propertyHolder.enums.toStringSet())
        Type.NUMBER -> Property.NumberProperty(
            propertyHolder.toNumberINumberProperty(),
            propertyHolder.enums.toNumberSet()
        )
        Type.INTEGER -> Property.IntegerProperty(
            propertyHolder.toIntegerINumberProperty(),
            propertyHolder.enums.toIntegerSet()
        )
        Type.OBJECT -> Property.ObjectProperty(propertyHolder)
        Type.ARRAY -> Property.ArrayProperty(propertyHolder)
        Type.BOOLEAN -> Property.BooleanProperty(propertyHolder, propertyHolder.enums.toBooleanSet())
        Type.NULL -> Property.NullProperty(propertyHolder)
    }

private fun PropertyHolder.toNumberINumberProperty(): INumberProperty<Double> =
    object : IProperty by this, INumberProperty<Double> {
        override val minimum: Double? = this@toNumberINumberProperty.minimum?.toDouble()
        override val maximum: Double? = this@toNumberINumberProperty.maximum?.toDouble()
        override val exclusiveMinimum: Double? = this@toNumberINumberProperty.exclusiveMinimum?.toDouble()
        override val exclusiveMaximum: Double? = this@toNumberINumberProperty.exclusiveMaximum?.toDouble()
    }

private fun PropertyHolder.toIntegerINumberProperty(): INumberProperty<Long> =
    object : IProperty by this, INumberProperty<Long> {
        override val minimum: Long? = this@toIntegerINumberProperty.minimum?.toLong()
        override val maximum: Long? = this@toIntegerINumberProperty.maximum?.toLong()
        override val exclusiveMinimum: Long? = this@toIntegerINumberProperty.exclusiveMinimum?.toLong()
        override val exclusiveMaximum: Long? = this@toIntegerINumberProperty.exclusiveMaximum?.toLong()
    }
