package org.orkg.testing.acceptancetests.graph

import org.junit.jupiter.api.Test
import org.orkg.testing.dsl.junit.DslTestCase

class CreatePredicatesTest : DslTestCase() {
    @Test
    fun `can create predicates when logged in`() {
        registrationAPI.createUser("alice")
        publicAPI.login("alice")
        // TODO: implement test
    }
}
