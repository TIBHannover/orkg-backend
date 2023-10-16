package eu.tib.orkg.prototype.datacite.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.*
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.net.URI

@JsonTypeName(value = "data")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
data class DataCiteJson(
    val attributes: Attributes,
    val type: String = "dois"
) {
    @JsonInclude(Include.NON_NULL)
    data class Attributes(
        @JsonInclude(Include.NON_NULL)
        val doi: String? = null,
        @JsonInclude(Include.NON_NULL)
        val event: String? = null,
        val creators: List<Creator>,
        val titles: List<Title>,
        val publicationYear: Int?,
        val subjects: List<Subject>,
        val types: Type,
        val relatedIdentifiers: List<RelatedIdentifier>,
        val rightsList: List<Rights>,
        val descriptions: List<Description>,
        val url: URI,
        val language: String = "en",
        val publisher: String = "Open Research Knowledge Graph"
    )

    data class Creator(
        val name: String,
        val nameIdentifiers: List<NameIdentifier>,
        val nameType: String = "Personal"
    )

    data class NameIdentifier(
        val schemeUri: URI,
        val nameIdentifier: String,
        val nameIdentifierScheme: String
    ) {
        companion object {
            fun fromORCID(orcid: String): NameIdentifier =
                NameIdentifier(
                    schemeUri = URI.create("https://orcid.org"),
                    nameIdentifier = "https://orcid.org/$orcid",
                    nameIdentifierScheme = "ORCID"
                )
        }
    }

    data class Title(
        val title: String,
        val lang: String = "en"
    )

    data class Subject(
        val subject: String,
        val lang: String = "en"
    )

    data class Type(
        val resourceType: String,
        val resourceTypeGeneral: String
    )

    data class RelatedIdentifier(
        val relatedIdentifier: String,
        val relatedIdentifierType: String,
        val relationType: String = "References"
    ) {
        companion object {
            fun fromDOI(doi: String): RelatedIdentifier =
                RelatedIdentifier(
                    relatedIdentifier = sanitizeDOI(doi),
                    relatedIdentifierType = "DOI"
                )

            private fun sanitizeDOI(doi: String): String =
                doi.trim().let {
                    if (!doi.matches(Regex("""^https?://(www\.)?doi\.org/.*"""))) {
                        "https://doi.org/$it"
                    } else {
                        it
                    }
                }
        }
    }

    data class Rights(
        val rights: String,
        val rightsUri: URI
    ) {
        companion object {
            val CC_BY_SA_4_0 = Rights(
                rights = "Creative Commons Attribution-ShareAlike 4.0 International License.",
                rightsUri = URI.create("https://creativecommons.org/licenses/by-sa/4.0/")
            )
        }
    }

    data class Description(
        val description: String,
        val descriptionType: String = "Abstract"
    )
}
