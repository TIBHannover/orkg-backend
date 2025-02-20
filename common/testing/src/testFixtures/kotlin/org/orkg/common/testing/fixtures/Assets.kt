package org.orkg.common.testing.fixtures

import org.springframework.core.io.ClassPathResource
import java.net.URI

object Assets {
    fun requestJson(name: String): String = fileContents("requests/$name.json").decodeToString()

    fun responseJson(name: String): String = fileContents("responses/$name.json").decodeToString()

    fun png(name: String): ByteArray = fileContents("images/$name.png")

    private fun fileContents(name: String): ByteArray =
        ClassPathResource(URI.create("classpath:/assets/$name").path).inputStream.use { it.readBytes() }
}
