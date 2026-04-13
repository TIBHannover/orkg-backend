plugins {
    id("org.orkg.gradle.kotlin-library")
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("org.commonmark:commonmark")
    api("org.commonmark:commonmark-ext-gfm-strikethrough")
    api("org.commonmark:commonmark-ext-gfm-tables")
    api("org.jetbrains.kotlinx:kotlinx-html-jvm")
    api("org.springframework.data:spring-data-commons")
    api(project(":common:datatypes"))
    api(project(":common:core-identifiers"))
    api(project(":common:spring-webmvc"))
    api(project(":community:community-ports-output"))
    api(project(":content-types:content-types-core-model"))
    api(project(":content-types:content-types-ports-input"))
    api(project(":graph:graph-adapter-input-representations"))
    api(project(":graph:graph-core-model"))
    api(project(":graph:graph-ports-input"))
    implementation("org.apache.commons:commons-csv")
    implementation("org.commonmark:commonmark-ext-autolink")
    implementation("org.jbibtex:jbibtex")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation(project(":common:serialization"))
    implementation(project(":common:external-identifiers"))
    implementation(project(":common:functional"))
    implementation(project(":common:pagination"))
    implementation(project(":community:community-core-model"))
    implementation(project(":graph:graph-core-constants"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api")
                implementation("org.springframework:spring-test")
                runtimeOnly("org.xmlunit:xmlunit-core") // required by XmlExpectationsHelper in AbstractJatsRendererTest
            }
        }
    }
}
