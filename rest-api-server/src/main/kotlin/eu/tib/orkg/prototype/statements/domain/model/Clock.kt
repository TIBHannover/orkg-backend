package eu.tib.orkg.prototype.statements.domain.model

import java.time.OffsetDateTime
import org.springframework.stereotype.Component

interface Clock {
    fun now(): OffsetDateTime
}

@Component
class SystemClock : Clock {
    override fun now(): OffsetDateTime = OffsetDateTime.now()
}
