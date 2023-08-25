plugins {
    id("org.orkg.kotlin-conventions")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":common:exceptions"))
    implementation(project(":graph:graph-application"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework:spring-web")
}
