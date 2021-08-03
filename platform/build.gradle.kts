plugins {
    `java-platform`
}

dependencies {
    // Declare constraints on all components that need alignment
    constraints {
        api(rootProject)
        api(":application:core")
        api(":application:shared")
        api(":adapters:input:core")
        api(":adapters:output:core")
        api(":rest-api")
    }
}
