package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.paperswithcode.application.port.output.FindResearchProblemQuery
import eu.tib.orkg.prototype.researchproblem.application.domain.ResearchProblem
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResource
import eu.tib.orkg.prototype.statements.api.IterableResourcesGenerator
import eu.tib.orkg.prototype.statements.api.ResourceGenerator
import eu.tib.orkg.prototype.statements.api.ResourceRepresentation
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase.FieldCount
import eu.tib.orkg.prototype.statements.api.RetrieveResearchProblemUseCase.PaperCountPerAuthor
import eu.tib.orkg.prototype.statements.application.ResourceNotFound
import eu.tib.orkg.prototype.statements.domain.model.ClassId
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ContributorPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.DetailsPerProblem
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jProblemRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val ProblemClass = ClassId("Problem")

@Service
@Transactional
class ResearchProblemService(
    private val neo4jProblemRepository: Neo4jProblemRepository,
    private val resourceService: ResourceUseCases,
    private val researchProblemQueries: FindResearchProblemQuery,
) : RetrieveResearchProblemUseCase {
    override fun findById(id: ResourceId): Optional<ResourceRepresentation> =
        Optional.ofNullable(resourceService.findByIdAndClasses(id, setOf(ProblemClass)))

    override fun findFieldsPerProblem(problemId: ResourceId): List<FieldCount> {
        return neo4jProblemRepository.findResearchFieldsPerProblem(problemId).map {
            FieldCount(
                field = resourceService.map(ResourceGenerator { it.field.toResource() }), freq = it.freq
            )
        }
    }

    override fun findFieldsPerProblemAndClasses(
        problemId: ResourceId,
        featured: Boolean?,
        unlisted: Boolean,
        classes: List<String>,
        pageable: Pageable
    ): Page<DetailsPerProblem> {
        val resultList = mutableListOf<DetailsPerProblem>()
        val totals = mutableListOf<Long>()
        if (classes.isNotEmpty()) {
            when (featured) {
                null -> getProblemsWithoutFeatured(classes, problemId, unlisted, pageable, resultList, totals)
                else -> getProblemsWithFeatured(classes, problemId, featured, unlisted, pageable, resultList, totals)
            }
            resultList.sortBy(DetailsPerProblem::createdAt)
        } else {
            return Page.empty()
        }
        return PageImpl(resultList.take(pageable.pageSize), pageable, totals.sum())
    }

    override fun findTopResearchProblems(): List<ResourceRepresentation> =
        resourceService.map(IterableResourcesGenerator {
            findTopResearchProblemsGoingBack(
                listOf(1, 2, 3, 6), emptyList()
            ).map(Neo4jResource::toResource)
        }).toList()

    /*
    Iterate over the list of months, and if no problems are found go back a bit more in time
    and if none found take all time results
     */
    private fun findTopResearchProblemsGoingBack(
        listOfMonths: List<Int>,
        result: List<Neo4jResource>
    ): Iterable<Neo4jResource> {
        val month = listOfMonths.firstOrNull()
        val problems = if (month == null)
            neo4jProblemRepository.findTopResearchProblemsAllTime()
        else
            neo4jProblemRepository.findTopResearchProblemsGoingBack(month)
        val newResult = result.plus(problems).distinct()
        return if (newResult.count() >= 5)
            newResult.take(5)
        else
            findTopResearchProblemsGoingBack(listOfMonths.drop(1), newResult)
    }

    override fun findContributorsPerProblem(problemId: ResourceId, pageable: Pageable): List<ContributorPerProblem> {
        return neo4jProblemRepository
            .findContributorsLeaderboardPerProblem(problemId, pageable)
            .content
    }

    override fun findAuthorsPerProblem(problemId: ResourceId, pageable: Pageable): List<PaperCountPerAuthor> {
        return neo4jProblemRepository.findAuthorsLeaderboardPerProblem(problemId, pageable).content.map {
                if (it.isLiteral) PaperCountPerAuthor(
                    author = it.author,
                    papers = it.papers,
                )
                else PaperCountPerAuthor(
                    // It is important that this is a ResourceRepresentation! Otherwise, it will break clients.
                    // This is not ideal, but would need custom serialization code.
                    author = resourceService.map(ResourceGenerator { it.toAuthorResource.toResource() }),
                    papers = it.papers,
                )
            }
    }

    override fun forDataset(id: ResourceId): Optional<List<ResearchProblem>> {
        val dataset = resourceService.findById(id)
        if (!dataset.isPresent) return Optional.empty()
        return Optional.of(researchProblemQueries.findResearchProblemForDataset(id))
    }

    override fun getFeaturedProblemFlag(id: ResourceId): Boolean {
        val result = neo4jProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.featured ?: false
    }

    override fun getUnlistedProblemFlag(id: ResourceId): Boolean {
        val result = neo4jProblemRepository.findById(id)
        return result.orElseThrow { ResourceNotFound(id.toString()) }.unlisted ?: false
    }

    override fun loadFeaturedProblems(pageable: Pageable): Page<Resource> =
        neo4jProblemRepository.findAllFeaturedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun loadNonFeaturedProblems(pageable: Pageable): Page<Resource> =
        neo4jProblemRepository.findAllNonFeaturedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun loadUnlistedProblems(pageable: Pageable):
        Page<Resource> =
        neo4jProblemRepository.findAllUnlistedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun loadListedProblems(pageable: Pageable):
        Page<Resource> =
        neo4jProblemRepository.findAllListedProblems(pageable)
            .map(Neo4jResource::toResource)

    override fun markAsFeatured(resourceId: ResourceId): Optional<Resource> {
        setUnlistedFlag(resourceId, false)
        return setFeaturedFlag(resourceId, true)
    }

    override fun markAsNonFeatured(resourceId: ResourceId) = setFeaturedFlag(resourceId, false)

    override fun markAsUnlisted(resourceId: ResourceId): Optional<Resource> {
        setFeaturedFlag(resourceId, false)
        return setUnlistedFlag(resourceId, true)
    }

    override fun markAsListed(resourceId: ResourceId) = setUnlistedFlag(resourceId, false)

    private fun setFeaturedFlag(resourceId: ResourceId, featured: Boolean): Optional<Resource> {
        val result = neo4jProblemRepository.findById(resourceId)
        if (result.isPresent) {
            val problem = result.get()
            problem.featured = featured
            return Optional.of(neo4jProblemRepository.save(problem).toResource())
        }
        return Optional.empty()
    }

    private fun setUnlistedFlag(resourceId: ResourceId, unlisted: Boolean): Optional<Resource> {
        val result = neo4jProblemRepository.findById(resourceId)
        if (result.isPresent) {
            val problem = result.get()
            problem.unlisted = unlisted
            return Optional.of(neo4jProblemRepository.save(problem).toResource())
        }
        return Optional.empty()
    }

    private fun getProblemsWithFeatured(
        classesList: List<String>,
        problemId: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerProblem>,
        totals: MutableList<Long>,
    ) {
        classesList.forEach { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> {
                    val result = neo4jProblemRepository.findPapersByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "CONTRIBUTION" -> {
                    val result =
                        neo4jProblemRepository.findContributionsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "COMPARISON" -> {
                    val result =
                        neo4jProblemRepository.findComparisonsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "SMARTREVIEWPUBLISHED" -> {
                    val result =
                        neo4jProblemRepository.findSmartReviewsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "LITERATURELISTPUBLISHED" -> {
                    val result =
                        neo4jProblemRepository.findLiteratureListsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "VISUALIZATION" -> {
                    val result =
                        neo4jProblemRepository.findVisualizationsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                else -> {
                    val result =
                        neo4jProblemRepository.findResearchFieldsByProblems(problemId, featured, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
            }
        }
    }

    private fun getProblemsWithoutFeatured(
        classesList: List<String>,
        problemId: ResourceId,
        unlisted: Boolean,
        pageable: Pageable,
        resultList: MutableList<DetailsPerProblem>,
        totals: MutableList<Long>,
    ) {
        classesList.forEach { classType ->
            when (classType.uppercase(Locale.getDefault())) {
                "PAPER" -> {
                    val result = neo4jProblemRepository.findPapersByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "CONTRIBUTION" -> {
                    val result = neo4jProblemRepository.findContributionsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "COMPARISON" -> {
                    val result = neo4jProblemRepository.findComparisonsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "SMARTREVIEWPUBLISHED" -> {
                    val result = neo4jProblemRepository.findSmartReviewsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "LITERATURELISTPUBLISHED" -> {
                    val result = neo4jProblemRepository.findLiteratureListsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                "VISUALIZATION" -> {
                    val result = neo4jProblemRepository.findVisualizationsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
                else -> {
                    val result = neo4jProblemRepository.findResearchFieldsByProblems(problemId, unlisted, pageable)
                    resultList.addAll(result.content)
                    totals += result.totalElements
                }
            }
        }
    }
}
