plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // TODO: Can be removed after orgnization refactoring
    implementation("org.springframework.boot:spring-boot-starter-web")
}
