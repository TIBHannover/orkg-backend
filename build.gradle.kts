import org.asciidoctor.gradle.AsciidoctorTask
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "eu.tib"
version = "0.0.1-SNAPSHOT"

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java)
    .kotlinPluginVersion

val neo4jVersion = "3.3.+" // should match version in Dockerfile

plugins {
    kotlin("jvm") version "1.3.0"
    kotlin("plugin.spring") version "1.3.0"
    id("org.springframework.boot") version "2.0.6.RELEASE"
    id("org.asciidoctor.convert") version "1.5.8.1"
    id("com.palantir.docker") version "0.19.2"
    war
}

apply {
    plugin("io.spring.dependency-management")
}

dependencies {
    // BOMs
    implementation("org.junit:junit-bom:5.3.1")

    //
    // Runtime
    //
    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation(kotlin("reflect", kotlinVersion))

    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(module = "neo4j-ogm-http-driver")
    }
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.data:spring-data-neo4j:5.0.1.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.eclipse.rdf4j:rdf4j-repository-sparql:2.2.4")
    // Add Tomcat as "provided" runtime so that we can deploy as WAR
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")

    //
    // Testing
    //
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.neo4j:neo4j-ogm-embedded-driver")
    testImplementation("org.neo4j:neo4j:$neo4jVersion")

    //
    // Documentation
    //
    asciidoctor("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.2.RELEASE")
}

val snippetsDir = file("build/generated-snippets")


asciidoctorj {
    version = "1.5.6" // AsciiDoctor (Ruby!) version
}

allprojects {
    repositories {
        jcenter()
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Test> {
        systemProperty("spring.profiles.active", "testing")

        useJUnitPlatform()

        outputs.dir(snippetsDir)
    }

    "asciidoctor"(AsciidoctorTask::class) {
        inputs.dir(snippetsDir)
        dependsOn("test")

        //outputs.upToDateWhen { false }
        backends("html5")

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
        dependsOn(tasks["build"])
        name = "orkg/prototype"
        buildArgs(
            mapOf(
                "PROJECT_NAME" to project.name,
                "VERSION" to "$version"
            )
        )
        files(tasks["jar"].outputs)
    }
}
