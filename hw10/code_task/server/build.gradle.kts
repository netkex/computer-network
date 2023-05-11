plugins {
    application
    alias(libs.plugins.kotlinxSerialization)
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation(libs.kotlinxCli)
    implementation(libs.kotlinxSerialization)
    implementation(project(":common"))
}

application {
    mainClass.set("MainKt")
}
