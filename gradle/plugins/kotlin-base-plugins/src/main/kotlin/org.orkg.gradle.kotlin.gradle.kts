import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("jacoco")
    id("idea")
    id("org.orkg.gradle.base")
    id("org.orkg.gradle.consistent-resolution")
    id("com.diffplug.spotless")
}

val javaLanguageVersion = JavaLanguageVersion.of(17)

// Support downloading JavaDoc and sources artifacts by enabling it via Gradle properties
val downloadJavadoc: String? by project
val downloadSources: String? by project

idea {
    module {
        isDownloadJavadoc = downloadJavadoc?.let(String::toBoolean) ?: false
        isDownloadSources = downloadSources?.let(String::toBoolean) ?: false
    }
}

kotlin {
    jvmToolchain {
        // This also configures the Java toolchain
        languageVersion.set(javaLanguageVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// Configure details for *all* test executions directly on 'Test' task
tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    // maxParallelForks = 4
    // maxHeapSize = "1g"

    // testLogging.showStandardStreams = true

    systemProperty("file.encoding", "UTF-8")
}
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("") // Configure "version-less" dependency, provided by platform
        }
    }
}

// Configure common test runtime dependencies for *all* projects
dependencies {
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("ch.qos.logback:logback-classic") // Logger implementation. Should be same as in production.
}

// Create reproducible archives
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Add a 'compileAll' task to run all Java compilations in one go
tasks.register("compileAll") {
    group = LifecycleBasePlugin.BUILD_GROUP
    description = "Compile all Java code (use to prime the build cache for CI pipeline)"
    dependsOn(tasks.withType<JavaCompile>())
}

extensions.configure<SpotlessExtension> {
    ratchetFrom("origin/master")
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
