plugins {
    kotlin("jvm")
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation(kotlin("stdlib"))
}

tasks {
    withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }
}
