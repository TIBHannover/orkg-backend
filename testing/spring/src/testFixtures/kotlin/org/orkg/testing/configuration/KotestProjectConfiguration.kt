package org.orkg.testing.configuration

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension

class KotestProjectConfiguration : AbstractProjectConfig() {
    override val extensions: List<Extension> = listOf(SpringExtension())
}
