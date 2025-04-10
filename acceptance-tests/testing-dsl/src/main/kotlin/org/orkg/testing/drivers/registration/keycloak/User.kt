package org.orkg.testing.drivers.registration.keycloak

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.Cookie
import it.skrape.fetcher.Method
import it.skrape.fetcher.extractIt
import it.skrape.fetcher.skrape
import it.skrape.matchers.toBe
import it.skrape.matchers.toBePresentExactlyOnce
import it.skrape.matchers.toContain
import it.skrape.selects.Doc
import it.skrape.selects.ElementNotFoundException
import it.skrape.selects.attribute
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.form
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import it.skrape.selects.text
import org.orkg.testing.drivers.registration.email.MailBox
import java.net.URLEncoder

class User(
    val username: String,
    val displayName: String,
    val email: String,
    val password: String,
    val mailbox: MailBox,
) {
    fun goto(keycloak: Keycloak): KeycloakVisited = KeycloakVisited(FlowState(user = this, keycloak = keycloak, mailbox = mailbox))
}

data class KeycloakVisited(
    var state: FlowState? = null,
) {
    fun visitAccountServicePage(): AccountServicePageVisited = skrape(BrowserFetcher) {
        request {
            url = state?.keycloak?.realmInfo?.accountService ?: stateError("Account service URL")
        }
        extractIt { it ->
            status { code toBe 200 }
            it.state = state!!.apply {
                loginCookies = cookies
            }
            htmlDocument {
                div("#kc-registration") {
                    span {
                        a {
                            findAll {
                                toBePresentExactlyOnce
                                it.registrationPageURL = attribute("href")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper class to capture all "global" state that is valid for the whole session.
 */
data class FlowState(
    val user: User,
    val keycloak: Keycloak,
    val mailbox: MailBox,
    var loginCookies: List<Cookie> = emptyList(),
    var verificationCookies: List<Cookie> = emptyList(),
    var activationMessageId: String? = null,
)

data class AccountServicePageVisited(
    var state: FlowState? = null,
    var registrationPageURL: String? = null,
) {
    fun visitRegistrationPage(): RegistrationPageVisited = skrape(BrowserFetcher) {
        request {
            val baseURL = state?.keycloak?.baseURL ?: stateError("Keycloak base URL")
            val registrationPageURL = registrationPageURL ?: stateError("Registration page URL")
            url = "$baseURL$registrationPageURL"
            cookies = state?.loginCookies?.toKeyValuePairs() ?: stateError("Login cookies")
        }
        extractIt<RegistrationPageVisited> {
            status { code toBe 200 }
            // The "login cookies" are still valid (and the same) as in the previous request
            it.state = this@AccountServicePageVisited.state
            htmlDocument {
                form("#kc-register-form") {
                    findAll {
                        toBePresentExactlyOnce
                        it.submitFormURL = attribute("action")
                    }
                }
            }
        }
    }
}

data class RegistrationPageVisited(
    var state: FlowState? = null,
    var submitFormURL: String? = null,
) {
    fun submitRegistrationForm(): RegistrationFormSubmitted = skrape(BrowserFetcher) {
        request {
            // This is the full URL now, as inserted into the form action
            url = submitFormURL ?: stateError("Submit form URL")
            cookies = state?.loginCookies?.toKeyValuePairs() ?: stateError("Login cookies")
            method = Method.POST
            body {
                val user = this@RegistrationPageVisited.state?.user ?: stateError("User information")
                form {
                    "username" to user.username
                    "password" to user.password
                    "password-confirm" to user.password
                    "displayName" to URLEncoder.encode(user.displayName, Charsets.UTF_8)
                    "email" to user.email
                    // The value for "termsAccepted" was determined from the form and hardcoded.
                    // It is unlikely to change, but could be a potential point for breaking.
                    "termsAccepted" to "on"
                }
            }
        }
        extractIt<RegistrationFormSubmitted> { it ->
            status { code toBe 200 }
            it.state = state
            htmlDocument {
                // We could have redirected to the same page with errors. This also has status code 200, so we need to check.
                // Since the absence of elements does not make sense when scraping, we need to be creative in assertAbsense().
                listOf("password", "password-confirm", "username", "email", "displayName", "termsAccepted").forEach { property ->
                    assertAbsense(property)
                }
                // If we passed this barrier, check if we have the right page. This is to detect errors during submission.
                span {
                    withClass = "kc-feedback-text"
                    findAll {
                        text toContain "You need to verify your email address to activate your account."
                    }
                }
            }
        }
    }
}

data class RegistrationFormSubmitted(
    var state: FlowState? = null,
) {
    fun checkEmail(): EmailChecked {
        val mailbox = state?.mailbox ?: stateError("Mailbox")
        val messageId = mailbox.findActivationEmail()
        val verificationPageURL = mailbox.getActivationPageLink(messageId)
        mailbox.markAsRead(messageId)
        state!!.activationMessageId = messageId
        return EmailChecked(state, verificationPageURL)
    }
}

data class EmailChecked(
    var state: FlowState? = null,
    var verificationPageURL: String? = null,
) {
    fun visitVerificationPage(): VerificationPageVisited = skrape(BrowserFetcher) {
        request {
            url = verificationPageURL ?: stateError("Verification page URL")
        }
        extractIt<VerificationPageVisited> {
            status { code toBe 200 }
            it.state = state!!.apply {
                // Previous cookies are not passed down. We get new cookies here, basically imitating a new tab or session.
                verificationCookies = cookies
            }
            htmlDocument {
                div {
                    withId = "kc-info-message"
                    a {
                        findAll {
                            this.text toContain "Click here to proceed"
                            toBePresentExactlyOnce
                            it.verificationLinkURL = attribute("href")
                        }
                    }
                }
            }
        }
    }
}

data class VerificationPageVisited(
    var state: FlowState? = null,
    var verificationLinkURL: String? = null,
) {
    fun clickVerificationLink(): VerificationLinkClicked = skrape(BrowserFetcher) {
        request {
            url = verificationLinkURL ?: stateError("Verification link URL")
            cookies = state?.verificationCookies?.toKeyValuePairs() ?: stateError("Verification link cookies")
        }
        extractIt<VerificationLinkClicked> { it ->
            status { code toBe 200 }
            it.state = state
            htmlDocument {
                div {
                    withId = "kc-info-message"
                    p {
                        findFirst {
                            text toContain "Your email address has been verified."
                        }
                    }
                }
            }
        }
    }
}

data class VerificationLinkClicked(
    var state: FlowState? = null,
) {
    fun deleteVerificationEmail() {
        val mailbox = state?.mailbox ?: stateError("Mailbox")
        val messageId = state?.activationMessageId ?: stateError("Activation message ID")
        mailbox.delete(messageId)
    }
}

internal fun Doc.assertAbsense(property: String) {
    div("#input-error-container-$property") {
        try {
            // This needs to use this syntax. Element selectors will not work.
            val errorMessage: String = findFirst("div#input-error-$property") {
                span { findFirst { text } }
            }
            error("Property <$property> contains an error in the registration form: <$errorMessage>")
        } catch (_: ElementNotFoundException) {
            // All is well.
        }
    }
}

internal fun List<Cookie>.toKeyValuePairs() = associate { it.name to it.value }

private fun stateError(what: String): Nothing = error("$what not found in global state. Was it set correctly in a previous step?")
