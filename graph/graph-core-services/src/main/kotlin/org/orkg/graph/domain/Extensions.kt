package org.orkg.graph.domain

import org.orkg.graph.input.UpdateResourceUseCase

fun UpdateResourceUseCase.UpdateCommand.hasNoContents(): Boolean =
    label == null && classes == null && observatoryId == null && organizationId == null &&
        extractionMethod == null && modifiable == null && visibility == null && verified == null

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
