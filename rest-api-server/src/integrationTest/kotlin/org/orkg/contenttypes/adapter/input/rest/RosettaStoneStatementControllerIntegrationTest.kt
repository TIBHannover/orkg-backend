@file:Suppress("HttpUrlsUsage")

package org.orkg.contenttypes.adapter.input.rest

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.community.input.ContributorUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.Certainty
import org.orkg.contenttypes.domain.RosettaStoneStatementNotFound
import org.orkg.contenttypes.input.CreateRosettaStoneTemplateUseCase
import org.orkg.contenttypes.input.NumberLiteralPropertyDefinition
import org.orkg.contenttypes.input.OtherLiteralPropertyDefinition
import org.orkg.contenttypes.input.ResourcePropertyDefinition
import org.orkg.contenttypes.input.RosettaStoneStatementUseCases
import org.orkg.contenttypes.input.RosettaStoneTemplateUseCases
import org.orkg.contenttypes.input.StringLiteralPropertyDefinition
import org.orkg.contenttypes.input.UntypedPropertyDefinition
import org.orkg.createClass
import org.orkg.createClasses
import org.orkg.createContributor
import org.orkg.createLiteral
import org.orkg.createObservatory
import org.orkg.createOrganization
import org.orkg.createPredicate
import org.orkg.createResource
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.FormattedLabel
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
import org.orkg.testing.MockUserDetailsService
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectRosettaStoneStatement
import org.orkg.testing.annotations.Neo4jContainerIntegrationTest
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Neo4jContainerIntegrationTest
@DisplayName("Rosetta Stone Statement Controller")
@Transactional
@Import(value = [MockUserDetailsService::class, ContentTypeJacksonModule::class])
internal class RosettaStoneStatementControllerIntegrationTest : RestDocsTest("rosetta-stone-statements") {

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
    private lateinit var rosettaStoneTemplateService: RosettaStoneTemplateUseCases

    @Autowired
    private lateinit var rosettaStoneStatementService: RosettaStoneStatementUseCases

    @BeforeEach
    fun setup() {
        val tempPageable = PageRequest.of(0, 1)

        cleanup()

        assertThat(predicateService.findAll(tempPageable)).hasSize(0)
        assertThat(resourceService.findAll(tempPageable)).hasSize(0)
        assertThat(classService.findAll(tempPageable)).hasSize(0)
        assertThat(observatoryService.findAll(tempPageable)).hasSize(0)
        assertThat(organizationService.listOrganizations()).hasSize(0)
        assertThat(organizationService.listConferences()).hasSize(0)
        assertThat(rosettaStoneStatementService.findAll(tempPageable)).hasSize(0)

        listOf(
            Predicates.description,
            Predicates.exampleOfUsage,
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
            Predicates.hasListElement,
            Predicates.hasSubjectPosition,
            Predicates.hasObjectPosition,
        ).forEach { predicateService.createPredicate(it) }

        setOf(
            Classes.rosettaNodeShape,
            Classes.propertyShape,
            Classes.researchField,
        ).forEach { classService.createClass(label = it.value, id = it.value) }

        Literals.XSD.entries.forEach {
            classService.createClass(
                label = it.`class`.value,
                id = it.`class`.value,
                uri = ParsedIRI(it.uri)
            )
        }

        resourceService.createResource(
            id = "R12",
            label = "Computer Science",
            classes = setOf(Classes.researchField.value)
        )

        // Example specific entities

        classService.createClasses("C123", "C28", "C25")

        resourceService.createResource(id = "R789")
        resourceService.createResource(id = "R174")
        resourceService.createResource(id = "R258", classes = setOf("C28"))
        resourceService.createResource(id = "R369", classes = setOf("C28"))

        literalService.createLiteral(id = ThingId("L123"), label = "123456")
        literalService.createLiteral(id = ThingId("L456"), label = "5", datatype = Literals.XSD.INT.prefixedUri)
        literalService.createLiteral(id = ThingId("L789"), label = "custom type", datatype = "http://orkg.org/orkg/class/C25")

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
        predicateService.removeAll()
        resourceService.removeAll()
        classService.removeAll()
        observatoryService.removeAll()
        organizationService.removeAll()
        contributorService.deleteAll()
    }

    @Test
    @TestWithMockUser
    fun createAndUpdate() {
        val rosettaStoneTemplateId = createRosettaStoneTemplate()
        val id = createRosettaStoneStatement(rosettaStoneTemplateId)

        val rosettaStoneStatement = get("/api/rosetta-stone/statements/{id}", id)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isOk)
            .andExpectRosettaStoneStatement()
            .andReturn()
            .response
            .contentAsString
            .let { objectMapper.readValue(it, RosettaStoneStatementRepresentation::class.java) }

        rosettaStoneStatement.asClue {
            it.id shouldBe id
            it.context shouldBe ThingId("R789")
            it.templateId shouldBe rosettaStoneTemplateId
            it.classId shouldNotBe null
            it.versionId shouldNotBe id
            it.latestVersion shouldBe id
            it.formattedLabel shouldBe "{0} {1} {2} {3} {4} {5}"
            it.subjects.asClue { subjects ->
                subjects.size shouldBe 3
                subjects[0] shouldBe ResourceReferenceRepresentation(ThingId("R258"), "label", setOf(ThingId("C28")))
                subjects[1] shouldBe ResourceReferenceRepresentation(ThingId("R369"), "label", setOf(ThingId("C28")))
                subjects[2].shouldBeInstanceOf<ResourceReferenceRepresentation>().asClue { subject ->
                    subject.id shouldNotBe null
                    subject.label shouldBe "Subject Resource"
                    subject.classes shouldBe setOf(ThingId("C28"))
                }
            }
            it.objects.asClue { objects ->
                objects.size shouldBe 5
                objects[0].asClue { position ->
                    position.size shouldBe 3
                    position[0] shouldBe ResourceReferenceRepresentation(ThingId("R174"), "label", emptySet())
                    position[1].shouldBeInstanceOf<PredicateReferenceRepresentation>().asClue { `object` ->
                        `object`.id shouldNotBe null
                        `object`.label shouldBe "hasResult"
                    }
                    position[2].shouldBeInstanceOf<ClassReferenceRepresentation>().asClue { `object` ->
                        `object`.id shouldNotBe null
                        `object`.label shouldBe "new class"
                        `object`.uri shouldBe null
                    }
                }
                objects[1].asClue { position ->
                    position.size shouldBe 2
                    position[0] shouldBe LiteralReferenceRepresentation("123456", Literals.XSD.STRING.prefixedUri)
                    position[1] shouldBe LiteralReferenceRepresentation("0123456789", Literals.XSD.STRING.prefixedUri)
                }
                objects[2].asClue { position ->
                    position.size shouldBe 2
                    position[0] shouldBe LiteralReferenceRepresentation("5", Literals.XSD.INT.prefixedUri)
                    position[1] shouldBe LiteralReferenceRepresentation("1", Literals.XSD.INT.prefixedUri)
                }
                objects[3].asClue { position ->
                    position.size shouldBe 2
                    position[0] shouldBe LiteralReferenceRepresentation("custom type", "http://orkg.org/orkg/class/C25")
                    position[1] shouldBe LiteralReferenceRepresentation("some literal value", "http://orkg.org/orkg/class/C25")
                }
                objects[4].asClue { position ->
                    position.size shouldBe 3
                    position[0] shouldBe ResourceReferenceRepresentation(ThingId("R258"), "label", setOf(ThingId("C28")))
                    position[1] shouldBe ResourceReferenceRepresentation(ThingId("R369"), "label", setOf(ThingId("C28")))
                    position[2].shouldBeInstanceOf<ResourceReferenceRepresentation>().asClue { `object` ->
                        `object`.id shouldNotBe null
                        `object`.label shouldBe "list"
                        `object`.classes shouldBe setOf(Classes.list)
                    }
                }
            }
            it.createdAt shouldNotBe null
            it.createdBy shouldBe ContributorId(MockUserId.USER)
            it.certainty shouldBe Certainty.HIGH
            it.negated shouldBe false
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.MANUAL
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
            it.modifiable shouldBe true
            it.deletedBy shouldBe null
            it.deletedAt shouldBe null
        }

        val updatedId = post("/api/rosetta-stone/statements/{id}", id)
            .content(updateRosettaStoneStatementJson)
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)

        val updatedRosettaStoneStatement = rosettaStoneStatementService.findByIdOrVersionId(id)
            .orElseThrow { RosettaStoneStatementNotFound(id) }

        updatedRosettaStoneStatement.asClue {
            it.id shouldBe rosettaStoneStatement.latestVersion
            it.contextId shouldBe rosettaStoneStatement.context
            it.templateId shouldBe rosettaStoneTemplateId
            it.templateTargetClassId.asClue { templateTargetClassId ->
                templateTargetClassId shouldNotBe null
                templateTargetClassId shouldBe rosettaStoneStatement.classId
            }
            it.label shouldBe ""
            it.versions.size shouldBe 2
            it.versions[0].asClue { version ->
                version.id shouldBe rosettaStoneStatement.versionId
                version.formattedLabel shouldBe FormattedLabel.of("{0} {1} {2} {3} {4} {5}")
                version.subjects.asClue { subjects ->
                    subjects.size shouldBe 3
                    subjects[0].shouldBeInstanceOf<Resource>().asClue { subject ->
                        subject.id shouldBe ThingId("R258")
                        subject.label shouldBe "label"
                        subject.classes shouldBe setOf(ThingId("C28"))
                    }
                    subjects[1].shouldBeInstanceOf<Resource>().asClue { subject ->
                        subject.id shouldBe ThingId("R369")
                        subject.label shouldBe "label"
                        subject.classes shouldBe setOf(ThingId("C28"))
                    }
                    subjects[2].shouldBeInstanceOf<Resource>().asClue { subject ->
                        subject.id shouldNotBe null
                        subject.label shouldBe "Subject Resource"
                        subject.classes shouldBe setOf(ThingId("C28"))
                    }
                }
                version.objects.asClue { objects ->
                    objects.size shouldBe 5
                    objects[0].asClue { position ->
                        position.size shouldBe 3
                        position[0].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldBe ThingId("R174")
                            `object`.label shouldBe "label"
                            `object`.classes shouldBe emptySet()
                        }
                        position[1].shouldBeInstanceOf<Predicate>().asClue { `object` ->
                            `object`.id shouldNotBe null
                            `object`.label shouldBe "hasResult"
                        }
                        position[2].shouldBeInstanceOf<Class>().asClue { `object` ->
                            `object`.id shouldNotBe null
                            `object`.label shouldBe "new class"
                            `object`.uri shouldBe null
                        }
                    }
                    objects[1].asClue { position ->
                        position.size shouldBe 2
                        position[0].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "123456"
                            `object`.datatype shouldBe Literals.XSD.STRING.prefixedUri
                        }
                        position[1].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "0123456789"
                            `object`.datatype shouldBe Literals.XSD.STRING.prefixedUri
                        }
                    }
                    objects[2].asClue { position ->
                        position.size shouldBe 2
                        position[0].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "5"
                            `object`.datatype shouldBe Literals.XSD.INT.prefixedUri
                        }
                        position[1].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "1"
                            `object`.datatype shouldBe Literals.XSD.INT.prefixedUri
                        }
                    }
                    objects[3].asClue { position ->
                        position.size shouldBe 2
                        position[0].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "custom type"
                            `object`.datatype shouldBe "http://orkg.org/orkg/class/C25"
                        }
                        position[1].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "some literal value"
                            `object`.datatype shouldBe "http://orkg.org/orkg/class/C25"
                        }
                    }
                    objects[4].asClue { position ->
                        position.size shouldBe 3
                        position[0].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldBe ThingId("R258")
                            `object`.label shouldBe "label"
                            `object`.classes shouldBe setOf(ThingId("C28"))
                        }
                        position[1].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldBe ThingId("R369")
                            `object`.label shouldBe "label"
                            `object`.classes shouldBe setOf(ThingId("C28"))
                        }
                        position[2].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldNotBe null
                            `object`.label shouldBe "list"
                            `object`.classes shouldBe setOf(Classes.list)
                        }
                    }
                }
                version.createdAt shouldNotBe null
                version.createdBy shouldBe ContributorId(MockUserId.USER)
                version.certainty shouldBe Certainty.HIGH
                version.negated shouldBe false
                version.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
                version.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
                version.extractionMethod shouldBe ExtractionMethod.MANUAL
                version.visibility shouldBe Visibility.DEFAULT
                version.unlistedBy shouldBe null
                version.modifiable shouldBe true
                version.deletedBy shouldBe null
                version.deletedAt shouldBe null
            }
            it.versions[1].asClue { version ->
                version.id shouldNotBe updatedId
                version.formattedLabel shouldBe FormattedLabel.of("{0} {1} {2} {3} {4} {5}")
                version.subjects.asClue { subjects ->
                    subjects.size shouldBe 3
                    subjects[0].shouldBeInstanceOf<Resource>().asClue { subject ->
                        subject.id shouldBe ThingId("R369")
                        subject.label shouldBe "label"
                        subject.classes shouldBe setOf(ThingId("C28"))
                    }
                    subjects[1].shouldBeInstanceOf<Resource>().asClue { subject ->
                        subject.id shouldBe ThingId("R258")
                        subject.label shouldBe "label"
                        subject.classes shouldBe setOf(ThingId("C28"))
                    }
                    subjects[2].shouldBeInstanceOf<Resource>().asClue { subject ->
                        subject.id shouldNotBe null
                        subject.label shouldBe "Updated Subject Resource"
                        subject.classes shouldBe setOf(ThingId("C28"))
                    }
                }
                version.objects.asClue { objects ->
                    objects.size shouldBe 5
                    objects[0].asClue { position ->
                        position.size shouldBe 3
                        position[0].shouldBeInstanceOf<Predicate>().asClue { `object` ->
                            `object`.id shouldNotBe null
                            `object`.label shouldBe "hasResult"
                        }
                        position[1].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldBe ThingId("R174")
                            `object`.label shouldBe "label"
                            `object`.classes shouldBe emptySet()
                        }
                        position[2].shouldBeInstanceOf<Class>().asClue { `object` ->
                            `object`.id shouldNotBe null
                            `object`.label shouldBe "updated new class"
                            `object`.uri shouldBe null
                        }
                    }
                    objects[1].asClue { position ->
                        position.size shouldBe 2
                        position[0].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "9876543210"
                            `object`.datatype shouldBe Literals.XSD.STRING.prefixedUri
                        }
                        position[1].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "123456"
                            `object`.datatype shouldBe Literals.XSD.STRING.prefixedUri
                        }
                    }
                    objects[2].asClue { position ->
                        position.size shouldBe 2
                        position[0].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "4"
                            `object`.datatype shouldBe Literals.XSD.INT.prefixedUri
                        }
                        position[1].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "5"
                            `object`.datatype shouldBe Literals.XSD.INT.prefixedUri
                        }
                    }
                    objects[3].asClue { position ->
                        position.size shouldBe 2
                        position[0].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "some updated literal value"
                            `object`.datatype shouldBe "http://orkg.org/orkg/class/C25"
                        }
                        position[1].shouldBeInstanceOf<Literal>().asClue { `object` ->
                            `object`.label shouldBe "custom type"
                            `object`.datatype shouldBe "http://orkg.org/orkg/class/C25"
                        }
                    }
                    objects[4].asClue { position ->
                        position.size shouldBe 3
                        position[0].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldBe ThingId("R369")
                            `object`.label shouldBe "label"
                            `object`.classes shouldBe setOf(ThingId("C28"))
                        }
                        position[1].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldBe ThingId("R258")
                            `object`.label shouldBe "label"
                            `object`.classes shouldBe setOf(ThingId("C28"))
                        }
                        position[2].shouldBeInstanceOf<Resource>().asClue { `object` ->
                            `object`.id shouldNotBe null
                            `object`.label shouldBe "list"
                            `object`.classes shouldBe setOf(Classes.list)
                        }
                    }
                }
                version.createdAt shouldNotBe null
                version.createdBy shouldBe ContributorId(MockUserId.USER)
                version.certainty shouldBe Certainty.MODERATE
                version.negated shouldBe false
                version.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
                version.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
                version.extractionMethod shouldBe ExtractionMethod.AUTOMATIC
                version.visibility shouldBe Visibility.DEFAULT
                version.unlistedBy shouldBe null
                version.modifiable shouldBe true
                version.deletedBy shouldBe null
                version.deletedAt shouldBe null
            }
            it.observatories shouldBe listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
            it.organizations shouldBe listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e"))
            it.extractionMethod shouldBe ExtractionMethod.AUTOMATIC
            it.visibility shouldBe Visibility.DEFAULT
            it.unlistedBy shouldBe null
            it.modifiable shouldBe true
        }
    }

    private fun createRosettaStoneTemplate() = rosettaStoneTemplateService.create(
        CreateRosettaStoneTemplateUseCase.CreateCommand(
            contributorId = ContributorId(MockUserId.USER),
            label = "rosetta stone template",
            description = "rosetta stone template description",
            formattedLabel = FormattedLabel.of("{0} {1} {2} {3} {4} {5}"),
            exampleUsage = "example statement usage",
            properties = listOf(
                ResourcePropertyDefinition(
                    label = "subject position",
                    placeholder = "subject",
                    description = "subject",
                    minCount = 1,
                    maxCount = 4,
                    path = Predicates.hasSubjectPosition,
                    `class` = ThingId("C28")
                ),
                UntypedPropertyDefinition(
                    label = "property label",
                    placeholder = "property placeholder",
                    description = "property description",
                    minCount = 1,
                    maxCount = 3,
                    path = Predicates.hasObjectPosition
                ),
                StringLiteralPropertyDefinition(
                    label = "string literal property label",
                    placeholder = "string literal property placeholder",
                    description = "string literal property description",
                    minCount = 1,
                    maxCount = 2,
                    pattern = "\\d+",
                    path = Predicates.hasObjectPosition,
                    datatype = Classes.string
                ),
                NumberLiteralPropertyDefinition(
                    label = "number literal property label",
                    placeholder = "number literal property placeholder",
                    description = "number literal property description",
                    minCount = 1,
                    maxCount = 2,
                    minInclusive = RealNumber(-1),
                    maxInclusive = RealNumber(10),
                    path = Predicates.hasObjectPosition,
                    datatype = Classes.integer
                ),
                OtherLiteralPropertyDefinition(
                    label = "literal property label",
                    placeholder = "literal property placeholder",
                    description = "literal property description",
                    minCount = 1,
                    maxCount = 2,
                    path = Predicates.hasObjectPosition,
                    datatype = ThingId("C25")
                ),
                ResourcePropertyDefinition(
                    label = "resource property label",
                    placeholder = "resource property placeholder",
                    description = "resource property description",
                    minCount = 3,
                    maxCount = 4,
                    path = Predicates.hasObjectPosition,
                    `class` = ThingId("C28")
                ),
            ),
            organizations = listOf(OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")),
            observatories = listOf(ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3"))
        )
    )

    private fun createRosettaStoneStatement(templateId: ThingId): ThingId =
        post("/api/rosetta-stone/statements")
            .content(createRosettaStoneStatementJson.replace("\$templateId", templateId.value))
            .accept(ROSETTA_STONE_STATEMENT_JSON_V1)
            .contentType(ROSETTA_STONE_STATEMENT_JSON_V1)
            .characterEncoding("utf-8")
            .perform()
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .getHeaderValue("Location")!!
            .toString()
            .substringAfterLast("/")
            .let(::ThingId)
}

private const val createRosettaStoneStatementJson = """{
  "template_id": "${'$'}templateId",
  "context": "R789",
  "subjects": ["R258", "R369", "#temp1"],
  "objects": [
    ["R174", "#temp2", "#temp3"],
    ["L123", "#temp4"],
    ["L456", "#temp5"],
    ["L789", "#temp6"],
    ["R258", "R369", "#temp7"]
  ],
  "certainty": "HIGH",
  "negated": false,
  "resources": {
    "#temp1": {
      "label": "Subject Resource",
      "classes": ["C28"]
    }
  },
  "predicates": {
    "#temp2": {
      "label": "hasResult",
      "description": "has result"
    }
  },
  "classes": {
    "#temp3": {
      "label": "new class",
      "uri": null
    }
  },
  "literals": {
    "#temp4": {
      "label": "0123456789",
      "data_type": "xsd:string"
    },
    "#temp5": {
      "label": "1",
      "data_type": "xsd:integer"
    },
    "#temp6": {
      "label": "some literal value",
      "data_type": "http://orkg.org/orkg/class/C25"
    }
  },
  "lists": {
    "#temp7": {
      "label": "list",
      "elements": ["#temp1", "C123"]
    }
  },
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "extraction_method": "MANUAL"
}"""

private const val updateRosettaStoneStatementJson = """{
  "subjects": ["R369", "R258", "#temp1"],
  "objects": [
    ["#temp2", "R174", "#temp3"],
    ["#temp4", "L123"],
    ["#temp5", "L456"],
    ["#temp6", "L789"],
    ["R369", "R258", "#temp7"]
  ],
  "certainty": "MODERATE",
  "negated": false,
  "resources": {
    "#temp1": {
      "label": "Updated Subject Resource",
      "classes": ["C28"]
    }
  },
  "predicates": {
    "#temp2": {
      "label": "hasResult",
      "description": "has result too"
    }
  },
  "classes": {
    "#temp3": {
      "label": "updated new class",
      "uri": null
    }
  },
  "literals": {
    "#temp4": {
      "label": "9876543210",
      "data_type": "xsd:string"
    },
    "#temp5": {
      "label": "4",
      "data_type": "xsd:integer"
    },
    "#temp6": {
      "label": "some updated literal value",
      "data_type": "http://orkg.org/orkg/class/C25"
    }
  },
  "lists": {
    "#temp7": {
      "label": "list",
      "elements": ["C123", "#temp1"]
    }
  },
  "observatories": [
    "1afefdd0-5c09-4c9c-b718-2b35316b56f3"
  ],
  "organizations": [
    "edc18168-c4ee-4cb8-a98a-136f748e912e"
  ],
  "extraction_method": "AUTOMATIC"
}"""
