import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.springframework.boot.gradle.tasks.run.BootRun

group = "eu.tib"
version = "0.14.0"

val neo4jVersion = "3.5.+" // should match version in Dockerfile
val springDataNeo4jVersion = "5.3.4"
val springSecurityOAuthVersion = "2.3.8"
val testContainersVersion = "1.17.3"

val containerRegistryLocation = "registry.gitlab.com/tibhannover/orkg/orkg-backend"
val dockerImageTag: String? by project

// Support downloading JavaDoc artifacts by enabling it via Gradle properties
val downloadJavadoc: String? by project

plugins {
    id("org.orkg.spring-conventions")
    id("idea")

    id("org.jetbrains.dokka") version "0.10.1"
    id("com.coditory.integration-test") version "1.2.1"
    id("de.jansauer.printcoverage") version "2.0.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.palantir.docker") version "0.25.0"
    id("com.google.cloud.tools.jib") version "3.1.1"
    id("org.barfuin.gradle.taskinfo") version "1.2.0"
    id("com.diffplug.spotless")
}

// SECURITY: Upgrade Log4j to version >= 2.15.0 due to a vulnerability. It is not used in ORKG, this is just
//           a safety measure. The line should be removed when Spring upgrades to a version higher than this one.
extra["log4j2.version"] = "2.15.0"

configurations {
    // The Asciidoctor Gradle plug-in does not create it anymore, so we have to...
    create("asciidoctor")
}

idea {
    module {
        isDownloadJavadoc = downloadJavadoc?.let(String::toBoolean) ?: false
    }
}

dependencies {
    // Platform alignment for ORKG components
    api(platform(project(":platform")))

    implementation(platform(libs.forkhandles.bom))
    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)

    kapt("org.springframework.boot:spring-boot-configuration-processor")

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
    //
    // Testing
    //
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation(libs.bundles.testcontainers)
    testImplementation(libs.kotest.assertions.core)

    //
    // Documentation
    //
    "asciidoctor"("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.4.RELEASE")

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

val snippetsDir = file("build/generated-snippets")

tasks {
    val build by existing
    val integrationTest by existing

    // Wire tasks so they always generate a coverage report and print the coverage on build
    val check by existing { dependsOn(jacocoTestCoverageVerification, jacocoTestReport, printCoverage) }
    val jacocoTestCoverageVerification by existing { mustRunAfter(test, integrationTest) }
    val printCoverage by existing { mustRunAfter(jacocoTestCoverageVerification) }

    withType(Test::class.java).configureEach {
        outputs.dir(snippetsDir)
        useJUnitPlatform {
            // Exclude test marked as "development", because those are for features only used in dev, and rather slow.
            excludeTags = setOf("development")
        }
    }

    named("bootJar", org.springframework.boot.gradle.tasks.bundling.BootJar::class) {
        enabled = true
        archiveClassifier.set("boot")
    }

    named("bootRun", BootRun::class.java) {
        args("--spring.profiles.active=development")
    }

    named("dokka", org.jetbrains.dokka.gradle.DokkaTask::class) {
        outputFormat = "html"
        configuration {
            includes = listOf("packages.md")
        }
    }

    jacocoTestReport {
        mustRunAfter(test, integrationTest)
        reports {
            html.required.set(true)
        }
    }

    named("asciidoctor", AsciidoctorTask::class).configure {
        inputs.dir(snippetsDir)
        dependsOn(integrationTest)
        configurations("asciidoctor")
        // TODO: Use {includedir} in documentation, change strategy afterwards
        baseDirFollowsSourceFile()

        // outputs.upToDateWhen { false }
        outputOptions {
            backends("html5")
        }

        options(mapOf("doctype" to "book"))

        attributes(
            mapOf(
                "source-highlighter" to "rouge",
                "coderay-linenums-mode" to "table",
                "toc" to "left",
                "icons" to "font",
                "linkattrs" to "true",
                "encoding" to "utf-8"
            )
        )

        sources(
            delegateClosureOf<PatternSet> {
                exclude("parts/**")
                include("*.adoc")
                include("api-doc/*.adoc")
                include("architecture/*.adoc")
                include("references/*.adoc")
            }
        )
    }

    docker {
        dependsOn(build.get())
        pull(true)
        val tag = dockerImageTag ?: "latest"
        name = "$containerRegistryLocation:$tag"
        files(bootJar.get().outputs)
        copySpec.from("build/libs").into("build/libs")
    }

    jib {
        to {
            image = containerRegistryLocation
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
}

asciidoctorj {
    modules {
        diagram.use()
    }
}

kapt {
    // Turn off the discovery of annotation processors in the compile classpath. This means that all annotation
    // processors need to be listed manually.
    // The problem seems to be that the Neo4j annotation processor leaks into the classpath.
    // TODO: Check if classpath leakage is fixed in later versions.
    includeCompileClasspath = false
}
