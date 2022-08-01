package eu.tib.orkg.prototype.statements.domain.model

sealed interface Thing {
    val thingId: ThingId
}

@JvmInline
value class ThingId(val value: String)
