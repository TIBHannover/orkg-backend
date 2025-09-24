plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform(project(":plugins-platform")))

    implementation("com.autonomousapps.dependency-analysis:com.autonomousapps.dependency-analysis.gradle.plugin")
}
