package eu.tib.orkg.prototype.community.domain.model

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import eu.tib.orkg.prototype.community.application.json.ConferenceSeriesIdDeserializer
import eu.tib.orkg.prototype.community.application.json.ConferenceSeriesIdSerializer
import java.util.UUID

@JsonDeserialize(using = ConferenceSeriesIdDeserializer::class)
@JsonSerialize(using = ConferenceSeriesIdSerializer::class)
data class ConferenceSeriesId(val value: UUID) {

    constructor(s: String) : this(UUID.fromString(s))
    override fun toString() = value.toString()
}
