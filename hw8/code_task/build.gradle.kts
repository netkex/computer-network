import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

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

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}