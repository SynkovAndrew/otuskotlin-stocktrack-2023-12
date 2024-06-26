plugins {
    id("com.otus.otuskotlin.build.build-jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.docker.compose)
}

dependencies {
    implementation(libs.logback)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    testImplementation("com.otus.otuskotlin.stocktrack:stocktrack-api-v1-model-kotlin")
    testImplementation("com.otus.otuskotlin.stocktrack:stocktrack-bootstrap-ktor")

    testImplementation(kotlin("test"))

    testImplementation("io.ktor:ktor-serialization-kotlinx-json:2.3.9")
    testImplementation("io.ktor:ktor-server-test-host:2.3.9")
    testImplementation(libs.coroutines.core)
    testImplementation(libs.kotlinx.datetime)
    testImplementation(libs.assertj)
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "failed")
    }

    dependsOn(tasks.composeUp)
    finalizedBy(tasks.composeDown)
}

dockerCompose {
    dockerComposeWorkingDirectory = project.file("./docker")
/*    executable = "/Applications/Docker.app/Contents/Resources/bin/docker-compose"
    dockerExecutable = "/Applications/Docker.app/Contents/Resources/bin/docker"*/
}
