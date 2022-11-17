import org.springframework.boot.gradle.plugin.SpringBootPlugin

group = "eu.tib"
version = "0.17.2"

val neo4jVersion = "3.5.+" // should match version in Dockerfile
val springDataNeo4jVersion = "5.3.4"
val springSecurityOAuthVersion = "2.3.8"

plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot") version libs.versions.spring.boot.get() apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    id("idea")

    id("org.jetbrains.dokka") version "0.10.1"
    id("com.coditory.integration-test") version "1.2.1"
    id("com.diffplug.spotless")
}

// SECURITY: Upgrade Log4j to version >= 2.15.0 due to a vulnerability. It is not used in ORKG, this is just
//           a safety measure. The line should be removed when Spring upgrades to a version higher than this one.
extra["log4j2.version"] = "2.15.0"
extra["postgresql.version"] = "42.2.25"

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    // Platform alignment for ORKG components
    api(platform(project(":platform")))

    api(platform(SpringBootPlugin.BOM_COORDINATES))

    implementation(platform(kotlin("bom", "1.7.10")))
    implementation(platform(libs.forkhandles.bom))
    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)

    kapt("org.springframework.boot:spring-boot-configuration-processor:${libs.versions.spring.boot.get()}")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(module = "neo4j-ogm-http-driver")
    }
    implementation("org.neo4j:neo4j-ogm-bolt-native-types")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security.oauth:spring-security-oauth2:$springSecurityOAuthVersion.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.data:spring-data-neo4j:$springDataNeo4jVersion.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // JAXB stuff. Was removed from Java 9. Seems to be needed for OAuth2.
    implementation(libs.bundles.jaxb)
    implementation(libs.annotations.jsr305) // provides @Nullable and other JSR305 annotations
    // RDF
    implementation("org.eclipse.rdf4j:rdf4j-client:3.7.7") {
        exclude(group = "commons-collections", module = "commons-collections") // Version 3, vulnerable
    }
    implementation("io.github.config4k:config4k:0.4.2") {
        because("Required for parsing the essential entity configuration")
    }
    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jolokia:jolokia-core")
    implementation("io.hawt:hawtio-springboot:2.15.1")
    //
    // Testing
    //
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.kotest.assertions.core)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Disable JUnit 4 (aka Vintage)
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
        // exclude(module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:2.0.1")

    // Security-related adjustments
    testImplementation("junit:junit") {
        version {
            strictly("[4.13.1,5.0[")
            because("Vulnerable to CVE-2020-15250")
        }
    }
    implementation("commons-beanutils:commons-beanutils") {
        exclude(group = "commons-collections", module = "commons-collections")
        version {
            strictly("[1.9.4,2[")
            because("Vulnerable to CVE-2019-10086, CVE-2014-0114")
        }
    }
    implementation("org.apache.commons:commons-collections4") {
        // group is either common-collections or org.apache.commons
        version {
            strictly("[4.3,5.0[")
            because("Vulnerable to Cx78f40514-81ff, CWE-674")
        }
    }
    implementation("org.apache.httpcomponents:httpclient") {
        version {
            strictly("[4.5.13,5.0[")
            because("Vulnerable to CVE-2020-13956")
        }
    }
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform {
            // Exclude test marked as "development", because those are for features only used in dev, and rather slow.
            excludeTags = setOf("development")
        }
    }

    named("dokka", org.jetbrains.dokka.gradle.DokkaTask::class).configure {
        outputFormat = "html"
        configuration {
            includes = listOf("packages.md")
        }
    }

    withType<JacocoReport>().configureEach {
        reports {
            html.required.set(true)
        }
    }
}

spotless {
    kotlin {
        ktlint().userData(
            // TODO: This should be moved to .editorconfig once the Gradle plug-in supports that.
            mapOf(
                "ij_kotlin_code_style_defaults" to "KOTLIN_OFFICIAL",
                // Disable some rules to keep the changes minimal
                "disabled_rules" to "no-wildcard-imports,filename,import-ordering,indent",
                "ij_kotlin_imports_layout" to "*,^",
            )
        )
    }
    kotlinGradle {
        ktlint()
    }
}

kapt {
    // Turn off the discovery of annotation processors in the compile classpath. This means that all annotation
    // processors need to be listed manually.
    // The problem seems to be that the Neo4j annotation processor leaks into the classpath.
    // TODO: Check if classpath leakage is fixed in later versions.
    includeCompileClasspath = false
}