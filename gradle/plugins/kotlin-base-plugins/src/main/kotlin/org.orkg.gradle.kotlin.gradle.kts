plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("jacoco")
    id("idea")
    id("org.orkg.gradle.base")
    id("org.orkg.gradle.consistent-resolution")
    id("com.autonomousapps.dependency-analysis")
    id("com.github.gmazzo.buildconfig")
}

val javaLanguageVersion = JavaLanguageVersion.of(21)

// Support downloading Javadoc and sources artifacts by enabling it via Gradle properties
val downloadJavadoc: String? by project
val downloadSources: String? by project

idea {
    module {
        isDownloadJavadoc = downloadJavadoc?.let(String::toBoolean) == true
        isDownloadSources = downloadSources?.let(String::toBoolean) == true
    }
}

java {
    consistentResolution {
        useCompileClasspathVersions()
    }
}

kotlin {
    jvmToolchain {
        // This also configures the Java toolchain
        languageVersion.set(javaLanguageVersion)
    }
    compilerOptions {
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_1_9)
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
    systemProperty("kotest.framework.config.fqn", "org.orkg.testing.configuration.KotestProjectConfiguration")
}

configurations.all {
    // Prevent old or unwanted dependency to end up on the classpath
    exclude(group = "junit", module = "junit")
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    exclude(group = "org.mockito")
    exclude(group = "jakarta.json")
}

// Configure common test runtime dependencies for *all* projects
dependencies {
    api(platform("org.orkg:platform"))
    testApi(platform("org.orkg:platform"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
