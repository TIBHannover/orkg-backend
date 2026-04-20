package org.orkg.testing

import org.orkg.constants.BuildConfig
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.neo4j.Neo4jContainer
import org.testcontainers.utility.DockerImageName

/**
 * Initializer to start a Neo4j instance using TestContainers.
 */
class Neo4jContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    // TODO: might be nice to aggregate values for debugging, if possible

    companion object {
        val neo4jContainer: Neo4jContainer = Neo4jContainer(DockerImageName.parse(BuildConfig.CONTAINER_IMAGE_NEO4J))
            .withNeo4jConfig("initial.dbms.default_database", "orkg")
            .withNeo4jConfig("apoc.custom.procedures.refresh", "100")
            .withNeo4jConfig("db.query.default_language", "CYPHER_5")
            .withoutAuthentication()
            .withPlugins("apoc", "apoc-extended")
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        neo4jContainer.start()
        TestPropertyValues.of(neo4jContainer.settings()).applyTo(applicationContext)
    }

    private fun Neo4jContainer.settings() = listOf(
        "spring.neo4j.uri=$boltUrl",
        "spring.neo4j.authentication.username=neo4j",
        "spring.neo4j.authentication.password=$adminPassword",
    )
}
