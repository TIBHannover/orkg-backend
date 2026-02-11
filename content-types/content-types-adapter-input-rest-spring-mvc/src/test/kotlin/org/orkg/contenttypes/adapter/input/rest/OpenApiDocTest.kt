package org.orkg.contenttypes.adapter.input.rest

import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.RealNumber
import org.orkg.common.ThingId
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.LiteratureListListSectionRequest
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.LiteratureListSectionRequest
import org.orkg.contenttypes.adapter.input.rest.LiteratureListController.LiteratureListTextSectionRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionRequestPart.StatementObjectRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewComparisonSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewOntologySectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewPredicateSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewResourceSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewTextSectionRequest
import org.orkg.contenttypes.adapter.input.rest.SmartReviewController.SmartReviewVisualizationSectionRequest
import org.orkg.contenttypes.domain.ComparisonTargetCell
import org.orkg.contenttypes.domain.ConfiguredComparisonTargetCell
import org.orkg.contenttypes.domain.EmptyComparisonTargetCell
import org.orkg.contenttypes.domain.ObjectIdAndLabel
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureListTextSection
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.domain.testing.fixtures.createSmartReviewComparisonSection
import org.orkg.contenttypes.input.testing.fixtures.commonTemplatePropertyResponseFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.configuredComparisonTargetCellResponseFields
import org.orkg.contenttypes.input.testing.fixtures.embeddedStatementResponseFields
import org.orkg.contenttypes.input.testing.fixtures.literalReferenceResponseFields
import org.orkg.contenttypes.input.testing.fixtures.literatureListListSectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.literatureListTextSectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.smartReviewComparisonSectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.smartReviewOntologySectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.smartReviewPropertySectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.smartReviewResourceSectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.smartReviewTextSectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.smartReviewVisualizationSectionResponseFields
import org.orkg.contenttypes.input.testing.fixtures.untypedTemplatePropertyRequest
import org.orkg.graph.adapter.input.rest.ResourceRepresentation
import org.orkg.graph.adapter.input.rest.SimpleAuthorRepresentation
import org.orkg.graph.adapter.input.rest.SimpleAuthorRepresentation.LiteralAuthorRepresentation
import org.orkg.graph.adapter.input.rest.SimpleAuthorRepresentation.ResourceAuthorRepresentation
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Visibility
import org.orkg.testing.spring.MockMvcOpenApiBaseTest
import org.orkg.testing.spring.restdocs.oneOf
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.test.context.ContextConfiguration
import java.time.OffsetDateTime

@WebMvcTest
@ContextConfiguration(classes = [ContentTypeControllerUnitTestConfiguration::class])
internal class OpenApiDocTest : MockMvcOpenApiBaseTest() {
    @Test
    fun configuredComparisonTargetCell() {
        val configuredComparisonTargetCell = ConfiguredComparisonTargetCell(
            id = "R192326",
            label = "Covid-19 Pandemic Ontology Development",
            classes = listOf(Classes.problem),
            path = listOf(ThingId("R187004"), Predicates.hasResearchProblem),
            pathLabels = listOf("Contribution 1", "research problem"),
            `class` = "resource"
        )

        document(configuredComparisonTargetCell) {
            responseFields<ConfiguredComparisonTargetCell>(configuredComparisonTargetCellResponseFields())
        }
    }

    @Test
    fun emptyComparisonTargetCell() {
        document(EmptyComparisonTargetCell) {
            responseFields<EmptyComparisonTargetCell>()
        }
    }

    @Test
    fun comparisonTargetCell() {
        document(EmptyComparisonTargetCell) {
            responseFields<ComparisonTargetCell>(
                oneOf(EmptyComparisonTargetCell::class, ConfiguredComparisonTargetCell::class)
            )
        }
    }

    @Test
    fun contentTypeRepresentation() {
        document(createPaper()) {
            responseFields<ContentTypeRepresentation>(
                oneOf(
                    "_class",
                    mapOf(
                        "paper" to PaperRepresentation::class,
                        "comparison" to ComparisonRepresentation::class,
                        "visualization" to VisualizationRepresentation::class,
                        "template" to TemplateRepresentation::class,
                        "literature-list" to LiteratureListRepresentation::class,
                        "smart-review" to SmartReviewRepresentation::class,
                    )
                )
            )
        }
    }

    @Test
    fun statementObjectRequest() {
        val statementObjectRepresentation = StatementObjectRequest(
            id = "R3004",
            statements = mapOf(
                "P32" to listOf(
                    StatementObjectRequest(
                        id = "#temp2",
                        statements = null
                    )
                )
            )
        )

        document(statementObjectRepresentation) {
            responseFields<StatementObjectRequest>(
                fieldWithPath("id").description("The ID of the object of the statement."),
                fieldWithPath("statements").description("A map of predicate id to nested statement request parts.").optional(),
                fieldWithPath("statements.*").description("The ID of the predicate."),
                subsectionWithPath("statements.*[]").description("The list of nested statement request parts."),
            )
        }
    }

    @Test
    fun embeddedStatementRepresentation() {
        val embeddedStatementRepresentation = EmbeddedStatementRepresentation(
            thing = createResourceRepresentation(),
            createdAt = OffsetDateTime.parse("2023-10-06T11:28:14.613254+01:00"),
            createdBy = ContributorId.UNKNOWN,
            statements = mapOf(
                ThingId("P32") to listOf(
                    EmbeddedStatementRepresentation(
                        thing = createResourceRepresentation(),
                        createdAt = OffsetDateTime.parse("2023-10-06T11:28:14.613254+01:00"),
                        createdBy = ContributorId.UNKNOWN,
                        statements = emptyMap(),
                    )
                )
            )
        )

        document(embeddedStatementRepresentation) {
            responseFields<EmbeddedStatementRepresentation>(embeddedStatementResponseFields())
        }
    }

    @Test
    fun literatureListSectionRepresentation() {
        document(createLiteratureListTextSection()) {
            responseFields<LiteratureListSectionRepresentation>(
                oneOf(
                    "type",
                    mapOf(
                        "text" to LiteratureListListSectionRepresentation::class,
                        "list" to LiteratureListTextSectionRepresentation::class,
                    )
                )
            )
        }
    }

    @Test
    fun literatureListTextSectionRepresentation() {
        val literatureListTextSectionRepresentation = LiteratureListTextSectionRepresentation(
            id = ThingId("R154686"),
            heading = "Heading",
            headingSize = 2,
            text = "text section contents"
        )

        document(literatureListTextSectionRepresentation) {
            responseFields<LiteratureListTextSectionRepresentation>(literatureListTextSectionResponseFields())
        }
    }

    @Test
    fun literatureListListSectionRepresentation() {
        val literatureListListSectionRepresentation = LiteratureListListSectionRepresentation(
            id = ThingId("R456351"),
            entries = listOf(
                LiteratureListListSectionRepresentation.EntryRepresentation(
                    ResourceReferenceRepresentation(
                        id = ThingId("R154686"),
                        label = "Paper",
                        classes = setOf(Classes.paper)
                    ),
                    "paper entry description"
                )
            )
        )

        document(literatureListListSectionRepresentation) {
            responseFields<LiteratureListListSectionRepresentation>(literatureListListSectionResponseFields())
        }
    }

    @Test
    fun literatureListSectionRequest() {
        val literatureListSectionRequest = LiteratureListTextSectionRequest(
            heading = "heading",
            headingSize = 2,
            text = "text contents"
        )

        document(literatureListSectionRequest) {
            responseFields<LiteratureListSectionRequest>(
                oneOf(LiteratureListListSectionRequest::class, LiteratureListTextSectionRequest::class)
            )
        }
    }

    @Test
    fun simpleAuthorRepresentation() {
        val simpleAuthorRepresentation = ResourceAuthorRepresentation(createResourceRepresentation())

        document(simpleAuthorRepresentation) {
            responseFields<SimpleAuthorRepresentation>(
                oneOf(ResourceAuthorRepresentation::class, LiteralAuthorRepresentation::class)
            )
        }
    }

    @Test
    fun resourceAuthorRepresentation() {
        val resourceAuthorRepresentation = ResourceAuthorRepresentation(createResourceRepresentation())

        document(resourceAuthorRepresentation) {
            responseFields<ResourceAuthorRepresentation>(
                subsectionWithPath("value").description("The resource representation of the author."),
            )
        }
    }

    @Test
    fun literalAuthorRepresentation() {
        val literalAuthorRepresentation = LiteralAuthorRepresentation("author name")

        document(literalAuthorRepresentation) {
            responseFields<LiteralAuthorRepresentation>(
                fieldWithPath("value").description("The name of the author.")
            )
        }
    }

    @Test
    fun thingReferenceRepresentation() {
        document(createResourceReferenceRepresentation()) {
            responseFields<ThingReferenceRepresentation>(
                oneOf(
                    "_class",
                    mapOf(
                        "resource_ref" to ResourceReferenceRepresentation::class,
                        "predicate_ref" to PredicateReferenceRepresentation::class,
                        "class_ref" to ClassReferenceRepresentation::class,
                        "literal_ref" to LiteralReferenceRepresentation::class,
                    )
                )
            )
        }
    }

    @Test
    fun templatePropertyRequest() {
        document(untypedTemplatePropertyRequest()) {
            responseFields<TemplatePropertyRequest>(
                oneOf(
                    UntypedPropertyRequest::class,
                    StringLiteralPropertyRequest::class,
                    NumberLiteralPropertyRequest::class,
                    OtherLiteralPropertyRequest::class,
                    ResourcePropertyRequest::class,
                )
            )
        }
    }

    @Test
    fun templatePropertyRepresentation() {
        document(createUntypedTemplatePropertyRepresentation()) {
            responseFields<TemplatePropertyRepresentation>(
                oneOf(
                    UntypedTemplatePropertyRepresentation::class,
                    StringLiteralTemplatePropertyRepresentation::class,
                    NumberLiteralTemplatePropertyRepresentation::class,
                    OtherLiteralTemplatePropertyRepresentation::class,
                    ResourceTemplatePropertyRepresentation::class,
                )
            )
        }
    }

    @Test
    fun untypedTemplatePropertyRepresentation() {
        document(createUntypedTemplatePropertyRepresentation()) {
            responseFields<UntypedTemplatePropertyRepresentation>(
                commonTemplatePropertyResponseFields()
            )
        }
    }

    @Test
    fun stringLiteralTemplatePropertyRepresentation() {
        val stringLiteralTemplatePropertyRepresentation = StringLiteralTemplatePropertyRepresentation(
            id = ThingId("R23"),
            label = "string literal property label",
            placeholder = "string literal property placeholder",
            description = "string literal property description",
            order = 0,
            minCount = 1,
            maxCount = 2,
            pattern = """\d+""",
            path = ObjectIdAndLabel(Predicates.field, "property path label"),
            createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
            createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
            datatype = createClassReferenceRepresentation(),
        )

        document(stringLiteralTemplatePropertyRepresentation) {
            responseFields<StringLiteralTemplatePropertyRepresentation>(
                *commonTemplatePropertyResponseFields().toTypedArray(),
                fieldWithPath("pattern").description("The pattern that the string must have. (optional)").optional(),
                fieldWithPath("datatype").description("""The class reference representation of the datatype of the property. Always has the value "String"."""),
                fieldWithPath("datatype.id").description("""The id of the thing."""),
                fieldWithPath("datatype.label").description("""The label of the thing."""),
                fieldWithPath("datatype.uri").description("""The uri of the class."""),
                fieldWithPath("datatype._class").description("""Indicates which type of entity was returned. Alaways has the value `class_ref`."""),
            )
        }
    }

    @Test
    fun numberLiteralTemplatePropertyRepresentation() {
        val numberLiteralTemplatePropertyRepresentation = NumberLiteralTemplatePropertyRepresentation(
            id = ThingId("R23"),
            label = "number literal property label",
            placeholder = "number literal property placeholder",
            description = "number literal property description",
            order = 0,
            minCount = 1,
            maxCount = 2,
            minInclusive = RealNumber(5),
            maxInclusive = RealNumber(10),
            path = ObjectIdAndLabel(Predicates.field, "property path label"),
            createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
            createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
            datatype = createClassReferenceRepresentation(),
        )

        document(numberLiteralTemplatePropertyRepresentation) {
            responseFields<NumberLiteralTemplatePropertyRepresentation>(
                *commonTemplatePropertyResponseFields().toTypedArray(),
                fieldWithPath("min_inclusive").description("The minimum value (inclusive) that the number can have (optional).").optional(),
                fieldWithPath("max_inclusive").description("The maximum value (inclusive) that the number can have (optional).").optional(),
                fieldWithPath("datatype").description("""The class reference representation of the datatype of the property. Either of "Integer", "Decimal" or "Float"."""),
                fieldWithPath("datatype.id").description("""The id of the class."""),
                fieldWithPath("datatype.label").description("""The label of the class."""),
                fieldWithPath("datatype.uri").description("""The uri of the class."""),
                fieldWithPath("datatype._class").description("""Indicates which type of entity was returned. Alaways has the value `class_ref`."""),
            )
        }
    }

    @Test
    fun otherLiteralTemplatePropertyRepresentation() {
        val otherLiteralTemplatePropertyRepresentation = OtherLiteralTemplatePropertyRepresentation(
            id = ThingId("R23"),
            label = "other literal property label",
            placeholder = "other literal property placeholder",
            description = "other literal property description",
            order = 0,
            minCount = 1,
            maxCount = 2,
            path = ObjectIdAndLabel(Predicates.field, "property path label"),
            createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
            createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
            datatype = createClassReferenceRepresentation(),
        )

        document(otherLiteralTemplatePropertyRepresentation) {
            responseFields<OtherLiteralTemplatePropertyRepresentation>(
                *commonTemplatePropertyResponseFields().toTypedArray(),
                fieldWithPath("datatype").description("""The class reference representation of the datatype of the property, indicating a literal property."""),
                fieldWithPath("datatype.id").description("""The id of the thing."""),
                fieldWithPath("datatype.label").description("""The label of the thing."""),
                fieldWithPath("datatype.uri").description("""The uri of the class."""),
                fieldWithPath("datatype._class").description("""Indicates which type of entity was returned. Alaways has the value `class_ref`."""),
            )
        }
    }

    @Test
    fun resourceTemplatePropertyRepresentation() {
        val resourceTemplatePropertyRepresentation = ResourceTemplatePropertyRepresentation(
            id = ThingId("R23"),
            label = "resource property label",
            placeholder = "resource property placeholder",
            description = "resource property description",
            order = 0,
            minCount = 1,
            maxCount = 2,
            path = ObjectIdAndLabel(Predicates.field, "property path label"),
            createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
            createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
            `class` = ObjectIdAndLabel(ThingId("C28"), "some class"),
        )

        document(resourceTemplatePropertyRepresentation) {
            responseFields<ResourceTemplatePropertyRepresentation>(
                *commonTemplatePropertyResponseFields().toTypedArray(),
                fieldWithPath("class").description("""The object id and label of the range of the property, indicating a resource property."""),
                fieldWithPath("class.id").description("""The id of the thing."""),
                fieldWithPath("class.label").description("""The label of the thing."""),
            )
        }
    }

    @Test
    fun smartReviewSection() {
        document(createSmartReviewComparisonSection()) {
            responseFields<SmartReviewSectionRepresentation>(
                oneOf(
                    "type",
                    mapOf(
                        "comparison" to SmartReviewComparisonSectionRepresentation::class,
                        "visualization" to SmartReviewVisualizationSectionRepresentation::class,
                        "resource" to SmartReviewResourceSectionRepresentation::class,
                        "property" to SmartReviewPredicateSectionRepresentation::class,
                        "ontology" to SmartReviewOntologySectionRepresentation::class,
                        "text" to SmartReviewTextSectionRepresentation::class,
                    )
                )
            )
        }
    }

    @Test
    fun smartReviewComparisonSectionRepresentation() {
        val smartReviewComparisonSectionRepresentation = SmartReviewComparisonSectionRepresentation(
            id = ThingId("R154686"),
            heading = "Heading",
            comparison = createResourceReferenceRepresentation(),
        )

        document(smartReviewComparisonSectionRepresentation) {
            responseFields<SmartReviewComparisonSectionRepresentation>(smartReviewComparisonSectionResponseFields())
        }
    }

    @Test
    fun smartReviewVisualizationSectionRepresentation() {
        val smartReviewVisualizationSectionRepresentation = SmartReviewVisualizationSectionRepresentation(
            id = ThingId("R154686"),
            heading = "Heading",
            visualization = createResourceReferenceRepresentation(),
        )

        document(smartReviewVisualizationSectionRepresentation) {
            responseFields<SmartReviewVisualizationSectionRepresentation>(smartReviewVisualizationSectionResponseFields())
        }
    }

    @Test
    fun smartReviewResourceSectionRepresentation() {
        val smartReviewResourceSectionRepresentation = SmartReviewResourceSectionRepresentation(
            id = ThingId("R154686"),
            heading = "Heading",
            resource = createResourceReferenceRepresentation(),
        )

        document(smartReviewResourceSectionRepresentation) {
            responseFields<SmartReviewResourceSectionRepresentation>(smartReviewResourceSectionResponseFields())
        }
    }

    @Test
    fun smartReviewPropertySectionRepresentation() {
        val smartReviewPropertySectionRepresentation = SmartReviewPredicateSectionRepresentation(
            id = ThingId("R154686"),
            heading = "Heading",
            predicate = createPredicateReferenceRepresentation(),
        )

        document(smartReviewPropertySectionRepresentation) {
            responseFields<SmartReviewPredicateSectionRepresentation>(smartReviewPropertySectionResponseFields())
        }
    }

    @Test
    fun smartReviewOntologySectionRepresentation() {
        val smartReviewOntologySectionRepresentation = SmartReviewOntologySectionRepresentation(
            id = ThingId("R154686"),
            heading = "Heading",
            entities = listOf(createResourceReferenceRepresentation()),
            predicates = listOf(createPredicateReferenceRepresentation()),
        )

        document(smartReviewOntologySectionRepresentation) {
            responseFields<SmartReviewOntologySectionRepresentation>(smartReviewOntologySectionResponseFields())
        }
    }

    @Test
    fun smartReviewTextSectionRepresentation() {
        val smartReviewTextSectionRepresentation = SmartReviewTextSectionRepresentation(
            id = ThingId("R154686"),
            heading = "Heading",
            classes = setOf(Classes.introduction),
            text = "text",
        )

        document(smartReviewTextSectionRepresentation) {
            responseFields<SmartReviewTextSectionRepresentation>(smartReviewTextSectionResponseFields())
        }
    }

    @Test
    fun literalReferenceRepresentation() {
        document(createLiteralReferenceRepresentation()) {
            responseFields<LiteralReferenceRepresentation>(literalReferenceResponseFields())
        }
    }

    @Test
    fun smartReviewSectionRequest() {
        val smartReviewSectionRequest = SmartReviewComparisonSectionRequest(
            heading = "comparison section heading",
            comparison = ThingId("comparisonId")
        )

        document(smartReviewSectionRequest) {
            responseFields<SmartReviewSectionRequest>(
                oneOf(
                    SmartReviewComparisonSectionRequest::class,
                    SmartReviewVisualizationSectionRequest::class,
                    SmartReviewResourceSectionRequest::class,
                    SmartReviewPredicateSectionRequest::class,
                    SmartReviewOntologySectionRequest::class,
                    SmartReviewTextSectionRequest::class,
                )
            )
        }
    }

    private fun createResourceReferenceRepresentation() = ResourceReferenceRepresentation(
        id = ThingId("R123"),
        label = "Default label",
        classes = emptySet(),
    )

    private fun createPredicateReferenceRepresentation() = PredicateReferenceRepresentation(
        id = ThingId("P123"),
        label = "Default label",
    )

    private fun createClassReferenceRepresentation() = ClassReferenceRepresentation(
        id = ThingId("C123"),
        label = "Default label",
        uri = ParsedIRI("http://example.org")
    )

    private fun createLiteralReferenceRepresentation() = LiteralReferenceRepresentation(
        id = ThingId("L123"),
        label = "Default label",
        datatype = Literals.XSD.STRING.prefixedUri,
    )

    private fun createResourceRepresentation(): ResourceRepresentation = ResourceRepresentation(
        id = ThingId("R1"),
        label = "Default Label",
        classes = emptySet(),
        shared = 0,
        observatoryId = ObservatoryId.UNKNOWN,
        organizationId = OrganizationId.UNKNOWN,
        createdAt = OffsetDateTime.parse("2023-10-06T11:28:14.613254+01:00"),
        createdBy = ContributorId.UNKNOWN,
        featured = false,
        unlisted = false,
        visibility = Visibility.DEFAULT,
        verified = false,
        unlistedBy = null,
        formattedLabel = null,
        extractionMethod = ExtractionMethod.UNKNOWN,
        modifiable = true,
    )

    private fun createUntypedTemplatePropertyRepresentation(): UntypedTemplatePropertyRepresentation = UntypedTemplatePropertyRepresentation(
        id = ThingId("R23"),
        label = "property label",
        placeholder = "property placeholder",
        description = "property description",
        order = 0,
        minCount = 1,
        maxCount = 2,
        path = ObjectIdAndLabel(Predicates.field, "property path label"),
        createdAt = OffsetDateTime.parse("2023-11-02T14:57:05.959539600+01:00"),
        createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620"),
    )
}
