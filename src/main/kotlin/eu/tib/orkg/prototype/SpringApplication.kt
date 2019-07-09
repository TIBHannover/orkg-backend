package eu.tib.orkg.prototype

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
@EnableNeo4jRepositories("eu.tib.orkg.prototype.statements.domain.model.neo4j")
@EntityScan("eu.tib.orkg.prototype.statements.domain.model.neo4j")
class Application : SpringBootServletInitializer() {
    override fun configure(builder: SpringApplicationBuilder?) =
        builder?.sources(Application::class.java)
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
