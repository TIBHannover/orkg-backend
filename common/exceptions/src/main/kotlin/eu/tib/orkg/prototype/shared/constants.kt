package eu.tib.orkg.prototype.shared

import org.springframework.data.domain.PageRequest

object PageRequests {
    val ALL: PageRequest = PageRequest.of(0, Int.MAX_VALUE)
}
