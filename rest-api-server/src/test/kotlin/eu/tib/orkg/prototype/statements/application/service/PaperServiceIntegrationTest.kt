package eu.tib.orkg.prototype.statements.application.service

import com.fasterxml.jackson.databind.ObjectMapper
import eu.tib.orkg.prototype.contributions.domain.model.ContributorService
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryClassRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryLiteralRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryPredicateRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryResourceRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryStatementRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryThingRepository
import eu.tib.orkg.prototype.statements.adapter.output.inmemory.InMemoryUserRepository
import eu.tib.orkg.prototype.statements.application.ObjectController
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
import java.util.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PaperServiceIntegrationTest {

    private lateinit var mapper: ObjectMapper

    private val thingRepository: ThingRepository = InMemoryThingRepository()
    private val classRepository: ClassRepository = InMemoryClassRepository()
    private val contributorService = ContributorService(InMemoryUserRepository())
    private val literalRepository: LiteralRepository = InMemoryLiteralRepository()
    private val literalService = LiteralService(literalRepository)
    private val predicateRepository: PredicateRepository = InMemoryPredicateRepository()
    private val predicateService = PredicateService(predicateRepository)
    private val resourceRepository: ResourceRepository = InMemoryResourceRepository()
    private val statementRepository: StatementRepository = InMemoryStatementRepository()
    private val statementService = StatementService(thingRepository, predicateRepository, statementRepository)

    private val resourceService = ResourceService(
        // TODO: The Neo4j*Repositories need to be refactored out. (And also not needed for this test.
        neo4jComparisonRepository = null!!,
        neo4jContributionRepository = null!!,
        neo4jVisualizationRepository = null!!,
        neo4jSmartReviewRepository = null!!,
        resourceRepository,
        statementRepository,
    )

    private val objectController: ObjectController = ObjectController(
        resourceService = resourceService,
        literalService = literalService,
        predicateService = predicateService,
        statementService = statementService,
        classService = ClassService(classRepository),
        contributorService = contributorService,
    )

    private val paperService: PaperService = PaperService(
        resourceService = resourceService,
        literalService = literalService,
        predicateService = predicateService,
        statementService = statementService,
        contributorService = contributorService,
        resourceRepository = resourceRepository,
        objectController = objectController,
    )

    @Test
    @Tag("regression") // see https://gitlab.com/TIBHannover/orkg/orkg-backend/-/issues/292
    fun `Creating a paper twice should add data to the paper`() {
        val userId = UUID.randomUUID()
        val req: CreatePaperRequest = mapper.readValue(exampleDataFromIssue, CreatePaperRequest::class.java)
        // Create twice
        paperService.addPaperContent(req, mergeIfExists = false, userId)
        paperService.addPaperContent(req, mergeIfExists = false, userId)
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
