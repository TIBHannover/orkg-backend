plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")
}

dependencies {
    val kotestVersion = "4.6.0"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-datatest:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
}

tasks {
    withType(Test::class).configureEach { useJUnitPlatform() }
    withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).configureEach {
        kotlinOptions.jvmTarget = "${JavaVersion.VERSION_11}"
    }
}

spotless {
    val ktlintVersion = "0.41.0" // Updated because of issues with Kotest
    kotlin { ktlint(ktlintVersion) }
    kotlinGradle { ktlint(ktlintVersion) }
}
