plugins {
    kotlin("jvm")
}

group = "com.jsonschema2kotlin"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":parser"))
}
