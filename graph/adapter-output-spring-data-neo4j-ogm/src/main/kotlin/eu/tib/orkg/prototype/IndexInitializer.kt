package eu.tib.orkg.prototype

import org.neo4j.ogm.session.SessionFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
// Required to suppress bean initialization during integration tests, when no neo4j container is running
@ConditionalOnProperty("spring.data.neo4j.uri")
class IndexInitializer : InitializingBean {
    @Autowired
    private lateinit var sessionFactory: SessionFactory

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    override fun afterPropertiesSet() {
        with (sessionFactory.openSession()) {
            logger.info("Creating indexes for Neo4j")
            query("""CREATE FULLTEXT INDEX fulltext_idx_for_resource_on_label IF NOT EXISTS FOR (n:Resource) ON EACH [n.label]""", emptyMap<String, Any>())
            query("""CREATE FULLTEXT INDEX fulltext_idx_for_class_on_label IF NOT EXISTS FOR (n:Class) ON EACH [n.label]""", emptyMap<String, Any>())
            query("""CREATE FULLTEXT INDEX fulltext_idx_for_predicate_on_label IF NOT EXISTS FOR (n:Predicate) ON EACH [n.label]""", emptyMap<String, Any>())
            query("""CREATE FULLTEXT INDEX fulltext_idx_for_literal_on_label IF NOT EXISTS FOR (n:Literal) ON EACH [n.label]""", emptyMap<String, Any>())
            clear()
        }
    }
}
