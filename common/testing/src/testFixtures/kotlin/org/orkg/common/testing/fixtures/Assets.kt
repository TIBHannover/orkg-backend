package org.orkg.common.testing.fixtures

import org.springframework.core.io.ClassPathResource
import java.net.URI

object Assets {
    fun requestJson(name: String): String = fileContents("requests/$name.json").decodeToString()

    fun responseJson(name: String): String = fileContents("responses/$name.json").decodeToString()

    fun representationJson(name: String): String = fileContents("representations/$name.json").decodeToString()

    fun modelJson(name: String): String = fileContents("models/$name.json").decodeToString()

    fun emailText(name: String): String = fileContents("email/text/$name.txt").decodeToString()

    fun emailHtml(name: String): String = fileContents("email/html/$name.html").decodeToString()

    fun png(name: String): ByteArray = fileContents("images/$name.png")

    fun csv(name: String): String = fileContents("csvs/$name.csv").decodeToString()

    private fun fileContents(name: String): ByteArray =
        ClassPathResource(URI.create("classpath:/assets/$name").path).inputStream.use { it.readBytes() }
}
