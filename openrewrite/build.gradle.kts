@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version "2.0.21"
    id("jvm-test-suite")
    id("idea")
}

repositories {
    mavenCentral()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

dependencies {
    // import Rewrite's bill of materials.
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:2.23.1"))

    // rewrite-java dependencies only necessary for Java Recipe development
    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-kotlin")

    // You only need the version that corresponds to your current
    // Java version. It is fine to add all of them, though, as
    // they can coexist on a classpath.
    runtimeOnly("org.openrewrite:rewrite-java-17")


    // For authoring tests for any kind of Recipe
    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.springframework.data:spring-data-commons:3.4.0")
    testImplementation("org.springframework.data:spring-data-neo4j:7.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.12")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}
