package org.orkg.profiling.adapter.output.neo4j

import kotlin.reflect.KFunction
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.contenttypes.output.ContributionRepository
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.contenttypes.output.ResearchFieldHierarchyRepository
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.contenttypes.output.VisualizationRepository
import org.orkg.graph.output.AuthorRepository
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.StatsRepository
import org.orkg.graph.output.TemplateRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.profiling.domain.RepositoryProfiler
import org.orkg.profiling.output.ProfilingResultWriterFactory
import org.orkg.profiling.output.ValueGenerator
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
