plugins {
    id("java-base")
}

/*
// Expose the ':rest-api-server' project runtime classpath in every project
val app = configurations.dependencyScope("rest-api-server") {
    withDependencies {
        // Depend on ':app' and with this on all its (transitive) dependencies
        add(project.dependencies.create(project(":rest-api-server")))
        // Get our own version information from the platform project
        add(project.dependencies.create(project.dependencies.platform("org.orkg:platform")))
    }
}
val appRuntimeClasspath = configurations.resolvable("appRuntimeClasspath") {
    description = "Runtime classpath of the complete application"
    extendsFrom(app.get())
    attributes {
        // We want the runtime classpath represented by Usage.JAVA_RUNTIME
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}
*/

// Every compile classpath and runtime classpath uses the versions of the
sourceSets.all {
//    configurations[compileClasspathConfigurationName].shouldResolveConsistentlyWith(appRuntimeClasspath.get())
//    configurations[runtimeClasspathConfigurationName].shouldResolveConsistentlyWith(appRuntimeClasspath.get())
    // Source sets without production code (tests / fixtures) are allowed to have dependencies that are
    // not part of the consistent resolution result and might need additional version information
//    if (this != sourceSets["main"]) {
        dependencies.add(implementationConfigurationName, dependencies.platform("org.orkg:platform"))
//    }
}
