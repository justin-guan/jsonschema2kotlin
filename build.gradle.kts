group = "com.jsonschema2kotlin"
version = "0.0.1-SNAPSHOT"

plugins {
    id("java")
    kotlin("jvm")
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation(kotlin("stdlib"))
    }
}
