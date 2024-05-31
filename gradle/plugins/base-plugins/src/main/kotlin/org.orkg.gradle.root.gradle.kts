plugins {
    id("lifecycle-base")
    id("org.orkg.gradle.dependency-analysis-root")
    id("com.diffplug.spotless-changelog")
    id("com.osacky.doctor")
}

doctor {
    javaHome {
        failOnError.set(false)
        ensureJavaHomeMatches.set(false)
        ensureJavaHomeIsSet.set(false)
    }
}
