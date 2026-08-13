package org.orkg.widget.domain

import org.orkg.common.exceptions.SimpleMessageException
import org.orkg.common.exceptions.createProblemURI
import org.springframework.http.HttpStatus

class WidgetServiceUnavailable :
    SimpleMessageException(
        HttpStatus.SERVICE_UNAVAILABLE,
        """Widget service is not available.""",
        type = createProblemURI("service_unavailable"),
    )
