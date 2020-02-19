package com.jsonschema2kotlin.codegen

import com.squareup.kotlinpoet.*

internal sealed class EnumInfo<T : Any>(
    val enumName: String,
    val type: TypeName,
    val enumeration: Set<T?>,
    val constructorInitializer: String,
    val enumConstantNameBuilder: (T) -> String
) {
    class StringEnum(
        name: String,
        enum: Set<String?>
    ) : EnumInfo<String>(name, String::class.asTypeName(), enum, "%S", { it })

    class NumberEnum(
        name: String,
        enum: Set<Double?>,
        constantNameBuilder: (Double) -> String
    ) : EnumInfo<Double>(name, Double::class.asTypeName(), enum, "%L", constantNameBuilder)

    class IntegerEnum(
        name: String,
        enum: Set<Long?>,
        constantNameBuilder: (Long) -> String
    ) : EnumInfo<Long>(name, Long::class.asTypeName(), enum, "%L", constantNameBuilder)

    class BooleanEnum(
        name: String,
        enum: Set<Boolean?>
    ) : EnumInfo<Boolean>(name, Boolean::class.asTypeName(), enum, "%L", { it.toString() })
}

internal fun <T : Any> buildEnum(enumInfo: EnumInfo<T>): TypeSpec {
    val enumConstructor = FunSpec.constructorBuilder()
        .addParameter("value", enumInfo.type)
        .build()
    val enumPropertyInitializer = PropertySpec.builder("value", enumInfo.type)
        .initializer("value")
        .build()
    val enumBuilder = TypeSpec.enumBuilder(enumInfo.enumName)
        .primaryConstructor(enumConstructor)
        .addProperty(enumPropertyInitializer)
    enumInfo.enumeration.filterNotNull().forEach { enumConstant ->
        val enumConstantInitializer = TypeSpec.anonymousClassBuilder()
            .addSuperclassConstructorParameter(enumInfo.constructorInitializer, enumConstant)
            .build()
        enumBuilder.addEnumConstant(
            enumInfo.enumConstantNameBuilder(enumConstant).toUpperCase(),
            enumConstantInitializer
        )
    }
    return enumBuilder.build()
}
