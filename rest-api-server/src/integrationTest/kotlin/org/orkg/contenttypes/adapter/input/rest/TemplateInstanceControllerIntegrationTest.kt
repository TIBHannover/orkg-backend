package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
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
import org.orkg.contenttypes.input.CreateTemplateUseCase
import org.orkg.contenttypes.input.StringLiteralPropertyCommand
import org.orkg.contenttypes.input.TemplateRelationsCommand
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.adapter.input.rest.LiteralRepresentation
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.IntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@IntegrationTest
internal class TemplateInstanceControllerIntegrationTest : MockMvcBaseTest("template-instances") {
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
            Predicates.placeholder,
        )

        classService.createClasses(
            Classes.nodeShape,
            Classes.propertyShape,
            Classes.problem,
            Classes.researchField,
        )

        resourceService.createResource(
            id = ThingId("R12"),
            label = "Computer Science",
            classes = setOf(Classes.researchField),
        )

        // Example specific entities

        classService.createClasses(
            Classes.string,
            ThingId("Test"),
            ThingId("Additional"),
        )

        val contributorId = contributorService.createContributor()

        organizationService.createOrganization(
            id = OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"),
            createdBy = contributorId,
        )

        observatoryService.createObservatory(
            id = ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"),
            researchField = ThingId("R12"),
            organizations = setOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")),
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
                    predicate = null,
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
                    ),
                ),
                isClosed = true,
                observatories = listOf(
                    ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"),
                ),
                organizations = listOf(
                    OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"),
                ),
                extractionMethod = ExtractionMethod.UNKNOWN,
            ),
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
    fun createAndUpdate() {
        val templateId = resourceService.findAll(
            includeClasses = setOf(Classes.nodeShape),
            pageable = PageRequests.SINGLE,
        ).single().id

        val templateInstanceId = post("/api/templates/{id}/instances", templateId)
            .content(requestJson("orkg/createTemplateInstance"))
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

        val templateInstance = get("/api/templates/{id}/instances/{instanceId}", templateId, templateInstanceId)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, TemplateInstanceRepresentation::class.java) }

        templateInstance.asClue {
            it.root.asClue { root ->
                root.id shouldBe templateInstanceId
                root.label shouldBe "test instance"
                root.classes shouldBe setOf(ThingId("Test"), ThingId("Additional"))
                root.shared shouldBe 0
                root.observatoryId shouldBe ObservatoryId.UNKNOWN
                root.organizationId shouldBe OrganizationId.UNKNOWN
                root.createdAt shouldNotBe null
                root.createdBy shouldBe ContributorId(MockUserId.USER)
                root.verified shouldBe false
                root.visibility shouldBe Visibility.DEFAULT
                root.modifiable shouldBe true
                root.unlistedBy shouldBe null
                root.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            }
            it.predicates.asClue { predicates ->
                predicates.size shouldBe 1
                predicates shouldContainKey Predicates.hasURL
                predicates[Predicates.hasURL].shouldNotBeNull().asClue { hasURL ->
                    hasURL.id shouldBe Predicates.hasURL
                    hasURL.label shouldBe "url"
                    hasURL.description shouldBe null
                    hasURL.createdAt shouldNotBe null
                    hasURL.createdBy shouldBe ContributorId(MockUserId.UNKNOWN)
                    hasURL.modifiable shouldBe true
                    hasURL.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                }
            }
            it.statements.asClue { statements ->
                statements.size shouldBe 1
                statements shouldContainKey Predicates.hasURL
                statements[Predicates.hasURL].shouldNotBeNull().asClue { hasUrlStatements ->
                    hasUrlStatements.size shouldBe 1
                    hasUrlStatements.single().asClue { `object` ->
                        `object`.shouldNotBeNull()
                        `object`.thing.shouldBeInstanceOf<LiteralRepresentation>().asClue { thing ->
                            thing.id shouldNotBe null
                            thing.label shouldBe "https://sandbox.orkg.org/"
                            thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                            thing.createdAt shouldNotBe null
                            thing.createdBy shouldBe ContributorId(MockUserId.USER)
                            thing.modifiable shouldBe true
                            thing.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                        }
                        `object`.createdAt shouldNotBe null
                        `object`.createdBy shouldBe ContributorId(MockUserId.USER)
                        `object`.statements shouldBe emptyMap()
                    }
                }
            }
        }

        put("/api/templates/{id}/instances/{instanceId}", templateId, templateInstanceId)
            .content(requestJson("orkg/updateTemplateInstance"))
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .contentType(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTemplateInstance = get("/api/templates/{id}/instances/{instanceId}", templateId, templateInstanceId)
            .accept(TEMPLATE_INSTANCE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, TemplateInstanceRepresentation::class.java) }

        updatedTemplateInstance.asClue {
            it.root.asClue { root ->
                root.id shouldBe templateInstanceId
                root.label shouldBe "test instance"
                root.classes shouldBe setOf(ThingId("Test"), ThingId("Additional"))
                root.shared shouldBe 0
                root.observatoryId shouldBe ObservatoryId.UNKNOWN
                root.organizationId shouldBe OrganizationId.UNKNOWN
                root.createdAt shouldNotBe null
                root.createdBy shouldBe ContributorId(MockUserId.USER)
                root.verified shouldBe false
                root.visibility shouldBe Visibility.DEFAULT
                root.modifiable shouldBe true
                root.unlistedBy shouldBe null
                root.extractionMethod shouldBe ExtractionMethod.UNKNOWN
            }
            it.predicates.asClue { predicates ->
                predicates.size shouldBe 1
                predicates shouldContainKey Predicates.hasURL
                predicates[Predicates.hasURL].shouldNotBeNull().asClue { hasURL ->
                    hasURL.id shouldBe Predicates.hasURL
                    hasURL.label shouldBe "url"
                    hasURL.description shouldBe null
                    hasURL.createdAt shouldNotBe null
                    hasURL.createdBy shouldBe ContributorId(MockUserId.UNKNOWN)
                    hasURL.modifiable shouldBe true
                    hasURL.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                }
            }
            it.statements.asClue { statements ->
                statements.size shouldBe 1
                statements shouldContainKey Predicates.hasURL
                statements[Predicates.hasURL].shouldNotBeNull().asClue { hasUrlStatements ->
                    hasUrlStatements.size shouldBe 1
                    hasUrlStatements.single().asClue { `object` ->
                        `object`.shouldNotBeNull()
                        `object`.thing.shouldBeInstanceOf<LiteralRepresentation>().asClue { thing ->
                            thing.id shouldNotBe null
                            thing.label shouldBe "https://orkg.org/"
                            thing.datatype shouldBe Literals.XSD.STRING.prefixedUri
                            thing.createdAt shouldNotBe null
                            thing.createdBy shouldBe ContributorId(MockUserId.USER)
                            thing.modifiable shouldBe true
                            thing.extractionMethod shouldBe ExtractionMethod.UNKNOWN
                        }
                        `object`.createdAt shouldNotBe null
                        `object`.createdBy shouldBe ContributorId(MockUserId.USER)
                        `object`.statements shouldBe emptyMap()
                    }
                }
            }
        }
    }
}
