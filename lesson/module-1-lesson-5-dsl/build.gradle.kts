plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.otus.otuskotlin.marketplace.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
