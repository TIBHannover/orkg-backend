plugins {
    kotlin("jvm")
}

dependencies {
    components.all<JacksonBomAlignmentRule>()
}
