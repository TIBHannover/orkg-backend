pluginManagement {
    includeBuild("../meta-plugins")
}
plugins {
    id("org.orkg.gradle.settings")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
}
