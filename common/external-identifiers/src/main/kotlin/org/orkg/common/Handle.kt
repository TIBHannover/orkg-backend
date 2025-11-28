package org.orkg.common

internal val VALID_HANDLE_REGEX = Regex("""[0-9a-z]+(\.[0-9a-z]+)*/.{2,}$""")
internal val VALID_HANDLE_URI_REGEX = Regex("""^https?://hdl\.handle\.net/([0-9a-z]+(?:\.[0-9a-z]+)*/.{2,})""")

@JvmInline
value class Handle private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<Handle>(
        fn = ::Handle,
        validationRegex = VALID_HANDLE_REGEX,
        uriValidationRegex = VALID_HANDLE_URI_REGEX,
    )

    override val uri: String get() = "https://hdl.handle.net/$value"
}
