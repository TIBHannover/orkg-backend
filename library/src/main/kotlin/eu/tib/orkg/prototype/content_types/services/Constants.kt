package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.statements.domain.model.ThingId
import org.springframework.data.domain.PageRequest

object Predicates {
    val hasResearchField = ThingId("P30")
    val hasDOI = ThingId("P26")
    val hasAuthor = ThingId("P27")
    val monthPublished = ThingId("P28")
    val yearPublished = ThingId("P29")
    val hasVenue = ThingId("HAS_VENUE")
    val hasURL = ThingId("url")
}

object Classes {
    val paper = ThingId("Paper")
    val paperDeleted = ThingId("PaperDeleted")
}

object PageRequests {
    val ALL: PageRequest = PageRequest.of(0, Int.MAX_VALUE)
}
