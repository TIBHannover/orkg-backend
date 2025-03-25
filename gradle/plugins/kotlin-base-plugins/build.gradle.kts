plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform(project(":plugins-platform")))

    implementation(project(":base-plugins"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation("com.diffplug.spotless:spotless-plugin-gradle")
    implementation("com.github.gmazzo.buildconfig:com.github.gmazzo.buildconfig.gradle.plugin")
}
