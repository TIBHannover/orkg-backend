package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.auth.input.AuthUseCase
import org.orkg.auth.output.UserRepository
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.createUser
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectRosettaStoneTemplate
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocumentationBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@DisplayName("Rosetta Stone Template Controller")
@Transactional
@Import(MockUserDetailsService::class)
class RosettaStoneTemplateControllerIntegrationTest : RestDocumentationBaseTest() {

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var userService: AuthUseCase

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var rosettaStoneTemplateService: RosettaStoneTemplateUseCases

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 10)

        cleanup()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.listOrganizations()).hasSize(0)
        assertThat(organizationService.listConferences()).hasSize(0)

        listOf(
            Predicates.description,
            Predicates.hasAuthor,
            Predicates.hasAuthors,
            Predicates.hasContribution,
            Predicates.hasResearchField,
            Predicates.hasResearchProblem,
            Predicates.placeholder,
            Predicates.shClass,
            Predicates.shClosed,
            Predicates.shDatatype,
            Predicates.shMaxCount,
            Predicates.shMaxInclusive,
            Predicates.shMinCount,
            Predicates.shMinInclusive,
            Predicates.shOrder,
            Predicates.shPath,
            Predicates.shPattern,
            Predicates.shProperty,
            Predicates.shTargetClass,
            Predicates.templateLabelFormat,
            Predicates.hasSubjectPosition,
            Predicates.hasObjectPosition,
        ).forEach { predicateService.createPredicate(it) }

        setOf(
            Classes.rosettaNodeShape,
            Classes.propertyShape,
            Classes.problem,
            Classes.researchField,
            Classes.string,
            Classes.integer,
            Classes.decimal,
            Classes.float
        ).forEach { classService.createClass(label = it.value, id = it.value) }

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf("ResearchField")
        )

        // Example specific entities

        classService.createClasses("C123", "C24", "C25", "C27", "C28", "C456")

        resourceService.createResource(id = "R15", classes = setOf(Classes.problem.value))
        resourceService.createResource(id = "R16", classes = setOf(Classes.problem.value))
        resourceService.createResource(
            id = "R13",
            label = "Engineering",
            classes = setOf("ResearchField")
        )

        val userId = userService.createUser()

        organizationService.createOrganization(
            createdBy = ContributorId(userId),
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
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()
        observatoryService.removeAll()
        organizationService.removeAll()
        userRepository.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun create() {
        val id = createRosettaStoneTemplate()

        val rosettaStoneTemplate = get("/api/rosetta-stone/templates/{id}", id)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneTemplate()
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, RosettaStoneTemplateRepresentation::class.java) }

        rosettaStoneTemplate.asClue {
            it.id shouldBe id
            it.label shouldBe "rosetta stone template"
            it.description shouldBe "rosetta stone template description"
            it.formattedLabel shouldBe "{0} {1} {2} {3} {4} {5}"
            it.targetClass shouldNotBe null
            it.properties.size shouldBe 6
            it.properties[0].shouldBeInstanceOf<ResourceTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "subject position"
                property.placeholder shouldBe "subject"
                property.description shouldBe "subject"
                property.order shouldBe 0
                property.minCount shouldBe 1
                property.maxCount shouldBe 4
                property.path shouldBe ObjectIdAndLabel(Predicates.hasSubjectPosition, "label")
                property.`class` shouldBe ObjectIdAndLabel(ThingId("C28"), "C28")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[1].shouldBeInstanceOf<UntypedTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "property label"
                property.placeholder shouldBe "property placeholder"
                property.description shouldBe "property description"
                property.order shouldBe 1
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "label")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[2].shouldBeInstanceOf<StringLiteralTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "string literal property label"
                property.placeholder shouldBe "string literal property placeholder"
                property.description shouldBe "string literal property description"
                property.order shouldBe 2
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.pattern shouldBe "\\d+"
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "label")
                property.datatype shouldBe ObjectIdAndLabel(ThingId("String"), "String")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[3].shouldBeInstanceOf<NumberLiteralTemplatePropertyRepresentation<*>>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "number literal property label"
                property.placeholder shouldBe "number literal property placeholder"
                property.description shouldBe "number literal property description"
                property.order shouldBe 3
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.minInclusive shouldBe -1
                property.maxInclusive shouldBe 10
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "label")
                property.datatype shouldBe ObjectIdAndLabel(ThingId("Integer"), "Integer")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[4].shouldBeInstanceOf<OtherLiteralTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "literal property label"
                property.placeholder shouldBe "literal property placeholder"
                property.description shouldBe "literal property description"
                property.order shouldBe 4
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "label")
                property.datatype shouldBe ObjectIdAndLabel(ThingId("C25"), "C25")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[5].shouldBeInstanceOf<ResourceTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "resource property label"
                property.placeholder shouldBe "resource property placeholder"
                property.description shouldBe "resource property description"
                property.order shouldBe 5
                property.minCount shouldBe 3
                property.maxCount shouldBe 4
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "label")
                property.`class` shouldBe ObjectIdAndLabel(ThingId("C28"), "C28")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
        }
    }

    private fun createRosettaStoneTemplate(): ThingId =
        post("/api/rosetta-stone/templates")
            .content(createRosettaStoneTemplateJson)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

    private fun RequestBuilder.perform(): ResultActions = mockMvc.perform(this)
}

private const val createRosettaStoneTemplateJson = """{
  "label": "rosetta stone template",
  "description": "rosetta stone template description",
  "formatted_label": "{0} {1} {2} {3} {4} {5}",
  "properties": [
    {
      "label": "subject position",
      "placeholder": "subject",
      "description": "subject",
      "min_count": 1,
      "max_count": 4,
      "path": "hasSubjectPosition",
      "class": "C28"
    },
    {
      "label": "property label",
      "placeholder": "property placeholder",
      "description": "property description",
      "min_count": 1,
      "max_count": 2,
      "path": "hasObjectPosition"
    },
    {
      "label": "string literal property label",
      "placeholder": "string literal property placeholder",
      "description": "string literal property description",
      "min_count": 1,
      "max_count": 2,
      "pattern": "\\d+",
      "path": "hasObjectPosition",
      "datatype": "String"
    },
    {
      "label": "number literal property label",
      "placeholder": "number literal property placeholder",
      "description": "number literal property description",
      "min_count": 1,
      "max_count": 2,
      "min_inclusive": -1,
      "max_inclusive": 10,
      "path": "hasObjectPosition",
      "datatype": "Integer"
    },
    {
      "label": "literal property label",
      "placeholder": "literal property placeholder",
      "description": "literal property description",
      "min_count": 1,
      "max_count": 2,
      "path": "hasObjectPosition",
      "datatype": "C25"
    },
    {
      "label": "resource property label",
      "placeholder": "resource property placeholder",
      "description": "resource property description",
      "min_count": 3,
      "max_count": 4,
      "path": "hasObjectPosition",
      "class": "C28"
    }
  ],
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ]
}"""
