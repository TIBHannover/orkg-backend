package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import eu.tib.orkg.prototype.statements.domain.model.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.*
import org.springframework.beans.factory.annotation.*
import org.springframework.boot.test.context.*
import org.springframework.test.context.junit.jupiter.*
import org.springframework.transaction.annotation.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Transactional
@DisplayName("Neo4: Resource service")
class Neo4jResourceServiceTest {

    @Autowired
    private lateinit var service: ResourceService

    @Test
    @DisplayName("should create a new resource from label")
    fun shouldCreateNewResourceFromLabel() {
        val expected = Resource(ResourceId(0), "some label")
        assertThat(service.create("some label")).isEqualTo(expected)
    }

    @Test
    @DisplayName("should find created resources")
    fun shouldFindCreatedResources() {
        service.create("first")
        service.create("second")

        val resources = service.findAll()
        val labels = resources.map(Resource::label)

        assertThat(resources).hasSize(2)
        assertThat(labels).containsExactlyInAnyOrder("first", "second")
    }
}
