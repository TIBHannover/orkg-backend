package org.orkg.profiling.adapter.output.neo4j

import org.orkg.contenttypes.output.AuthorRepository
import org.orkg.contenttypes.output.ComparisonRepository
import org.orkg.contenttypes.output.ContentTypeRepository
import org.orkg.contenttypes.output.ContributionComparisonRepository
import org.orkg.contenttypes.output.FindDatasetsQuery
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.contenttypes.output.PaperRepository
import org.orkg.contenttypes.output.ResearchFieldHierarchyRepository
import org.orkg.contenttypes.output.ResearchFieldRepository
import org.orkg.contenttypes.output.ResearchProblemRepository
import org.orkg.contenttypes.output.SmartReviewRepository
import org.orkg.contenttypes.output.SummarizeBenchmarkQuery
import org.orkg.contenttypes.output.SummarizeDatasetQuery
import org.orkg.contenttypes.output.TemplateRepository
import org.orkg.curation.output.CurationRepository
import org.orkg.graph.output.ClassHierarchyRepository
import org.orkg.graph.output.ClassRelationRepository
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.FormattedLabelRepository
import org.orkg.graph.output.LegacyStatisticsRepository
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.profiling.domain.RepositoryProfiler
import org.orkg.profiling.output.ProfilingResultWriterFactory
import org.orkg.profiling.output.ValueGenerator
import org.orkg.statistics.output.StatisticsRepository
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component
import java.time.Clock
import kotlin.reflect.KFunction

@Component
@Profile("profileNeo4jRepositories")
class Neo4jRepositoryProfiler(
    context: ConfigurableApplicationContext,
    resultWriterFactory: ProfilingResultWriterFactory,
    clock: Clock,
    valueGenerators: List<ValueGenerator<*>>,
    private val neo4jClient: Neo4jClient,
) : RepositoryProfiler(context, resultWriterFactory, clock, valueGenerators) {
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
        ContentTypeRepository::class,
        ContributionComparisonRepository::class,
        CurationRepository::class,
        FindDatasetsQuery::class,
        FormattedLabelRepository::class,
        LegacyStatisticsRepository::class,
        ListRepository::class,
        LiteralRepository::class,
        LiteratureListRepository::class,
        PaperRepository::class,
        PredicateRepository::class,
        ResearchFieldHierarchyRepository::class,
        ResearchFieldRepository::class,
        ResearchProblemRepository::class,
        ResourceRepository::class,
        SmartReviewRepository::class,
        StatementRepository::class,
        StatisticsRepository::class,
        SummarizeBenchmarkQuery::class,
        SummarizeDatasetQuery::class,
        TemplateRepository::class,
        ThingRepository::class,
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
