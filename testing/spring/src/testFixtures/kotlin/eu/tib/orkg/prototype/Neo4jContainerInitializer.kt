package eu.tib.orkg.prototype

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.containers.Neo4jLabsPlugin
import org.testcontainers.utility.DockerImageName

/**
 * Initializer to start a Neo4j instance using TestContainers.
 */
class Neo4jContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    // TODO: might be nice to aggregate values for debugging, if possible

    companion object {
        val neo4jContainer: Neo4jContainer<*> = Neo4jContainer(DockerImageName.parse("neo4j:4.4-community"))
            .withoutAuthentication()
            .withLabsPlugins(Neo4jLabsPlugin.APOC) // adds significant startup overhead
            .withReuse(true)
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        neo4jContainer.start()
        TestPropertyValues.of(settingsForSDN5(neo4jContainer)).applyTo(applicationContext)
    }

    private fun settingsForSDN5(neo4j: Neo4jContainer<*>) = listOf(
        "spring.data.neo4j.uri=${neo4j.boltUrl}",
        "spring.data.neo4j.username=neo4j",
        "spring.data.neo4j.password=${neo4j.adminPassword}",
        "spring.data.neo4j.use-native-types=true", // TODO: Remove after upgrade, not supported anymore
    )
}
