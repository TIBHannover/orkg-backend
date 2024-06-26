plugins {
    id("org.orkg.gradle.kotlin")
    kotlin("kapt")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

springBoot {
    buildInfo {
        excludes.set(setOf("time"))
    }
}
