plugins {
    id("org.orkg.gradle.kotlin")
}

buildConfig {
    useKotlinOutput { internalVisibility = false }

    packageName("org.orkg.constants")
    className("BuildConfig")

    val neo4jImageName = "neo4j"
    val neo4jImageTag = "5-community"

    val postgresImageName = "postgres"
    val postgresImageTag = "17"

    val keycloakImageName = "registry.gitlab.com/tibhannover/orkg/keycloak-docker-image"
    val keycloakImageTag = "26"

    buildConfigField("APP_NAME", rootProject.name)
    buildConfigField("CONTAINER_IMAGE_NEO4J", "$neo4jImageName:$neo4jImageTag")
    buildConfigField("CONTAINER_IMAGE_NEO4J_NAME", neo4jImageName)
    buildConfigField("CONTAINER_IMAGE_NEO4J_TAG", neo4jImageTag)
    buildConfigField("CONTAINER_IMAGE_POSTGRES", "$postgresImageName:$postgresImageTag")
    buildConfigField("CONTAINER_IMAGE_POSTGRES_NAME", postgresImageName)
    buildConfigField("CONTAINER_IMAGE_POSTGRES_TAG", postgresImageTag)
    buildConfigField("CONTAINER_IMAGE_KEYCLOAK", "$keycloakImageName:$keycloakImageTag")
    buildConfigField("CONTAINER_IMAGE_KEYCLOAK_NAME", keycloakImageName)
    buildConfigField("CONTAINER_IMAGE_KEYCLOAK_TAG", keycloakImageTag)
}
