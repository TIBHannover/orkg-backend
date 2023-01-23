package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.configuration.CacheConfiguration
import eu.tib.orkg.prototype.configuration.Neo4jConfiguration
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.Neo4jDockerContainer.Companion.Neo4jDockerContainerContextInitializer
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.extensions.spring.testContextManager
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.Neo4jContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@DataNeo4jTest
@ContextConfiguration(
    classes = [SpringDataNeo4jResourceAdapter::class],
    //initializers = [Neo4jContainerInitializer::class]
)
@Import(Neo4jConfiguration::class, CacheConfiguration::class)
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal"])
internal class TestForContainer(
    @Autowired private val springDataNeo4jResourceAdapter: ResourceRepository,
    @Autowired private val configurableApplicationContext: ConfigurableApplicationContext,
) : DescribeSpec({

    //include(resourceRepositoryContract(springDataNeo4jResourceAdapter))
    it("works") { 1 shouldBe 1 }
    it("does not work") { 1 shouldBe 2 }
}) {
    companion object {
        val POSTGRES_IMAGE: DockerImageName = DockerImageName.parse("neo4j:3.5-community")
        fun DescribeSpec.attachBasicPostgresContainer(): Neo4jContainer<*> {
            println("ATTACHING!")
            val postgresContainer = Neo4jContainer<Nothing>(POSTGRES_IMAGE).apply {
                withoutAuthentication()
            }
            println("STARTED (ATTACH)")
            //println(settingsForSDN5(container))
            //TestPropertyValues.of(settingsForSDN5(container)).applyTo(configurableApplicationContext)

            //listener(postgresContainer.perSpec()) // TODO: maybe we can hook in here?
            println("REGISTERED LISTENER. DONE!")
            return postgresContainer
        }
    }

    val container = attachBasicPostgresContainer()

    override fun extensions() = listOf(
        //SpringExtension
        SpringTestExtension(SpringTestLifecycleMode.Root)
    )

    override suspend fun beforeSpec(spec: Spec) {
        println("RUUUUUUUNNNN!")
        container.start()
        TestPropertyValues.of(
            listOf(
                "spring.test.database.replace=none",  // Prevent all extending tests from starting an in-memory database
                "spring.data.neo4j.uri=${container.boltUrl}",
                "spring.data.neo4j.username=neo4j",
                "spring.data.neo4j.password=${container.adminPassword}",
                "spring.data.neo4j.use-native-types=true", // TODO: Remove after upgrade, not supported anymore
            )
        ).applyTo(configurableApplicationContext)

        //Neo4jDockerContainer.start(testContextManager().testContext)
    }

    override fun afterSpec(f: suspend (Spec) -> Unit) {
        container.stop()
        //Neo4jDockerContainer.stop()
        println("STOPPED!")
    }
}

class Neo4jDockerContainer(image: String) : Neo4jContainer<Neo4jDockerContainer>(image) {
    constructor() : this("neo4j:3.5-community")

    companion object {
        @Container
        private lateinit var container: Neo4jDockerContainer

        fun start(context: TestContext) {
            if (!Companion::container.isInitialized) {
                container = Neo4jDockerContainer().withoutAuthentication()
                container.start()
                println("DONE STARING!")
                val ac: ConfigurableApplicationContext = context.applicationContext as ConfigurableApplicationContext
                //println(settingsForSDN5(container))
                //TestPropertyValues.of(settingsForSDN5(container)).applyTo(ac)
                //println("DONE INITIALIZING WHILE STARTING")
            }

        }

        fun stop() {
            container.stop()
        }

        private fun settingsForSDN5(neo4j: Neo4jContainer<*>) = listOf(
            "spring.test.database.replace=none",  // Prevent all extending tests from starting an in-memory database
            "spring.data.neo4j.uri=${neo4j.boltUrl}",
            "spring.data.neo4j.username=neo4j",
            "spring.data.neo4j.password=${neo4j.adminPassword}",
            "spring.data.neo4j.use-native-types=true", // TODO: Remove after upgrade, not supported anymore
        )

        class Neo4jDockerContainerContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(applicationContext: ConfigurableApplicationContext) {
                println(settingsForSDN5(container))
                TestPropertyValues.of(settingsForSDN5(container)).applyTo(applicationContext)
                println("DONE INITIALIZING")
            }

            private fun settingsForSDN5(neo4j: Neo4jContainer<*>) = listOf(
                "spring.test.database.replace=none",  // Prevent all extending tests from starting an in-memory database
                "spring.data.neo4j.uri=${neo4j.boltUrl}",
                "spring.data.neo4j.username=neo4j",
                "spring.data.neo4j.password=${neo4j.adminPassword}",
                "spring.data.neo4j.use-native-types=true", // TODO: Remove after upgrade, not supported anymore
            )
        }
    }
}
