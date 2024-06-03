plugins {
    id("org.orkg.gradle.kotlin-library-with-test-fixtures")
}

dependencies {
    testFixturesApi("io.kotest:kotest-framework-api")
    testFixturesImplementation("org.apache.commons:commons-lang3")
}

dependencyAnalysis {
    issues {
        onUnusedDependencies {
            // We do not use the "main" source set, so the (automatically added) stdlib is always unused.
            exclude("org.jetbrains.kotlin:kotlin-stdlib")
        }
    }
}
