package org.orkg.contenttypes.domain.identifiers

private val VALID_ISBN_REGEX = Regex("""^(97[89]-?(?:\d-?){9}\d|(?:\d-?){9}(?:\d|X))$""")
private val VALID_ISBN_URI_REGEX = Regex("""^urn:ISBN:(97[89]-?(?:\d-?){9}\d|(?:\d-?){9}(?:\d|X))$""")

@JvmInline
value class ISBN private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<ISBN>(
        fn = ::ISBN,
        validationRegex = VALID_ISBN_REGEX,
        uriValidationRegex = VALID_ISBN_URI_REGEX
    )

    override val uri: String get() = "urn:ISBN:$value"
}
