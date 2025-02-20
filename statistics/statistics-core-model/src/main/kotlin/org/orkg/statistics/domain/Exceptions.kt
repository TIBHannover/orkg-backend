package org.orkg.statistics.domain

import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

class GroupNotFound(id: String) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Group "$id" not found."""
    )

class MetricNotFound(
    group: String,
    name: String,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Metric "$group-$name" not found."""
    )
