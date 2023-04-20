plugins {
    application
}

application {
    mainClass.set("ClientKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
}
