package eu.tib.orkg.prototype

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = ["eu.tib.orkg.prototype.statements.domain.model.neo4j"])
@ComponentScan(basePackages = ["eu.tib.orkg.prototype.core.statements"]) // FIXME: namespace mismatch, should be aligned
internal class TestApplication

internal fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
