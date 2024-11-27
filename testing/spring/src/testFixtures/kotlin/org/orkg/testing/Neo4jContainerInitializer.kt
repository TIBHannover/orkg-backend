package org.orkg.testing

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.utility.DockerImageName

/**
 * Initializer to start a Neo4j instance using TestContainers.
 */
class Neo4jContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    // TODO: might be nice to aggregate values for debugging, if possible

    companion object {
        val neo4jContainer: Neo4jContainer<*> = Neo4jContainer(DockerImageName.parse("neo4j:4.4-community"))
            .withoutAuthentication()
            .withPlugins("apoc")
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        neo4jContainer.start()
        TestPropertyValues.of(settingsForSDN(neo4jContainer)).applyTo(applicationContext)
    }

    private fun settingsForSDN(neo4j: Neo4jContainer<*>) = listOf(
        "spring.neo4j.uri=${neo4j.boltUrl}",
        "spring.neo4j.authentication.username=neo4j",
        "spring.neo4j.authentication.password=${neo4j.adminPassword}"
    )
}
