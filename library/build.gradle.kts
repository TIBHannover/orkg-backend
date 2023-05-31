val neo4jVersion = "3.5.+" // should match version in Dockerfile
val springDataNeo4jVersion = "5.3.4"
val springSecurityOAuthVersion = "2.3.8"

plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")

    id("idea")

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
    api(project(":graph:application"))
    api(project(":graph:adapter-input-rest-spring-mvc")) // e.g. BaseController

    // Upgrade for security reasons. Can be removed after Spring upgrade.
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.19.0"))

    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)

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
    implementation("commons-fileupload:commons-fileupload:1.5") {
        because("Required for custom CommonsMultipartResolver implementation")
        because("Fixes a security vulnerability")
    }
    // JAXB stuff. Was removed from Java 9. Seems to be needed for OAuth2.
    implementation(libs.bundles.jaxb)
    implementation(libs.annotations.jsr305) // provides @Nullable and other JSR305 annotations
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
}

tasks {
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
