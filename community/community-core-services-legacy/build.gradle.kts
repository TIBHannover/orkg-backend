plugins {
    id("org.orkg.gradle.spring-library")
}

dependencies {
    api("org.springframework:spring-context")
    api(project(":community:community-ports-input"))
    api(project(":community:community-ports-input-legacy"))
    api(project(":identity-management:idm-core-model"))
    api(project(":identity-management:idm-ports-output"))
}
