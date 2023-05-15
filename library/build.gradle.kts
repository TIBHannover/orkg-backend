val neo4jVersion = "3.5.+" // should match version in Dockerfile
val springDataNeo4jVersion = "5.3.4"
val springSecurityOAuthVersion = "2.3.8"

plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("idea")

    id("org.jetbrains.dokka") version "0.10.1"
    id("com.coditory.integration-test") version "1.2.1"
    id("com.diffplug.spotless")
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    // Platform alignment for ORKG components
    api(platform(project(":platform")))

    api(project(":common:exceptions"))
    api(project(":identity-management:application"))
    api(project(":identity-management:adapter-output-spring-data-jpa")) // required by OrganizationEntity

    // Upgrade for security reasons. Can be removed after Spring upgrade.
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.19.0"))

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
    implementation("org.keycloak:keycloak-admin-client:21.1.1")
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
    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("io.github.stepio.coffee-boots:coffee-boots:3.0.0")
    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jolokia:jolokia-core")
    // Data Faker
    implementation("net.datafaker:datafaker:1.7.0")
    //
    // Testing
    //
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.bundles.kotest)
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Disable JUnit 4 (aka Vintage)
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
        // exclude(module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:2.0.1")
    testImplementation("com.redfin:contractual:3.0.0")
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
