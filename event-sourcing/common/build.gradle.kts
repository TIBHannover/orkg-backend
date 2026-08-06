plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.axonframework.extensions.spring:axon-spring")
    api("org.axonframework:axon-common")
    api("org.hibernate.orm:hibernate-core")
    api("org.springframework.boot:spring-boot-persistence")
    api("org.springframework:spring-context")
    api("org.springframework:spring-orm")
    implementation("org.springframework:spring-tx")
}
