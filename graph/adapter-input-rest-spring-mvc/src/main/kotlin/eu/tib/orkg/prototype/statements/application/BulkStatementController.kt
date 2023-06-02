package eu.tib.orkg.prototype.statements.application

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.spring.spi.FeatureFlagService
import eu.tib.orkg.prototype.statements.api.StatementRepresentation
import eu.tib.orkg.prototype.statements.StatementRepresentationAdapter
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.api.UpdateStatementUseCase
import eu.tib.orkg.prototype.statements.domain.model.StatementId
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.TemplateRepository
import java.security.Principal
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.noContent
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/statements", produces = [MediaType.APPLICATION_JSON_VALUE])
class BulkStatementController(
    override val statementService: StatementUseCases,
    override val templateRepository: TemplateRepository,
    override val flags: FeatureFlagService
) : BaseController(), StatementRepresentationAdapter {

    @GetMapping("/subjects")
    fun findBySubjects(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable
    ): List<BulkGetStatementsResponse> =
        resourceIds.map {
            BulkGetStatementsResponse(
                id = it,
                statements = statementService.findAllBySubject(it, pageable).mapToStatementRepresentation()
            )
        }

    @GetMapping("/objects")
    fun findByObjects(
        @RequestParam("ids") resourceIds: List<ThingId>,
        pageable: Pageable
    ): List<BulkGetStatementsResponse> =
        resourceIds.map {
            BulkGetStatementsResponse(
                id = it,
                statements = statementService.findAllByObject(it, pageable).mapToStatementRepresentation()
            )
        }

    @DeleteMapping("/")
    fun delete(
        @RequestParam("ids") statementsIds: Set<StatementId>,
        principal: Principal?
    ): ResponseEntity<Unit> {
        if (principal?.name == null)
            return ResponseEntity(HttpStatus.FORBIDDEN)
        statementService.delete(statementsIds)
        return noContent().build()
    }

    @PutMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun edit(
        @RequestParam("ids") statementsIds: List<StatementId>,
        @RequestBody(required = true) statementEditRequest: BulkStatementEditRequest
    ): Iterable<BulkPutStatementResponse> =
        statementsIds.map {
            statementService.update(
                UpdateStatementUseCase.UpdateCommand(
                    statementId = it,
                    subjectId = statementEditRequest.subjectId,
                    predicateId = statementEditRequest.predicateId,
                    objectId = statementEditRequest.objectId,
                )
            )
            statementService.findById(it).get()
        }
            .mapToStatementRepresentation()
            .map { BulkPutStatementResponse(it.id, it) }
}

data class BulkGetStatementsResponse(
    val id: ThingId,
    val statements: Page<StatementRepresentation>
)

data class BulkPutStatementResponse(
    @JsonProperty("id")
    val statementId: StatementId,
    val statement: StatementRepresentation
)

data class BulkStatementEditRequest(
    @JsonProperty("subject_id")
    val subjectId: ThingId? = null,

    @JsonProperty("predicate_id")
    val predicateId: ThingId? = null,

    @JsonProperty("object_id")
    val objectId: ThingId? = null
)
