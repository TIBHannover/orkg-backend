package org.orkg.testing.drivers.registration.email

import com.fasterxml.jackson.annotation.JsonProperty
import it.skrape.core.htmlDocument
import it.skrape.matchers.toBePresentExactlyOnce
import it.skrape.selects.attribute
import it.skrape.selects.html5.a
import org.orkg.world.Environment
import tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI.create
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import kotlin.text.Charsets.UTF_8

class MailBox(
    private val email: String,
    mailServer: String = "http://localhost:8025",
) {
    // "Constants"
    private val mailAPI = "$mailServer/api/v1"
    private val messageEndpoint = "$mailAPI/message"
    private val messagesEndpoint = "$mailAPI/messages"

    // Members
    private val mapper = jacksonObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val client = HttpClient.newBuilder().followRedirects(NORMAL).build()
    private val requestBuilder = HttpRequest.newBuilder()
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")

    fun findActivationEmail(): String {
        val encodedQuery = URLEncoder.encode("to:$email subject:\"Verify email\" is:unread", UTF_8)
        val request = requestBuilder.copy().uri(create("$mailAPI/search?query=$encodedQuery")).GET().build()
        val response = client.send(request, BodyHandlers.ofString())
            .run { mapper.readValue(body(), MessageListResponse::class.java) }
        require(response.messages.isNotEmpty()) { "Expected at least one email message, but found none!" }
        return response.messages.single().id
    }

    fun getActivationPageLink(messageId: String): String {
        val entity = "$messageEndpoint/$messageId"
        val request = requestBuilder.copy().GET().uri(create(entity)).build()
        val message = client.send(request, BodyHandlers.ofString())
            .run { mapper.readValue<SingleMessageResponse>(body(), SingleMessageResponse::class.java) }
        return htmlDocument(message.html) {
            a {
                withAttribute = "rel" to "nofollow"
                findAll {
                    // There should be only one link that is marked as to not be followed by the mail client.
                    // This is the activation link
                    toBePresentExactlyOnce
                    attribute("href")
                }
            }
        }
    }

    fun markAsRead(messageId: String) {
        val payload = """{"IDs":["$messageId"],"Read":true}"""
        val request = requestBuilder.copy().uri(create(messagesEndpoint)).PUT(BodyPublishers.ofString(payload)).build()
        val response = client.send(request, BodyHandlers.discarding())
        assert(response.statusCode() == 200) { "Unable to mark message <$messageId> as read!" }
    }

    fun delete(messageId: String) {
        val payload = """{"IDs":["$messageId"]}"""
        // Applause for the java.net API. Not.
        val request = requestBuilder.uri(create(messagesEndpoint)).method("DELETE", BodyPublishers.ofString(payload)).build()
        val response = client.send(request, BodyHandlers.discarding())
        assert(response.statusCode() == 200) { "Unable to delete message <$messageId> as read" }
    }

    companion object {
        fun from(environment: Environment, email: String): MailBox {
            val hostname = environment["MAILSERVER_HOST"] ?: error("Mail server hostname not set in environment!")
            val port = environment["MAILSERVER_TCP_8025"] ?: error("Mail server port not set in environment!")
            return MailBox(email, "http://$hostname:$port")
        }
    }

    // Not all of this information is needed; it exists because I found a better API later.
    private data class MessageListResponse(
        val messages: List<Message>,
    ) {
        data class Message(
            @get:JsonProperty("ID")
            val id: String,
            @get:JsonProperty("MessageID")
            val messageID: String,
            @get:JsonProperty("Subject")
            val subject: String,
            @get:JsonProperty("From")
            val from: MailAddress,
            @get:JsonProperty("To")
            val to: List<MailAddress>,
            @get:JsonProperty("Created")
            val created: String,
        ) {
            data class MailAddress(
                @get:JsonProperty("Address")
                val address: String,
                @get:JsonProperty("Name")
                val name: String,
            )
        }
    }

    // Returns only what we need, nothing more.
    private data class SingleMessageResponse(
        @get:JsonProperty("HTML")
        val html: String,
    )
}
