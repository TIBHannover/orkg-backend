plugins {
    id("org.orkg.kotlin-conventions")
    kotlin("kapt")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // Disable JUnit 4 (aka Vintage)
        exclude(group = "junit", module = "junit")
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        // TODO: We currently have a mixture of MockK and Mockito tests. After migration, we should disable Mockito.
        // exclude(module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:3.0.1")
}
