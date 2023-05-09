plugins {
    kotlin("jvm") version "1.8.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


subprojects {
    repositories {
        mavenCentral()
    }

    apply {
        plugin("kotlin")
    }
}