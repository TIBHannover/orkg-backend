plugins {
    `java-platform`
}

dependencies {
    // Declare constraints on all components that need alignment
    constraints {
        api(rootProject)
        api(":test-helper")
    }
}
