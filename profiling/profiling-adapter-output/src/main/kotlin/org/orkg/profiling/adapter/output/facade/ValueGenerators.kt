package org.orkg.profiling.adapter.output.facade

import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.contenttypes.domain.ContentTypeClass
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.ExactSearchString
import org.orkg.graph.domain.FuzzySearchString
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.profiling.output.ValueGenerator
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.random.Random
import kotlin.reflect.KType

@Component
@Profile("profileRepositories")
class PageableValueGenerator : ValueGenerator<Pageable> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Pageable> =
        listOf(
            PageRequest.of(0, 25),
//            PageRequest.of(0, 25).withSort(Sort.by("created_at").descending())
        )
}

@Component
@Profile("profileRepositories")
class ThingIdValueGenerator(
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
    private val literalRepository: LiteralRepository,
    private val classRepository: ClassRepository,
) : ValueGenerator<ThingId> {
    private val resourceCount: Int by lazy { resourceRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }
    private val predicateCount: Int by lazy { predicateRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }
    private val literalCount: Int by lazy { literalRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }
    private val classCount: Int by lazy { classRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<ThingId> =
        when {
            "resource" in name -> listOf(random.nextResourceId())
            "class" in name -> listOf(random.nextClassId())
            "predicate" in name -> listOf(random.nextPredicateId())
            "literal" in name -> listOf(random.nextLiteralId())
            "field" in name -> listOf(random.nextResourceId(Classes.researchField))
            "paper" in name -> listOf(random.nextResourceId(Classes.paper))
            "problem" in name -> listOf(random.nextResourceId(Classes.problem))
            else -> listOf(
                random.nextResourceId(),
                random.nextClassId(),
                random.nextPredicateId(),
                random.nextLiteralId()
            )
        }

    private fun Random.nextPredicateId() = predicateRepository.findAll(PageRequest.of(nextInt(predicateCount), 1)).first().id

    private fun Random.nextLiteralId() = literalRepository.findAll(PageRequest.of(nextInt(literalCount), 1)).first().id

    private fun Random.nextClassId() = classRepository.findAll(PageRequest.of(nextInt(classCount), 1)).first().id

    private fun Random.nextResourceId(classId: ThingId? = null) =
        if (classId == null) {
            resourceRepository.findAll(PageRequest.of(nextInt(resourceCount), 1)).first().id
        } else {
            val count = resourceRepository.findAll(
                includeClasses = setOf(classId),
                pageable = PageRequest.of(0, 1)
            ).totalElements.toInt()
            resourceRepository.findAll(
                includeClasses = setOf(classId),
                pageable = PageRequest.of(nextInt(count), 1)
            ).first().id
        }
}

@Component
@Profile("profileRepositories")
class ThingIdMapGenerator : ValueGenerator<Map<ThingId, ThingId>> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Map<ThingId, ThingId>> = listOf(
        mapOf(ThingId("R44543") to ThingId("C5000"))
    )
}

@Component
@Profile("profileRepositories")
class ListValueGenerator : ValueGenerator<List<*>> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<List<*>> = listOf(randomInstances(random, name, type.arguments.first().type!!))
}

@Component
@Profile("profileRepositories")
class IterableValueGenerator : ValueGenerator<Iterable<*>> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Iterable<*>> = listOf(randomInstances(random, name, type.arguments.first().type!!))
}

@Component
@Profile("profileRepositories")
class SetValueGenerator : ValueGenerator<Set<*>> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Set<*>> = listOf(randomInstances(random, name, type.arguments.first().type!!).toSet())
}

@Component
@Transactional
@Profile("profileRepositories")
class ObservatoryIdValueGenerator(
    private val observatoryRepository: ObservatoryRepository,
) : ValueGenerator<ObservatoryId> {
    private val count: Int by lazy { observatoryRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<ObservatoryId> = listOf(observatoryRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id)
}

@Component
@Transactional
@Profile("profileRepositories")
class OrganizationIdValueGenerator(
    private val organizationRepository: PostgresOrganizationRepository,
) : ValueGenerator<OrganizationId> {
    private val count: Int by lazy { organizationRepository.count().toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<OrganizationId> = listOf(OrganizationId(organizationRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id!!))
}

@Component
@Profile("profileRepositories")
class BundleConfigurationValueGenerator : ValueGenerator<BundleConfiguration> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<BundleConfiguration> = listOf(BundleConfiguration.firstLevelConf())
}

@Component
@Profile("profileRepositories")
class VisibilityFilterValueGenerator : ValueGenerator<VisibilityFilter> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<VisibilityFilter> = VisibilityFilter.entries
}

@Component
@Profile("profileRepositories")
class VisibilityValueGenerator : ValueGenerator<Visibility> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Visibility> = Visibility.entries
}

@Component
@Profile("profileRepositories")
class ContentTypeClassValueGenerator : ValueGenerator<ContentTypeClass> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<ContentTypeClass> = ContentTypeClass.entries
}

@Component
@Profile("profileRepositories")
class SearchFilterValueGenerator : ValueGenerator<SearchFilter> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<SearchFilter> = listOf(
        SearchFilter(
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.problem,
            values = setOf(
                SearchFilter.Value(
                    op = SearchFilter.Operator.NE,
                    value = "R182085"
                )
            ),
            exact = true
        ),
        SearchFilter(
            path = listOf(Predicates.hasResearchProblem),
            range = Classes.problem,
            values = setOf(
                SearchFilter.Value(
                    op = SearchFilter.Operator.NE,
                    value = "R182085"
                )
            ),
            exact = false
        )
    )
}

@Component
@Profile("profileRepositories")
class BooleanValueGenerator : ValueGenerator<Boolean> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Boolean> = listOf(true, false)
}

@Component
@Profile("profileRepositories")
class IntValueGenerator : ValueGenerator<Int> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Int> = listOf(random.nextInt(10))
}

@Component
@Profile("profileRepositories")
class StringValueGenerator : ValueGenerator<String> {
    private val labels = listOf(
        "ResearchField",
        "Paper",
        "Contribution",
        "Problem",
        "Author",
        "FeaturedPaper",
        "Comparison",
        "Venue",
        "Visualization",
        "SmartReview",
        "Benchmark"
    )

    @Suppress("HttpUrlsUsage")
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<String> =
        when {
            "class" in name -> labels
            "date" == name -> listOf("2023-01-01")
            "uri" == name -> listOf("http://purl.org/linked-data/cube#Observation")
            "doi" == name -> listOf(
                "10.48366/r609337", // comparison
                "10.1101/2020.03.03.20029983", // paper
                "https://doi.org/10.48366/r609337", // comparison
                "https://doi.org/10.1101/2020.03.03.20029983" // paper
            )
            else -> labels + listOf("covid", "2023-01-01")
        }
}

@Component
@Profile("profileRepositories")
class StatementIdValueGenerator(
    private val statementRepository: StatementRepository,
) : ValueGenerator<StatementId> {
    private val count: Int by lazy { statementRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<StatementId> =
        listOf(statementRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id)
}

@Component
@Profile("profileRepositories")
class ContributorIdValueGenerator(
    private val contributorRepository: ContributorRepository,
) : ValueGenerator<ContributorId> {
    private val count: Int by lazy { contributorRepository.count().toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<ContributorId> =
        listOf(contributorRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id)
}

@Component
@Profile("profileRepositories")
class SearchStringValueGenerator(
    private val exactStringValueGenerator: ExactStringValueGenerator,
    private val fuzzyStringValueGenerator: FuzzyStringValueGenerator,
) : ValueGenerator<SearchString> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<SearchString> =
        exactStringValueGenerator(random, name, type, randomInstances) +
            fuzzyStringValueGenerator(random, name, type, randomInstances)
}

@Component
@Profile("profileRepositories")
class FuzzyStringValueGenerator : ValueGenerator<FuzzySearchString> {
    private val instances = listOf(
        SearchString.of(string = "covid", exactMatch = false) as FuzzySearchString
    )

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<FuzzySearchString> = instances
}

@Component
@Profile("profileRepositories")
class ExactStringValueGenerator : ValueGenerator<ExactSearchString> {
    private val instances = listOf(
        SearchString.of(string = "covid", exactMatch = true) as ExactSearchString
    )

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<ExactSearchString> = instances
}

@Component
@Profile("profileRepositories")
class OffsetDateTimeValueGenerator : ValueGenerator<OffsetDateTime> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<OffsetDateTime> = listOf(OffsetDateTime.parse("2023-01-23T16:28:34.513038Z"))
}

@Component
@Profile("profileRepositories")
class LocalDateValueGenerator : ValueGenerator<LocalDate> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<LocalDate> = listOf(LocalDate.parse("2023-01-23"))
}

@Component
@Profile("profileRepositories")
class SortValueGenerator : ValueGenerator<Sort> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>,
    ): List<Sort> = listOf(Sort.by("created_by"))
}
