package org.orkg.common

import org.springframework.data.domain.PageRequest

object PageRequests {
    val ALL: PageRequest = PageRequest.of(0, Int.MAX_VALUE)
    val SINGLE: PageRequest = PageRequest.of(0, 1)
}
