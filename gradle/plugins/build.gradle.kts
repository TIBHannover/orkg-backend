// TODO: Work-around for broken dependencies in AsciiDoctor Gradle plugin. Should be fine for versions >= 4.0.2.
subprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            // Change group name and version
            substitute(module("com.burgstaller:okhttp-digest")).using(module("io.github.rburgst:okhttp-digest:1.21"))
        }
    }
}

plugins {
    // id("org.example.plugin-analysis")
}
