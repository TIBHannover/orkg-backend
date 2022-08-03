package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.statements.application.json.ObservatoryIdDeserializer
import eu.tib.orkg.prototype.statements.application.json.ObservatoryIdSerializer
import java.util.UUID

@JsonDeserialize(using = ObservatoryIdDeserializer::class)
@JsonSerialize(using = ObservatoryIdSerializer::class)
data class ObservatoryId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))

    companion object {
        fun createUnknownObservatory() = ObservatoryId(UUID(0, 0))
    }

    override fun toString() = value.toString()
}
