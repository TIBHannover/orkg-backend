package eu.tib.orkg.prototype.statements.application

import eu.tib.orkg.prototype.statements.domain.model.LiteralService
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

@RestController
@RequestMapping("/api/papers/")
@CrossOrigin(origins = ["*"])
class PaperController(private val resourceService: ResourceService, private val literalService: LiteralService,
                      private val predicateService: PredicateService,
                      private val statementWithLiteralService: StatementWithLiteralService,
                      private val statementWithResourceService: StatementWithResourceService){

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@RequestBody paper: CreatePaperRequest, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Resource> {


        return ResponseEntity.accepted().body(resourceService.findById(ResourceId("R10")).get())
    }
}

data class CreatePaperRequest(
    val predicates: List<HashMap<String,String>>?,
    val paper: Paper
)

data class Paper(
    val `@title`: String,
    val values: HashMap<String,List<PaperValue>>?
)

data class PaperValue(
    val `@id`: String?,
    val `@temp`: String?,
    val text: String?,
    val label: String?,
    val values: HashMap<String,List<PaperValue>>?
)
