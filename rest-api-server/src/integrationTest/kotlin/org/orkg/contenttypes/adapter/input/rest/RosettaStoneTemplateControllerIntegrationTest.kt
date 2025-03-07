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
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.Assets.requestJson
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.domain.ClassReference
import org.orkg.contenttypes.domain.NumberLiteralTemplateProperty
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.OtherLiteralTemplateProperty
import org.orkg.contenttypes.domain.ResourceTemplateProperty
import org.orkg.contenttypes.domain.RosettaStoneTemplateNotFound
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectRosettaStoneTemplate
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class RosettaStoneTemplateControllerIntegrationTest : MockMvcBaseTest("rosetta-stone-templates") {
    @Autowired
    private lateinit var contributorService: ContributorUseCases

    @Autowired
    private lateinit var predicateService: PredicateUseCases

    @Autowired
    private lateinit var resourceService: ResourceUseCases

    @Autowired
    private lateinit var classService: ClassUseCases

    @Autowired
    private lateinit var organizationService: OrganizationUseCases

    @Autowired
    private lateinit var observatoryService: ObservatoryUseCases

    @Autowired
    private lateinit var rosettaStoneTemplateService: RosettaStoneTemplateUseCases

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
            Predicates.exampleOfUsage,
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
        )

        classService.createClasses(
            Classes.rosettaNodeShape,
            Classes.propertyShape,
            Classes.problem,
            Classes.researchField,
        )

        Literals.XSD.entries.forEach { xsd ->
            classService.createClass(
                label = xsd.`class`.value,
                id = xsd.`class`,
                uri = ParsedIRI(xsd.uri)
            )
        }

        resourceService.createResource(
            id = ThingId("R12"),
            label = "Computer Science",
            classes = setOf(Classes.researchField)
        )

        // Example specific entities

        classService.createClasses(
            ThingId("C123"),
            ThingId("C24"),
            ThingId("C25"),
            ThingId("C27"),
            ThingId("C28"),
            ThingId("C456"),
        )

        resourceService.createResource(id = ThingId("R15"), classes = setOf(Classes.problem))
        resourceService.createResource(id = ThingId("R16"), classes = setOf(Classes.problem))
        resourceService.createResource(
            id = ThingId("R13"),
            label = "Engineering",
            classes = setOf(Classes.researchField)
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
    fun createAndUpdate() {
        val id = createRosettaStoneTemplate()

        val rosettaStoneTemplate = get("/api/rosetta-stone/templates/{id}", id)
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
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
            it.exampleUsage shouldBe "example statement usage"
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
                property.path shouldBe ObjectIdAndLabel(Predicates.hasSubjectPosition, "hasSubjectPosition")
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
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
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
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
                property.datatype shouldBe ClassReferenceRepresentation(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[3].shouldBeInstanceOf<NumberLiteralTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "number literal property label"
                property.placeholder shouldBe "number literal property placeholder"
                property.description shouldBe "number literal property description"
                property.order shouldBe 3
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.minInclusive shouldBe RealNumber(-1)
                property.maxInclusive shouldBe RealNumber(10)
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
                property.datatype shouldBe ClassReferenceRepresentation(Classes.integer, "Integer", ParsedIRI(Literals.XSD.INT.uri))
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
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
                property.datatype shouldBe ClassReferenceRepresentation(ThingId("C25"), "C25", null)
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
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
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
            it.modifiable shouldBe true
        }

        put("/api/rosetta-stone/templates/{id}", id)
            .content(requestJson("orkg/updateRosettaStoneTemplate"))
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedRosettaStoneTemplate = rosettaStoneTemplateService.findById(id)
            .orElseThrow { RosettaStoneTemplateNotFound(id) }

        updatedRosettaStoneTemplate.asClue {
            it.id shouldBe id
            it.label shouldBe "updated rosetta stone template"
            it.description shouldBe "updated rosetta stone template description"
            it.formattedLabel shouldBe FormattedLabel.of("updated {0} {1} {2} {3} {4} {5}")
            it.exampleUsage shouldBe "updated example statement usage"
            it.targetClass shouldBe rosettaStoneTemplate.targetClass
            it.properties.size shouldBe 6
            it.properties[0].shouldBeInstanceOf<ResourceTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated subject position"
                property.placeholder shouldBe "updated subject"
                property.description shouldBe "updated subject"
                property.order shouldBe 0
                property.minCount shouldBe 2
                property.maxCount shouldBe 5
                property.path shouldBe ObjectIdAndLabel(Predicates.hasSubjectPosition, "hasSubjectPosition")
                property.`class` shouldBe ObjectIdAndLabel(ThingId("C28"), "C28")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[1].shouldBeInstanceOf<UntypedTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated property label"
                property.placeholder shouldBe "updated property placeholder"
                property.description shouldBe "updated property description"
                property.order shouldBe 1
                property.minCount shouldBe 2
                property.maxCount shouldBe 3
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[2].shouldBeInstanceOf<StringLiteralTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated string literal property label"
                property.placeholder shouldBe "updated string literal property placeholder"
                property.description shouldBe "updated string literal property description"
                property.order shouldBe 2
                property.minCount shouldBe 2
                property.maxCount shouldBe 3
                property.pattern shouldBe "\\w+"
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
                property.datatype shouldBe ClassReference(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[3].shouldBeInstanceOf<NumberLiteralTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated number literal property label"
                property.placeholder shouldBe "updated number literal property placeholder"
                property.description shouldBe "updated number literal property description"
                property.order shouldBe 3
                property.minCount shouldBe 2
                property.maxCount shouldBe 3
                property.minInclusive shouldBe RealNumber(0)
                property.maxInclusive shouldBe RealNumber(11)
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
                property.datatype shouldBe ClassReference(Classes.integer, "Integer", ParsedIRI(Literals.XSD.INT.uri))
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[4].shouldBeInstanceOf<OtherLiteralTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated literal property label"
                property.placeholder shouldBe "updated literal property placeholder"
                property.description shouldBe "updated literal property description"
                property.order shouldBe 4
                property.minCount shouldBe 2
                property.maxCount shouldBe 3
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
                property.datatype shouldBe ClassReference(ThingId("C25"), "C25", null)
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[5].shouldBeInstanceOf<ResourceTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated resource property label"
                property.placeholder shouldBe "updated resource property placeholder"
                property.description shouldBe "updated resource property description"
                property.order shouldBe 5
                property.minCount shouldBe 4
                property.maxCount shouldBe 5
                property.path shouldBe ObjectIdAndLabel(Predicates.hasObjectPosition, "hasObjectPosition")
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
            it.modifiable shouldBe true
        }
    }

    private fun createRosettaStoneTemplate(): ThingId =
        post("/api/rosetta-stone/templates")
            .content(requestJson("orkg/createRosettaStoneTemplate"))
            .accept(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .contentType(ROSETTA_STONE_TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)
}
