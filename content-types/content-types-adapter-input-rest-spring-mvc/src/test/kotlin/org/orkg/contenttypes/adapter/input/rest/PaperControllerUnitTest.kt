package org.orkg.contenttypes.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.exceptions.UnknownSortingProperty
import org.orkg.common.json.CommonJacksonModule
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreateContributionRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.ContributionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.PaperContentsDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.CreatePaperRequest.StatementObjectDefinitionDTO
import org.orkg.contenttypes.adapter.input.rest.PaperController.UpdatePaperRequest
import org.orkg.contenttypes.adapter.input.rest.json.ContentTypeJacksonModule
import org.orkg.contenttypes.domain.AmbiguousAuthor
import org.orkg.contenttypes.domain.Author
import org.orkg.contenttypes.domain.AuthorNotFound
import org.orkg.contenttypes.domain.DuplicateTempIds
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.InvalidStatementSubject
import org.orkg.contenttypes.domain.InvalidTempId
import org.orkg.contenttypes.domain.OnlyOneObservatoryAllowed
import org.orkg.contenttypes.domain.OnlyOneOrganizationAllowed
import org.orkg.contenttypes.domain.OnlyOneResearchFieldAllowed
import org.orkg.contenttypes.domain.PaperAlreadyExists
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.ThingIsNotAPredicate
import org.orkg.contenttypes.domain.ThingNotDefined
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.ContributionUseCases
import org.orkg.contenttypes.input.PaperUseCases
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectPage
import org.orkg.testing.andExpectPaper
import org.orkg.testing.annotations.TestWithMockUser
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.orkg.testing.spring.restdocs.RestDocsTest
import org.orkg.testing.spring.restdocs.documentedGetRequestTo
import org.orkg.testing.spring.restdocs.documentedPostRequestTo
import org.orkg.testing.spring.restdocs.documentedPutRequestTo
import org.orkg.testing.spring.restdocs.timestampFieldWithPath
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [PaperController::class, ExceptionHandler::class, CommonJacksonModule::class, ContentTypeJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [PaperController::class])
internal class PaperControllerUnitTest : RestDocsTest("papers") {

    @MockkBean
    private lateinit var paperService: PaperUseCases

    @MockkBean
    private lateinit var contributionService: ContributionUseCases

    @Test
    @DisplayName("Given a paper, when it is fetched by id and service succeeds, then status is 200 OK and paper is returned")
    fun getSingle() {
        val paper = createPaper()
        every { paperService.findById(paper.id) } returns Optional.of(paper)

        documentedGetRequestTo("/api/papers/{id}", paper.id)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPaper()
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the paper to retrieve.")
                    ),
                    responseFields(
                        // The order here determines the order in the generated table. More relevant items should be up.
                        fieldWithPath("id").description("The identifier of the paper."),
                        fieldWithPath("title").description("The title of the paper."),
                        fieldWithPath("research_fields").description("The list of research fields the paper is assigned to."),
                        fieldWithPath("research_fields[].id").description("The id of the research field."),
                        fieldWithPath("research_fields[].label").description("The label of the research field."),
                        fieldWithPath("contributions").description("The list of contributions of the paper."),
                        fieldWithPath("contributions[].id").description("The ID of the contribution."),
                        fieldWithPath("contributions[].label").description("The label of the contribution."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to."),
                        fieldWithPath("mentionings[]").description("Set of important resources in the paper."),
                        fieldWithPath("mentionings[].id").description("The ID of the mentioned resource."),
                        fieldWithPath("mentionings[].label").description("The label of the mentioned resource."),
                        fieldWithPath("mentionings[].classes").description("The class ids of the mentioned resource."),
                        fieldWithPath("mentionings[]._class").description("Indicates which type of entity was returned. Always has the value `resource_ref`."),
                        fieldWithPath("extraction_method").description("""The method used to extract the paper resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC"."""),
                        timestampFieldWithPath("created_at", "the paper resource was created"),
                        // TODO: Add links to documentation of special user UUIDs.
                        fieldWithPath("created_by").description("The UUID of the user or service who created this paper."),
                        fieldWithPath("verified").description("Determines if the paper was verified by a curator."),
                        fieldWithPath("visibility").description("""Visibility of the paper. Can be one of "DEFAULT", "FEATURED", "UNLISTED" or "DELETED"."""),
                        fieldWithPath("modifiable").description("Whether this paper can be modified."),
                        fieldWithPath("unlisted_by").type("String").description("The UUID of the user or service who unlisted this paper.").optional(),
                        fieldWithPath("_class").description("Indicates which type of entity was returned. Always has the value `paper`."),
                    ).and(authorListFields("paper"))
                        .and(publicationInfoFields("paper"))
                        .and(sustainableDevelopmentGoalsFields("paper"))
                        .and(paperIdentifierFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.findById(paper.id) }
    }

    @Test
    fun `Given a paper, when it is fetched by id and service reports missing paper, then status is 404 NOT FOUND`() {
        val id = ThingId("Missing")
        val exception = PaperNotFound(id)
        every { paperService.findById(id) } returns Optional.empty()

        get("/api/papers/{id}", id)
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.findById(id) }
    }

    @Test
    @DisplayName("Given several papers, when they are fetched, then status is 200 OK and papers are returned")
    fun getPaged() {
        every {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
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
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given several papers, when filtering by several parameters, then status is 200 OK and papers are returned")
    fun getPagedWithParameters() {
        every {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
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
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andExpectPaper("$.content[*]")
            .andDo(
                documentationHandler.document(
                    queryParameters(
                        parameterWithName("title").description("A search term that must be contained in the title of the paper. (optional)"),
                        parameterWithName("exact").description("Whether title matching is exact or fuzzy (optional, default: false)"),
                        parameterWithName("doi").description("Filter for the DOI of the paper. (optional)"),
                        parameterWithName("doi_prefix").description("Filter for the DOI prefix of the DOI of the paper. (optional)"),
                        parameterWithName("visibility").description("""Optional filter for visibility. Either of "ALL_LISTED", "UNLISTED", "FEATURED", "NON_FEATURED", "DELETED"."""),
                        parameterWithName("verified").description("Filter for the verified flag of the paper. (optional)"),
                        parameterWithName("created_by").description("Filter for the UUID of the user or service who created this paper. (optional)"),
                        parameterWithName("created_at_start").description("Filter for the created at timestamp, marking the oldest timestamp a returned paper can have. (optional)"),
                        parameterWithName("created_at_end").description("Filter for the created at timestamp, marking the most recent timestamp a returned paper can have. (optional)"),
                        parameterWithName("observatory_id").description("Filter for the UUID of the observatory that the paper belongs to. (optional)"),
                        parameterWithName("organization_id").description("Filter for the UUID of the organization that the paper belongs to. (optional)"),
                        parameterWithName("research_field").description("Filter for research field id. (optional)"),
                        parameterWithName("include_subfields").description("Flag for whether subfields are included in the search or not. (optional, default: false)"),
                        parameterWithName("sdg").description("Filter for the sustainable development goal that the paper belongs to. (optional)"),
                        parameterWithName("mentionings").description("Filter for resources that are linked to the paper via a mentions statement. (optional)"),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
                mentionings = mentionings
            )
        }
    }

    @Test
    fun `Given several papers, when invalid sorting property is specified, then status is 400 BAD REQUEST`() {
        val exception = UnknownSortingProperty("unknown")
        every {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } throws exception

        mockMvc.perform(get("/api/papers?sort=unknown"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value(exception.message))
            .andExpect(jsonPath("$.error").value(exception.status.reasonPhrase))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.path").value("/api/papers"))

        verify(exactly = 1) {
            paperService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    @DisplayName("Given a paper, when contributors are fetched, then status 200 OK and contributors are returned")
    fun getContributors() {
        val id = ThingId("R8186")
        val contributors = listOf(ContributorId("0a56acb7-cd97-4277-9c9b-9b3089bde45f"))
        every { paperService.findAllContributorsByPaperId(id, any()) } returns PageImpl(contributors, PageRequest.of(0, 5), 1)

        documentedGetRequestTo("/api/papers/{id}/contributors", id)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isOk)
            .andExpectPage()
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.findAllContributorsByPaperId(id, any()) }
    }

    @Test
    fun `Given a paper, when contributors are fetched but paper is missing, then status 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = PaperNotFound(id)
        every { paperService.findAllContributorsByPaperId(id, any()) } throws exception

        get("/api/papers/{id}/contributors", id)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id/contributors"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("api/resources/$paperVersionId")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the paper to publish.")
                    ),
                    requestFields(
                        fieldWithPath("subject").description("The subject of the paper."),
                        fieldWithPath("description").description("The description of the paper."),
                    ).and(authorListFields("paper"))
                )
            )
            .andDo(generateDefaultDocSnippets())

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
        val exception = PaperNotFound(id)

        every { paperService.publish(any()) } throws exception

        post("/api/papers/$id/publish")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id/publish"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .perform()
            .andExpect(status().isServiceUnavailable)
            .andExpect(jsonPath("$.status").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id/publish"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
        every { paperService.create(any()) } returns id

        documentedPostRequestTo("/api/papers")
            .content(createPaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/papers/$id")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created paper can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the paper."),
                        fieldWithPath("research_fields").description("The list of research fields the paper will be assigned to. Must be exactly one research field."),
                        fieldWithPath("publication_info").description("The publication info of the paper. (optional)").optional(),
                        fieldWithPath("publication_info.published_month").description("The month in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_year").description("The year in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_in").description("The venue where the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.url").description("The URL to the original paper. (optional)").optional(),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the paper will be assigned to. (optional)").optional(),
                        fieldWithPath("mentionings").description("The set of ids of resources that are mentioned in the paper and should be used for extended search. (optional)").optional(),
                        fieldWithPath("contents").description("Definition of the contents of the paper. (optional)"),
                        fieldWithPath("contents.resources").description("Definition of resources that need to be created."),
                        fieldWithPath("contents.resources.*.label").description("The label of the resource."),
                        fieldWithPath("contents.resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("contents.literals").description("Definition of literals that need to be created."),
                        fieldWithPath("contents.literals.*.label").description("The value of the literal."),
                        fieldWithPath("contents.literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("contents.predicates").description("Definition of predicates that need to be created."),
                        fieldWithPath("contents.predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("contents.predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("contents.lists").description("Definition of lists that need to be created."),
                        fieldWithPath("contents.lists.*.label").description("The label of the list."),
                        fieldWithPath("contents.lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("contents.contributions").description("List of definitions of contribution that need to be created."),
                        fieldWithPath("contents.contributions[].label").description("Label of the contribution."),
                        fieldWithPath("contents.contributions[].classes").description("The classes of the contribution resource."),
                        subsectionWithPath("contents.contributions[].statements").description("Recursive map of statements contained within the contribution."),
                        fieldWithPath("contents.contributions[].statements.*[].id").description("The ID of the object of the statement."),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to. Can be at most one organization id."),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to. Can be at most one observatory id."),
                        fieldWithPath("extraction_method").description("""The method used to extract the paper resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC".""")
                    ).and(authorListFields("paper"))
                        .and(paperIdentifierFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one research field allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneResearchFieldAllowed()
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one organization allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneOrganizationAllowed()
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports only one observatory allowed, then status is 400 BAD REQUEST`() {
        val exception = OnlyOneObservatoryAllowed()
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing not defined, then status is 400 BAD REQUEST`() {
        val exception = ThingNotDefined("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports author not found, then status is 404 NOT FOUND`() {
        val exception = AuthorNotFound(ThingId("R123"))
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports duplicate temp ids, then status is 400 BAD REQUEST`() {
        val exception = DuplicateTempIds(mapOf("#temp1" to 2))
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports invalid temp id, then status is 400 BAD REQUEST`() {
        val exception = InvalidTempId("invalid")
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports paper already exists with title, then status is 400 BAD REQUEST`() {
        val exception = PaperAlreadyExists.withTitle("paper title")
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports paper already exists with identifier, then status is 400 BAD REQUEST`() {
        val exception = PaperAlreadyExists.withIdentifier("paper title")
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
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
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing id is not a class, then status is 400 BAD REQUEST`() {
        val exception = ThingIsNotAClass(ThingId("R123"))
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing id is not a predicate, then status is 400 BAD REQUEST`() {
        val exception = ThingIsNotAPredicate("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports invalid statement subject, then status is 400 BAD REQUEST`() {
        val exception = InvalidStatementSubject("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports thing not found, then status is 404 NOT FOUND`() {
        val exception = ThingNotFound("R123")
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper create request, when service reports empty contributions, then status is 400 BAD REQUEST`() {
        val exception = EmptyContribution(0)
        every { paperService.create(any()) } throws exception

        post("/api/papers")
            .content(objectMapper.writeValueAsString(createPaperRequest()))
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.create(any()) }
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
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated paper can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("title").description("The title of the paper. (optional)"),
                        fieldWithPath("research_fields").description("The list of research fields the paper will be assigned to. (optional)"),
                        fieldWithPath("publication_info").description("The publication info of the paper. (optional)").optional(),
                        fieldWithPath("publication_info.published_month").description("The month in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_year").description("The year in which the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.published_in").description("The venue where the paper was published. (optional)").optional(),
                        fieldWithPath("publication_info.url").description("The URL to the original paper. (optional)").optional(),
                        fieldWithPath("sdgs").description("The set of ids of sustainable development goals the paper will be assigned to. (optional)"),
                        fieldWithPath("mentionings").description("The updated set of ids of resources that are mentioned in the paper and should be used for extended search. (optional)"),
                        fieldWithPath("organizations[]").description("The list of IDs of the organizations the paper belongs to. (optional)").optional(),
                        fieldWithPath("observatories[]").description("The list of IDs of the observatories the paper belongs to. (optional)").optional()
                    ).and(authorListFields("paper"))
                        .and(paperIdentifierFields())
                )
            )
            .andDo(generateDefaultDocSnippets())

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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

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
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a paper update request, when service reports paper not found, then status is 404 NOT FOUND`() {
        val id = ThingId("R123")
        val exception = PaperNotFound(id)
        every { paperService.update(any()) } throws exception

        put("/api/papers/{id}", id)
            .content(updatePaperRequest())
            .accept(PAPER_JSON_V2)
            .contentType(PAPER_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$id"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.update(any()) }
    }

    @Test
    @TestWithMockUser
    @DisplayName("Given a contribution request, when service succeeds, it creates and returns the contribution")
    fun createContribution() {
        val paperId = ThingId("R3541")
        val contributionId = ThingId("R123")
        every { paperService.createContribution(any()) } returns contributionId

        documentedPostRequestTo("/api/papers/{id}/contributions", paperId)
            .content(createContributionRequest())
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("Location", endsWith("/api/contributions/$contributionId")))
            .andDo(
                documentationHandler.document(
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the newly created contribution can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("extraction_method").description("""The method used to extract the contribution resource. Can be one of "UNKNOWN", "MANUAL" or "AUTOMATIC". (default: "UNKNOWN")""").optional(),
                        fieldWithPath("resources").description("Definition of resources that need to be created."),
                        fieldWithPath("resources.*.label").description("The label of the resource."),
                        fieldWithPath("resources.*.classes").description("The list of classes of the resource."),
                        fieldWithPath("literals").description("Definition of literals that need to be created."),
                        fieldWithPath("literals.*.label").description("The value of the literal."),
                        fieldWithPath("literals.*.data_type").description("The data type of the literal."),
                        fieldWithPath("predicates").description("Definition of predicates that need to be created."),
                        fieldWithPath("predicates.*.label").description("The label of the predicate."),
                        fieldWithPath("predicates.*.description").description("The description of the predicate."),
                        fieldWithPath("lists").description("Definition of lists that need to be created."),
                        fieldWithPath("lists.*.label").description("The label of the list."),
                        fieldWithPath("lists.*.elements").description("The IDs of the elements of the list."),
                        fieldWithPath("contribution").description("List of definitions of contribution that need to be created."),
                        fieldWithPath("contribution.label").description("Label of the contribution."),
                        fieldWithPath("contribution.classes").description("The classes of the contribution resource."),
                        subsectionWithPath("contribution.statements").description("Recursive map of statements contained within the contribution."),
                        fieldWithPath("contribution.statements.*[].id").description("The ID of the object of the statement.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports invalid temp id, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = InvalidTempId("invalid")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports duplicate temp ids, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = DuplicateTempIds(mapOf("#temp1" to 2))
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports paper not found, then status is 404 NOT FOUND`() {
        val paperId = ThingId("R123")
        val exception = PaperNotFound(ThingId("R123"))
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing not defined, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingNotDefined("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing not found, then status is 404 NOT FOUND`() {
        val paperId = ThingId("R123")
        val exception = ThingNotFound("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing id is not a class, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingIsNotAClass(ThingId("R123"))
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports thing id is not a predicate, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = ThingIsNotAPredicate("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports invalid statement subject, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = InvalidStatementSubject("R123")
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    @Test
    @TestWithMockUser
    fun `Given a contribution request, when service reports empty contribution, then status is 400 BAD REQUEST`() {
        val paperId = ThingId("R123")
        val exception = EmptyContribution()
        every { paperService.createContribution(any()) } throws exception

        post("/api/papers/{id}/contributions", paperId)
            .content(objectMapper.writeValueAsString(createContributionRequest()))
            .accept(CONTRIBUTION_JSON_V2)
            .contentType(CONTRIBUTION_JSON_V2)
            .perform()
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.path").value("/api/papers/$paperId/contributions"))
            .andExpect(jsonPath("$.message").value(exception.message))

        verify(exactly = 1) { paperService.createContribution(any()) }
    }

    private fun createPaperRequest() =
        CreatePaperRequest(
            title = "example paper",
            researchFields = listOf(ThingId("R12")),
            identifiers = IdentifierMapDTO(mapOf("doi" to listOf("10.48550 / arXiv.2304.05327"))),
            publicationInfo = PublicationInfoDTO(
                publishedMonth = 5,
                publishedYear = 2015,
                publishedIn = "conference",
                url = ParsedIRI("https://www.example.org")
            ),
            authors = listOf(
                AuthorDTO(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI("https://example.org/author")
                ),
                AuthorDTO(
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
            contents = PaperContentsDTO(
                resources = mapOf(
                    "#temp1" to ResourceDefinitionDTO(
                        label = "MOTO",
                        classes = setOf(ThingId("Result"))
                    )
                ),
                literals = mapOf(
                    "#temp2" to LiteralDefinitionDTO(
                        label = "0.1",
                        dataType = "xsd:decimal"
                    )
                ),
                predicates = mapOf(
                    "#temp3" to PredicateDefinitionDTO(
                        label = "hasResult",
                        description = "has result"
                    )
                ),
                lists = mapOf(
                    "#temp4" to ListDefinitionDTO(
                        label = "list",
                        elements = listOf("#temp1", "C123")
                    )
                ),
                contributions = listOf(
                    ContributionDTO(
                        label = "Contribution 1",
                        classes = setOf(ThingId("C123")),
                        statements = mapOf(
                            "P32" to listOf(
                                StatementObjectDefinitionDTO(
                                    id = "R3003",
                                    statements = null
                                )
                            ),
                            "HAS_EVALUATION" to listOf(
                                StatementObjectDefinitionDTO(
                                    id = "#temp1",
                                    statements = null
                                ),
                                StatementObjectDefinitionDTO(
                                    id = "R3004",
                                    statements = mapOf(
                                        "#temp3" to listOf(
                                            StatementObjectDefinitionDTO(
                                                id = "R3003",
                                                statements = null
                                            ),
                                            StatementObjectDefinitionDTO(
                                                id = "#temp2",
                                                statements = null
                                            ),
                                            StatementObjectDefinitionDTO(
                                                id = "#temp4",
                                                statements = null
                                            )
                                        ),
                                        "P32" to listOf(
                                            StatementObjectDefinitionDTO(
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
            identifiers = IdentifierMapDTO(mapOf("doi" to listOf("10.48550/arXiv.2304.05327"))),
            publicationInfo = PublicationInfoDTO(
                publishedMonth = 5,
                publishedYear = 2015,
                publishedIn = "conference",
                url = ParsedIRI("https://www.example.org")
            ),
            authors = listOf(
                AuthorDTO(
                    id = ThingId("R123"),
                    name = "Author with id",
                    identifiers = null,
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("0000-1111-2222-3333"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = ThingId("R456"),
                    name = "Author with id and orcid",
                    identifiers = IdentifierMapDTO(mapOf("orcid" to listOf("1111-2222-3333-4444"))),
                    homepage = null
                ),
                AuthorDTO(
                    id = null,
                    name = "Author with homepage",
                    identifiers = null,
                    homepage = ParsedIRI("https://example.org/author")
                ),
                AuthorDTO(
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
            )
        )

    private fun createContributionRequest() =
        CreateContributionRequest(
            resources = mapOf(
                "#temp1" to ResourceDefinitionDTO(
                    label = "MOTO",
                    classes = setOf(ThingId("Result"))
                )
            ),
            literals = mapOf(
                "#temp2" to LiteralDefinitionDTO(
                    label = "0.1",
                    dataType = "xsd:decimal"
                )
            ),
            predicates = mapOf(
                "#temp3" to PredicateDefinitionDTO(
                    label = "hasResult",
                    description = "has result"
                )
            ),
            lists = mapOf(
                "#temp4" to ListDefinitionDTO(
                    label = "list",
                    elements = listOf("#temp1", "C123")
                )
            ),
            contribution = ContributionDTO(
                label = "Contribution 1",
                classes = setOf(ThingId("C123")),
                statements = mapOf(
                    "P32" to listOf(
                        StatementObjectDefinitionDTO(
                            id = "R3003",
                            statements = null
                        )
                    ),
                    "HAS_EVALUATION" to listOf(
                        StatementObjectDefinitionDTO(
                            id = "#temp1",
                            statements = null
                        ),
                        StatementObjectDefinitionDTO(
                            id = "R3004",
                            statements = mapOf(
                                "#temp3" to listOf(
                                    StatementObjectDefinitionDTO(
                                        id = "R3003",
                                        statements = null
                                    ),
                                    StatementObjectDefinitionDTO(
                                        id = "#temp2",
                                        statements = null
                                    ),
                                    StatementObjectDefinitionDTO(
                                        id = "#temp4",
                                        statements = null
                                    )
                                ),
                                "P32" to listOf(
                                    StatementObjectDefinitionDTO(
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
}
