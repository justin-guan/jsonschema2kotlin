group = "com.jsonschema2kotlin"
version = "0.0.1-SNAPSHOT"

plugins {
    id("java")
    kotlin("jvm")
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation(kotlin("stdlib"))
    }
}
