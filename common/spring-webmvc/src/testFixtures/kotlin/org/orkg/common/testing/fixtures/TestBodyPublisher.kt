package org.orkg.common.testing.fixtures

import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublisher
import java.nio.ByteBuffer
import java.util.concurrent.Flow

/**
 * Testing class that enables easily accessible body contents
 */
data class TestBodyPublisher(val content: String) : BodyPublisher {
    private val delegate: BodyPublisher = HttpRequest.BodyPublishers.ofString(content)

    override fun subscribe(subscriber: Flow.Subscriber<in ByteBuffer>?) = delegate.subscribe(subscriber)

    override fun contentLength(): Long = delegate.contentLength()
}
