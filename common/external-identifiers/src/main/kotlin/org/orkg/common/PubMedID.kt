package org.orkg.common

internal val VALID_PUB_MED_ID_REGEX = Regex("""^(?:[1-4]\d{7}|[1-9]\d{0,6})$""")
internal val VALID_PUB_MED_ID_URI_REGEX = Regex("""^https?://pubmed\.ncbi\.nlm\.nih\.gov/([1-4]\d{7}|[1-9]\d{0,6})/$""")

@JvmInline
value class PubMedID private constructor(override val value: String) : IdentifierValue {
    companion object : IdentifierValueFactory<PubMedID>(
        fn = ::PubMedID,
        validationRegex = VALID_PUB_MED_ID_REGEX,
        uriValidationRegex = VALID_PUB_MED_ID_URI_REGEX,
    )

    override val uri: String get() = "https://pubmed.ncbi.nlm.nih.gov/$value/"
}
