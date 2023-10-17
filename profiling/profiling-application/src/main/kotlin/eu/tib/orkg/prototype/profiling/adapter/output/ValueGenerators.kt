package eu.tib.orkg.prototype.profiling.adapter.output

import eu.tib.orkg.prototype.auth.spi.UserRepository
import eu.tib.orkg.prototype.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.profiling.spi.ValueGenerator
import eu.tib.orkg.prototype.statements.api.BundleConfiguration
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ClassRepository
import eu.tib.orkg.prototype.statements.spi.LiteralRepository
import eu.tib.orkg.prototype.statements.spi.PredicateRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.time.OffsetDateTime
import kotlin.random.Random
import kotlin.reflect.KType
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
@Profile("profileRepositories")
class PageableValueGenerator : ValueGenerator<Pageable> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
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
    private val classRepository: ClassRepository
) : ValueGenerator<ThingId> {
    private val resourceCount: Int by lazy { resourceRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }
    private val predicateCount: Int by lazy { predicateRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }
    private val literalCount: Int by lazy { literalRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }
    private val classCount: Int by lazy { classRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
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
            val count = resourceRepository.findAllByClass(classId, PageRequest.of(0, 1)).totalElements.toInt()
            resourceRepository.findAllByClass(classId, PageRequest.of(nextInt(count), 1)).first().id
        }
}

@Component
@Profile("profileRepositories")
class ListValueGenerator : ValueGenerator<List<*>> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<List<*>> = listOf(randomInstances(random, name, type.arguments.first().type!!))
}

@Component
@Profile("profileRepositories")
class IterableValueGenerator : ValueGenerator<Iterable<*>> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<Iterable<*>> = listOf(randomInstances(random, name, type.arguments.first().type!!))
}

@Component
@Profile("profileRepositories")
class SetValueGenerator : ValueGenerator<Set<*>> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<Set<*>> = listOf(randomInstances(random, name, type.arguments.first().type!!).toSet())
}

@Component
@Profile("profileRepositories")
class ObservatoryIdValueGenerator(
    private val observatoryRepository: ObservatoryRepository
) : ValueGenerator<ObservatoryId> {
    private val count: Int by lazy { observatoryRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<ObservatoryId> = listOf(observatoryRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id)
}

@Component
@Profile("profileRepositories")
class OrganizationIdValueGenerator(
    private val organizationRepository: PostgresOrganizationRepository
) : ValueGenerator<OrganizationId> {
    private val count: Int by lazy { organizationRepository.count().toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<OrganizationId> = listOf(OrganizationId(organizationRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id!!))
}

@Component
@Profile("profileRepositories")
class BundleConfigurationValueGenerator : ValueGenerator<BundleConfiguration> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<BundleConfiguration> = listOf(BundleConfiguration.firstLevelConf())
}

@Component
@Profile("profileRepositories")
class VisibilityFilterValueGenerator : ValueGenerator<VisibilityFilter> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<VisibilityFilter> = VisibilityFilter.values().toList()
}

@Component
@Profile("profileRepositories")
class VisibilityValueGenerator : ValueGenerator<Visibility> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<Visibility> = Visibility.values().toList()
}

@Component
@Profile("profileRepositories")
class BooleanValueGenerator : ValueGenerator<Boolean> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<Boolean> = listOf(true, false)
}

@Component
@Profile("profileRepositories")
class IntValueGenerator : ValueGenerator<Int> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
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

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
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
    private val statementRepository: StatementRepository
) : ValueGenerator<StatementId> {
    private val count: Int by lazy { statementRepository.findAll(PageRequest.of(0, 1)).totalElements.toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<StatementId> =
        listOf(statementRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id!!)
}

@Component
@Profile("profileRepositories")
class ContributorIdValueGenerator(
    private val userRepository: UserRepository
) : ValueGenerator<ContributorId> {
    private val count: Int by lazy { userRepository.count().toInt() }

    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<ContributorId> =
        listOf(ContributorId(userRepository.findAll(PageRequest.of(random.nextInt(count), 1)).first().id))
}

@Component
@Profile("profileRepositories")
class SearchStringValueGenerator : ValueGenerator<SearchString> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<SearchString> = listOf(
        SearchString.of(string = "covid", exactMatch = true),
        SearchString.of(string = "covid", exactMatch = false)
    )
}

@Component
@Profile("profileRepositories")
class OffsetDateTimeValueGenerator : ValueGenerator<OffsetDateTime> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<OffsetDateTime> = listOf(OffsetDateTime.parse("2023-01-23T16:28:34.513038Z"))
}

@Component
@Profile("profileRepositories")
class SortValueGenerator : ValueGenerator<Sort> {
    override operator fun invoke(
        random: Random,
        name: String,
        type: KType,
        randomInstances: (Random, String, KType) -> List<Any>
    ): List<Sort> = listOf(Sort.by("created_by"))
}
