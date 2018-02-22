import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "eu.tib.prinzm"
version = "0.0.1-SNAPSHOT"

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion

plugins {
    kotlin("jvm") version "1.2.21"
    kotlin("plugin.spring") version "1.2.21"
    id("org.springframework.boot") version "1.5.10.RELEASE"
}

dependencies {
    compile(kotlin("stdlib-jdk8", kotlinVersion))
    //compile(kotlin("reflect", kotlinVersion))
    compile("org.springframework.boot:spring-boot-starter-web")
    compile("org.eclipse.rdf4j:rdf4j-repository-sparql:2.2.4")

    testCompile("org.jetbrains.spek:spek-api:1.1.5") {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntime("org.jetbrains.spek:spek-junit-platform-engine:1.1.5") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.junit.platform")
    }
    testCompile("org.springframework.boot:spring-boot-starter-test")
}

repositories {
    jcenter()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
