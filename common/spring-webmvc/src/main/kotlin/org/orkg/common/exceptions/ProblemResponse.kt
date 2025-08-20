package org.orkg.common.exceptions

import org.springframework.http.HttpHeaders
import org.springframework.http.ProblemDetail

data class ProblemResponse(
    val httpHeaders: HttpHeaders,
    val problemDetail: ProblemDetail,
)
