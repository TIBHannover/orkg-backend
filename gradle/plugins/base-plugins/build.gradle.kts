plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform(project(":plugins-platform")))

    implementation(project(":dependency-rules-plugins"))

    implementation("org.asciidoctor:asciidoctor-gradle-jvm")
    implementation("org.asciidoctor:asciidoctor-gradle-jvm-gems")
    implementation("com.diffplug.spotless-changelog:spotless-changelog-plugin-gradle")
}
