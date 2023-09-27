@file:Suppress("UnstableApiUsage")

import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.orkg.gradle.withSnippets
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

group = "eu.tib"
version = "0.38.0"

val springSecurityOAuthVersion = "2.5.2"

val containerRegistryLocation = "registry.gitlab.com/tibhannover/orkg/orkg-backend"
val dockerImageTag: String? by project

// Support downloading JavaDoc artifacts by enabling it via Gradle properties
val downloadJavadoc: String? by project

plugins {
    id("org.orkg.kotlin-conventions")
    id("org.orkg.neo4j-conventions")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("idea")
    id("jacoco-report-aggregation")
    alias(libs.plugins.spring.boot)

    id("org.jetbrains.dokka") version "0.10.1"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    id("org.asciidoctor.jvm.gems") version "3.3.2"
    id("com.google.cloud.tools.jib") version "3.1.1"
    // The taskinfo plugin currently does not work with Gradle 7.6: https://gitlab.com/barfuin/gradle-taskinfo/-/issues/20
    // It was used only occasionally for debugging, and can be re-enabled again later (if needed).
    // id("org.barfuin.gradle.taskinfo") version "1.2.0"
    id("com.diffplug.spotless")
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

val restdocs by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val asciidoctor by configurations.creating

idea {
    module {
        isDownloadJavadoc = downloadJavadoc?.let(String::toBoolean) ?: false
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-application")))
                implementation("org.springframework.security:spring-security-test")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    // Disable JUnit 4 (aka Vintage)
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
                    // exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
            }
        }
        val integrationTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(testFixtures(project(":testing:spring")))
                implementation(testFixtures(project(":graph:graph-application")))
                implementation(project(":identity-management:idm-application"))
                implementation(project(":identity-management:idm-adapter-output-spring-data-jpa")) // for JpaUserAdapter
                implementation(project(":discussions:discussions-adapter-output-spring-data-jpa-postgres"))
                implementation(project(":media-storage:media-storage-adapter-output-spring-data-jpa-postgres"))
                implementation(project(":feature-flags:feature-flags-ports"))
                implementation("org.springframework.security:spring-security-test")
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    // Disable JUnit 4 (aka Vintage)
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
                    // exclude(module = "mockito-core")
                }
                implementation(libs.spring.mockk)
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                implementation("org.postgresql:postgresql")
                implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
                    exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
                }
                implementation(libs.spring.boot.starter.neo4j.migrations)
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

val bootJar by tasks.getting(BootJar::class) {
    enabled = true
    archiveClassifier.set("boot")
}

val jar by tasks.getting(Jar::class) {
    // Build regular JAR, so we can share the application class and database migrations (e.g. for use in tests)
    enabled = true
}

dependencies {
    // Platform alignment for ORKG components
    api(platform(project(":platform")))
    testApi(enforcedPlatform(libs.junit5.bom))
    "integrationTestApi"(platform(project(":platform")))
    "integrationTestApi"(enforcedPlatform(libs.junit5.bom))

    kapt(platform(project(":platform")))

    // Upgrade for security reasons. Can be removed after Spring upgrade.
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.19.0"))

    // This project is essentially a "configuration" project in Spring's sense, so we depend on all components:
    implementation(project(":common:exceptions"))
    compileOnly(project(":identity-management:idm-application")) // only ports used, replace later
    runtimeOnly(project(":identity-management:idm-adapter-input-rest-spring-security"))
    runtimeOnly(project(":identity-management:idm-adapter-output-spring-data-jpa"))
    implementation(project(":graph:graph-application"))
    implementation(project(":graph:graph-adapter-input-rest-spring-mvc"))
    implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))
    implementation(project(":discussions:discussions-adapter-output-spring-data-jpa-postgres"))
    implementation(project(":media-storage:media-storage-adapter-output-spring-data-jpa-postgres"))
    implementation(project(":feature-flags:feature-flags-ports"))
    implementation(project(":feature-flags:feature-flags-adapter-output-spring-properties"))
    implementation(project(":rdf-export:rdf-export-application"))
    implementation(project(":rdf-export:rdf-export-adapter-input-rest-spring-mvc"))
    implementation(project(":licenses:licenses-application"))
    implementation(project(":licenses:licenses-adapter-input-rest-spring-mvc"))
    implementation(project(":licenses:licenses-adapter-output-spring"))
    implementation(project(":widget"))

    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
    }
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security.oauth:spring-security-oauth2:$springSecurityOAuthVersion.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.data:spring-data-neo4j")
    implementation(libs.spring.boot.starter.neo4j.migrations)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // JAXB stuff. Was removed from Java 9. Seems to be needed for OAuth2.
    implementation(libs.bundles.jaxb)
    implementation(libs.annotations.jsr305) // provides @Nullable and other JSR305 annotations
    // File uploads
    implementation("commons-fileupload:commons-fileupload:1.5")
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
    // Data Faker
    implementation("net.datafaker:datafaker:1.7.0")
    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jolokia:jolokia-core")
    implementation("io.micrometer:micrometer-registry-jmx")
    //
    // Testing
    //
    // Note: Version Catalogs are not yet supported in the test suites plugin
    "integrationTestImplementation"(libs.bundles.testcontainers)
    "integrationTestImplementation"(libs.bundles.kotest)
    //
    // Documentation
    //
    asciidoctor("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.7.RELEASE")
    restdocs(project(withSnippets(":graph:graph-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":rdf-export:rdf-export-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":licenses:licenses-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":widget")))
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

tasks {
    val integrationTest by existing(Test::class) {
        // Declare snippets generated by Spring RestDoc as output, so that they can be cached.
        outputs.dir(layout.buildDirectory.dir("generated-snippets")).withPropertyName("snippetsOutputDirectory")
    }

    // Wire tasks, so they always generate a coverage report and print the coverage on build
    val check by existing {
        dependsOn(named<JacocoReport>("testCodeCoverageReport"))
    }

    withType<Test>().configureEach {
        useJUnitPlatform {
            // Exclude test marked as "development", because those are for features only used in dev, and rather slow.
            excludeTags = setOf("development")
        }
    }

    named("bootRun", BootRun::class.java).configure {
        args("--spring.profiles.active=development")
    }

    register("populatePostgresDatabase").configure {
        group = "datagen"
        description = "Populates the postgres database with live data and randomly generates required user information."
        doFirst {
            named<BootRun>("bootRun").configure {
                args("--spring.profiles.active=development,datagen")
            }
        }
        finalizedBy("bootRun")
    }

    register("runListMigrations").configure {
        group = "migration"
        description = "Migrates the current database to use list entities."
        doFirst {
            named<BootRun>("bootRun").configure {
                args("--spring.profiles.active=development,listMigrations")
            }
        }
        finalizedBy("bootRun")
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

    val generatedSnippets = fileTree(layout.buildDirectory.dir("generated-snippets")) {
        include("**/*.adoc")
        builtBy(integrationTest)
    }

    val aggregatedSnippetsDir = layout.buildDirectory.dir("restdocs-snippets")

    val aggregateRestDocsSnippets by registering(Copy::class) {
        group = "documentation"

        // Explicitly add a dependency on the configuration, because it will not resolve otherwise.
        dependsOn(restdocs)

        // Obtain the list of ZIP files (and extract them). This only works if the configuration was resolved.
        restdocs.files.forEach {
            from(zipTree(it)) {
                include("**/*.adoc")
            }
        }
        from(generatedSnippets)
        into(aggregatedSnippetsDir)
    }

    named("asciidoctor", AsciidoctorTask::class).configure {
        // Declare all generated Asciidoc snippets as inputs. This connects the tasks, so dependsOn() is not required.
        // Other outputs are filtered, because they do not affect the output of this task.
        val docSources = files(sourceDir).asFileTree.matching { include("**/*.adoc") }
        inputs.files(docSources, aggregateRestDocsSnippets)
            .withPathSensitivity(PathSensitivity.RELATIVE)
            .ignoreEmptyDirectories()
            .withPropertyName("asciidocFiles")

        configurations("asciidoctor")
        // TODO: Use {includedir} in documentation, change strategy afterwards
        baseDirFollowsSourceFile()

        asciidoctorj {
            modules {
                diagram.use()
                diagram.version("2.2.10")
            }
            fatalWarnings(missingIncludes())

            // Work-around for JRE 16+, because Java's internal APIs are no longer available due to JPMS.
            // This should be fixed in the Asciidoctor plugin, but never was.
            inProcess = org.asciidoctor.gradle.base.process.ProcessMode.JAVA_EXEC
            forkOptions {
                jvmArgs(
                    "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
                    "--add-opens", "java.base/java.io=ALL-UNNAMED",
                    "--add-opens", "java.base/java.security=ALL-UNNAMED",
                )
            }
        }

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
                "encoding" to "utf-8",
                "snippets" to aggregatedSnippetsDir,
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
}

jib {
    from.image = "gcr.io/distroless/java17"
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

kapt {
    // Turn off the discovery of annotation processors in the compile classpath. This means that all annotation
    // processors need to be listed manually.
    // The problem seems to be that the Neo4j annotation processor leaks into the classpath.
    // TODO: Check if classpath leakage is fixed in later versions.
    includeCompileClasspath = false
}

springBoot {
    buildInfo()
}

normalization {
    runtimeClasspath {
        // This only affects build cache key calculation. The file will be included in the build.
        ignore("**/build-info.properties")
    }
}
