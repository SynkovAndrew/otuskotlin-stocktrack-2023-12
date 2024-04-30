plugins {
    id("com.otus.otuskotlin.build.build-jvm")
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":stocktrack-log-v1-mapper"))
    implementation(project(":stocktrack-internal-model"))

    implementation(project(":stocktrack-api-v1-model-kotlin"))
    implementation(project(":stocktrack-api-v1-mapper"))

    implementation("com.otus.otuskotlin.stocktrack:stocktrack-log-core")
    implementation("com.otus.otuskotlin.stocktrack:stocktrack-log-logback")
    implementation("com.otus.otuskotlin.stocktrack:stocktrack-log-model")

    implementation(libs.kotlin.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.core)
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-client-apache")
    implementation("io.ktor:ktor-client-okhttp-jvm")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-cors")

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation(kotlin("test"))
}

buildJvm {
    mainClass = "com.otus.otuskotlin.stocktrack.StockTrackApplicationKt"
    jarName = "stocktrack-backend"
    dockerRepositoryOwner = "andrewsynkov"
}

tasks.test {
    useJUnitPlatform()
}
