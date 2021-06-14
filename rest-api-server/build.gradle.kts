import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "eu.tib"
version = "0.5.1-SNAPSHOT"

val neo4jVersion = "3.5.+" // should match version in Dockerfile
val springDataNeo4jVersion = "5.3.4"
val springSecurityOAuthVersion = "2.3.8"
val testContainersVersion = "1.14.3"

val containerRegistryLocation = "registry.gitlab.com/tibhannover/orkg/orkg-backend"
val dockerImageTag: String? by project

plugins {
    id("org.orkg.spring-conventions")

    jacoco
    id("org.jetbrains.dokka") version "0.10.1"
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("com.coditory.integration-test") version "1.1.8"
    id("de.jansauer.printcoverage") version "2.0.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("com.palantir.docker") version "0.25.0"
    id("com.google.cloud.tools.jib") version "3.1.1"
    id("org.barfuin.gradle.taskinfo") version "1.2.0"
    id("com.diffplug.spotless")
}

apply {
    plugin("io.spring.dependency-management")
}

configurations {
    // The Asciidoctor Gradle plug-in does not create it anymore, so we have to...
    create("asciidoctor")
}

dependencies {
    // Platform alignment for ORKG components
    api(platform(project(":platform")))

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
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
        // exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.ninja-squad:springmockk:2.0.1")

    testImplementation("org.neo4j:neo4j-ogm-embedded-driver")
    testImplementation("org.neo4j:neo4j-ogm-embedded-native-types")
    testImplementation("org.neo4j:neo4j:$neo4jVersion")
    // TestContainers
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")

    //
    // Documentation
    //
    "asciidoctor"("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.4.RELEASE")
}

val snippetsDir = file("build/generated-snippets")

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

tasks {
    val build by existing
    val integrationTest by existing

    // Wire tasks so they always generate a coverage report and print the coverage on build
    val check by existing { dependsOn(jacocoTestCoverageVerification, jacocoTestReport, printCoverage) }
    val jacocoTestCoverageVerification by existing { mustRunAfter(test, integrationTest) }
    val jacocoTestReport by existing { mustRunAfter(test, integrationTest) }
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

    named("jacocoTestReport", JacocoReport::class).configure {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
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
