plugins {
    id("org.orkg.kotlin-conventions")
}

dependencies {
    api(platform(project(":platform")))

    implementation(project(":library"))
    implementation("com.fasterxml.jackson.core:jackson-databind")
}
