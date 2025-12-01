package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.DOI
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.common.thingIdConstraint
import org.orkg.common.uuidConstraint
import org.orkg.community.domain.ContributorNotFound
import org.orkg.community.domain.ObservatoryNotFound
import org.orkg.community.domain.OrganizationNotFound
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionRequestPart
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionRequestPart.StatementObjectRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.PaperContentsRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.PublishPaperRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.UpdatePaperRequest
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidDOI
import org.orkg.contenttypes.domain.InvalidMonth
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperNotModifiable
import org.orkg.contenttypes.domain.ResearchFieldNotFound
import org.orkg.contenttypes.domain.SustainableDevelopmentGoalNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.contenttypes.input.testing.fixtures.authorListFields
import org.orkg.contenttypes.input.testing.fixtures.configuration.ContentTypeControllerUnitTestConfiguration
import org.orkg.contenttypes.input.testing.fixtures.paperIdentifierFields
import org.orkg.contenttypes.input.testing.fixtures.paperResponseFields
import org.orkg.contenttypes.input.testing.fixtures.publicationInfoRequestFields
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.InvalidLiteralDatatype
import org.orkg.graph.domain.InvalidLiteralLabel
import org.orkg.graph.domain.NeitherOwnerNorCurator
import org.orkg.graph.domain.NotACurator
import org.orkg.graph.domain.ReservedClass
import org.orkg.graph.domain.ResourceNotFound
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.URIAlreadyInUse
import org.orkg.graph.domain.URINotAbsolute
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.testing.asciidoc.allowedExtractionMethodValues
import org.orkg.graph.testing.asciidoc.allowedVisibilityValues
import org.orkg.graph.testing.asciidoc.visibilityFilterQueryParameter
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPaper
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.arrayItemsType
import org.orkg.testing.spring.restdocs.constraints
import org.orkg.testing.spring.restdocs.format
import org.orkg.testing.spring.restdocs.references
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Optional
import java.util.UUID

@ContextConfiguration(classes = [PaperController::class, ContentTypeControllerUnitTestConfiguration::class])
@WebMvcTest(controllers = [PaperController::class])
internal class PaperControllerUnitTest : MockMvcBaseTest("papers") {
    @MockkBean
    private lateinit var paperService: PaperUseCases

    @Test
    @DisplayName("Given a paper, when it is fetched by id and service succeeds, then status is 200 OK and paper is returned")
    fun findById() {
        val paper = createPaper()
        every { paperService.findById(paper.id) } returns Optional.of(paper)

        documentedGetRequestTo("/api/papers/{id}", paper.id)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPaper()
            .andDocument {
                summary("Fetching papers")
                description(
                    """
                    A `GET` request provides information about a paper.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the paper to retrieve.")
                )
                responseFields<PaperRepresentation>(paperResponseFields())
                throws(PaperNotFound::class)
            }

        verify(exactly = 1) { paperService.findById(paper.id) }
    }

    @Test
    fun `Given a paper, when it is fetched by id and service reports missing paper, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        every { paperService.findById(id) } returns Optional.empty()

        get("/api/papers/{id}", id)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")

        verify(exactly = 1) { paperService.findById(id) }
    }

    @Test
    @DisplayName("Given several papers, when they are fetched, then status is 200 OK and papers are returned")
    fun getPaged() {
        every {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createPaper())

        documentedGetRequestTo("/api/papers")
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPaper("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several papers, when filtering by several parameters, then status is 200 OK and papers are returned")
    fun findAll() {
        every {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns pageOf(createPaper())

        val title = "label"
        val exact = true
        val doi = "10.456/8764"
        val doiPrefix = "10.456"
        val visibility = VisibilityFilter.ALL_LISTED
        val verified = true
        val createdBy = ContributorId("dca4080c-e23f-489d-b900-af8bfc2b0620")
        val createdAtStart = OffsetDateTime.now(fixedClock).minusHours(1)
        val createdAtEnd = OffsetDateTime.now(fixedClock).plusHours(1)
        val observatoryId = ObservatoryId("cb71eebf-8afd-4fe3-9aea-d0966d71cece")
        val organizationId = OrganizationId("a700c55f-aae2-4696-b7d5-6e8b89f66a8f")
        val researchFieldId = ThingId("R456")
        val includeSubfields = true
        val sdg = ThingId("SDG_1")
        val mentionings = setOf(ThingId("R357"))
        val researchProblemId = ThingId("R357")
        val venueId = ThingId("159")

        documentedGetRequestTo("/api/papers")
            .param("title", title)
            .param("exact", exact.toString())
            .param("doi", doi)
            .param("doi_prefix", doiPrefix)
            .param("visibility", visibility.name)
            .param("verified", verified.toString())
            .param("created_by", createdBy.value.toString())
            .param("created_at_start", createdAtStart.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("created_at_end", createdAtEnd.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            .param("observatory_id", observatoryId.value.toString())
            .param("organization_id", organizationId.value.toString())
            .param("research_field", researchFieldId.value)
            .param("include_subfields", includeSubfields.toString())
            .param("sdg", sdg.value)
            .param("mentionings", mentionings.joinToString(","))
            .param("research_problem", researchProblemId.value)
            .param("venue", venueId.value)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPaper("$.content[*]")
            .andDocument {
                summary("Listing papers")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<papers-fetch,papers>>.
                    If no paging request parameters are provided, the default values will be used.
                    """
                )
                pagedQueryParameters(
                    parameterWithName("title").description("A search term that must be contained in the title of the paper. (optional)").optional(),
                    parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)").optional(),
                    parameterWithName("doi").description("Filter for the DOI of the paper. (optional)").optional(),
                    parameterWithName("doi_prefix").description("Filter for the DOI prefix of the DOI of the paper. (optional)").optional(),
                    visibilityFilterQueryParameter(),
                    parameterWithName("verified").description("Filter for the verified flag of the paper. (optional)").optional(),
                    parameterWithName("created_by").description("Filter for the UUID of the user or service who created this paper. (optional)").format("uuid").optional(),
                    parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned paper can have. (optional)").optional(),
                    parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned paper can have. (optional)").optional(),
                    parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the paper belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("organization_id").description("Filter for the UUID of the organization that the paper belongs to. (optional)").format("uuid").optional(),
                    parameterWithName("research_field").description("Filter for research field id. (optional)").optional(),
                    parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)").optional(),
                    parameterWithName("sdg").description("Filter for the sustainable development goal that the paper belongs to. (optional)").optional(),
                    parameterWithName("mentionings").description("Filter for resources that are linked to the paper via a mentions statement. (optional)").optional(),
                    parameterWithName("research_problem").description("Filter for research problem id. (optional)").optional(),
                    parameterWithName("venue").description("Filter for venue id. (optional)").optional(),
                )
                pagedResponseFields<PaperRepresentation>(paperResponseFields())
                throws(UnknownSortingProperty::class)
            }

        verify(exactly = 1) {
            paperService.findAll(
                pageable = any(),
                label = withArg {
                    it.shouldBeInstanceOf<ExactSearchString>().input shouldBe title
                },
                doi = doi,
                doiPrefix = doiPrefix,
                visibility = visibility,
                verified = verified,
                createdBy = createdBy,
                createdAtStart = createdAtStart,
                createdAtEnd = createdAtEnd,
                observatoryId = observatoryId,
                organizationId = organizationId,
                researchField = researchFieldId,
                includeSubfields = includeSubfields,
                sustainableDevelopmentGoal = sdg,
                mentionings = mentionings,
                researchProblem = researchProblemId,
                venue = venueId,
            )
        }
    }

    @Test
    fun `Given several papers, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        get("/api/papers")
            .param("sort", "unknown")
            .accept(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:unknown_sorting_property")

        verify(exactly = 1) {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given a paper, when contributors are fetched, then status 200 OK and contributors are returned")
    fun findAllContributorsByPaperId() {
        val id = ThingId("R8186")
        val contributors = listOf(ContributorId("0a56acb7-cd97-4277-9c9b-9b3089bde45f"))
        every { paperService.findAllContributorsByPaperId(id, any()) } returns pageOf(contributors)

        documentedGetRequestTo("/api/papers/{id}/contributors", id)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDocument {
                summary("Fetching contributors of papers")
                description(
                    """
                    A `GET` request returns a <<sorting-and-pagination,paged>> list of <<contributors,contributor>> ids.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the paper."),
                )
                pagedQueryParameters()
                pagedResponseFields<UUID>(
                    fieldWithPath("content[]")
                        .references<UUID>()
                        .constraints(uuidConstraint)
                        .arrayItemsType("string")
                )
                throws(PaperNotFound::class)
            }

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    @Test
    fun `Given a paper, when contributors are fetched but paper is missing, then status 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = PaperNotFound.withId(id)
        every { paperService.findAllContributorsByPaperId(id, any()) } throws exception

        get("/api/papers/{id}/contributors", id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a paper, when publishing, then status 204 NO CONTENT")
    fun publish() {
        val id = ThingId("R123")
        val subject = "paper subject"
        val description = "paper description"
        val authors = listOf(Author("Author 1"))
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors
        )
        val paperVersionId = ThingId("R456")

        every { paperService.publish(any()) } returns paperVersionId

        documentedPostRequestTo("/api/papers/{id}/publish", id)
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/resources/$paperVersionId")))
            .andDocument {
                summary("Publishing papers")
                description(
                    """
                    A `POST` request publishes an existing paper with the given parameters.
                    In the process, a new paper version resource is created and linked to the original paper resource.
                    All contribution statements of the original paper are archived in a separate database.
                    The response will be `201 Created` when successful.
                    The paper resource can be retrieved by following the URI in the `Location` header field.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the paper to publish."),
                )
                requestFields<PublishPaperRequest>(
                    fieldWithPath("subject").description("A short title or subject for the paper version."),
                    fieldWithPath("description").description("A description or abstract of the paper."),
                    *authorListFields("paper").toTypedArray(),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the published paper can be fetched from."),
                )
                throws(
                    PaperNotFound::class,
                    AuthorNotFound::class,
                    AmbiguousAuthor::class,
                    InvalidLabel::class,
                    ServiceUnavailable::class,
                )
            }

        verify(exactly = 1) {
            paperService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper, when publishing but service reports missing paper, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val subject = "paper subject"
        val description = "paper description"
        val authors = listOf(Author("Author 1"))
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors
        )
        val exception = PaperNotFound.withId(id)

        every { paperService.publish(any()) } throws exception

        post("/api/papers/$id/publish")
            .content(request)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")

        verify(exactly = 1) {
            paperService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper, when publishing but service reports doi service unavailable, then status is 503 SERVICE UNAVAILABLE`() {
        val id = ThingId("R123")
        val subject = "paper subject"
        val description = "paper description"
        val authors = listOf(Author("Author 1"))
        val request = mapOf(
            "subject" to subject,
            "description" to description,
            "authors" to authors
        )
        val exception = ServiceUnavailable.create("DOI", 500, "Internal error")

        every { paperService.publish(any()) } throws exception

        post("/api/papers/$id/publish")
            .content(request)
            .perform()
            .andExpectErrorStatus(SERVICE_UNAVAILABLE)
            .andExpectType("orkg:problem:service_unavailable")

        verify(exactly = 1) {
            paperService.publish(
                withArg {
                    it.id shouldBe id
                    it.contributorId shouldBe ContributorId(MockUserId.USER)
                    it.description shouldBe description
                    it.subject shouldBe subject
                    it.authors shouldBe authors
                }
            )
        }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a paper create request, when service succeeds, it creates and returns the paper")
    fun create() {
        val id = ThingId("R123")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } returns id

        documentedPostRequestTo("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDocument {
                summary("Creating papers")
                description(
                    """
                    A `POST` request creates a new paper with the provided details.
                    Upon successful creation, returns `201 Created` with the `Location` header pointing to the newly created paper.
                    The response body contains the created paper for convenience.
                    """
                )
                requestFields<CreatePaperRequest>(
                    fieldWithPath("title").description("The title of the paper."),
                    fieldWithPath("research_fields").description("The list of research fields the paper will be assigned to. Must be exactly one research field."),
                    *publicationInfoRequestFields().toTypedArray(),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the paper will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                    fieldWithPath("mentionings").description("The set of ids of resources that are mentioned in the paper and should be used for extended search. (optional)").optional(),
                    fieldWithPath("contents").description("Definition of the contents of the paper. (optional)").optional(),
                    fieldWithPath("contents.resources").description("A map of temporary ids to resource definitions for resources that need to be created. (optional)").optional(),
                    fieldWithPath("contents.resources.*.label").description("The label of the resource."),
                    fieldWithPath("contents.resources.*.classes").description("The list of classes of the resource."),
                    fieldWithPath("contents.literals").description("A map of temporary ids to literal definitions for literals that need to be created. (optional)").optional(),
                    fieldWithPath("contents.literals.*.label").description("The value of the literal."),
                    fieldWithPath("contents.literals.*.data_type").description("The data type of the literal."),
                    fieldWithPath("contents.predicates").description("A map of temporary ids to predicate definitions for predicates that need to be created. (optional)").optional(),
                    fieldWithPath("contents.predicates.*.label").description("The label of the predicate."),
                    fieldWithPath("contents.predicates.*.description").description("The description of the predicate."),
                    fieldWithPath("contents.lists").description("A map of temporary ids to list definitions for lists that need to be created. (optional)").optional(),
                    fieldWithPath("contents.lists.*.label").description("The label of the list."),
                    fieldWithPath("contents.lists.*.elements").description("The IDs of the elements of the list."),
                    fieldWithPath("contents.contributions").description("List of definitions of contribution that need to be created."),
                    fieldWithPath("contents.contributions[].label").description("Label of the contribution."),
                    fieldWithPath("contents.contributions[].classes").description("The classes of the contribution resource."),
                    fieldWithPath("contents.contributions[].statements").description("A recursive map of predicate id to list of statements contained within the contribution."),
                    fieldWithPath("contents.contributions[].statements.*").description("A predicate id."),
                    subsectionWithPath("contents.contributions[].statements.*[]").description("A list of statement object requests."),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to. Can be at most one organization id."),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to. Can be at most one observatory id."),
                    fieldWithPath("extraction_method").description("""The method used to extract the paper resource. Can be one of $allowedExtractionMethodValues."""),
                    *authorListFields("paper").toTypedArray(),
                    *paperIdentifierFields().toTypedArray(),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the newly created paper can be fetched from."),
                )
                throws(
                    InvalidTempId::class,
                    DuplicateTempIds::class,
                    InvalidMonth::class,
                    InvalidLabel::class,
                    PaperAlreadyExists::class,
                    OnlyOneResearchFieldAllowed::class,
                    ResearchFieldNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    SustainableDevelopmentGoalNotFound::class,
                    ResourceNotFound::class,
                    ThingNotDefined::class,
                    ThingNotFound::class,
                    ReservedClass::class,
                    ThingIsNotAClass::class,
                    InvalidLabel::class,
                    InvalidLiteralLabel::class,
                    InvalidLiteralDatatype::class,
                    URINotAbsolute::class,
                    URIAlreadyInUse::class,
                    EmptyContribution::class,
                    ThingIsNotAPredicate::class,
                    InvalidStatementSubject::class,
                )
            }

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one research field allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneResearchFieldAllowed()
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_research_field_allowed")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one organization allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneOrganizationAllowed()
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_organization_allowed")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one observatory allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneObservatoryAllowed()
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_observatory_allowed")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing not defined, then status is 400 BAD REQUEST`() {
        val exception = ThingNotDefined("R123")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_not_defined")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports author not found, then status is 404 NOT FOUND`() {
        val exception = AuthorNotFound(ThingId("R123"))
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:author_not_found")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports duplicate temp ids, then status is 400 BAD REQUEST`() {
        val exception = DuplicateTempIds(mapOf("#temp1" to 2))
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:duplicate_temp_ids")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports invalid temp id, then status is 400 BAD REQUEST`() {
        val exception = InvalidTempId("invalid")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_temp_id")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports paper already exists with title, then status is 400 BAD REQUEST`() {
        val exception = PaperAlreadyExists.withTitle("paper title")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:paper_already_exists")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports paper already exists with identifier, then status is 400 BAD REQUEST`() {
        val exception = PaperAlreadyExists.withIdentifier("paper title")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:paper_already_exists")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports ambiguous author, then status is 400 BAD REQUEST`() {
        val exception = AmbiguousAuthor(
            Author(
                id = ThingId("R123"),
                name = "author",
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333"))
            )
        )
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:ambiguous_author")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing id is not a class, then status is 400 BAD REQUEST`() {
        val exception = ThingIsNotAClass(ThingId("R123"))
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_is_not_a_class")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing id is not a predicate, then status is 400 BAD REQUEST`() {
        val exception = ThingIsNotAPredicate("R123")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:thing_is_not_a_predicate")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports invalid statement subject, then status is 400 BAD REQUEST`() {
        val exception = InvalidStatementSubject("R123")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_statement_subject")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing not found, then status is 404 NOT FOUND`() {
        val exception = ThingNotFound("R123")
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:thing_not_found")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports empty contributions, then status is 400 BAD REQUEST`() {
        val exception = EmptyContribution(0)
        every { paperService.create(any<CreatePaperUseCase.CreateCommand>()) } throws exception

        post("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:empty_contribution")

        verify(exactly = 1) { paperService.create(any<CreatePaperUseCase.CreateCommand>()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a paper update request, when service succeeds, it updates the paper")
    fun update() {
        val id = ThingId("R123")
        every { paperService.update(any()) } just runs

        documentedPutRequestTo("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDocument {
                summary("Updating papers")
                description(
                    """
                    A `PUT` request updates an existing paper with all the given parameters.
                    The response will be `204 No Content` when successful.
                    The updated paper (object) can be retrieved by following the URI in the `Location` header field.
                    
                    [NOTE]
                    ====
                    1. All fields at the top level in the request can be omitted or `null`, meaning that the corresponding fields should not be updated.
                    2. The same rules as for <<resources-edit,updating resources>> apply when updating the visibility of a paper.
                    3. If the verified status is being modified and the performing user is not a curator, the return status will be `403 FORBIDDEN`.
                    ====
                    
                    WARNING: Author names will not be updated if a resource id is specified for a given author.
                    """
                )
                pathParameters(
                    parameterWithName("id").description("The identifier of the paper to update.")
                )
                requestFields<UpdatePaperRequest>(
                    fieldWithPath("title").description("The title of the paper. (optional)").optional(),
                    fieldWithPath("research_fields").description("The list of research fields the paper will be assigned to. (optional)").optional(),
                    *publicationInfoRequestFields().toTypedArray(),
                    fieldWithPath("sdgs").description("The set of ids of sustainable development goals the paper will be assigned to. (optional)").arrayItemsType("String").constraints(thingIdConstraint).optional(),
                    fieldWithPath("mentionings").description("The updated set of ids of resources that are mentioned in the paper and should be used for extended search. (optional)").optional(),
                    fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to. (optional)").optional(),
                    fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to. (optional)").optional(),
                    fieldWithPath("extraction_method").description("""The´updated method used to extract the paper resource. Can be one of $allowedExtractionMethodValues. (optional)""").optional(),
                    fieldWithPath("visibility").description("The updated visibility of the paper. Can be one of $allowedVisibilityValues. (optional)").optional(),
                    fieldWithPath("verified").description("The updated verification status of the paper. (optional)").optional(),
                    *authorListFields("paper", optional = true).toTypedArray(),
                    *paperIdentifierFields().toTypedArray(),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the updated paper can be fetched from."),
                )
                throws(
                    PaperNotFound::class,
                    PaperNotModifiable::class,
                    InvalidMonth::class,
                    InvalidLabel::class,
                    ContributorNotFound::class,
                    NeitherOwnerNorCurator::class,
                    NotACurator::class,
                    OnlyOneResearchFieldAllowed::class,
                    ResearchFieldNotFound::class,
                    OnlyOneObservatoryAllowed::class,
                    ObservatoryNotFound::class,
                    OnlyOneOrganizationAllowed::class,
                    OrganizationNotFound::class,
                    SustainableDevelopmentGoalNotFound::class,
                    ResourceNotFound::class,
                    PaperAlreadyExists::class,
                    AuthorNotFound::class,
                    AmbiguousAuthor::class,
                    AmbiguousAuthor::class,
                )
            }

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports paper already exists with title, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = PaperAlreadyExists.withTitle("paper title")
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:paper_already_exists")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports paper already exists with identifier, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = PaperAlreadyExists.withIdentifier("paper title")
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:paper_already_exists")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports ambiguous author, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = AmbiguousAuthor(
            Author(
                id = ThingId("R123"),
                name = "author",
                identifiers = mapOf("orcid" to listOf("0000-1111-2222-3333"))
            )
        )
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:ambiguous_author")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports author not found, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = AuthorNotFound(ThingId("R123"))
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:author_not_found")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports only one research field allowed, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = OnlyOneResearchFieldAllowed()
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_research_field_allowed")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports only one organization allowed, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = OnlyOneOrganizationAllowed()
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_organization_allowed")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports only one observatory allowed, then status is 400 BAD REQUEST`() {
        val id = ThingId("R123")
        val exception = OnlyOneObservatoryAllowed()
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:only_one_observatory_allowed")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports paper not found, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = PaperNotFound.withId(id)
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:paper_not_found")

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @DisplayName("Given a paper, when checking existence by doi and paper is found, then status is 200 OK")
    fun existsByDoi() {
        val doi = DOI.of("10.456/8764")
        val id = ThingId("R123")
        every { paperService.existsByDOI(doi) } returns Optional.of(id)

        // TODO: For unknown reasons, head requests do not work with param builders.
        // Tested on spring rest docs 3.0.3.
        documentedHeadRequestTo("/api/papers?doi=${doi.value}")
            .accept(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().string(""))
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDocument {
                summary("Checking for existing papers by DOI")
                description(
                    """
                    A `HEAD` request checks if a paper with the specified DOI exists.
                    The response will be `200 OK` when found, otherwise status will be `404 NOT FOUND`.
                    If found, the paper resource can be retrieved by following the URI in the `Location` header field.
                    """
                )
                queryParameters(
                    parameterWithName("doi").description("The DOI of the paper."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the found paper can be fetched from."),
                )
                throws(InvalidDOI::class)
            }

        verify(exactly = 1) { paperService.existsByDOI(doi) }
    }

    @Test
    fun `Given a paper, when checking existence by doi and doi is invalid, then status is 400 BAD REQUEST`() {
        val doi = ""

        head("/api/papers")
            .param("doi", doi)
            .accept(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Given a paper, when checking existence by doi and paper is not found, then status is 404 NOT FOUND`() {
        val doi = DOI.of("10.456/8764")
        every { paperService.existsByDOI(doi) } returns Optional.empty()

        head("/api/papers")
            .param("doi", doi.value)
            .accept(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(content().string(""))

        verify(exactly = 1) { paperService.existsByDOI(doi) }
    }

    @Test
    @DisplayName("Given a paper, when checking existence by title and paper is found, then status is 200 OK")
    fun existsByTitle() {
        val title = ExactSearchString("example paper")
        val id = ThingId("R123")
        every { paperService.existsByTitle(any()) } returns Optional.of(id)

        // TODO: For unknown reasons, head requests do not work with param builders.
        // Tested on spring rest docs 3.0.3.
        documentedHeadRequestTo("/api/papers?title=${title.input}")
            .accept(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().string(""))
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDocument {
                summary("Checking for existing papers by title")
                description(
                    """
                    A `HEAD` request checks if a paper with the specified title exists.
                    The response will be `200 OK` when found, otherwise status will be `404 NOT FOUND`.
                    If found, the paper resource can be retrieved by following the URI in the `Location` header field.
                    """
                )
                queryParameters(
                    parameterWithName("title").description("An exact search term that must match the title of the paper."),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the found paper can be fetched from."),
                )
            }

        verify(exactly = 1) { paperService.existsByTitle(withArg { it.input shouldBe title.input }) }
    }

    /**
     * Exists only for openapi spec generation.
     */
    @Test
    @DisplayName("Given a paper, when checking existence by doi or title and paper is found, then status is 200 OK")
    fun existsBy() {
        val doi = DOI.of("10.456/8764")
        val id = ThingId("R123")
        every { paperService.existsByDOI(doi) } returns Optional.of(id)

        // TODO: For unknown reasons, head requests do not work with param builders.
        // Tested on spring rest docs 3.0.3.
        documentedHeadRequestTo("/api/papers?doi=${doi.value}")
            .accept(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpect(content().string(""))
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDocument {
                summary("Checking for existing papers")
                description(
                    """
                    A `HEAD` request checks if a paper with the specified DOI ot title exists.
                    The response will be `200 OK` when found, otherwise status will be `404 NOT FOUND`.
                    If found, the paper resource can be retrieved by following the URI in the `Location` header field.
                    The query parameters are mutually exclusive.
                    """
                )
                queryParameters(
                    parameterWithName("doi").description("The DOI of the paper.").optional(),
                    parameterWithName("title").description("An exact search term that must match the title of the paper.").optional(),
                )
                responseHeaders(
                    headerWithName("Location").description("The uri path where the found paper can be fetched from."),
                )
                throws(InvalidDOI::class)
            }

        verify(exactly = 1) { paperService.existsByDOI(doi) }
    }

    @Test
    fun `Given a paper, when checking existence by title and paper is not found, then status is 404 NOT FOUND`() {
        val title = ExactSearchString("example paper")
        every { paperService.existsByTitle(any()) } returns Optional.empty()

        head("/api/papers")
            .param("title", title.input)
            .accept(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(content().string(""))

        verify(exactly = 1) { paperService.existsByTitle(withArg { it.input shouldBe title.input }) }
    }

    private fun createPaperRequest() =
        CreatePaperRequest(
            title = "example paper",
            researchFields = listOf(ThingId("R12")),
            identifiers = IdentifierMapRequest(mapOf("doi" to listOf("10.48550 / arXiv.2304.05327"))),
            publicationInfo = PublicationInfoRequest(
                publishedMonth = 5,
                publishedYear = 2015,
                publishedIn = "conference",
                url = ParsedIRI.create("https://www.example.org")
            ),
            authors = listOf(
                AuthorRequest(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI.create("https://example.org/author")
                ),
                AuthorRequest(
                    id = null,
                    name = "Author that just has a name",
                    identifiers = null,
                    homepage = null
                )
            ),
            sustainableDevelopmentGoals = setOf(ThingId("SDG_1")),
            mentionings = setOf(ThingId("R159"), ThingId("R753")),
            observatories = listOf(
                ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
            ),
            organizations = listOf(
                OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
            ),
            contents = PaperContentsRequest(
                resources = mapOf(
                    "#temp1" to CreateResourceRequestPart(
                        label = "MOTO",
                        classes = setOf(ThingId("Result"))
                    )
                ),
                literals = mapOf(
                    "#temp2" to CreateLiteralRequestPart(
                        label = "0.1",
                        dataType = "xsd:decimal"
                    )
                ),
                predicates = mapOf(
                    "#temp3" to CreatePredicateRequestPart(
                        label = "hasResult",
                        description = "has result"
                    )
                ),
                lists = mapOf(
                    "#temp4" to CreateListRequestPart(
                        label = "list",
                        elements = listOf("#temp1", "C123")
                    )
                ),
                contributions = listOf(
                    ContributionRequestPart(
                        label = "Contribution 1",
                        classes = setOf(ThingId("C123")),
                        statements = mapOf(
                            "P32" to listOf(
                                StatementObjectRequest(
                                    id = "R3003",
                                    statements = null
                                )
                            ),
                            "HAS_EVALUATION" to listOf(
                                StatementObjectRequest(
                                    id = "#temp1",
                                    statements = null
                                ),
                                StatementObjectRequest(
                                    id = "R3004",
                                    statements = mapOf(
                                        "#temp3" to listOf(
                                            StatementObjectRequest(
                                                id = "R3003",
                                                statements = null
                                            ),
                                            StatementObjectRequest(
                                                id = "#temp2",
                                                statements = null
                                            ),
                                            StatementObjectRequest(
                                                id = "#temp4",
                                                statements = null
                                            )
                                        ),
                                        "P32" to listOf(
                                            StatementObjectRequest(
                                                id = "#temp2",
                                                statements = null
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            extractionMethod = ExtractionMethod.MANUAL
        )

    private fun updatePaperRequest() =
        UpdatePaperRequest(
            title = "example paper",
            researchFields = listOf(ThingId("R14")),
            identifiers = IdentifierMapRequest(mapOf("doi" to listOf("10.48550/arXiv.2304.05327"))),
            publicationInfo = PublicationInfoRequest(
                publishedMonth = 5,
                publishedYear = 2015,
                publishedIn = "conference",
                url = ParsedIRI.create("https://www.example.org")
            ),
            authors = listOf(
                AuthorRequest(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapRequest(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorRequest(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI.create("https://example.org/author")
                ),
                AuthorRequest(
                    id = null,
                    name = "Author that just has a name",
                    identifiers = null,
                    homepage = null
                )
            ),
            sustainableDevelopmentGoals = setOf(
                ThingId("SDG_3"),
                ThingId("SDG_4")
            ),
            mentionings = setOf(
                ThingId("R953"),
                ThingId("R357")
            ),
            observatories = listOf(
                ObservatoryId("1afefdd0-5c09-4c9c-b718-2b35316b56f3")
            ),
            organizations = listOf(
                OrganizationId("edc18168-c4ee-4cb8-a98a-136f748e912e")
            ),
            extractionMethod = ExtractionMethod.AUTOMATIC,
            visibility = Visibility.FEATURED,
            verified = true
        )
}
