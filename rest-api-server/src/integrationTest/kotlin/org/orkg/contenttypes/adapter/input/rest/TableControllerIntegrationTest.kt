package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.domain.TableNotFound
import org.orkg.contenttypes.input.TableUseCases
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createLiteral
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.PostgresContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
@PostgresContainerIntegrationTest
internal class TableControllerIntegrationTest : MockMvcBaseTest("tables") {
    @Autowired
    private lateinit var contributorService: ContributorUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var tableService: TableUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        cleanup()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.findAll()).hasSize(0)
        assertThat(organizationService.findAllConferences()).hasSize(0)

        predicateService.createPredicates(
            Predicates.csvwCells,
            Predicates.csvwColumn,
            Predicates.csvwColumns,
            Predicates.csvwNumber,
            Predicates.csvwRows,
            Predicates.csvwTitles,
            Predicates.csvwValue,
            Predicates.description,
            Predicates.hasListElement
        )

        classService.createClasses(
            Classes.table,
            Classes.row,
            Classes.column,
            Classes.cell,
            Classes.researchField,
        )

        resourceService.createResource(
            id = ThingId("R12"),
            label = "Computer Science",
            classes = setOf(Classes.researchField)
        )

        // Example specific entities

        classService.createClass(label = "some class", id = ThingId("C123"))
        resourceService.createResource(id = ThingId("R123"), label = "some resource")
        predicateService.createPredicate(id = ThingId("P123"), label = "some predicate")
        literalService.createLiteral(id = ThingId("L123"), label = "other header name")

        classService.createClass(label = "Result", id = ThingId("Result"))

        val contributorId = contributorService.createContributor()

        organizationService.createOrganization(
            createdBy = contributorId,
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
        )

        observatoryService.createObservatory(
            organizations = setOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")),
            researchField = ThingId("R12"),
            id = ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
        )
    }

    @AfterEach
    fun cleanup() {
        literalService.deleteAll()
        predicateService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()
        observatoryService.deleteAll()
        organizationService.deleteAll()
        contributorService.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndUpdate() {
        val id = post("/api/tables")
            .content(requestJson("orkg/createTable"))
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

        val table = get("/api/tables/{id}", id)
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, TableRepresentation::class.java) }

        table.asClue {
            it.id shouldBe id
            it.label shouldBe "example table"
            it.rows.size shouldBe 7
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 2
                row.data[0].shouldBeInstanceOf<LiteralReferenceRepresentation> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<LiteralReferenceRepresentation> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 2
                row.data[0].shouldBeInstanceOf<ResourceReferenceRepresentation> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO"
                    thing.classes shouldBe setOf(ThingId("Result"))
                }
                row.data[1].shouldBeInstanceOf<LiteralReferenceRepresentation> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.1"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 2
                row.data[0].shouldBeInstanceOf<ResourceReferenceRepresentation> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<PredicateReferenceRepresentation> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "hasResult"
                }
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 2
                row.data[0].shouldBeInstanceOf<ClassReferenceRepresentation> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[1].shouldBeInstanceOf<PredicateReferenceRepresentation> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
            }
            it.rows[4].asClue { row ->
                row.label shouldBe "row 4"
                row.data.size shouldBe 2
                row.data[0].shouldBeInstanceOf<ClassReferenceRepresentation> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C123")
                }
                row.data[1] shouldBe null
            }
            it.rows[5].asClue { row ->
                row.label shouldBe "row 5"
                row.data.size shouldBe 2
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<ResourceReferenceRepresentation> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "list"
                    thing.classes shouldBe setOf(Classes.list)
                }
            }
            it.rows[6].asClue { row ->
                row.label shouldBe "row 6"
                row.data.size shouldBe 2
                row.data[0] shouldBe null
                row.data[1] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }

        // test column creation, row deletion

        put("/api/tables/{id}", id)
            .content(requestJson("orkg/updateTable4x3"))
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTable4x3 = tableService.findById(id).orElseThrow { TableNotFound(id) }

        updatedTable4x3.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example table"
            it.rows.size shouldBe 4
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 3"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[3].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 4"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO2"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.8"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
                row.data[3].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "has other Result"
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[1].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
                row.data[2].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "some other test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C1234")
                }
                row.data[3] shouldBe null
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 4
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "different list"
                    thing.classes shouldBe setOf(Classes.list)
                }
                row.data[2] shouldBe null
                row.data[3] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }

        // test row creation, column deletion

        put("/api/tables/{id}", id)
            .content(requestJson("orkg/updateTable3x4"))
            .accept(TABLE_JSON_V1)
            .contentType(TABLE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        assertTableMatchesExpectedResult(id)

        // test single row creation

        post("/api/tables/{id}/rows/{index}", id, 2)
            .content(requestJson("orkg/createTableRow"))
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        tableService.findById(id).orElseThrow { TableNotFound(id) }.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example table"
            it.rows.size shouldBe 6
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 3"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO2"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.8"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "inserted row"
                row.data.size shouldBe 3
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO3"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "has other Result"
                }
                row.data[1].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[2].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
            }
            it.rows[4].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "some other test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C12345")
                }
                row.data[1] shouldBe null
                row.data[2] shouldBe null
            }
            it.rows[5].asClue { row ->
                row.label shouldBe "row 4"
                row.data.size shouldBe 3
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "different list"
                    thing.classes shouldBe setOf(Classes.list)
                }
                row.data[2] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }

        // test single row update

        put("/api/tables/{id}/rows/{index}", id, 2)
            .content(requestJson("orkg/updateTableRow"))
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        tableService.findById(id).orElseThrow { TableNotFound(id) }.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example table"
            it.rows.size shouldBe 6
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 3"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO2"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.8"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "updated row"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "updated test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C123456")
                }
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
                row.data[2] shouldBe null
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "has other Result"
                }
                row.data[1].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[2].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
            }
            it.rows[4].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "some other test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C12345")
                }
                row.data[1] shouldBe null
                row.data[2] shouldBe null
            }
            it.rows[5].asClue { row ->
                row.label shouldBe "row 4"
                row.data.size shouldBe 3
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "different list"
                    thing.classes shouldBe setOf(Classes.list)
                }
                row.data[2] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }

        // testing single row deletion

        delete("/api/tables/{id}/rows/{index}", id, 2)
            .accept(TABLE_ROW_JSON_V1)
            .contentType(TABLE_ROW_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        assertTableMatchesExpectedResult(id)

        // test single column creation

        post("/api/tables/{id}/columns/{index}", id, 2)
            .content(requestJson("orkg/createTableColumn"))
            .accept(TABLE_COLUMN_JSON_V1)
            .contentType(TABLE_COLUMN_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        tableService.findById(id).orElseThrow { TableNotFound(id) }.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example table"
            it.rows.size shouldBe 5
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "inserted column"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[3].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 3"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO2"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.8"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldBe ThingId("L123")
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[3].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "has other Result"
                }
                row.data[1].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO4"
                    thing.classes shouldBe emptySet()
                }
                row.data[3].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "some other test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C12345")
                }
                row.data[1] shouldBe null
                row.data[2] shouldBe null
                row.data[3] shouldBe null
            }
            it.rows[4].asClue { row ->
                row.label shouldBe "row 4"
                row.data.size shouldBe 4
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "different list"
                    thing.classes shouldBe setOf(Classes.list)
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
                row.data[3] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }

        // test single column update

        put("/api/tables/{id}/columns/{index}", id, 2)
            .content(requestJson("orkg/updateTableColumn"))
            .accept(TABLE_COLUMN_JSON_V1)
            .contentType(TABLE_COLUMN_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        tableService.findById(id).orElseThrow { TableNotFound(id) }.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example table"
            it.rows.size shouldBe 5
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "updated column"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[3].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 3"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO2"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.8"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
                row.data[2] shouldBe null
                row.data[3].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "has other Result"
                }
                row.data[1].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
                row.data[3].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 4
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "some other test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C12345")
                }
                row.data[1] shouldBe null
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldBe ThingId("L123")
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[3] shouldBe null
            }
            it.rows[4].asClue { row ->
                row.label shouldBe "row 4"
                row.data.size shouldBe 4
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "different list"
                    thing.classes shouldBe setOf(Classes.list)
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
                row.data[3] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }

        // testing single column deletion

        delete("/api/tables/{id}/columns/{index}", id, 2)
            .accept(TABLE_COLUMN_JSON_V1)
            .contentType(TABLE_COLUMN_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        assertTableMatchesExpectedResult(id)

        // test single cell update

        put("/api/tables/{id}/cells/{row}/{column}", id, 3, 2)
            .content(requestJson("orkg/updateTableCell"))
            .accept(TABLE_CELL_JSON_V1)
            .contentType(TABLE_CELL_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        tableService.findById(id).orElseThrow { TableNotFound(id) }.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example table"
            it.rows.size shouldBe 5
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 3"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO2"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.8"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "has other Result"
                }
                row.data[1].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[2].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "some other test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C12345")
                }
                row.data[1] shouldBe null
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[4].asClue { row ->
                row.label shouldBe "row 4"
                row.data.size shouldBe 3
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "different list"
                    thing.classes shouldBe setOf(Classes.list)
                }
                row.data[2] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }
    }

    private fun assertTableMatchesExpectedResult(id: ThingId) {
        tableService.findById(id).orElseThrow { TableNotFound(id) }.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example table"
            it.rows.size shouldBe 5
            it.rows[0].asClue { row ->
                row.label shouldBe null
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "header value"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "other header name"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "column 3"
                    thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                }
            }
            it.rows[1].asClue { row ->
                row.label shouldBe "row 1"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "MOTO2"
                    thing.classes shouldBe emptySet()
                }
                row.data[1].shouldBeInstanceOf<Literal> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "0.8"
                    thing.datatype shouldBe Literals.XSD.DECIMAL.prefixedUri
                }
                row.data[2].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldBe ThingId("R123")
                    thing.label shouldBe "some resource"
                    thing.classes shouldBe emptySet()
                }
            }
            it.rows[2].asClue { row ->
                row.label shouldBe "row 2"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "has other Result"
                }
                row.data[1].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldBe ThingId("C123")
                    thing.label shouldBe "some class"
                    thing.uri shouldBe null
                }
                row.data[2].shouldBeInstanceOf<Predicate> { thing ->
                    thing.id shouldBe ThingId("P123")
                    thing.label shouldBe "some predicate"
                }
            }
            it.rows[3].asClue { row ->
                row.label shouldBe "row 3"
                row.data.size shouldBe 3
                row.data[0].shouldBeInstanceOf<Class> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "some other test class"
                    thing.uri shouldBe ParsedIRI.create("https://orkg.org/class/C12345")
                }
                row.data[1] shouldBe null
                row.data[2] shouldBe null
            }
            it.rows[4].asClue { row ->
                row.label shouldBe "row 4"
                row.data.size shouldBe 3
                row.data[0] shouldBe null
                row.data[1].shouldBeInstanceOf<Resource> { thing ->
                    thing.id shouldNotBe null
                    thing.label shouldBe "different list"
                    thing.classes shouldBe setOf(Classes.list)
                }
                row.data[2] shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.modifiable shouldBe true
            it.unlistedBy shouldBe null
        }
    }
}
