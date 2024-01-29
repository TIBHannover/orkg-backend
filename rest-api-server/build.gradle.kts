@file:Suppress("UnstableApiUsage")

import org.asciidoctor.gradle.jvm.AsciidoctorTask
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

version = "0.46.0"

val springSecurityOAuthVersion = "2.5.2"

// Support downloading JavaDoc artifacts by enabling it via Gradle properties
val downloadJavadoc: String? by project

fun withSnippets(path: String): Map<String, String> = mapOf("path" to path, "configuration" to "restdocs")

plugins {
    id("org.orkg.gradle.spring-boot-application")
    id("idea")
    id("jacoco-report-aggregation")
    id("org.orkg.gradle.asciidoctor")
    id("org.orkg.gradle.docker-image")

    // The taskinfo plugin currently does not work with Gradle 7.6: https://gitlab.com/barfuin/gradle-taskinfo/-/issues/20
    // It was used only occasionally for debugging, and can be re-enabled again later (if needed).
    // id("org.barfuin.gradle.taskinfo") version "1.2.0"
    id("com.diffplug.spotless")
}

val restdocs: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val liquibase: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val neo4jMigrations: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val runtimeClasspath by configurations.getting {
    extendsFrom(liquibase)
    extendsFrom(neo4jMigrations)
}

idea {
    module {
        isDownloadJavadoc = downloadJavadoc?.let(String::toBoolean) ?: false
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
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
            dependencies {
                implementation(project())
                implementation(project(":common"))
                implementation(testFixtures(project(":testing:spring")))
                implementation(project(":migrations:liquibase"))
                implementation(project(":migrations:neo4j-migrations"))
                implementation(project(":graph:graph-core-model"))
                implementation(project(":graph:graph-core-services"))
                implementation(project(":graph:graph-ports-input"))
                implementation(project(":graph:graph-ports-output"))
                implementation(project(":content-types:content-types-adapter-input-rest-spring-mvc"))
                implementation(project(":content-types:content-types-ports-input"))
                implementation(project(":content-types:content-types-ports-output"))
                implementation(project(":identity-management:idm-ports-input"))
                implementation(project(":identity-management:idm-ports-output"))
                implementation(project(":identity-management:idm-core-model"))
                implementation(project(":identity-management:idm-adapter-output-spring-data-jpa")) // for JpaUserAdapter
                implementation(project(":community:community-core-model"))
                implementation(project(":community:community-ports-input"))
                implementation(project(":community:community-ports-output")) // for CuratorRepository
                implementation(project(":media-storage:media-storage-core-model"))
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
    testApi(enforcedPlatform(libs.junit5.bom))
    "integrationTestApi"(enforcedPlatform(libs.junit5.bom))

    kapt(platform("org.orkg:platform"))

    // Upgrade for security reasons. Can be removed after Spring upgrade.
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.19.0"))

    // This project is essentially a "configuration" project in Spring's sense, so we depend on all components:
    implementation(project(":common"))
    implementation(project(":common:serialization"))

    runtimeOnly(project(":community:community-adapter-input-rest-spring-mvc"))
    implementation(project(":community:community-ports-input"))
    implementation(project(":community:community-core-model"))
    runtimeOnly(project(":community:community-core-services"))
    implementation(project(":community:community-ports-output"))
    runtimeOnly(project(":community:community-adapter-output-spring-data-jpa"))

    implementation(project(":content-types:content-types-adapter-input-rest-spring-mvc"))
    implementation(project(":content-types:content-types-adapter-output-spring-data-neo4j-sdn6"))
    implementation(project(":content-types:content-types-adapter-output-web"))
    implementation(project(":content-types:content-types-core-model"))
    implementation(project(":content-types:content-types-core-services"))
    implementation(project(":content-types:content-types-ports-input"))
    implementation(project(":content-types:content-types-ports-output"))

    implementation(project(":data-export:data-export-adapters"))
    implementation(project(":data-export:data-export-core"))
    implementation(project(":data-export:data-export-ports-input"))

    runtimeOnly(project(":discussions:discussions-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":discussions:discussions-core-services"))
    runtimeOnly(project(":discussions:discussions-adapter-output-spring-data-jpa"))

    implementation(project(":feature-flags:feature-flags-ports")) // for cache warmup
    runtimeOnly(project(":feature-flags:feature-flags-adapter-output-spring-properties"))

    runtimeOnly(project(":graph:graph-adapter-input-rest-spring-mvc"))
    implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-services"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))

    implementation(project(":identity-management:idm-ports-input")) // for PostgresDummyDataSetup
    runtimeOnly(project(":identity-management:idm-adapter-input-rest-spring-security"))
    runtimeOnly(project(":identity-management:idm-core-services"))
    runtimeOnly(project(":identity-management:idm-adapter-output-spring-data-jpa"))

    runtimeOnly(project(":licenses:licenses-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":licenses:licenses-core-services"))

    implementation(project(":media-storage:media-storage-adapter-input-serialization"))
    implementation(project(":media-storage:media-storage-adapter-output-spring-data-jpa"))
    implementation(project(":media-storage:media-storage-core-model"))
    implementation(project(":media-storage:media-storage-ports-input"))
    implementation(project(":media-storage:media-storage-ports-output"))
    implementation(project(":media-storage:media-storage-core-services"))

    runtimeOnly(project(":profiling:profiling-adapter-output"))
    runtimeOnly(project(":profiling:profiling-adapter-output-spring-data-neo4j-sdn6"))
    runtimeOnly(project(":profiling:profiling-core-model"))
    runtimeOnly(project(":profiling:profiling-core-services"))
    runtimeOnly(project(":profiling:profiling-ports-output"))

    runtimeOnly(project(":widget"))

    // Migrations
    liquibase(project(mapOf("path" to ":migrations:liquibase", "configuration" to "liquibase")))
    neo4jMigrations(project(mapOf("path" to ":migrations:neo4j-migrations", "configuration" to "neo4jMigrations")))

    implementation(libs.forkhandles.result4k)
    implementation(libs.forkhandles.values4k)

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly(libs.liquibase)
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
    implementation(libs.javax.activation)
    implementation(libs.annotations.jsr305) // provides @Nullable and other JSR305 annotations
    // File uploads
    implementation("commons-fileupload:commons-fileupload:1.5")
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
    "integrationTestRuntimeOnly"(libs.bundles.testcontainers)
    "integrationTestRuntimeOnly"(libs.bundles.kotest)
    "integrationTestApi"("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
    //
    // Documentation
    //
    asciidoctor("org.springframework.restdocs:spring-restdocs-asciidoctor:2.0.7.RELEASE")
    restdocs(project(withSnippets(":common")))
    restdocs(project(withSnippets(":graph:graph-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":data-export:data-export-adapters")))
    restdocs(project(withSnippets(":licenses:licenses-adapter-input-rest-spring-mvc")))
    restdocs(project(withSnippets(":content-types:content-types-adapter-input-rest-spring-mvc")))
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

    register("profileNeo4jRepositories").configure {
        group = "profiling"
        description = "Profiles neo4j repositories."
        doFirst {
            named<BootRun>("bootRun").configure {
                args("--spring.profiles.active=development,profileRepositories,profileNeo4jRepositories")
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
