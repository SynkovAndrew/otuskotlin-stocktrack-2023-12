package com.otus.otuskotlin.build

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.the
import java.io.ByteArrayOutputStream

internal const val JVM_PLUGIN = "org.jetbrains.kotlin.jvm"
internal const val MULTIPLATFORM_PLUGIN = "org.jetbrains.kotlin.multiplatform"
internal const val RUNTIME_CLASSPATH = "runtimeClasspath"
internal const val BUILD_JVM = "buildJvm"
internal const val BUILD_DOCKER_IMAGE = "buildDockerImage"
internal const val PUSH_DOCKER_IMAGE = "pushDockerImage"
internal const val MAIN_CLASS_MANIFEST_ATTRIBUTE = "Main-Class"
internal const val JAR_FILE_EXTENSION = "jar"

internal fun Project.mainSourceSet(): SourceSet {
    return the(SourceSetContainer::class)
        .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
}

internal fun Project.configurationsRuntimeClasspath(): Configuration {
    return configurations.getByName(RUNTIME_CLASSPATH)
}

internal fun Project.runCommand(command: String) {
    val byteOut = ByteArrayOutputStream()
    exec {
        executable = "sh"
        args = listOf("-c") + command
        standardOutput = byteOut
    }
    println(String(byteOut.toByteArray()).trim())
}