rootProject.name = "stocktrack-backend"

include(
    "stocktrack-bootstrap-ktor",
    "stocktrack-api-v1-model-kotlin",
    "stocktrack-api-v1-mapper",
    "stocktrack-log-v1-mapper",
    "stocktrack-core",
    "stocktrack-core-model",
    "stocktrack-business",
    "stocktrack-application-api",
    "stocktrack-transport-kafka",
    "stocktrack-repository-core",
    "stocktrack-repository-core-test",
    "stocktrack-repository-in-memory",
    "stocktrack-repository-postgresql",
    "stocktrack-repository-cassandra",
    "stocktrack-repository-stub",
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("../../build-plugin")
    plugins {
        id("com.otus.otuskotlin.build.build-jvm") apply false
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}