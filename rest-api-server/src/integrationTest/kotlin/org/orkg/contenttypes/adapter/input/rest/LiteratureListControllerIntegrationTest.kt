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
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.LiteratureListListSection
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListTextSection
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.ResourceReference
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.input.LiteratureListUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createLiteral
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.createStatement
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class LiteratureListControllerIntegrationTest : MockMvcBaseTest("literature-lists") {
    @Autowired
    private lateinit var contributorService: ContributorUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var statementService: StatementUseCases

    @Autowired
    private lateinit var literalService: LiteralUseCases

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var literatureListService: LiteratureListUseCases

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
            Predicates.description,
            Predicates.hasAuthors,
            Predicates.hasContent,
            Predicates.hasEntry,
            Predicates.hasEvaluation,
            Predicates.hasHeadingLevel,
            Predicates.hasLink,
            Predicates.hasListElement,
            Predicates.hasORCID,
            Predicates.hasResearchField,
            Predicates.hasSection,
            Predicates.hasURL,
            Predicates.hasWebsite,
            Predicates.sustainableDevelopmentGoal,
        )

        classService.createClasses(
            Classes.literatureList,
            Classes.contribution,
            Classes.paper,
            Classes.dataset,
            Classes.researchField,
            Classes.author,
            Classes.software,
            Classes.sustainableDevelopmentGoal,
        )

        resourceService.createResource(
            id = ThingId("R12"),
            label = "Computer Science",
            classes = setOf(Classes.researchField)
        )
        resourceService.createResource(
            id = ThingId("R194"),
            label = "Engineering",
            classes = setOf(Classes.researchField)
        )

        // Example specific entities

        classService.createClasses(ThingId("C123"))

        resourceService.createResource(id = ThingId("R3003"), label = "Some resource", classes = setOf(Classes.paper))
        resourceService.createResource(id = ThingId("R3004"), label = "Some other resource", classes = setOf(Classes.software))
        resourceService.createResource(id = ThingId("R3005"), label = "Some dataset resource", classes = setOf(Classes.dataset))

        resourceService.createResource(id = ThingId("R123"), label = "Author with id", classes = setOf(Classes.author))
        resourceService.createResource(id = ThingId("SDG_1"), label = "No poverty", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_2"), label = "Zero hunger", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_3"), label = "Good health and well-being", classes = setOf(Classes.sustainableDevelopmentGoal))
        resourceService.createResource(id = ThingId("SDG_4"), label = "Quality education", classes = setOf(Classes.sustainableDevelopmentGoal))

        statementService.createStatement(
            subject = resourceService.createResource(
                id = ThingId("R456"),
                label = "Author with id and orcid",
                classes = setOf(Classes.author)
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "1111-2222-3333-4444")
        )

        statementService.createStatement(
            subject = resourceService.createResource(
                id = ThingId("R4567"),
                label = "Author with orcid",
                classes = setOf(Classes.author)
            ),
            predicate = Predicates.hasORCID,
            `object` = literalService.createLiteral(label = "0000-1111-2222-3333")
        )

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
        predicateService.deleteAll()
        resourceService.deleteAll()
        classService.deleteAll()
        observatoryService.deleteAll()
        organizationService.deleteAll()
        contributorService.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdate() {
        val id = createLiteratureList()

        val literatureList = get("/api/literature-lists/{id}", id)
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, LiteratureListRepresentation::class.java) }

        literatureList.asClue {
            it.id shouldBe id
            it.title shouldBe "example literature list"
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(ThingId("R12"), "Computer Science")
            )
            it.authors.size shouldBe 5
            it.authors[0] shouldBe AuthorRepresentation(
                name = "Author with id",
                id = ThingId("R123"),
                identifiers = emptyMap(),
                homepage = null
            )
            it.authors[1] shouldBe AuthorRepresentation(
                name = "Author with orcid",
                id = ThingId("R4567"),
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                homepage = null
            )
            it.authors[2] shouldBe AuthorRepresentation(
                name = "Author with id and orcid",
                id = ThingId("R456"),
                identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444")),
                homepage = null
            )
            it.authors[3].asClue { author ->
                author.name shouldBe "Author with homepage"
                author.id shouldNotBe null
                author.identifiers shouldBe emptyMap()
                author.homepage shouldBe ParsedIRI("https://example.org/author")
            }
            it.authors[4] shouldBe AuthorRepresentation(
                name = "Author that just has a name",
                id = null,
                identifiers = emptyMap(),
                homepage = null
            )
            it.versions shouldBe VersionInfoRepresentation(
                head = HeadVersionRepresentation(id, it.title, it.createdAt, it.createdBy),
                published = emptyList()
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                LabeledObjectRepresentation(ThingId("SDG_1"), "No poverty"),
                LabeledObjectRepresentation(ThingId("SDG_2"), "Zero hunger")
            )
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
            it.published shouldBe false
            it.sections.size shouldBe 2
            it.sections[0].shouldBeInstanceOf<TextSectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "this is a heading"
                section.headingSize shouldBe 2
                section.text shouldBe "text contents of this section"
            }
            it.sections[1].shouldBeInstanceOf<ListSectionRepresentation>().asClue { section ->
                section.id shouldNotBe null
                section.entries shouldBe listOf(
                    ListSectionRepresentation.EntryRepresentation(
                        ResourceReferenceRepresentation(ThingId("R3003"), "Some resource", setOf(Classes.paper)),
                        "example description"
                    ),
                    ListSectionRepresentation.EntryRepresentation(
                        ResourceReferenceRepresentation(ThingId("R3004"), "Some other resource", setOf(Classes.software)),
                        null
                    )
                )
            }
            it.acknowledgements shouldBe mapOf(
                ContributorId(MockUserId.USER) to 0.6666666666666666,
                ContributorId.UNKNOWN to 0.3333333333333333
            )
        }

        put("/api/literature-lists/{id}", id)
            .content(requestJson("orkg/updateLiteratureList"))
            .accept(LITERATURE_LIST_JSON_V1)
            .contentType(LITERATURE_LIST_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedLiteratureList = literatureListService.findById(id).orElseThrow { LiteratureListNotFound(id) }

        updatedLiteratureList.asClue {
            it.id shouldBe id
            it.title shouldBe "updated literature list title"
            it.researchFields shouldBe listOf(
                ObjectIdAndLabel(ThingId("R194"), "Engineering")
            )
            it.authors.size shouldBe 5
            it.authors[0] shouldBe Author(
                name = "Author with id",
                id = ThingId("R123"),
                identifiers = emptyMap(),
                homepage = null
            )
            it.authors[1] shouldBe Author(
                name = "Author with orcid",
                id = ThingId("R4567"),
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333")),
                homepage = null
            )
            it.authors[2] shouldBe Author(
                name = "Author with id and orcid",
                id = ThingId("R456"),
                identifiers = mapOf("orcid" to listOf("1111-2222-3333-4444", "4444-3333-2222-1111")),
                homepage = null
            )
            it.authors[3].asClue { author ->
                author.name shouldBe "Author with homepage"
                author.id shouldNotBe null
                author.identifiers shouldBe emptyMap()
                author.homepage shouldBe ParsedIRI("https://example.org/author")
            }
            it.authors[4] shouldBe Author(
                name = "Another author that just has a name",
                id = null,
                identifiers = emptyMap(),
                homepage = null
            )
            it.versions shouldBe VersionInfo(
                head = HeadVersion(id, it.title, it.createdAt, it.createdBy),
                published = emptyList()
            )
            it.sustainableDevelopmentGoals shouldBe setOf(
                ObjectIdAndLabel(ThingId("SDG_3"), "Good health and well-being"),
                ObjectIdAndLabel(ThingId("SDG_4"), "Quality education")
            )
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.visibility shouldBe Visibility.DELETED
            it.unlistedBy shouldBe null
            it.published shouldBe false
            it.sections[0].shouldBeInstanceOf<LiteratureListTextSection>().asClue { section ->
                section.id shouldNotBe null
                section.heading shouldBe "updated heading"
                section.headingSize shouldBe 3
                section.text shouldBe "updated text contents"
            }
            it.sections[1].shouldBeInstanceOf<LiteratureListListSection>().asClue { section ->
                section.id shouldNotBe null
                section.entries shouldBe listOf(
                    LiteratureListListSection.Entry(
                        ResourceReference(ThingId("R3004"), "Some other resource", setOf(Classes.software)),
                        "new description"
                    ),
                    LiteratureListListSection.Entry(
                        ResourceReference(ThingId("R3003"), "Some resource", setOf(Classes.paper)),
                        null
                    ),
                    LiteratureListListSection.Entry(
                        ResourceReference(ThingId("R3005"), "Some dataset resource", setOf(Classes.dataset)),
                        "updated example description"
                    )
                )
            }
            it.acknowledgements shouldBe mapOf(
                ContributorId(MockUserId.USER) to 0.625,
                ContributorId.UNKNOWN to 0.375
            )
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdateTextSection() {
        val id = createLiteratureList()

        post("/api/literature-lists/$id/sections")
            .content(requestJson("orkg/createLiteratureListTextSection"))
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val literatureList = literatureListService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        literatureList.sections.size shouldBe 3
        literatureList.sections.last().shouldBeInstanceOf<LiteratureListTextSection>().asClue {
            it.id shouldNotBe null
            it.heading shouldBe "text section heading"
            it.headingSize shouldBe 2
            it.text shouldBe "text section contents"
        }

        val literatureListSectionId = literatureList.sections.last().id

        put("/api/literature-lists/{id}/sections/{literatureListSectionId}", id, literatureListSectionId)
            .content(requestJson("orkg/updateLiteratureListTextSection"))
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedLiteratureList = literatureListService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedLiteratureList.sections.last().shouldBeInstanceOf<LiteratureListTextSection>().asClue {
            it.id shouldBe literatureListSectionId
            it.heading shouldBe "updated text section heading"
            it.headingSize shouldBe 3
            it.text shouldBe "updated text section contents"
        }
    }

    @Test
    @TestWithMockUser
    fun createAndFetchAndUpdateListSection() {
        val id = createLiteratureList()

        post("/api/literature-lists/$id/sections")
            .content(requestJson("orkg/createLiteratureListListSection"))
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val literatureList = literatureListService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        literatureList.sections.size shouldBe 3
        literatureList.sections.last().shouldBeInstanceOf<LiteratureListListSection>().asClue {
            it.id shouldNotBe null
            it.entries shouldBe listOf(
                LiteratureListListSection.Entry(
                    ResourceReference(ThingId("R3005"), "Some dataset resource", setOf(Classes.dataset)),
                    "example description"
                ),
                LiteratureListListSection.Entry(
                    ResourceReference(ThingId("R3004"), "Some other resource", setOf(Classes.software)),
                    null
                ),
                LiteratureListListSection.Entry(
                    ResourceReference(ThingId("R3003"), "Some resource", setOf(Classes.paper)),
                    null
                )
            )
        }

        val literatureListSectionId = literatureList.sections.last().id

        put("/api/literature-lists/{id}/sections/{literatureListSectionId}", id, literatureListSectionId)
            .content(requestJson("orkg/updateLiteratureListListSection"))
            .accept(LITERATURE_LIST_SECTION_JSON_V1)
            .contentType(LITERATURE_LIST_SECTION_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedLiteratureList = literatureListService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedLiteratureList.sections.last().shouldBeInstanceOf<LiteratureListListSection>().asClue {
            it.id shouldBe literatureListSectionId
            it.entries shouldBe listOf(
                LiteratureListListSection.Entry(
                    ResourceReference(ThingId("R3003"), "Some resource", setOf(Classes.paper)),
                    null
                ),
                LiteratureListListSection.Entry(
                    ResourceReference(ThingId("R3004"), "Some other resource", setOf(Classes.software)),
                    "new description"
                ),
                LiteratureListListSection.Entry(
                    ResourceReference(ThingId("R3005"), "Some dataset resource", setOf(Classes.dataset)),
                    "updated example description"
                )
            )
        }
    }

    private fun createLiteratureList() = post("/api/literature-lists")
        .content(requestJson("orkg/createLiteratureList"))
        .accept(LITERATURE_LIST_JSON_V1)
        .contentType(LITERATURE_LIST_JSON_V1)
        .perform()
        .andExpect(status().isCreated)
        .andReturn()
        .response
        .getHeaderValue("Location")!!
        .toString()
        .substringAfterLast("/")
        .let(::ThingId)
}
