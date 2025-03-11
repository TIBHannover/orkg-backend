@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

version = "0.78.0"

plugins {
    id("org.orkg.gradle.spring-boot-application")
    id("jacoco-report-aggregation")
    id("org.orkg.gradle.docker-image")

    // The taskinfo plugin currently does not work with Gradle 7.6: https://gitlab.com/barfuin/gradle-taskinfo/-/issues/20
    // It was used only occasionally for debugging, and can be re-enabled again later (if needed).
    // id("org.barfuin.gradle.taskinfo") version "1.2.0"
    id("com.diffplug.spotless")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(testFixtures(project(":common:spring-webmvc")))
                implementation(testFixtures(project(":testing:spring")))

                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.jetbrains.kotlin:kotlin-reflect")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation(project(":common:serialization"))
                implementation("org.springframework.boot:spring-boot-test")
                compileOnly("jakarta.servlet:jakarta.servlet-api")
                runtimeOnly("org.apache.tomcat.embed:tomcat-embed-core")
            }
        }
        val integrationTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
            dependencies {
                implementation(project())
                implementation(project(":common:pagination"))
                implementation(testFixtures(project(":common:testing")))
                implementation(project(":graph:graph-core-services"))
                implementation(project(":graph:graph-core-constants"))
                implementation(project(":graph:graph-ports-output"))
                implementation(testFixtures(project(":graph:graph-adapter-input-rest-spring-mvc")))
                implementation(testFixtures(project(":graph:graph-core-model")))
                implementation(project(":content-types:content-types-ports-output"))
                implementation(project(":community:community-ports-output")) // for CuratorRepository
                implementation(testFixtures(project(":community:community-adapter-input-rest-spring-mvc")))
                implementation(testFixtures(project(":community:community-core-model")))
                implementation(project(":content-types:content-types-adapter-input-representations"))
                implementation("org.springframework.restdocs:spring-restdocs-mockmvc")
                runtimeOnly("org.springframework.boot:spring-boot-starter-test")
                implementation("com.ninja-squad:springmockk")
                runtimeOnly("org.postgresql:postgresql")
                implementation("io.kotest:kotest-assertions-core")
                implementation("dev.forkhandles:values4k")
                implementation("io.rest-assured:rest-assured")
                implementation("org.hamcrest:hamcrest")
                implementation("org.springframework.data:spring-data-commons")
                implementation("org.springframework:spring-web")
                implementation("org.assertj:assertj-core")
                implementation("com.github.dasniko:testcontainers-keycloak")
                implementation("io.rest-assured:json-path")
                implementation("org.keycloak:keycloak-client-common-synced")
                runtimeOnly("org.springframework.boot:spring-boot")
                runtimeOnly(project(":keycloak"))
                implementation("org.springframework:spring-core")
                implementation(project(":media-storage:media-storage-ports-input"))
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

tasks.named<BootJar>("bootJar") {
    enabled = true
    archiveClassifier.set("boot")
}

tasks.named<Jar>("jar") {
    // Build regular JAR, so we can share the application class and database migrations (e.g. for use in tests)
    enabled = true
}

dependencies {
    kapt(platform("org.orkg:platform"))
    api(platform("org.orkg:platform"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib") // "downgrade" from api()

    // This project is essentially a "configuration" project in Spring's sense, so we depend on all components:
    implementation(project(":common:pagination"))
    implementation(project(":common:identifiers"))
    runtimeOnly(project(":common:serialization"))

    runtimeOnly(project(":community:community-adapter-input-keycloak"))
    runtimeOnly(project(":community:community-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":community:community-adapter-input-rest-spring-mvc-legacy"))
    implementation(project(":community:community-ports-input"))
    implementation(project(":community:community-core-model"))
    runtimeOnly(project(":community:community-core-services"))
    runtimeOnly(project(":community:community-core-services-legacy"))
    implementation(project(":community:community-ports-output"))
    runtimeOnly(project(":community:community-adapter-output-spring-data-jpa"))

    runtimeOnly(project(":content-types:content-types-adapter-input-rest-spring-mvc"))
    implementation(project(":content-types:content-types-adapter-output-simcomp"))
    runtimeOnly(project(":content-types:content-types-adapter-output-spring-data-neo4j"))
    runtimeOnly(project(":content-types:content-types-adapter-output-web"))
    runtimeOnly(project(":content-types:content-types-core-model"))
    runtimeOnly(project(":content-types:content-types-core-services"))
    implementation(project(":content-types:content-types-ports-input"))
    runtimeOnly(project(":content-types:content-types-ports-output"))

    runtimeOnly(project(":data-export:data-export-adapters"))
    runtimeOnly(project(":data-export:data-export-core"))
    runtimeOnly(project(":data-export:data-export-ports-input"))

    runtimeOnly(project(":eventbus"))

    runtimeOnly(project(":graph:graph-adapter-input-rest-spring-mvc"))
    implementation(project(":graph:graph-adapter-output-spring-data-neo4j"))
    runtimeOnly(project(":graph:graph-adapter-output-web"))
    implementation(project(":graph:graph-core-constants"))
    implementation(project(":graph:graph-core-model"))
    runtimeOnly(project(":graph:graph-core-services"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))

    runtimeOnly(project(":identity-management:idm-adapter-input-rest-spring-security-legacy"))

    runtimeOnly(project(":licenses:licenses-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":licenses:licenses-core-services"))

    runtimeOnly(project(":statistics:statistics-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":statistics:statistics-adapter-output-spring-data-neo4j"))
    runtimeOnly(project(":statistics:statistics-core-services"))

    runtimeOnly(project(":curation:curation-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":curation:curation-adapter-output-spring-data-neo4j"))
    runtimeOnly(project(":curation:curation-core-services"))

    runtimeOnly(project(":media-storage:media-storage-adapter-input-serialization"))
    runtimeOnly(project(":media-storage:media-storage-adapter-output-spring-data-jpa"))
    implementation(project(":media-storage:media-storage-core-model"))
    runtimeOnly(project(":media-storage:media-storage-ports-input"))
    runtimeOnly(project(":media-storage:media-storage-ports-output"))
    runtimeOnly(project(":media-storage:media-storage-core-services"))

    runtimeOnly(project(":profiling:profiling-adapter-output"))
    runtimeOnly(project(":profiling:profiling-adapter-output-spring-data-neo4j"))
    runtimeOnly(project(":profiling:profiling-core-model"))
    runtimeOnly(project(":profiling:profiling-core-services"))
    runtimeOnly(project(":profiling:profiling-ports-output"))

    runtimeOnly(project(":widget"))

    // TODO: uncomment once test issues are resolved (docs in unit tests only, and idempotent)
    // runtimeOnly(project(mapOf("path" to ":documentation", "configuration" to "staticFiles")))

    // Migrations
    runtimeOnly(project(":migrations:liquibase"))
    runtimeOnly(project(":migrations:neo4j-migrations"))

    // Direct transitive dependencies
    implementation("org.eclipse.rdf4j:rdf4j-common-io")
    implementation("org.neo4j.driver:neo4j-java-driver")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-web")
    implementation("org.springframework:spring-beans")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-web")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework:spring-tx")
    implementation("org.springframework:spring-webmvc")
    implementation("org.springframework:spring-orm")
    implementation("jakarta.persistence:jakarta.persistence-api")

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly("jakarta.servlet:jakarta.servlet-api")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.liquibase:liquibase-core")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-neo4j")
    runtimeOnly("org.springframework.boot:spring-boot-starter-security")
    runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web")
    runtimeOnly("org.springframework.boot:spring-boot-starter-cache")
    runtimeOnly("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    runtimeOnly("org.springframework.data:spring-data-neo4j")
    runtimeOnly("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // File uploads
    runtimeOnly("commons-fileupload:commons-fileupload")
    // Caching
    runtimeOnly("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
    // Data Faker
    implementation("net.datafaker:datafaker")
    // Monitoring
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-jmx")
    //
    // Testing
    //
    // TODO: Most of the runtime dependencies may not be needed, needs checking
    "integrationTestRuntimeOnly"("org.testcontainers:testcontainers")
    "integrationTestRuntimeOnly"("org.testcontainers:junit-jupiter")
    "integrationTestRuntimeOnly"("org.testcontainers:postgresql")
    "integrationTestRuntimeOnly"("org.testcontainers:neo4j")
    "integrationTestRuntimeOnly"("io.kotest:kotest-runner-junit5")
    "integrationTestRuntimeOnly"("io.kotest:kotest-assertions-core")
    "integrationTestRuntimeOnly"("io.kotest:kotest-property")
    "integrationTestRuntimeOnly"("io.kotest.extensions:kotest-extensions-spring")
    "integrationTestRuntimeOnly"("io.kotest.extensions:kotest-extensions-testcontainers")
    "integrationTestRuntimeOnly"("io.kotest:kotest-framework-datatest")
    "integrationTestApi"(project(":common:identifiers"))
    "integrationTestApi"(project(":community:community-core-model"))
    "integrationTestApi"(project(":community:community-ports-input"))
    "integrationTestApi"(project(":graph:graph-core-model"))
    "integrationTestApi"(project(":graph:graph-ports-input"))
    "integrationTestApi"(project(":media-storage:media-storage-core-model"))
    "integrationTestApi"("com.fasterxml.jackson.core:jackson-annotations")
    "integrationTestApi"("org.eclipse.rdf4j:rdf4j-common-io")
    "integrationTestApi"("org.springframework:spring-context")
    "integrationTestApi"("com.fasterxml.jackson.core:jackson-databind")
    "integrationTestImplementation"(project(":common:datatypes"))
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter-api")
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter-params")
    "integrationTestImplementation"("org.springframework.boot:spring-boot-test")
    "integrationTestImplementation"("org.springframework.restdocs:spring-restdocs-core")
    "integrationTestImplementation"("org.springframework:spring-beans")
    "integrationTestImplementation"("org.springframework:spring-test")
    "integrationTestImplementation"(project(":content-types:content-types-adapter-input-rest-spring-mvc"))
    "integrationTestImplementation"(project(":content-types:content-types-core-model"))
    "integrationTestImplementation"(project(":content-types:content-types-ports-input"))
    "integrationTestImplementation"(testFixtures(project(":content-types:content-types-adapter-output-simcomp")))
    "integrationTestImplementation"(testFixtures(project(":testing:spring")))
    "kaptIntegrationTest"("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}

tasks {
    val integrationTest by existing(Test::class) {
        // Declare snippets generated by Spring RestDoc as output, so that they can be cached.
        outputs.dir(layout.buildDirectory.dir("generated-snippets")).withPropertyName("snippetsOutputDirectory")
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

    registerBootRunTask("populatePostgresDatabase") {
        group = "datagen"
        description = "Populates the postgres database with live data and randomly generates required user information."
        args("--spring.profiles.active=development,datagen")
    }

    registerBootRunTask("profileNeo4jRepositories") {
        group = "profiling"
        description = "Profiles neo4j repositories."
        args("--spring.profiles.active=development,profileRepositories,profileNeo4jRepositories")
    }

    registerBootRunTask("runListMigrations") {
        group = "migration"
        description = "Migrates the current database to use list entities."
        args("--spring.profiles.active=development,listMigrations")
    }

    registerBootRunTask("runComparisonMigrations") {
        group = "migration"
        description = "Migrates the current database model for comparisons to a head and published version model."
        args("--spring.profiles.active=development,comparisonMigrations")
    }

    withType<JacocoReport>().configureEach {
        reports {
            html.required.set(true)
        }
    }
}

private fun TaskContainer.registerBootRunTask(
    name: String,
    configuration: BootRun.() -> Unit,
) = register<BootRun>(name) {
    mainClass.set(bootRun.get().mainClass)
    classpath(bootRun.get().classpath)
    apply(configuration)
}

kapt {
    // Turn off the discovery of annotation processors in the compile classpath. This means that all annotation
    // processors need to be listed manually.
    // The problem seems to be that the Neo4j annotation processor leaks into the classpath.
    // TODO: Check if classpath leakage is fixed in later versions.
    includeCompileClasspath = false
}

normalization {
    runtimeClasspath {
        // This only affects build cache key calculation. The file will be included in the build.
        metaInf {
            ignoreProperty("build.time") // The file will always be recreated, so the timestamp always changes
            ignoreProperty("build.version") // The version could change when switching branches, which is no problem
        }
    }
}

// Duplicated configuration because of conflicts with Gradle, and adjustments needed for integration tests

val restdocsSnippetsDir = layout.buildDirectory.dir("generated-snippets")

// Add consumable configuration for RestDocs snippets
val restdocs: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

val integrationTest by tasks.getting {
    outputs.dir(restdocsSnippetsDir).withPropertyName("restdocsSnippetsDirectory")
}

tasks {
    register<Zip>("restdocsSnippetsZip") {
        archiveClassifier.set("restdocs")
        from(integrationTest.outputs) {
            include("**/*.adoc")
        }
        includeEmptyDirs = false
    }
}

artifacts {
    add("restdocs", tasks.named("restdocsSnippetsZip"))
}
