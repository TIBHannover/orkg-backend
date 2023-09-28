import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    val kotlinVersion = "1.8.22"
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
            jvmTarget = "17"
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
