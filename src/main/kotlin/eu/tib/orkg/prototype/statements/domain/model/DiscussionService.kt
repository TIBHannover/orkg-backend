package eu.tib.orkg.prototype.statements.domain.model
import eu.tib.orkg.prototype.statements.application.DiscussionController
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Optional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DiscussionService() {
    @Value("\${orkg.discourse.url}")
    var discourseUrl: String? = null

    @Value("\${orkg.discourse.apiKey}")
    var discourseApiKey: String? = null

    @Value("\${orkg.discourse.userName}")
    var discourseUserName: String? = null

    fun createDiscussionTopic(topic: DiscussionController.CreateTopicRequest): Optional<String> {
        return try {
            val httpConnection = prepareHttpCall("$discourseUrl/posts.json", "POST")
            httpConnection.outputStream.write("""{
                "title": "${topic.title}",
                "raw": "${topic.raw}"
            }""".toByteArray())

            val responseBody = BufferedReader(InputStreamReader(httpConnection.inputStream, "utf-8"))
                .readLines()
                .joinToString("\n", transform = String::trim)
            return Optional.of(responseBody)
        } catch (e: Exception) {
            Optional.of(e.message.toString())
        }
    }

    fun findObservatoryDiscussion(id: String): Optional<String> {
        return try {
            val httpConnection = prepareHttpCall("$discourseUrl/t/$id.json", "GET")
            val responseBody = BufferedReader(InputStreamReader(httpConnection.inputStream, "utf-8"))
                .readLines()
                .joinToString("\n", transform = String::trim)
            return Optional.of(responseBody)
        } catch (e: Exception) {
            Optional.of(e.message.toString())
        }
    }

    private fun prepareHttpCall(url: String, method: String): HttpURLConnection {
        val con = URL(url).openConnection() as HttpURLConnection
        con.requestMethod = method
        con.setRequestProperty("Content-Type", "application/json")
        con.setRequestProperty("Api-Key", "$discourseApiKey")
        con.setRequestProperty("Api-Username", "$discourseUserName")
        con.doOutput = true
        return con
    }
}
