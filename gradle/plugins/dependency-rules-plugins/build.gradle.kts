plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform(project(":plugins-platform")))

    implementation("org.gradlex:jvm-dependency-conflict-resolution")
}
