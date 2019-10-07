package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.LiteralId
import eu.tib.orkg.prototype.statements.domain.model.LiteralService
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import eu.tib.orkg.prototype.statements.domain.model.PredicateService
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.ResourceService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithLiteralService
import eu.tib.orkg.prototype.statements.domain.model.StatementWithResourceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.util.LinkedList
import java.util.Queue

const val ID_ISA_PREDICATE = "P3"
const val ID_PAPER_RESOURCE = "R10"
const val ID_DOI_PREDICATE = "P26"
const val ID_AUTHOR_PREDICATE = "P27"
const val ID_PUBDATE_MONTH_PREDICATE = "P28"
const val ID_PUBDATE_YEAR_PREDICATE = "P29"
const val ID_RESEARCH_FIELD_PREDICATE = "P30"
const val ID_CONTRIBUTION_PREDICATE = "P31"

@RestController
@RequestMapping("/api/papers/")
@CrossOrigin(origins = ["*"])
class PaperController(
    private val resourceService: ResourceService,
    private val literalService: LiteralService,
    private val predicateService: PredicateService,
    private val statementWithLiteralService: StatementWithLiteralService,
    private val statementWithResourceService: StatementWithResourceService
) {

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@RequestBody paper: CreatePaperRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Resource> {
        return ResponseEntity.accepted().body(insertData(paper))
    }

    fun insertData(paper: CreatePaperRequest): Resource {
        val isAPredicate = predicateService.findById(PredicateId(ID_ISA_PREDICATE)).get().id!!
        val hasDoiPredicate = predicateService.findById(PredicateId(ID_DOI_PREDICATE)).get().id!!
        val hasAuthorPredicate = predicateService.findById(PredicateId(ID_AUTHOR_PREDICATE)).get().id!!
        val publicationMonthPredicate = predicateService.findById(PredicateId(ID_PUBDATE_MONTH_PREDICATE)).get().id!!
        val publicationYearPredicate = predicateService.findById(PredicateId(ID_PUBDATE_YEAR_PREDICATE)).get().id!!
        val researchFieldPredicate = predicateService.findById(PredicateId(ID_RESEARCH_FIELD_PREDICATE)).get().id!!
        val hasContributionPredicate = predicateService.findById(PredicateId(ID_CONTRIBUTION_PREDICATE)).get().id!!

        val predicates: HashMap<String, PredicateId> = HashMap()
        if (paper.predicates != null) {
            paper.predicates.forEach {
                val surrogateId = it[it.keys.first()]!!
                val predicateId = predicateService.create(it.keys.first()).id!!
                predicates[surrogateId] = predicateId
            }
        }

        // paper title
        val paperObj = resourceService.create(paper.paper.title)
        val paperId = paperObj.id!!
        statementWithResourceService.create(paperId, isAPredicate, ResourceId(ID_PAPER_RESOURCE))

        // paper doi
        if (paper.paper.doi != null) {
            val paperDoi = literalService.create(paper.paper.doi).id!!
            statementWithLiteralService.create(paperId, hasDoiPredicate, paperDoi)
        }

        // paper authors
        paper.paper.authors!!.forEach {
            val authorId = if (it.id == null) {
                resourceService.create(it.label!!).id!!
            } else {
                ResourceId(it.id)
            }
            statementWithResourceService.create(paperId, hasAuthorPredicate, authorId)
        }

        // paper publication date
        if (paper.paper.publicationMonth != null)
            statementWithLiteralService.create(
                paperId,
                publicationMonthPredicate,
                literalService.create(paper.paper.publicationMonth.toString()).id!!
            )
        if (paper.paper.publicationYear != null)
            statementWithLiteralService.create(
                paperId,
                publicationYearPredicate,
                literalService.create(paper.paper.publicationYear.toString()).id!!
            )

        // paper research field
        statementWithResourceService.create(paperId, researchFieldPredicate, ResourceId(paper.paper.researchField))

        val tempResources: HashMap<String, String> = HashMap()

        // paper contribution data
        paper.paper.contributions!!.forEach {
            if (it.values != null && it.values.count() > 0) {
                val contributionId = resourceService.create(it.name).id!!
                statementWithResourceService.create(paperId, hasContributionPredicate, contributionId)
                val resourceQueue: Queue<TempResource> = LinkedList()
                processContributionData(contributionId, it.values, tempResources, predicates, resourceQueue)
            }
        }
        return paperObj
    }

    fun processContributionData(
        subject: ResourceId,
        data: HashMap<String, List<PaperValue>>,
        tempResources: HashMap<String, String>,
        predicates: HashMap<String, PredicateId>,
        resourceQueue: Queue<TempResource>,
        recursive: Boolean = false
    ) {

        for ((predicate, value) in data) {
            val predicateId = if (predicate.startsWith("_")) {
                predicates[predicate]
            } else {
                PredicateId(predicate) //
            }
            if(!predicateService.findById(predicateId).isPresent)
                throw PredicateNotFound(predicate)
            for (resource in value) {
                when {
                    resource.`@id` != null -> { // Add an existing resource or literal
                        when {
                            resource.`@id`.startsWith("L") -> {
                                statementWithLiteralService.create(subject, predicateId!!, LiteralId(resource.`@id`))
                            }
                            resource.`@id`.startsWith("R") -> {
                                statementWithResourceService.create(subject, predicateId!!, ResourceId(resource.`@id`))
                            }
                            resource.`@id`.startsWith("_") -> {
                                // TODO: Check this
                                // need to check if the resource id exists in the map
                                // o/w put it in a queue and then loop over the queue somehow
                                // the loop needs to take into account that multiple iterations on the queue might be needed
                                if (!tempResources.containsKey(resource.`@id`))
                                    resourceQueue.add(TempResource(subject, predicateId!!, resource.`@id`))
                                else {
                                    val tempId = tempResources[resource.`@id`]
                                    if (tempId!!.startsWith("L")) {
                                        statementWithLiteralService.create(subject, predicateId!!, LiteralId(tempId))
                                    } else {
                                        statementWithResourceService.create(subject, predicateId!!, ResourceId(tempId))
                                    }
                                }
                            }
                        }
                    }
                    resource.label != null -> { // create new resource
                        val newResource = resourceService.create(resource.label).id!!
                        if (resource.`@temp` != null) {
                            tempResources[resource.`@temp`] = newResource.value
                        }
                        statementWithResourceService.create(subject, predicateId!!, newResource)
                        if (resource.values != null) { // TODO: This might be extracted (or should) to the outside of when
                            processContributionData(newResource, resource.values, tempResources, predicates, resourceQueue, true)
                        }
                    }
                    resource.text != null -> { // create new literal
                        val newLiteral = literalService.create(resource.text).id!!
                        if (resource.`@temp` != null) {
                            tempResources[resource.`@temp`] = newLiteral.value
                        }
                        statementWithLiteralService.create(subject, predicateId!!, newLiteral)
                    }
                }
            }
        }
        // Loop until the Queue is empty
        var limit = 50 // this is just to ensure that a user won't add an id that is not there // TODO: check for better solution
        while (!recursive && !resourceQueue.isEmpty() && limit > 0) {
            val temp = resourceQueue.remove()
            limit--
            if (tempResources.containsKey(temp.`object`)) {
                val tempId = tempResources[temp.`object`]
                if (tempId!!.startsWith("L")) {
                    statementWithLiteralService.create(temp.subject, temp.predicate, LiteralId(tempId))
                } else {
                    statementWithResourceService.create(temp.subject, temp.predicate, ResourceId(tempId))
                }
            } else {
                resourceQueue.add(temp)
            }
        }
    }
}

data class CreatePaperRequest(
    val predicates: List<HashMap<String, String>>?,
    val paper: Paper
)

data class Paper(
    val title: String,
    val doi: String?,
    val authors: List<Author>?,
    val publicationMonth: Int?,
    val publicationYear: Int?,
    val researchField: String,
    val contributions: List<Contribution>?
)

data class Author(
    val id: String?,
    val label: String?
)

data class Contribution(
    val name: String,
    val values: HashMap<String, List<PaperValue>>?
)

data class PaperValue(
    val `@id`: String?,
    val `@temp`: String?,
    val text: String?,
    val label: String?,
    val values: HashMap<String, List<PaperValue>>?
)

data class TempResource(
    val subject: ResourceId,
    val predicate: PredicateId,
    val `object`: String
)
