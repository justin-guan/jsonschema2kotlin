package com.jsonschema2kotlin.codegen

import com.jsonschema2kotlin.codegen.EnumInfo.BooleanEnum
import com.jsonschema2kotlin.codegen.EnumInfo.StringEnum
import com.jsonschema2kotlin.parser.model.Property
import com.jsonschema2kotlin.parser.toJsonSchema
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File

fun File.generateKotlinFiles(pkg: String) {
    toJsonSchema().forEach { (fileName, jsonSchema) ->
        val fileBuilder = FileSpec.builder(pkg, fileName)
        parseClass(fileBuilder, jsonSchema.properties, fileName.toKotlinFileName())
        fileBuilder.build()
            .takeIf { it.members.isNotEmpty() }
            ?.writeTo(System.out)
    }
}

private fun String.toKotlinFileName(): String = this.takeWhile { it != '.' }.capitalize()

private fun parseClass(fileBuilder: FileSpec.Builder, properties: Map<String, Property>?, className: String) {
    val dataClassBuilder = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.DATA)
    val constructorBuilder = FunSpec.constructorBuilder()
    properties?.forEach { propertyName, property ->
        parseProperties(fileBuilder, dataClassBuilder, constructorBuilder, property, propertyName)
    }
    dataClassBuilder.primaryConstructor(constructorBuilder.build())
    dataClassBuilder.build()
        .takeIf { it.propertySpecs.isNotEmpty() }
        ?.let { fileBuilder.addType(it) }
}

private fun parseProperties(
    fileSpec: FileSpec.Builder,
    classSpec: TypeSpec.Builder,
    constructor: FunSpec.Builder,
    property: Property,
    propertyName: String
) {
    when (property) {
        is Property.NullProperty -> parseNullProperty(fileSpec, classSpec, constructor, property, propertyName)
        is Property.StringProperty -> parseStringProperty(fileSpec, classSpec, constructor, property, propertyName)
        is Property.NumberProperty -> parseNumberProperty(fileSpec, classSpec, constructor, property, propertyName)
        is Property.IntegerProperty -> parseIntegerProperty(fileSpec, classSpec, constructor, property, propertyName)
        is Property.BooleanProperty -> parseBooleanProperty(fileSpec, classSpec, constructor, property, propertyName)
        is Property.ArrayProperty -> parseArrayProperty(fileSpec, classSpec, constructor, property, propertyName)
        is Property.ObjectProperty -> parseObjectProperty(fileSpec, classSpec, constructor, property, propertyName)
    }
}

private fun parseNullProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    property: Property.NullProperty,
    propertyName: String
) {
    val nothingType = ClassName("kotlin", "Nothing").copy(nullable = true)
    parseProperty(fileBuilder, dataClassBuilder, constructorBuilder, nothingType, propertyName)
}

private fun parseStringProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    property: Property.StringProperty,
    propertyName: String
) {
    val type = parseEnum(dataClassBuilder, property.enum, propertyName, ::StringEnum) ?: String::class.asTypeName()
    parseProperty(fileBuilder, dataClassBuilder, constructorBuilder, type, propertyName)
}

private fun parseNumberProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    property: Property.NumberProperty,
    propertyName: String
) {
    val type = parseEnum(dataClassBuilder, property.enum, propertyName) { enumName, enums ->
        EnumInfo.NumberEnum(enumName, enums) { "${enumName}_${it.toString().replace(".", "_")}" }
    } ?: Double::class.asTypeName()
    parseProperty(fileBuilder, dataClassBuilder, constructorBuilder, type, propertyName)
}

private fun parseIntegerProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    property: Property.IntegerProperty,
    propertyName: String
) {
    val type = parseEnum(dataClassBuilder, property.enum, propertyName) { enumName, enums ->
        EnumInfo.IntegerEnum(enumName, enums) { "${enumName}_${it.toString().replace(".", "_")}" }
    } ?: Long::class.asTypeName()

    parseProperty(fileBuilder, dataClassBuilder, constructorBuilder, type, propertyName)
}

private fun parseBooleanProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    property: Property.BooleanProperty,
    propertyName: String
) {
    val type = parseEnum(dataClassBuilder, property.enum, propertyName, ::BooleanEnum) ?: Boolean::class.asTypeName()
    parseProperty(fileBuilder, dataClassBuilder, constructorBuilder, type, propertyName)
}

private fun parseArrayProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    property: Property.ArrayProperty,
    propertyName: String
) {
    val type = Iterable::class.asTypeName().parameterizedBy(STAR)
    parseProperty(fileBuilder, dataClassBuilder, constructorBuilder, type, propertyName)
}

private fun parseObjectProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    property: Property.ObjectProperty,
    propertyName: String
) {
    val className = propertyName.toKotlinFileName()
    parseClass(fileBuilder, property.properties, className)
    val type = ClassName(fileBuilder.packageName, className)
    parseProperty(fileBuilder, dataClassBuilder, constructorBuilder, type, propertyName)
}

private fun parseProperty(
    fileBuilder: FileSpec.Builder,
    dataClassBuilder: TypeSpec.Builder,
    constructorBuilder: FunSpec.Builder,
    type: TypeName,
    propertyName: String
) {
    constructorBuilder.addParameter(propertyName, type)
    val propertySpec = PropertySpec.builder(propertyName, type)
        .initializer(propertyName)
        .build()
    dataClassBuilder.addProperty(propertySpec)
}

private fun <T> parseEnum(
    dataClassBuilder: TypeSpec.Builder,
    enum: Set<T>?,
    propertyName: String,
    enumInfoBuilder: (String, Set<T>) -> EnumInfo<*>
): ClassName? {
    return enum?.let { enumeration ->
        val enumName = propertyName.toKotlinFileName()
        val enumInfo = enumInfoBuilder.invoke(enumName, enumeration)
        dataClassBuilder.addType(buildEnum(enumInfo))
        ClassName("", enumName) // empty package name is ok since enum will be nested
    }
}
