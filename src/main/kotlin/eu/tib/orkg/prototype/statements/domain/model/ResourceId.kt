package eu.tib.orkg.prototype.statements.domain.model

import com.fasterxml.jackson.databind.annotation.*
import eu.tib.orkg.prototype.statements.application.json.*

@JsonDeserialize(using = ResourceIdDeserializer::class)
@JsonSerialize(using = ResourceIdSerializer::class)
data class ResourceId(private val value: Long) :
    Comparable<ResourceId> {

    init {
        require(value >= 0) { "Value must be greater than or equal to zero" }
    }

    @Deprecated("IDs of type String are no longer supported. Use Long instead.")
    constructor(value: String) : this(value.toLong(16))

    override fun toString() = "$value"

    override fun compareTo(other: ResourceId) =
        value.compareTo(other.value)
}
