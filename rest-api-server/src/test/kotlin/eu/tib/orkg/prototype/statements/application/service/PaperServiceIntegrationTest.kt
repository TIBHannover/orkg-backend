package eu.tib.orkg.prototype.statements.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.createClass
import eu.tib.orkg.prototype.createPredicate
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryLiteralRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryPredicateRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryResourceRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryStatementRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryThingRepository
import eu.tib.orkg.prototype.statements.application.service.ObjectService.Constants
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.services.ClassService
import eu.tib.orkg.prototype.statements.services.LiteralService
import eu.tib.orkg.prototype.statements.services.PredicateService
import eu.tib.orkg.prototype.statements.services.ResourceService
import eu.tib.orkg.prototype.statements.services.StatementService
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import io.mockk.every
import io.mockk.mockk
import java.util.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PaperServiceIntegrationTest {

    private val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    private val userRepository: UserRepository = mockk()

    private val classRepository: ClassRepository = InMemoryClassRepository()
    private val classService = ClassService(classRepository)
    private val contributorService = ContributorService(userRepository)
    private val literalRepository: LiteralRepository = InMemoryLiteralRepository()
    private val literalService = LiteralService(literalRepository)
    private val predicateRepository: PredicateRepository = InMemoryPredicateRepository()
    private val predicateService = PredicateService(predicateRepository)
    private val statementRepository: StatementRepository = InMemoryStatementRepository()
    private val resourceRepository: ResourceRepository = InMemoryResourceRepository(statementRepository)
    private val thingRepository: ThingRepository = InMemoryThingRepository(
        classRepository = classRepository,
        predicateRepository = predicateRepository,
        resourceRepository = resourceRepository,
        literalRepository = literalRepository,
    )
    private val statementService = StatementService(thingRepository, predicateRepository, statementRepository)

    private val resourceService = ResourceService(
        resourceRepository,
        statementRepository,
    )

    private val objectService: ObjectService = ObjectService(
        resourceService = resourceService,
        literalService = literalService,
        predicateService = predicateService,
        statementService = statementService,
        classService = classService,
        contributorService = contributorService,
    )

    private val paperService: PaperService = PaperService(
        resourceService = resourceService,
        literalService = literalService,
        predicateService = predicateService,
        statementService = statementService,
        contributorService = contributorService,
        resourceRepository = resourceRepository,
        objectService = objectService,
    )

    @Test
    @Tag("regression") // see https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/292
    fun `Creating a paper twice should add data to the paper`() {
        val userId = UUID.randomUUID()
        every { userRepository.findById(userId) } returns Optional.of(UserEntity().apply {
            id = userId
            displayName = "Some Testuser"
            email = "user@example.org"
            enabled = true
        })
        createExpectedClasses()
        createExpectedPredicates()
        // Data expected by test: // FIXME: problem if ID is in use already
        resourceRepository.save(
            createResource().copy(
                id = ResourceId("R106"),
                label = "some research field",
                classes = setOf(ClassId("ResearchField"))
            )
        )

        // Here goes the test setup:
        val req: CreatePaperRequest = mapper.readValue(exampleDataFromIssue, CreatePaperRequest::class.java)

        // Create twice
        paperService.addPaperContent(req, mergeIfExists = false, userId)
        paperService.addPaperContent(req, mergeIfExists = false, userId)
        // FIXME: assertion
    }

    private fun createExpectedClasses() {
        listOf(
            Constants.ContributionClass,
            Constants.AuthorClass,
            Constants.VenueClass,
        ).forEach {
            classRepository.save(createClass().copy(id = it))
        }
    }

    private fun createExpectedPredicates() {
        listOf(
            Constants.AuthorPredicate,
            Constants.ContributionPredicate,
            Constants.DoiPredicate,
            Constants.AuthorPredicate,
            Constants.PublicationMonthPredicate,
            Constants.PublicationYearPredicate,
            Constants.ResearchFieldPredicate,
            Constants.OrcidPredicate,
            Constants.VenuePredicate,
            Constants.UrlPredicate,
        ).forEach {
            predicateRepository.save(createPredicate().copy(id = it))
        }
    }

    //region Example data for issue #292
    @Language("json")
    @Suppress("HttpUrlsUsage")
    private val exampleDataFromIssue = """
    {
    	"paper": {
    		"authors": [
    			{
    				"label": "Robert Challen",
    				"orcid": "http://orcid.org/0000-0002-5504-7768"
    			},
    			{
    				"label": "Ellen Brooks-Pollock",
    				"orcid": "http://orcid.org/0000-0002-5984-4932"
    			},
    			{
    				"label": "Jonathan M Read",
    				"orcid": "http://orcid.org/0000-0002-9697-0962"
    			},
    			{
    				"label": "Louise Dyson",
    				"orcid": "http://orcid.org/0000-0001-9788-4858"
    			},
    			{
    				"label": "Krasimira Tsaneva-Atanasova",
    				"orcid": "http://orcid.org/0000-0002-6294-7051"
    			},
    			{
    				"label": "Leon Danon",
    				"orcid": "http://orcid.org/0000-0002-7076-1871"
    			}
    		],
    		"contributions": [
    			{
    				"classes": [
    					"Contribution"
    				],
    				"name": "Contribution 1",
    				"values": {}
    			}
    		],
    		"doi": "10.1136/bmj.n579",
    		"publicationMonth": 3,
    		"publicationYear": 2021,
    		"publishedIn": "BMJ",
    		"researchField": "R106",
    		"title": "Risk of mortality in patients infected with SARS-CoV-2 variant of concern 202012/1: matched cohort study",
    		"url": ""
    	},
    	"predicates": []
    }
    """.trimIndent()
    //endregion
}
