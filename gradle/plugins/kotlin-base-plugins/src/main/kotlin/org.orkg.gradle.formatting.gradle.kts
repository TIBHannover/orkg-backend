plugins {
    id("com.diffplug.spotless")
}

tasks.withType<JavaCompile>().configureEach {
    // Always run linter before compiling
    dependsOn(tasks.named("spotlessCheck"))
}

spotless {
    kotlin {
        ktlint("1.5.0") // TODO: Remove once Spotless uses this version
    }
    kotlinGradle {
        ktlint("1.5.0") // TODO: Remove once Spotless uses this version
    }
}
