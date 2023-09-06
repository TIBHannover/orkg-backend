package eu.tib.orkg.prototype

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import eu.tib.orkg.prototype.auth.api.AuthUseCase
import eu.tib.orkg.prototype.community.api.DummyDataUseCases
import eu.tib.orkg.prototype.community.api.ConferenceSeriesUseCases
import eu.tib.orkg.prototype.community.api.ObservatoryUseCases
import eu.tib.orkg.prototype.community.api.OrganizationUseCases
import eu.tib.orkg.prototype.community.domain.model.ConferenceSeries
import eu.tib.orkg.prototype.community.domain.model.Metadata
import eu.tib.orkg.prototype.community.domain.model.Observatory
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.Organization
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.community.domain.model.PeerReviewType
import eu.tib.orkg.prototype.community.spi.ObservatoryRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.application.BadPeerReviewType
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import net.datafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
@Profile("datagen")
class PostgresDummyDataSetup(
    private val userService: AuthUseCase,
    private val observatoryService: ObservatoryUseCases,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationService: OrganizationUseCases,
    private val conferenceSeriesService: ConferenceSeriesUseCases,
    private val resourceService: ResourceUseCases,
    private val objectMapper: ObjectMapper,
    private val dummyDataUseCases: DummyDataUseCases,
) : ApplicationRunner {

    class ObservatoryDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<Observatory?>(vc) {
        override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Observatory {
            val node: JsonNode = jp.codec.readTree(jp)
            return Observatory(
                id = ObservatoryId(UUID.fromString(node["id"].asText())),
                name = node["name"].asText(),
                description = node["description"].asText(),
                researchField = node["research_field"]["id"].takeIf { !it.isNull }?.let { ThingId(it.asText()) },
                members = node["members"].map { ContributorId(UUID.fromString(it.asText())) }.toSet(),
                organizationIds = node["organization_ids"].map { OrganizationId(UUID.fromString(it.asText())) }.toSet(),
                displayId = node["display_id"].asText()
            )
        }
    }

    init {
        objectMapper.registerModule(
            SimpleModule().addDeserializer(Observatory::class.java, ObservatoryDeserializer())
        )
    }

    @Autowired
    private lateinit var context: ConfigurableApplicationContext

    override fun run(args: ApplicationArguments?) {
        val users = findUserIds()
        generateUsers(users)
        val organizations = fetchOrganizations() + fetchConferences()
        generateOrganizations(organizations)
        val observatories = mutableListOf<Observatory>()
        fetchObservatories(observatories::add)
        generateObservatories(observatories)
        updateUserAffiliation(organizations, observatories)
        fetchConferenceSeries(::putConferenceSeries)
        context.close()
    }

    private fun findUserIds() =
        resourceService.findAllContributorIds(PageRequest.of(0, Int.MAX_VALUE))
            .map(ContributorId::value)
            .content

    private fun generateUsers(users: List<UUID>) {
        users.forEach {
            if (userService.findById(it).isEmpty) {
                registerFakeUser(it)
            }
        }

        // Add Example User with a special UUID
        if (userService.findByEmail("user@example.org").isEmpty) {
            val uuid = UUID.fromString("00000000-1111-2222-3333-444444444444")
            userService.registerUser("user@example.org", "secret", "Example User", uuid)
        }
    }

    private fun fetchOrganizations(url: String = "https://orkg.org/api/organizations/"): List<Organization> {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return objectMapper.readValue(response.body(), object : TypeReference<List<Organization>>() {})
    }

    private fun fetchConferences(): List<Organization> =
        fetchOrganizations("https://orkg.org/api/organizations/conferences")

    private fun generateOrganizations(organizations: List<Organization>) {
        organizations.forEach { organization ->
            organizationService.findById(organization.id!!).ifPresentOrElse({
                organizationService.updateOrganization(organization)
            }, {
                organizationService.create(
                    id = organization.id,
                    organizationName = organization.name!!,
                    createdBy = organization.createdBy!!,
                    url = organization.homepage!!,
                    displayId = organization.displayId!!,
                    type = organization.type!!,
                    logoId = null
                )
            })
        }
        // Ignore images as they are stored locally and depend on controller logic
    }

    private fun fetchObservatories(action: (Observatory) -> Unit) =
        fetchPaged(Observatory::class.java, action) { page ->
            "https://orkg.org/api/observatories/?page=$page&size=50"
        }

    private fun fetchConferenceSeries(action: (ConferenceSeries) -> Unit) =
        fetchPaged(ConferenceSeries::class.java, action) { page ->
            "https://orkg.org/api/conference-series/?page=$page&size=50"
        }

    private fun generateObservatories(observatories: List<Observatory>): List<Observatory> {
        observatories.forEach {
            val observatory = observatoryService.findById(it.id).orElseGet {
                observatoryRepository.save(it.copy(members = emptySet()))
                observatoryService.findById(it.id).get()
            }

            for (organizationId in it.organizationIds) {
                if (organizationId !in observatory.organizationIds) {
                    // There should exist a better method for adding organizations to an observatory but this is all we got
                    observatoryService.addOrganization(observatory.id, organizationId)
                }
            }
        }

        return observatories
    }

    private fun updateUserAffiliation(organizations: List<Organization>, observatories: List<Observatory>) {
        val userAffiliation = mutableMapOf<UUID, Pair<OrganizationId?, ObservatoryId?>>()
        organizations.associateWith { fetchOrganizationContributors(it.id!!) }.forEach {
            for (contributor in it.value) {
                userAffiliation.compute(contributor.id.value) { _, value ->
                    it.key.id to value?.second
                }
            }
        }
        observatories.associateWith { fetchObservatoryContributors(it.id) }.forEach {
            for (contributor in it.value) {
                userAffiliation.compute(contributor.id.value) { _, value ->
                    value?.first to it.key.id
                }
            }
        }
        userAffiliation.entries.forEach {
            if (userService.findById(it.key).isEmpty) {
                registerFakeUser(it.key)
            }
            dummyDataUseCases.updateOrganizationAndObservatory(it.key, it.value.first?.value, it.value.second?.value)
        }
    }

    private fun putConferenceSeries(conferenceSeries: ConferenceSeries) {
        if (conferenceSeriesService.findById(conferenceSeries.id).isEmpty) {
            conferenceSeriesService.create(
                id = conferenceSeries.id,
                organizationId = conferenceSeries.organizationId,
                name = conferenceSeries.name,
                url = conferenceSeries.homepage,
                displayId = conferenceSeries.displayId,
                metadata = Metadata(
                    startDate = conferenceSeries.metadata.startDate,
                    reviewType = PeerReviewType.fromOrNull(conferenceSeries.metadata.reviewType.name)
                        ?: throw BadPeerReviewType(conferenceSeries.metadata.reviewType.name),
                )
            )
        }
    }

    private fun registerFakeUser(uuid: UUID) {
        val faker = Faker(Random(uuid.hashCode().toLong()))
        val name = faker.name().fullName()
        val email = name.lowercase()
            .replace(Regex("""[^a-zA-Z0-9]"""), ".") // replace illegal chars with dots
            .replace(Regex("""\.+"""), ".") // replace multiple repeating dots with one
            .replace(Regex("""\.$"""), "") // remove trailing dots
        userService.registerUser("""$email@example.org""", """$uuid""", name, uuid)
    }

    private fun fetchOrganizationContributors(id: OrganizationId): List<Contributor> {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://orkg.org/api/organizations/$id/users"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return objectMapper.readValue(response.body(), object : TypeReference<List<Contributor>>() {})
    }

    private fun fetchObservatoryContributors(id: ObservatoryId): List<Contributor> =
        mutableListOf<Contributor>().apply { fetchObservatoryContributors(id, this::add) }

    private fun fetchObservatoryContributors(id: ObservatoryId, action: (Contributor) -> Unit) =
        fetchPaged(Contributor::class.java, action) { page -> "https://orkg.org/api/observatories/$id/users?page=$page&size=50" }

    private fun <T> fetchPaged(`class`: Class<T>, action: (T) -> Unit, url: (Int) -> String) {
        val client = HttpClient.newBuilder().build()
        var page = 0
        while (true) {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url(page)))
                .build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val pageResponse = objectMapper.readTree(response.body()) // We cant parse Page or PageImpl without custom deserializers
            pageResponse.path("content")
                .map { objectMapper.treeToValue(it, `class`) }
                .forEach(action)
            if (pageResponse.path("last").asBoolean(true)) return
            page++
        }
    }
}
