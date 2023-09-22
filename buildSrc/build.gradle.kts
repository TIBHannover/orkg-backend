import org.gradle.kotlin.dsl.accessors.runtime.conventionOf
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val kotlinVersion = "1.7.10"
    // Kotlin conventions
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    // Spring conventions
    api("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion") {
        because("""contains "kotlin.spring" plug-in""")
    }
    api("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion") {
        because("""contains "kotlin.jpa" plug-in""")
    }

    // Work-around: Fix issues running Jib. There is a conflict with an older version of
    // commons-compress somewhere. No idea where. Upgrading the version to 1.21 for all
    // seems to fix the problem. (It should be removed at some point.) -- MP, 2022-06-07
    implementation("org.apache.commons:commons-compress:1.21")
}

tasks {
    withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}

gradlePlugin {
    plugins {
        register("print-coverage") {
            id = "org.orkg.print-coverage"
            implementationClass = "org.orkg.gradle.plugins.PrintCoveragePlugin"
        }
    }
}

// Compile Groovy before Kotlin (https://docs.gradle.org/6.1/release-notes.html#compilation-order)
tasks.named<GroovyCompile>("compileGroovy") {
    // Groovy only needs the declared dependencies
    // and not the output of compileJava
    classpath = sourceSets.main.get().compileClasspath
}
tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileKotlin") {
    // Kotlin also depends on the result of Groovy compilation
    // which automatically makes it depend on compileGroovy
    classpath += files(conventionOf(sourceSets.main.get()).getPlugin(GroovySourceSet::class.java).groovy.classesDirectory)
}
