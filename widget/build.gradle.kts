plugins {
    id("org.orkg.kotlin-conventions")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":graph:graph-application"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
