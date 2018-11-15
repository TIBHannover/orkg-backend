package eu.tib.orkg.prototype.statements.application

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFound : RuntimeException()

@ResponseStatus(HttpStatus.NOT_FOUND)
class LiteralNotFound : RuntimeException()
