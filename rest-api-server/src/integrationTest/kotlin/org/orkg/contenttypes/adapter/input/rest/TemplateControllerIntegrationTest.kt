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
import org.orkg.contenttypes.domain.StringLiteralTemplateProperty
import org.orkg.contenttypes.domain.TemplateNotFound
import org.orkg.contenttypes.domain.TemplateRelations
import org.orkg.contenttypes.domain.UntypedTemplateProperty
import org.orkg.contenttypes.input.TemplateUseCases
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicates
import org.orkg.createResource
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.PredicateUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectTemplate
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.MockMvcBaseTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Neo4jContainerIntegrationTest
internal class TemplateControllerIntegrationTest : MockMvcBaseTest("templates") {
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
    private lateinit var templateService: TemplateUseCases

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
            Predicates.field,
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
            Predicates.templateOfPredicate,
            Predicates.templateOfResearchField,
            Predicates.templateOfResearchProblem,
        )

        classService.createClasses(
            Classes.nodeShape,
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
        val id = createTemplate()

        val template = get("/api/templates/{id}", id)
            .content(requestJson("orkg/createTemplate"))
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isOk)
            .andExpectTemplate()
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, TemplateRepresentation::class.java) }

        template.asClue {
            it.id shouldBe id
            it.label shouldBe "example template"
            it.description shouldBe "template description"
            it.formattedLabel shouldBe "{P32}"
            it.targetClass shouldBe ClassReferenceRepresentation(ThingId("C123"), "C123", null)
            it.relations shouldBe TemplateRelationRepresentation(
                researchFields = listOf(ObjectIdAndLabel(ThingId("R12"), "Computer Science")),
                researchProblems = listOf(ObjectIdAndLabel(ThingId("R15"), "label")),
                predicate = ObjectIdAndLabel(Predicates.hasResearchProblem, "P32")
            )
            it.properties.size shouldBe 5
            it.properties[0].shouldBeInstanceOf<UntypedTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "property label"
                property.placeholder shouldBe "property placeholder"
                property.description shouldBe "property description"
                property.order shouldBe 0
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[1].shouldBeInstanceOf<StringLiteralTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "string literal property label"
                property.placeholder shouldBe "string literal property placeholder"
                property.description shouldBe "string literal property description"
                property.order shouldBe 1
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.pattern shouldBe "\\d+"
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.datatype shouldBe ClassReferenceRepresentation(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[2].shouldBeInstanceOf<NumberLiteralTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "number literal property label"
                property.placeholder shouldBe "number literal property placeholder"
                property.description shouldBe "number literal property description"
                property.order shouldBe 2
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.minInclusive shouldBe RealNumber(-1)
                property.maxInclusive shouldBe RealNumber(10)
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.datatype shouldBe ClassReferenceRepresentation(Classes.integer, "Integer", ParsedIRI(Literals.XSD.INT.uri))
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[3].shouldBeInstanceOf<OtherLiteralTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "literal property label"
                property.placeholder shouldBe "literal property placeholder"
                property.description shouldBe "literal property description"
                property.order shouldBe 3
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.datatype shouldBe ClassReferenceRepresentation(ThingId("C25"), "C25", null)
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[4].shouldBeInstanceOf<ResourceTemplatePropertyRepresentation>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "resource property label"
                property.placeholder shouldBe "resource property placeholder"
                property.description shouldBe "resource property description"
                property.order shouldBe 4
                property.minCount shouldBe 3
                property.maxCount shouldBe 4
                property.path shouldBe ObjectIdAndLabel(Predicates.hasAuthor, "P27")
                property.`class` shouldBe ObjectIdAndLabel(ThingId("C28"), "C28")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.isClosed shouldBe false
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.AUTOMATIC
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
        }

        put("/api/templates/{id}", id)
            .content(requestJson("orkg/updateTemplate"))
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTemplate = templateService.findById(id).orElseThrow { TemplateNotFound(id) }

        updatedTemplate.asClue {
            it.id shouldBe id
            it.label shouldBe "updated example template"
            it.description shouldBe "updated template description"
            it.formattedLabel shouldBe FormattedLabel.of("{P34}")
            it.targetClass shouldBe ClassReference(ThingId("C456"), "C456", null)
            it.relations shouldBe TemplateRelations(
                researchFields = listOf(ObjectIdAndLabel(ThingId("R13"), "Engineering")),
                researchProblems = listOf(ObjectIdAndLabel(ThingId("R16"), "label")),
                predicate = ObjectIdAndLabel(Predicates.hasContribution, "P31")
            )
            it.properties.size shouldBe 5
            it.properties[0].shouldBeInstanceOf<UntypedTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated property label"
                property.placeholder shouldBe null
                property.description shouldBe null
                property.order shouldBe 0
                property.minCount shouldBe 4
                property.maxCount shouldBe 7
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[1].shouldBeInstanceOf<ResourceTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated resource property label"
                property.placeholder shouldBe "updated resource property placeholder"
                property.description shouldBe "updated resource property description"
                property.order shouldBe 1
                property.minCount shouldBe 3
                property.maxCount shouldBe 4
                property.path shouldBe ObjectIdAndLabel(Predicates.hasAuthor, "P27")
                property.`class` shouldBe ObjectIdAndLabel(ThingId("C28"), "C28")
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[2].shouldBeInstanceOf<StringLiteralTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated string literal property label"
                property.placeholder shouldBe "updated string literal property placeholder"
                property.description shouldBe "updated string literal property description"
                property.order shouldBe 2
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.pattern shouldBe "\\w+"
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.datatype shouldBe ClassReference(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[3].shouldBeInstanceOf<OtherLiteralTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated literal property label"
                property.placeholder shouldBe "updated literal property placeholder"
                property.description shouldBe "updated literal property description"
                property.order shouldBe 3
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.datatype shouldBe ClassReference(ThingId("C25"), "C25", null)
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.properties[4].shouldBeInstanceOf<NumberLiteralTemplateProperty>().asClue { property ->
                property.id shouldNotBe null
                property.label shouldBe "updated number literal property label"
                property.placeholder shouldBe "updated number literal property placeholder"
                property.description shouldBe "updated number literal property description"
                property.order shouldBe 4
                property.minCount shouldBe 1
                property.maxCount shouldBe 2
                property.minInclusive shouldBe RealNumber(-5)
                property.maxInclusive shouldBe RealNumber(15.5)
                property.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
                property.datatype shouldBe ClassReference(Classes.decimal, "Decimal", ParsedIRI(Literals.XSD.DECIMAL.uri))
                property.createdAt shouldNotBe null
                property.createdBy shouldBe ContributorId(MockUserId.USER)
            }
            it.isClosed shouldBe false
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.visibility shouldBe Visibility.DELETED
            it.unlistedBy shouldBe null
        }
    }

    @Test
    @TestWithMockUser
    fun createAndUpdateUntypedProperty() {
        val id = createTemplate()

        post("/api/templates/$id/properties")
            .content(requestJson("orkg/createUntypedTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val template = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        template.properties.last().shouldBeInstanceOf<UntypedTemplateProperty>().asClue {
            it.id shouldNotBe null
            it.label shouldBe "property label"
            it.placeholder shouldBe "property placeholder"
            it.description shouldBe "property description"
            it.order shouldBe template.properties.size - 1
            it.minCount shouldBe 1
            it.maxCount shouldBe 2
            it.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }

        val propertyId = template.properties.first().id

        put("/api/templates/{id}/properties/{propertyId}", id, propertyId)
            .content(requestJson("orkg/updateUntypedTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTemplate = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedTemplate.properties.first().shouldBeInstanceOf<UntypedTemplateProperty>().asClue {
            it.id shouldBe propertyId
            it.label shouldBe "updated property label"
            it.placeholder shouldBe null
            it.description shouldBe null
            it.order shouldBe 0
            it.minCount shouldBe 4
            it.maxCount shouldBe 7
            it.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }
    }

    @Test
    @TestWithMockUser
    fun createAndUpdateStringLiteralProperty() {
        val id = createTemplate()

        post("/api/templates/$id/properties")
            .content(requestJson("orkg/createStringLiteralTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val template = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        template.properties.last().shouldBeInstanceOf<StringLiteralTemplateProperty>().asClue {
            it.id shouldNotBe null
            it.label shouldBe "string literal property label"
            it.placeholder shouldBe "string literal property placeholder"
            it.description shouldBe "string literal property description"
            it.order shouldBe template.properties.size - 1
            it.minCount shouldBe 1
            it.maxCount shouldBe 2
            it.pattern shouldBe "\\d+"
            it.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
            it.datatype shouldBe ClassReference(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }

        val propertyId = template.properties.first().id

        put("/api/templates/{id}/properties/{propertyId}", id, propertyId)
            .content(requestJson("orkg/updateStringLiteralTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTemplate = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedTemplate.properties.first().shouldBeInstanceOf<StringLiteralTemplateProperty>().asClue {
            it.id shouldBe propertyId
            it.label shouldBe "updated string literal property label"
            it.placeholder shouldBe "updated string literal property placeholder"
            it.description shouldBe "updated string literal property description"
            it.order shouldBe 0
            it.minCount shouldBe 1
            it.maxCount shouldBe 2
            it.pattern shouldBe "\\w+"
            it.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
            it.datatype shouldBe ClassReference(Classes.string, "String", ParsedIRI(Literals.XSD.STRING.uri))
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }
    }

    @Test
    @TestWithMockUser
    fun createAndUpdateNumberLiteralProperty() {
        val id = createTemplate()

        post("/api/templates/$id/properties")
            .content(requestJson("orkg/createNumberLiteralTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val template = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        template.properties.last().shouldBeInstanceOf<NumberLiteralTemplateProperty>().asClue {
            it.id shouldNotBe null
            it.label shouldBe "number literal property label"
            it.placeholder shouldBe "number literal property placeholder"
            it.description shouldBe "number literal property description"
            it.order shouldBe template.properties.size - 1
            it.minCount shouldBe 1
            it.maxCount shouldBe 2
            it.minInclusive shouldBe RealNumber(-1)
            it.maxInclusive shouldBe RealNumber(10)
            it.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
            it.datatype shouldBe ClassReference(Classes.integer, "Integer", ParsedIRI(Literals.XSD.INT.uri))
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }

        val propertyId = template.properties.first().id

        put("/api/templates/{id}/properties/{propertyId}", id, propertyId)
            .content(requestJson("orkg/updateNumberLiteralTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTemplate = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedTemplate.properties.first().shouldBeInstanceOf<NumberLiteralTemplateProperty>().asClue {
            it.id shouldBe propertyId
            it.label shouldBe "updated number literal property label"
            it.placeholder shouldBe "updated number literal property placeholder"
            it.description shouldBe "updated number literal property description"
            it.order shouldBe 0
            it.minCount shouldBe 1
            it.maxCount shouldBe 2
            it.minInclusive shouldBe RealNumber(-5)
            it.maxInclusive shouldBe RealNumber(15.5)
            it.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
            it.datatype shouldBe ClassReference(Classes.decimal, "Decimal", ParsedIRI(Literals.XSD.DECIMAL.uri))
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }
    }

    @Test
    @TestWithMockUser
    fun createAndUpdateOtherLiteralProperty() {
        val id = createTemplate()

        post("/api/templates/$id/properties")
            .content(requestJson("orkg/createOtherLiteralTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val template = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        template.properties.last().shouldBeInstanceOf<OtherLiteralTemplateProperty>().asClue {
            it.id shouldNotBe null
            it.label shouldBe "literal property label"
            it.placeholder shouldBe "literal property placeholder"
            it.description shouldBe "literal property description"
            it.order shouldBe template.properties.size - 1
            it.minCount shouldBe 1
            it.maxCount shouldBe 2
            it.path shouldBe ObjectIdAndLabel(Predicates.field, "P24")
            it.datatype shouldBe ClassReference(ThingId("C25"), "C25", null)
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }

        val propertyId = template.properties.first().id

        put("/api/templates/{id}/properties/{propertyId}", id, propertyId)
            .content(requestJson("orkg/updateOtherLiteralTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTemplate = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedTemplate.properties.first().shouldBeInstanceOf<OtherLiteralTemplateProperty>().asClue {
            it.id shouldBe propertyId
            it.label shouldBe "updated literal property label"
            it.placeholder shouldBe "updated literal property placeholder"
            it.description shouldBe "updated literal property description"
            it.order shouldBe 0
            it.minCount shouldBe 1
            it.maxCount shouldBe 2
            it.path shouldBe ObjectIdAndLabel(Predicates.description, "description")
            it.datatype shouldBe ClassReference(ThingId("C27"), "C27", null)
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }
    }

    @Test
    @TestWithMockUser
    fun createAndUpdateResourceProperty() {
        val id = createTemplate()

        post("/api/templates/$id/properties")
            .content(requestJson("orkg/createResourceTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isCreated)

        val template = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        template.properties.last().shouldBeInstanceOf<ResourceTemplateProperty>().asClue {
            it.id shouldNotBe null
            it.label shouldBe "resource property label"
            it.placeholder shouldBe "resource property placeholder"
            it.description shouldBe "resource property description"
            it.order shouldBe template.properties.size - 1
            it.minCount shouldBe 3
            it.maxCount shouldBe 4
            it.path shouldBe ObjectIdAndLabel(Predicates.hasAuthor, "P27")
            it.`class` shouldBe ObjectIdAndLabel(ThingId("C28"), "C28")
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }

        val propertyId = template.properties.first().id

        put("/api/templates/{id}/properties/{propertyId}", id, propertyId)
            .content(requestJson("orkg/updateResourceTemplateProperty"))
            .accept(TEMPLATE_PROPERTY_JSON_V1)
            .contentType(TEMPLATE_PROPERTY_JSON_V1)
            .perform()
            .andExpect(status().isNoContent)

        val updatedTemplate = templateService.findById(id)
            .orElseThrow { throw IllegalStateException("Test did not initialize correctly! This is a bug!") }

        updatedTemplate.properties.first().shouldBeInstanceOf<ResourceTemplateProperty>().asClue {
            it.id shouldBe propertyId
            it.label shouldBe "updated resource property label"
            it.placeholder shouldBe "updated resource property placeholder"
            it.description shouldBe "updated resource property description"
            it.order shouldBe 0
            it.minCount shouldBe 2
            it.maxCount shouldBe 5
            it.path shouldBe ObjectIdAndLabel(Predicates.hasAuthor, "P27")
            it.`class` shouldBe ObjectIdAndLabel(ThingId("C28"), "C28")
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
        }
    }

    private fun createTemplate(): ThingId =
        post("/api/templates")
            .content(requestJson("orkg/createTemplate"))
            .accept(TEMPLATE_JSON_V1)
            .contentType(TEMPLATE_JSON_V1)
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)
}
