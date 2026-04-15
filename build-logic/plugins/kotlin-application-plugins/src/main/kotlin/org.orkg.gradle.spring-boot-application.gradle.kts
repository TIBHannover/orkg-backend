plugins {
    id("org.orkg.gradle.kotlin")
    id("org.orkg.gradle.formatting")
    kotlin("kapt")
    kotlin("plugin.spring")
    id("org.springframework.boot")
}

springBoot {
    buildInfo {
        excludes.set(setOf("time"))
    }
}
