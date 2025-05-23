package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.domain.SnapshotId
import org.orkg.contenttypes.domain.TemplateInstance
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.StringLiteralPropertyCommand
import org.orkg.contenttypes.input.TemplateBasedResourceSnapshotUseCases
import org.orkg.contenttypes.input.TemplateRelationsCommand
import org.orkg.contenttypes.input.TemplateUseCases
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
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Predicates
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
internal class TemplateBasedResourceSnapshotControllerIntegrationTest : MockMvcBaseTest("template-based-resource-snapshot") {
    @Autowired
    private lateinit var templateBasedResourceSnapshotUseCases: TemplateBasedResourceSnapshotUseCases

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
    private lateinit var templateService: TemplateUseCases

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

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
            Predicates.hasAuthor,
            Predicates.hasAuthors,
            Predicates.hasResearchField,
            Predicates.description,
            Predicates.shClass,
            Predicates.shClosed,
            Predicates.shDatatype,
            Predicates.shMaxCount,
            Predicates.shMinCount,
            Predicates.shOrder,
            Predicates.shPath,
            Predicates.shPattern,
            Predicates.shProperty,
            Predicates.shTargetClass,
            Predicates.templateLabelFormat,
            Predicates.templateOfPredicate,
            Predicates.templateOfResearchField,
            Predicates.templateOfResearchProblem,
            Predicates.hasResearchProblem,
            Predicates.hasURL,
            Predicates.placeholder
        )

        classService.createClasses(
            Classes.nodeShape,
            Classes.propertyShape,
            Classes.problem,
            Classes.researchField
        )

        resourceService.createResource(
            id = ThingId("R12"),
            label = "Computer Science",
            classes = setOf(Classes.researchField)
        )

        // Example specific entities

        classService.createClasses(
            Classes.string,
            ThingId("Test")
        )

        val contributorId = contributorService.createContributor()

        organizationService.createOrganization(
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"),
            createdBy = contributorId
        )

        observatoryService.createObservatory(
            id = ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"),
            researchField = ThingId("R12"),
            organizations = setOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
        )

        val targetClass = ThingId("Test")

        templateService.create(
            CreateTemplateUseCase.CreateCommand(
                contributorId = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
                label = "Dummy Template Label",
                description = "Some description about the template",
                formattedLabel = FormattedLabel.of("{P32}"),
                targetClass = targetClass,
                relations = TemplateRelationsCommand(
                    researchFields = listOf(ThingId("R12")),
                    researchProblems = emptyList(),
                    predicate = null
                ),
                properties = listOf(
                    StringLiteralPropertyCommand(
                        label = "literal property label",
                        description = "literal property description",
                        placeholder = "literal property placeholder",
                        minCount = 1,
                        maxCount = 2,
                        pattern = """https?\:\/\/.*""",
                        path = Predicates.hasURL,
                        datatype = Classes.string,
                    )
                ),
                isClosed = true,
                observatories = listOf(
                    ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
                ),
                organizations = listOf(
                    OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
                ),
                extractionMethod = ExtractionMethod.UNKNOWN
            )
        )
        statementService.createStatement(
            subject = resourceService.createResource(
                id = ThingId("R6458"),
                classes = setOf(targetClass)
            ),
            predicate = Predicates.hasURL,
            `object` = literalService.createLiteral()
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
    fun create() {
        val templateId = resourceService.findAll(
            includeClasses = setOf(Classes.nodeShape),
            pageable = PageRequests.SINGLE
        ).single().id

        val id = post("/api/resources/{id}/snapshots", "R6458")
            .content(requestJson("orkg/createTemplateBasedResourceSnapshot").replace("\${templateId}", templateId.value))
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::SnapshotId)

        val snapshot = templateBasedResourceSnapshotUseCases.findById(id)

        snapshot.isPresent shouldBe true
        snapshot.get().asClue {
            it.id shouldBe id
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.createdAt shouldNotBe null
            it.data.shouldBeInstanceOf<TemplateInstance>().asClue { data ->
                data.root.asClue { root ->
                    root.id shouldBe ThingId("R6458")
                }
                data.predicates shouldContainKey Predicates.hasURL
                data.statements shouldContainKey Predicates.hasURL
            }
            it.resourceId shouldBe ThingId("R6458")
            it.templateId shouldBe templateId
            it.handle shouldBe null
        }
    }
}
