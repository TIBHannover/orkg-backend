package org.orkg.testing.dsl

import org.orkg.world.Environment

internal fun Environment.require(variable: String): String = get(variable) ?: error("Environment variable <$variable> is not set!")
