defaultTasks("bundleThemes")

tasks {
    // Bundle the theme as a JAR file. We do this manually to keep dependencies to a minimum.
    register("bundleThemes", Zip::class) {
        group = "build"

        archiveFileName.set(project.name + ".jar")
        destinationDirectory.set(layout.buildDirectory.dir("themes"))

        // Theme meta-data
        from(layout.projectDirectory) {
            include("keycloak-themes.json")
            into("META-INF")
        }
        // Theme data
        from(layout.projectDirectory) {
            include("theme/**")
        }
    }
}
