@file:Suppress("UnstableApiUsage")

import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

version = "0.68.0"

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
                implementation(testFixtures(project(":common")))
                implementation(testFixtures(project(":testing:spring")))

                implementation("io.kotest:kotest-assertions-api")
                implementation("io.kotest:kotest-assertions-shared")
                implementation("org.springframework.boot:spring-boot-starter-test") {
                    // Disable JUnit 4 (aka Vintage)
                    exclude(group = "junit", module = "junit")
                    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
                    // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
                    // exclude(module = "mockito-core")
                }
                implementation("org.jetbrains.kotlin:kotlin-reflect")
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework.boot:spring-boot-test-autoconfigure")
                implementation("org.springframework:spring-test")
                implementation(project(":common:serialization"))
                implementation("org.springframework.boot:spring-boot-test")
                implementation("org.springframework.security:spring-security-test")
            }
        }
        val integrationTest by registering(JvmTestSuite::class) {
            testType.set(TestSuiteType.INTEGRATION_TEST)
            dependencies {
                implementation(project())
                implementation(project(":graph:graph-core-services"))
                implementation(project(":graph:graph-ports-output"))
                implementation(testFixtures(project(":graph:graph-adapter-input-rest-spring-mvc")))
                implementation(project(":content-types:content-types-ports-output"))
                implementation(project(":identity-management:idm-ports-output"))
                implementation(project(":identity-management:idm-core-model"))
                implementation(project(":identity-management:idm-adapter-output-spring-data-jpa")) // for JpaUserAdapter
                implementation(project(":community:community-ports-output")) // for CuratorRepository
                implementation(testFixtures(project(":community:community-adapter-input-rest-spring-mvc")))
                implementation(project(":feature-flags:feature-flags-ports"))
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
                implementation(libs.kotest.assertions.core)
                implementation(libs.forkhandles.values4k)
                implementation("jakarta.persistence:jakarta.persistence-api")
                implementation("org.hamcrest:hamcrest")
                implementation("org.hibernate:hibernate-core:5.4.32.Final")
                implementation("org.springframework.data:spring-data-commons")
                implementation("org.springframework:spring-core")
                implementation("org.springframework:spring-web")
                implementation(libs.assertj.core)
                runtimeOnly("org.springframework.boot:spring-boot")
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
    testApi(enforcedPlatform(libs.junit5.bom))
    "integrationTestApi"(enforcedPlatform(libs.junit5.bom))

    kapt(platform("org.orkg:platform"))

    implementation(kotlin("stdlib")) // "downgrade" from api()

    // This project is essentially a "configuration" project in Spring's sense, so we depend on all components:
    implementation(project(":common"))
    runtimeOnly(project(":common:serialization"))

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
    runtimeOnly(project(":content-types:content-types-adapter-output-spring-data-neo4j-sdn6"))
    runtimeOnly(project(":content-types:content-types-adapter-output-web"))
    runtimeOnly(project(":content-types:content-types-core-services"))
    implementation(project(":content-types:content-types-ports-input"))
    runtimeOnly(project(":content-types:content-types-ports-output"))

    runtimeOnly(project(":data-export:data-export-adapters"))
    runtimeOnly(project(":data-export:data-export-core"))
    runtimeOnly(project(":data-export:data-export-ports-input"))

    implementation(project(":eventbus"))

    runtimeOnly(project(":discussions:discussions-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":discussions:discussions-core-services"))
    runtimeOnly(project(":discussions:discussions-adapter-output-spring-data-jpa"))

    implementation(project(":feature-flags:feature-flags-ports")) // for cache warmup
    runtimeOnly(project(":feature-flags:feature-flags-adapter-output-spring-properties"))

    runtimeOnly(project(":graph:graph-adapter-input-rest-spring-mvc"))
    implementation(project(":graph:graph-adapter-output-spring-data-neo4j-sdn6"))
    runtimeOnly(project(":graph:graph-adapter-output-web"))
    implementation(project(":graph:graph-core-model"))
    implementation(project(":graph:graph-core-services"))
    implementation(project(":graph:graph-ports-input"))
    implementation(project(":graph:graph-ports-output"))

    implementation(project(":identity-management:idm-ports-input")) // for PostgresDummyDataSetup
    implementation(project(":identity-management:idm-ports-output")) // for Role
    implementation(project(":identity-management:idm-core-model")) // for Role
    runtimeOnly(project(":identity-management:idm-adapter-input-rest-spring-security"))
    runtimeOnly(project(":identity-management:idm-core-services"))
    runtimeOnly(project(":identity-management:idm-adapter-output-spring-data-jpa"))

    runtimeOnly(project(":licenses:licenses-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":licenses:licenses-core-services"))

    runtimeOnly(project(":statistics:statistics-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":statistics:statistics-adapter-output-spring-data-neo4j-sdn6"))
    runtimeOnly(project(":statistics:statistics-core-services"))

    runtimeOnly(project(":curation:curation-adapter-input-rest-spring-mvc"))
    runtimeOnly(project(":curation:curation-adapter-output-spring-data-neo4j-sdn6"))
    runtimeOnly(project(":curation:curation-core-services"))

    runtimeOnly(project(":media-storage:media-storage-adapter-input-serialization"))
    runtimeOnly(project(":media-storage:media-storage-adapter-output-spring-data-jpa"))
    implementation(project(":media-storage:media-storage-core-model"))
    runtimeOnly(project(":media-storage:media-storage-ports-input"))
    runtimeOnly(project(":media-storage:media-storage-ports-output"))
    runtimeOnly(project(":media-storage:media-storage-core-services"))

    runtimeOnly(project(":profiling:profiling-adapter-output"))
    runtimeOnly(project(":profiling:profiling-adapter-output-spring-data-neo4j-sdn6"))
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
    implementation("org.apache.tomcat.embed:tomcat-embed-core")
    implementation("org.eclipse.rdf4j:rdf4j-util")
    implementation("org.neo4j.driver:neo4j-java-driver")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-actuator-autoconfigure")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.security:spring-security-config")
    implementation("org.springframework.security:spring-security-core")
    implementation("org.springframework.security:spring-security-web")
    implementation("org.springframework:spring-beans")
    implementation("org.springframework:spring-context-support")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-core")
    implementation("org.springframework:spring-web")
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.hibernate:hibernate-core:5.6.9.Final") // TODO: remove after upgrade to 2.7 or make version-less ???
    runtimeOnly(libs.liquibase)
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j") {
        exclude(group = "org.springframework.data", module = "spring-data-neo4j") // TODO: remove after upgrade to 2.7
    }
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security.oauth:spring-security-oauth2:${libs.versions.spring.security.oauth.get()}")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.data:spring-data-neo4j")
    implementation(libs.spring.boot.starter.neo4j.migrations)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // File uploads
    runtimeOnly("commons-fileupload:commons-fileupload:1.5")
    // Caching
    runtimeOnly("org.springframework.boot:spring-boot-starter-cache")
    runtimeOnly("com.github.ben-manes.caffeine:caffeine")
    implementation("io.github.stepio.coffee-boots:coffee-boots:3.0.0")
    // Data Faker
    implementation("net.datafaker:datafaker:1.7.0")
    // Monitoring
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("org.jolokia:jolokia-core")
    runtimeOnly("io.micrometer:micrometer-registry-jmx")
    //
    // Testing
    //
    // Note: Version Catalogs are not yet supported in the test suites plugin
    "integrationTestRuntimeOnly"(libs.bundles.testcontainers)
    "integrationTestRuntimeOnly"(libs.bundles.kotest)
    "integrationTestApi"("eu.michael-simons.neo4j:neo4j-migrations-spring-boot-autoconfigure")
    "integrationTestApi"("org.springframework.security:spring-security-test")
    "integrationTestApi"(project(":common"))
    "integrationTestApi"(project(":community:community-core-model"))
    "integrationTestApi"(project(":community:community-ports-input"))
    "integrationTestApi"(project(":content-types:content-types-adapter-input-rest-spring-mvc"))
    "integrationTestApi"(project(":content-types:content-types-ports-input"))
    "integrationTestApi"(project(":graph:graph-core-model"))
    "integrationTestApi"(project(":graph:graph-ports-input"))
    "integrationTestApi"(project(":identity-management:idm-ports-input"))
    "integrationTestApi"(project(":media-storage:media-storage-core-model"))
    "integrationTestApi"(testFixtures(project(":testing:spring")))
    "integrationTestApi"("com.fasterxml.jackson.core:jackson-annotations")
    "integrationTestApi"("org.eclipse.rdf4j:rdf4j-util")
    "integrationTestApi"("org.junit.jupiter:junit-jupiter-api")
    "integrationTestApi"("org.junit.jupiter:junit-jupiter-params")
    "integrationTestApi"("org.springframework.boot:spring-boot-autoconfigure")
    "integrationTestApi"("org.springframework.boot:spring-boot-test-autoconfigure")
    "integrationTestApi"("org.springframework.boot:spring-boot-test")
    "integrationTestApi"("org.springframework.restdocs:spring-restdocs-core")
    "integrationTestApi"("org.springframework:spring-beans")
    "integrationTestApi"("org.springframework:spring-context")
    "integrationTestApi"("org.springframework:spring-test")
    "integrationTestApi"("org.springframework:spring-tx")
    "integrationTestApi"(libs.jackson.databind)
    "integrationTestImplementation"(project(":content-types:content-types-core-model"))
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
