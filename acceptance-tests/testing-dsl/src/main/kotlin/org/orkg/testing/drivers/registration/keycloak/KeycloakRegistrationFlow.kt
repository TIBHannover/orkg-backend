package org.orkg.testing.drivers.registration.keycloak

import org.orkg.testing.drivers.registration.email.MailBox
import org.orkg.testing.dsl.api.RegistrationData

class KeycloakRegistrationFlow(
    private val keycloak: Keycloak,
    private val mailBox: MailBox,
) {
    /**
     * Registers a user, following the exact flow of events that real users go through.
     *
     * This also includes checking mails and clicking the verification link that was sent to the user.
     * Once the activation link is determined, the email is marked as read.
     * If the activation was successful, it is removed from the mailbox.
     */
    fun register(username: String, displayName: String, email: String, password: String) {
        val user = User(username, displayName, email, password, mailBox)
        user
            .goto(keycloak)
            .visitAccountServicePage()
            .visitRegistrationPage()
            .submitRegistrationForm()
            .checkEmail()
            .visitVerificationPage()
            .clickVerificationLink()
            .deleteVerificationEmail()
    }

    fun register(user: RegistrationData) = register(user.username, user.displayName, user.email, user.password)
}
