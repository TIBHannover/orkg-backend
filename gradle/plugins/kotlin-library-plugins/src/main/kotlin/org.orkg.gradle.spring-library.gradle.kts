plugins {
    id("org.orkg.gradle.kotlin-library")
    // id("org.springframework.boot") apply false
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}
