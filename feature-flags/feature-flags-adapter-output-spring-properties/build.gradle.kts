plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework.boot:spring-boot")
    api("org.springframework:spring-context")
    api(project(":feature-flags:feature-flags-ports"))
}
