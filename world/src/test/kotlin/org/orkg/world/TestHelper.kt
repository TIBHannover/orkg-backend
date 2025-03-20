package org.orkg.world

import kotlin.random.Random

internal fun Random.produceValues(): List<Long> = buildList { repeat(100) { add(nextLong()) } }
