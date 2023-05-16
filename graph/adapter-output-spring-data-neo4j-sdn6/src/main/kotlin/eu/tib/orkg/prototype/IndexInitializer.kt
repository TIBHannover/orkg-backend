package eu.tib.orkg.prototype

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class IndexInitializer : InitializingBean {
    @Autowired
    private lateinit var neo4jClient: Neo4jClient

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun afterPropertiesSet() {
        with (neo4jClient) {
            logger.info("Creating indexes for Neo4j")
            query("""CREATE FULLTEXT INDEX fulltext_idx_for_resource_on_label IF NOT EXISTS FOR (n:Resource) ON EACH [n.label]""").run()
            query("""CREATE FULLTEXT INDEX fulltext_idx_for_class_on_label IF NOT EXISTS FOR (n:Class) ON EACH [n.label]""").run()
        }
    }
}
