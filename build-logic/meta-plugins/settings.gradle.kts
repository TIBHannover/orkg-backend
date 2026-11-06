dependencyResolutionManagement {
    repositories.gradlePluginPortal()

    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}

include("build-parameters-plugins", "plugin-analysis-plugins", "settings-plugins")
