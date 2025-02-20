package org.orkg.graph.domain

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
