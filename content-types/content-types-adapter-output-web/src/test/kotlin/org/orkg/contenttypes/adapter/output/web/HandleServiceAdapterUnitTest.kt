package org.orkg.contenttypes.adapter.output.web

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verify
import net.handle.api.HSAdapter
import net.handle.api.HSAdapterFactory
import net.handle.hdllib.HandleException
import net.handle.hdllib.HandleValue
import org.junit.jupiter.api.Test
import org.orkg.common.exceptions.ServiceUnavailable
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.identifiers.Handle
import org.orkg.contenttypes.output.testing.fixtures.registerHandleCommand

internal class HandleServiceAdapterUnitTest : MockkBaseTest {
    private val prefix: String = "test"
    private val adminHandle: String = "test/ADMIN"
    private val keyIndex: Int = 300
    private val secretKey: String = "secret"
    private val adapter = HandleServiceAdapter(prefix, adminHandle, keyIndex, secretKey)

    @Test
    fun `Creating a handle, returns success`() {
        val command = registerHandleCommand()
        val value1 = HandleValue(1, "TEXT", TERM_IDENTIFIER)
        val value2 = HandleValue(2, "URL", command.url.toString())
        val value3 = HandleValue(3, "TEXT", "ORKG")
        val handle = Handle.of("$prefix/${command.suffix}")

        mockkStatic(HSAdapterFactory::class) {
            // Mock HSAdapter initialization in lazy block
            val hsAdapter = mockk<HSAdapter>()
            every { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) } returns hsAdapter

            every { hsAdapter.createHandleValue(1, any(), any()) } returns value1
            every { hsAdapter.createHandleValue(2, any(), any()) } returns value2
            every { hsAdapter.createHandleValue(3, any(), any()) } returns value3
            every { hsAdapter.createHandle(handle.value, any()) } just runs

            val result = adapter.register(command)
            result shouldBe handle

            verify(exactly = 1) { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) }
            verify(exactly = 3) { hsAdapter.createHandleValue(any(), any(), any()) }
            verify(exactly = 1) { hsAdapter.createHandle(handle.value, any()) }
        }
    }

    @Test
    fun `Creating a handle, when external service returns an error, then an exception is thrown`() {
        val command = registerHandleCommand()
        val value1 = HandleValue(1, "TEXT", TERM_IDENTIFIER)
        val value2 = HandleValue(2, "URL", command.url.toString())
        val value3 = HandleValue(3, "TEXT", "ORKG")
        val handle = Handle.of("$prefix/${command.suffix}")
        val exception = HandleException(HandleException.SERVER_ERROR, "Internal server error")

        mockkStatic(HSAdapterFactory::class) {
            // Mock HSAdapter initialization in lazy block
            val hsAdapter = mockk<HSAdapter>()
            every { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) } returns hsAdapter

            every { hsAdapter.createHandleValue(1, any(), any()) } returns value1
            every { hsAdapter.createHandleValue(2, any(), any()) } returns value2
            every { hsAdapter.createHandleValue(3, any(), any()) } returns value3
            every { hsAdapter.createHandle(handle.value, any()) } throws exception

            shouldThrow<ServiceUnavailable> {
                adapter.register(command)
            }.asClue {
                it.message shouldBe "Service unavailable."
                it.internalMessage shouldBe "Handle service threw an exception."
                it.cause shouldBe exception
            }

            verify(exactly = 1) { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) }
            verify(exactly = 3) { hsAdapter.createHandleValue(any(), any(), any()) }
            verify(exactly = 1) { hsAdapter.createHandle(handle.value, any()) }
        }
    }

    @Test
    fun `Creating a handle, when external service fails to initialize, then an exception is thrown`() {
        val command = registerHandleCommand()
        val exception = HandleException(HandleException.SERVER_ERROR, "Internal server error")

        mockkStatic(HSAdapterFactory::class) {
            // Mock HSAdapter initialization in lazy block
            every { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) } throws exception

            shouldThrow<ServiceUnavailable> {
                adapter.register(command)
            }.asClue {
                it.message shouldBe "Service unavailable."
                it.internalMessage shouldBe "Handle service threw an exception."
                it.cause shouldBe exception
            }

            verify(exactly = 1) { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) }
        }
    }

    @Test
    fun `Deleting a handle, when external service succeeds, then it returns success`() {
        val handle = Handle.of("test/123")

        mockkStatic(HSAdapterFactory::class) {
            // Mock HSAdapter initialization in lazy block
            val hsAdapter = mockk<HSAdapter>()
            every { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) } returns hsAdapter
            every { hsAdapter.deleteHandle(handle.value) } just runs

            adapter.delete(handle)

            verify(exactly = 1) { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) }
            verify(exactly = 1) { hsAdapter.deleteHandle(handle.value) }
        }
    }

    @Test
    fun `Deleting a handle, when handle does not exist, then it returns success`() {
        val handle = Handle.of("test/123")
        val exception = HandleException(HandleException.HANDLE_DOES_NOT_EXIST, "Internal server error")

        mockkStatic(HSAdapterFactory::class) {
            // Mock HSAdapter initialization in lazy block
            val hsAdapter = mockk<HSAdapter>()
            every { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) } returns hsAdapter
            every { hsAdapter.deleteHandle(handle.value) } throws exception

            adapter.delete(handle)

            verify(exactly = 1) { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) }
            verify(exactly = 1) { hsAdapter.deleteHandle(handle.value) }
        }
    }

    @Test
    fun `Deleting a handle, when external service returns an error, then an exception is thrown`() {
        val handle = Handle.of("test/123")
        val exception = HandleException(HandleException.SERVER_ERROR, "Internal server error")

        mockkStatic(HSAdapterFactory::class) {
            // Mock HSAdapter initialization in lazy block
            val hsAdapter = mockk<HSAdapter>()
            every { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) } returns hsAdapter
            every { hsAdapter.deleteHandle(handle.value) } throws exception

            shouldThrow<ServiceUnavailable> {
                adapter.delete(handle)
            }.asClue {
                it.message shouldBe "Service unavailable."
                it.internalMessage shouldBe "Handle service threw an exception."
                it.cause shouldBe exception
            }

            verify(exactly = 1) { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) }
            verify(exactly = 1) { hsAdapter.deleteHandle(handle.value) }
        }
    }

    @Test
    fun `Deleting a handle, when external service fails to initialize, then an exception is thrown`() {
        val exception = HandleException(HandleException.SERVER_ERROR, "Internal server error")

        mockkStatic(HSAdapterFactory::class) {
            // Mock HSAdapter initialization in lazy block
            every { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) } throws exception

            shouldThrow<ServiceUnavailable> {
                adapter.delete(Handle.of("test/123"))
            }.asClue {
                it.message shouldBe "Service unavailable."
                it.internalMessage shouldBe "Handle service threw an exception."
                it.cause shouldBe exception
            }

            verify(exactly = 1) { HSAdapterFactory.newInstance(adminHandle, keyIndex, secretKey.toByteArray()) }
        }
    }
}
