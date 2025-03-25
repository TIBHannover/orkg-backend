plugins {
    id("org.orkg.gradle.kotlin")
}

buildConfig {
    useKotlinOutput { internalVisibility = false }

    packageName("org.orkg.constants")
    className("BuildConfig")

    buildConfigField("APP_NAME", rootProject.name)
    buildConfigField("CONTAINER_IMAGE_NEO4J", "neo4j:5-community")
    buildConfigField("CONTAINER_IMAGE_POSTGRES", "postgres:17")
    buildConfigField("CONTAINER_IMAGE_KEYCLOAK", "registry.gitlab.com/tibhannover/orkg/keycloak-docker-image:26")
}
