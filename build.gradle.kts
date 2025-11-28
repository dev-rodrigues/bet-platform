import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.spring") version "1.9.24" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

group = "br.devrodrigues"
version = "0.0.1-SNAPSHOT"
description = "bet-platform"

allprojects {
    group = "br.devrodrigues"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

val serviceProjects = subprojects.filter { it.path.startsWith(":services:") }
configure(serviceProjects) {
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.springframework.boot")

    dependencies {
        add("implementation", "org.springframework.boot:spring-boot-starter-web")
        add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")
        add("implementation", "org.springframework.boot:spring-boot-starter-actuator")
        add("implementation", "org.springframework.kafka:spring-kafka")
        add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin")
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }
}

val libraryProjects = subprojects.filter { it.path.startsWith(":libs:") }
configure(libraryProjects) {
    apply(plugin = "java-library")
}
