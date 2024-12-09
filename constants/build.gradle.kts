plugins {
    id("org.orkg.gradle.kotlin")
    id("com.github.gmazzo.buildconfig") version "5.5.1"
}

buildConfig {
    useKotlinOutput { internalVisibility = false }

    packageName("org.orkg.constants")
    className("BuildConfig")

    buildConfigField("APP_NAME", rootProject.name)
    buildConfigField("CONTAINER_IMAGE_NEO4J", "neo4j:4.4-community")
    buildConfigField("CONTAINER_IMAGE_POSTGRES", "postgres:11")
    buildConfigField("CONTAINER_IMAGE_KEYCLOAK", "registry.gitlab.com/tibhannover/orkg/keycloak-docker-image:26.0.5")
}
