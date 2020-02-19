plugins {
    kotlin("jvm")
}

group = "com.jsonschema2kotlin"
version = "0.0.1-SNAPSHOT"

dependencies {
    implementation(project(":parser"))
    implementation(project(":codegen"))
}
