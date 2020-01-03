package eu.tib.orkg.prototype

import eu.tib.orkg.prototype.statements.application.ResourceController
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

abstract class CorsBaseTest {

    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var controller: ResourceController

    @Autowired
    private lateinit var context: WebApplicationContext

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .dispatchOptions<DefaultMockMvcBuilder>(true)
            .build()
    }

    protected fun allOriginsAllowed(): ResultMatcher =
        header().string("Access-Control-Allow-Origin", "*")

    protected fun allAllowedMethodsPresent(): ResultMatcher =
        header().string(
            "Access-Control-Allow-Methods",
            "OPTIONS,GET,HEAD,POST,PUT,DELETE"
        )
}
