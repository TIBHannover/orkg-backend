package org.orkg.testing.acceptancetests.setup

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.orkg.testing.dsl.junit.DslTestCase

@Tag("setup")
class RegistrationTest : DslTestCase() {
    @BeforeEach
    fun setup() {
        registrationAPI.createUser(name = "setup")
    }

    @Test
    fun `can register a user`() = Unit

    @Test
    fun `user can log in`() {
        publicAPI.login("setup")
    }
}
