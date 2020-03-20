import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "eu.tib"
version = "0.0.1-SNAPSHOT"

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java)
    .kotlinPluginVersion

val neo4jVersion = "3.5.+" // should match version in Dockerfile
val springDataNeo4jVersion = "5.2.5"
val springSecurityOAuthVersion = "2.3.8"
val junitVersion = "5.6.0"
val testContainersVersion = "1.13.0"

plugins {
    kotlin("jvm") version "1.3.70"
    kotlin("plugin.spring") version "1.3.70"
    // Add no-arg annotations to @Entity, @Embeddable and @MappedSuperclass:
    kotlin("plugin.jpa") version "1.3.70"
    id("org.springframework.boot") version "2.2.5.RELEASE"
    id("com.coditory.integration-test") version "1.0.8"
    id("org.asciidoctor.jvm.convert") version "3.1.0"
    id("com.palantir.docker") version "0.25.0"
    id("com.diffplug.gradle.spotless") version "3.27.2"
    jacoco
    war
}

apply {
    plugin("io.spring.dependency-management")
}

configurations {
    // The Asciidoctor Gradle plug-in does not create it anymore, so we have to...
    create("asciidoctor")
}

dependencies {
    // BOMs
    implementation("org.junit:junit-bom:$junitVersion")

    //
    // Runtime
    //
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(module = "neo4j-ogm-http-driver")
    }
    implementation("org.neo4j:neo4j-ogm-bolt-native-types")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security.oauth:spring-security-oauth2:$springSecurityOAuthVersion.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.data:spring-data-neo4j:$springDataNeo4jVersion.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // JAXB stuff. Was removed from Java 9. Seems to be needed for OAuth2.
    implementation("javax.xml.bind:jaxb-api:2.3.0")
    implementation("javax.activation:activation:1.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:2.3.0")

    // Add Tomcat as "provided" runtime so that we can deploy as WAR
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")

    //
    // Testing
    //
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-junit-jupiter")

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
    compile("net.nprod:rdf4k:0.0.9")
}

val snippetsDir = file("build/generated-snippets")

allprojects {
    repositories {
        jcenter()
        maven { setUrl("https://dl.bintray.com/bjonnh/RDF4K") }
    }
}

jacoco {
    // Upgrade to a newer JaCoCo version, as the one provided by the Gradle
    // plug-in does not support ignoring Kotlin-generated methods.
    // TODO: Remove setting when the default version changes to at least that.
    toolVersion = "0.8.5"
}

tasks {
    val build by existing
    val integrationTest by existing
    val war by existing

    withType(KotlinCompile::class.java).configureEach {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType(Test::class.java).configureEach {
        useJUnitPlatform()

        outputs.dir(snippetsDir)
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
                "source-highlighter" to "highlightjs",
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
        })
    }

    docker {
        dependsOn(build.get())
        name = "orkg/prototype"
        buildArgs(
            mapOf(
                "PROJECT_NAME" to project.name,
                "VERSION" to "$version"
            )
        )
        files(war.get().outputs)
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
