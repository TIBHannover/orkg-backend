package eu.tib.orkg.prototype.statements.domain.model

sealed interface Thing {
    val thingId: ThingId
    val label: String
}

sealed interface ThingId
