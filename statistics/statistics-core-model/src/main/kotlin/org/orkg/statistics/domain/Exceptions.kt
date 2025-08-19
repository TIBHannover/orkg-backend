package org.orkg.statistics.domain

import org.orkg.common.exceptions.SimpleMessageException
import org.springframework.http.HttpStatus

class GroupNotFound(id: String) :
    SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Group "$id" not found.""",
        properties = mapOf("group_name" to id)
    )

class MetricNotFound(
    group: String,
    name: String,
) : SimpleMessageException(
        HttpStatus.NOT_FOUND,
        """Metric "$group-$name" not found.""",
        properties = mapOf(
            "group_name" to group,
            "metric_name" to name,
        )
    )

class TooManyParameterValues(name: String) :
    SimpleMessageException(
        HttpStatus.BAD_REQUEST,
        """Too many values for parameter "$name".""",
        properties = mapOf("parameter_name" to name)
    )
