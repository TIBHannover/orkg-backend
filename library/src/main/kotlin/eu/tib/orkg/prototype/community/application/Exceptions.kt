package eu.tib.orkg.prototype.community.application

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ObservatoryNotFound(id: ObservatoryId) : RuntimeException("""Observatory "$id" not found""")

@ResponseStatus(HttpStatus.NOT_FOUND)
class ObservatoryURLNotFound(id: String) : RuntimeException("""Observatory "$id" not found""")
