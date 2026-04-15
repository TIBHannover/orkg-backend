plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform(project(":plugins-platform")))

    implementation(project(":kotlin-base-plugins"))

    implementation("org.jetbrains.kotlin:kotlin-allopen")
    implementation("org.jetbrains.kotlin:kotlin-noarg")
}
