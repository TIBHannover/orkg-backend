package eu.tib.orkg.prototype.statements.infrastructure.neo4j.rdf

import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClass
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jClassRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jLiteral
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicate
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jPredicateRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResource
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jResourceRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatement
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatementRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.jupiter.MockitoExtension
import java.util.UUID.randomUUID

@ExtendWith(MockitoExtension::class)
class Neo4jRdfServiceTest {

    @Mock
    private lateinit var classRepository: Neo4jClassRepository

    @Mock
    private lateinit var predicateRepository: Neo4jPredicateRepository

    @Mock
    private lateinit var resourceRepository: Neo4jResourceRepository

    @Mock
    private lateinit var statementRepository: Neo4jStatementRepository

    @InjectMocks
    private lateinit var service: Neo4jRdfService

    @Test
    fun testDumpToNTriple() {
        doReturn(createListWithSingleClass()).`when`(classRepository).findAll()
        doReturn(createListWithResources()).`when`(resourceRepository).findAll()
        doReturn(createListWithSinglePredicate()).`when`(predicateRepository).findAll()
        doReturn(createListWithStatements()).`when`(statementRepository).findAll()

        val result = service.dumpToNTriple()

        assertThat(result).isEqualTo("""
            <http://orkg.org/orkg/class/C1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class> .
            <http://orkg.org/orkg/class/C1> <http://www.w3.org/2000/01/rdf-schema#label> "irrelevant"^^<http://www.w3.org/2001/XMLSchema#string> .
            <http://orkg.org/orkg/predicate/P301> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Predicate> .
            <http://orkg.org/orkg/predicate/P301> <http://www.w3.org/2000/01/rdf-schema#label> "irrelevant"^^<http://www.w3.org/2001/XMLSchema#string> .
            <http://orkg.org/orkg/resource/R201> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Resource> .
            <http://orkg.org/orkg/resource/R201> <http://www.w3.org/2000/01/rdf-schema#label> "irrelevant"^^<http://www.w3.org/2001/XMLSchema#string> .
            <http://orkg.org/orkg/resource/R211> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://orkg.org/orkg/class/Resource> .
            <http://orkg.org/orkg/resource/R211> <http://www.w3.org/2000/01/rdf-schema#label> "irrelevant"^^<http://www.w3.org/2001/XMLSchema#string> .
            <http://orkg.org/orkg/resource/R201> <http://orkg.org/orkg/predicate/P301> "irrelevant"^^<http://www.w3.org/2001/XMLSchema#string> .
            <http://orkg.org/orkg/resource/R201> <http://orkg.org/orkg/predicate/P302> <http://orkg.org/orkg/resource/R211> .
        """.trimIndent() + "\n"
        )
    }

    private fun createListWithSingleClass() = listOf(
        Neo4jClass(id = 1).apply {
            classId = ClassId(1)
            label = "irrelevant"
            createdBy = randomUUID()
        }
    )

    private fun createListWithSinglePredicate() = listOf(
        Neo4jPredicate(
            id = 300,
            predicateId = PredicateId(301),
            label = "irrelevant",
            createdBy = randomUUID()
        )
    )

    private fun createListWithResources() = listOf(
        Neo4jResource(id = 200).apply {
            resourceId = ResourceId(201)
            label = "irrelevant"
            createdBy = randomUUID()
        },
        Neo4jResource(id = 210).apply {
            resourceId = ResourceId(211)
            label = "irrelevant"
            createdBy = randomUUID()
        }
    )

    private fun createListWithStatements() = listOf(
        Neo4jStatement(id = 400).apply {
            statementId = StatementId(401)
            subject = createListWithResources().first()
            predicateId = PredicateId(301)
            `object` = Neo4jLiteral(id = 401).apply {
                label = "irrelevant"
                createdBy = randomUUID()
            }
            createdBy = randomUUID()
        },
        Neo4jStatement(id = 450).apply {
            statementId = StatementId(451)
            subject = createListWithResources().first()
            predicateId = PredicateId(302)
            `object` = createListWithResources()[1]
            createdBy = randomUUID()
        }
    )
}
