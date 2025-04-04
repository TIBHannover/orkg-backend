package org.orkg.graph.domain

import org.orkg.graph.input.UpdateClassUseCase
import org.orkg.graph.input.UpdateLiteralUseCase
import org.orkg.graph.input.UpdatePredicateUseCase
import org.orkg.graph.input.UpdateResourceUseCase
import org.orkg.graph.input.UpdateStatementUseCase
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ThingRepository

fun UpdateResourceUseCase.UpdateCommand.hasNoContents(): Boolean =
    label == null &&
        classes == null &&
        observatoryId == null &&
        organizationId == null &&
        extractionMethod == null &&
        modifiable == null &&
        visibility == null &&
        verified == null

fun Resource.apply(command: UpdateResourceUseCase.UpdateCommand): Resource =
    copy(
        label = command.label ?: label,
        classes = command.classes ?: classes,
        observatoryId = command.observatoryId ?: observatoryId,
        organizationId = command.organizationId ?: organizationId,
        extractionMethod = command.extractionMethod ?: extractionMethod,
        modifiable = command.modifiable ?: modifiable,
        visibility = command.visibility ?: visibility,
        verified = command.verified ?: verified,
        unlistedBy = when {
            command.visibility == Visibility.UNLISTED && visibility != Visibility.UNLISTED -> command.contributorId
            command.visibility != Visibility.UNLISTED && visibility == Visibility.UNLISTED -> null
            else -> unlistedBy
        }
    )

fun UpdateStatementUseCase.UpdateCommand.hasNoContents(): Boolean =
    subjectId == null && predicateId == null && objectId == null && modifiable == null

fun GeneralStatement.apply(
    command: UpdateStatementUseCase.UpdateCommand,
    thingRepository: ThingRepository,
    predicateRepository: PredicateRepository,
    subjectValidator: (Thing) -> Unit = {},
    predicateValidator: (Predicate) -> Unit = {},
    objectValidator: (Thing) -> Unit = {},
): GeneralStatement = copy(
    subject = command.subjectId?.takeIf { id -> id != subject.id }
        ?.let { id -> thingRepository.findById(id).orElseThrow { StatementSubjectNotFound(id) } }
        ?.also(subjectValidator)
        ?: subject,
    predicate = command.predicateId?.takeIf { id -> id != predicate.id }
        ?.let { id -> predicateRepository.findById(id).orElseThrow { StatementPredicateNotFound(id) } }
        ?.also(predicateValidator)
        ?: predicate,
    `object` = command.objectId?.takeIf { id -> id != `object`.id }
        ?.let { id -> thingRepository.findById(id).orElseThrow { StatementObjectNotFound(id) } }
        ?.also(objectValidator)
        ?: `object`,
    modifiable = command.modifiable ?: modifiable,
)

fun UpdatePredicateUseCase.UpdateCommand.hasNoContents(): Boolean =
    label == null && modifiable == null

fun Predicate.apply(command: UpdatePredicateUseCase.UpdateCommand): Predicate =
    copy(
        label = command.label ?: label,
        modifiable = command.modifiable ?: modifiable,
    )

fun UpdateLiteralUseCase.UpdateCommand.hasNoContents(): Boolean =
    label == null && datatype == null && modifiable == null

fun Literal.apply(command: UpdateLiteralUseCase.UpdateCommand): Literal =
    copy(
        label = command.label ?: label,
        datatype = command.datatype ?: datatype,
        modifiable = command.modifiable ?: modifiable,
    )

fun UpdateClassUseCase.UpdateCommand.hasNoContents(): Boolean =
    label == null && uri == null && modifiable == null

fun Class.apply(command: UpdateClassUseCase.UpdateCommand): Class =
    copy(
        label = command.label ?: label,
        uri = command.uri ?: uri,
        modifiable = command.modifiable ?: modifiable,
    )

fun Class.apply(command: UpdateClassUseCase.ReplaceCommand): Class =
    copy(
        label = command.label,
        uri = command.uri,
        modifiable = command.modifiable ?: modifiable,
    )
