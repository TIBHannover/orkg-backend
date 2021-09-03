import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "eu.tib"
version = "0.6.2-SNAPSHOT"

val springSecurityOAuthVersion = "2.3.8"
val testContainersVersion = "1.15.3"

val containerRegistryLocation = "registry.gitlab.com/tibhannover/orkg/orkg-backend"
val dockerImageTag: String? by project

plugins {
    id("org.orkg.spring-conventions")

    id("org.jetbrains.dokka") version "0.10.1"
    id("com.coditory.integration-test") version "1.2.1"
    id("de.jansauer.printcoverage") version "2.0.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.palantir.docker") version "0.25.0"
    id("com.google.cloud.tools.jib") version "3.1.1"
    id("org.barfuin.gradle.taskinfo") version "1.2.0"
    id("com.diffplug.spotless")
}

configurations {
    // The Asciidoctor Gradle plug-in does not create it anymore, so we have to...
    create("asciidoctor")
}

dependencies {
    // Platform alignment for ORKG components
    api(platform(project(":platform")))

    implementation(project(":application:shared"))
    implementation(project(":application:core"))
    implementation(project(":adapters:input:core"))
    implementation(project(":adapters:output:core"))

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(module = "neo4j-ogm-http-driver")
    }
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security.oauth:spring-security-oauth2:$springSecurityOAuthVersion.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // JAXB stuff. Was removed from Java 9. Seems to be needed for OAuth2.
    implementation("javax.xml.bind:jaxb-api:2.3.0")
    implementation("javax.activation:activation:1.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.0")
    // RDF
    implementation("org.eclipse.rdf4j:rdf4j-client:3.6.3")
    implementation("io.github.config4k:config4k:0.4.2") {
        because("Required for parsing the essential entity configuration")
    }
    //
    // Testing
    //
    testImplementation(testFixtures(project(":adapters:output:core")))

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    // TestContainers
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
    testImplementation("org.testcontainers:neo4j:$testContainersVersion")

    //
    // Documentation
    //
    "asciidoctor"("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.4.RELEASE")
}

val snippetsDir = file("build/generated-snippets")

tasks {
    val build by existing
    val integrationTest by existing

    // Wire tasks so they always generate a coverage report and print the coverage on build
    val check by existing { dependsOn(jacocoTestCoverageVerification, jacocoTestReport, printCoverage) }
    val jacocoTestCoverageVerification by existing { mustRunAfter(test, integrationTest) }
    val printCoverage by existing { mustRunAfter(jacocoTestCoverageVerification) }

    withType(KotlinCompile::class.java).configureEach {
        kotlinOptions.jvmTarget = "${JavaVersion.VERSION_11}"
    }

    withType(Test::class.java).configureEach {
        outputs.dir(snippetsDir)
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

        sources(delegateClosureOf<PatternSet> {
            exclude("parts/**")
            include("*.adoc")
            include("api-doc/*.adoc")
            include("architecture/*.adoc")
            include("references/*.adoc")
        })
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
            ktlint()
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
