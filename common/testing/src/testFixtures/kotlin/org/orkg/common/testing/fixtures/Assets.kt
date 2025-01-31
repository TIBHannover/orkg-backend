package org.orkg.common.testing.fixtures

import java.net.URI
import org.springframework.core.io.ClassPathResource

object Assets {
    fun requestJson(name: String): String = fileContents("requests/$name.json").decodeToString()

    fun responseJson(name: String): String = fileContents("responses/$name.json").decodeToString()

    private fun fileContents(name: String): ByteArray =
        ClassPathResource(URI.create("classpath:/assets/$name").path).inputStream.use { it.readBytes() }
}
