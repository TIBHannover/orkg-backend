plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(platform(project(":plugins-platform")))

    implementation(project(":dependency-rules-plugins"))
    implementation(project(":dependency-analysis-plugins"))

    implementation("org.asciidoctor:asciidoctor-gradle-jvm")
    implementation("org.asciidoctor:asciidoctor-gradle-jvm-gems")
    implementation("com.epages.restdocs-api-spec:com.epages.restdocs-api-spec.gradle.plugin")
    implementation("org.openapitools:openapi-generator-gradle-plugin")

    implementation("com.diffplug.spotless-changelog:spotless-changelog-plugin-gradle")
    implementation("com.github.ben-manes:gradle-versions-plugin")
    implementation("com.osacky.doctor:doctor-plugin")
    implementation("dev.iurysouza:modulegraph")
}
