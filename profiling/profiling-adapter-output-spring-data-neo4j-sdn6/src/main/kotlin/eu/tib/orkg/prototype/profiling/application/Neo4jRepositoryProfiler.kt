package eu.tib.orkg.prototype.profiling.application

import eu.tib.orkg.prototype.profiling.spi.ProfilingResultWriterFactory
import eu.tib.orkg.prototype.profiling.spi.ValueGenerator
import eu.tib.orkg.prototype.statements.spi.AuthorRepository
import eu.tib.orkg.prototype.statements.spi.ClassHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ClassRelationRepository
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.ComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ContributionComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ContributionRepository
import eu.tib.orkg.prototype.statements.spi.ListRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PaperRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResearchFieldHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import eu.tib.orkg.prototype.statements.spi.ResearchProblemRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.SmartReviewRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.spi.StatsRepository
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import eu.tib.orkg.prototype.statements.spi.ThingRepository
import eu.tib.orkg.prototype.statements.spi.VisualizationRepository
import kotlin.reflect.KFunction
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
@Profile("profileNeo4jRepositories")
class Neo4jRepositoryProfiler(
    context: ConfigurableApplicationContext,
    resultWriterFactory: ProfilingResultWriterFactory,
    valueGenerators: List<ValueGenerator<*>>,
    private val neo4jClient: Neo4jClient
) : RepositoryProfiler(context, resultWriterFactory, valueGenerators) {

    override fun clearQueryCache() {
        neo4jClient.query("CALL db.clearQueryCaches()").run()
    }

    override fun doProfileFunction(function: KFunction<*>): Boolean =
        ignoredFunctions.none { function.name.startsWith(it) }

    override val repositories = listOf(
        AuthorRepository::class,
        ClassHierarchyRepository::class,
        ClassRelationRepository::class,
        ClassRepository::class,
        ComparisonRepository::class,
        ContributionComparisonRepository::class,
        ContributionRepository::class,
        ListRepository::class,
        LiteralRepository::class,
        PaperRepository::class,
        PredicateRepository::class,
        ResearchFieldHierarchyRepository::class,
        ResearchFieldRepository::class,
        ResearchProblemRepository::class,
        ResourceRepository::class,
        SmartReviewRepository::class,
        StatementRepository::class,
        StatsRepository::class,
        TemplateRepository::class,
        ThingRepository::class,
        VisualizationRepository::class
    )

    private val ignoredFunctions = listOf(
        "toString",
        "hashCode",
        "equals",
        "save",
        "delete",
        "nextIdentity"
    )
}
