package org.orkg

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import net.datafaker.Faker
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.community.domain.ConferenceSeries
import org.orkg.community.domain.Contributor
import org.orkg.community.domain.InvalidPeerReviewType
import org.orkg.community.domain.Metadata
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.Organization
import org.orkg.community.domain.PeerReviewType
import org.orkg.community.domain.internal.SHA256
import org.orkg.community.input.ConferenceSeriesUseCases
import org.orkg.community.input.DummyDataUseCases
import org.orkg.community.input.ObservatoryUseCases
import org.orkg.community.input.OrganizationUseCases
import org.orkg.community.input.UpdateObservatoryUseCase
import org.orkg.community.input.UpdateOrganizationUseCases
import org.orkg.community.output.ContributorRepository
import org.orkg.community.output.ObservatoryRepository
import org.orkg.graph.input.ResourceUseCases
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.OffsetDateTime
import java.util.Random

@Component
@Profile("datagen")
class PostgresDummyDataSetup(
    private val observatoryService: ObservatoryUseCases,
    private val observatoryRepository: ObservatoryRepository,
    private val organizationService: OrganizationUseCases,
    private val conferenceSeriesService: ConferenceSeriesUseCases,
    private val resourceService: ResourceUseCases,
    private val objectMapper: ObjectMapper,
    private val dummyDataUseCases: DummyDataUseCases,
    private val contributorRepository: ContributorRepository,
    private val httpClient: HttpClient,
    @param:Value("\${orkg.http.user-agent}")
    private val userAgent: String,
) : ApplicationRunner {
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
        resourceService.findAllContributorIds(PageRequest.of(0, Int.MAX_VALUE)).content

    private fun generateUsers(users: List<ContributorId>) {
        users.forEach {
            if (contributorRepository.findById(it).isEmpty) {
                registerFakeUser(it)
            }
        }

        // Add Example User with a special UUID
        val exampleUserId = ContributorId("00000000-1111-2222-3333-444444444444")
        if (contributorRepository.findById(exampleUserId).isEmpty) {
            contributorRepository.save(
                Contributor(
                    id = exampleUserId,
                    name = "Example User",
                    joinedAt = OffsetDateTime.now(),
                    emailHash = SHA256.fromEmail("user@example.org"),
                    isAdmin = true,
                    isCurator = true
                )
            )
        }
    }

    private fun fetchOrganizations(url: String = "https://orkg.org/api/organizations/"): List<Organization> {
        val request = HttpRequest.newBuilder(URI.create(url))
            .header(USER_AGENT, userAgent)
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return objectMapper.readValue(response.body(), object : TypeReference<List<Organization>>() {})
    }

    private fun fetchConferences(): List<Organization> =
        fetchOrganizations("https://orkg.org/api/organizations/conferences")

    private fun generateOrganizations(organizations: List<Organization>) {
        organizations.forEach { organization ->
            organizationService.findById(organization.id!!).ifPresentOrElse({
                organizationService.update(
                    contributorId = ContributorId.SYSTEM,
                    command = UpdateOrganizationUseCases.UpdateOrganizationCommand(
                        id = it.id!!,
                        name = it.name,
                        url = it.homepage,
                        type = it.type!!,
                        logo = null
                    )
                )
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
            if ((it.organizationIds - observatory.organizationIds).isEmpty()) {
                observatoryService.update(
                    UpdateObservatoryUseCase.UpdateCommand(
                        id = observatory.id,
                        organizations = it.organizationIds + observatory.organizationIds
                    )
                )
            }
        }

        return observatories
    }

    private fun updateUserAffiliation(organizations: List<Organization>, observatories: List<Observatory>) {
        val userAffiliation = mutableMapOf<ContributorId, Pair<OrganizationId?, ObservatoryId?>>()
        organizations.associateWith { fetchOrganizationContributors(it.id!!) }.forEach {
            for (contributor in it.value) {
                userAffiliation.compute(contributor.id) { _, value ->
                    it.key.id to value?.second
                }
            }
        }
        observatories.associateWith { fetchObservatoryContributors(it.id) }.forEach {
            for (contributor in it.value) {
                userAffiliation.compute(contributor.id) { _, value ->
                    value?.first to it.key.id
                }
            }
        }
        userAffiliation.entries.forEach { (key, value) ->
            if (contributorRepository.findById(key).isEmpty) {
                registerFakeUser(key)
            }
            dummyDataUseCases.updateOrganizationAndObservatory(
                contributorId = key,
                organizationId = value.first ?: OrganizationId.UNKNOWN,
                observatoryId = value.second ?: ObservatoryId.UNKNOWN
            )
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
                        ?: throw InvalidPeerReviewType(conferenceSeries.metadata.reviewType.name),
                )
            )
        }
    }

    private fun registerFakeUser(uuid: ContributorId) {
        val faker = Faker(Random(uuid.hashCode().toLong()))
        val name = faker.name().fullName()
        val email = name.lowercase()
            .replace(Regex("""[^a-zA-Z0-9]"""), ".") // replace illegal chars with dots
            .replace(Regex("""\.+"""), ".") // replace multiple repeating dots with one
            .replace(Regex("""\.$"""), "") // remove trailing dots
        contributorRepository.save(
            Contributor(
                id = uuid,
                name = name,
                joinedAt = OffsetDateTime.now(),
                organizationId = OrganizationId.UNKNOWN,
                observatoryId = ObservatoryId.UNKNOWN,
                emailHash = SHA256.fromEmail(email)
            )
        )
    }

    private fun fetchOrganizationContributors(id: OrganizationId): List<Contributor> {
        val request = HttpRequest.newBuilder(URI.create("https://orkg.org/api/organizations/$id/users"))
            .header(USER_AGENT, userAgent)
            .build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return objectMapper.readValue(response.body(), object : TypeReference<List<Contributor>>() {})
    }

    private fun fetchObservatoryContributors(id: ObservatoryId): List<Contributor> =
        mutableListOf<Contributor>().apply { fetchObservatoryContributors(id, this::add) }

    private fun fetchObservatoryContributors(id: ObservatoryId, action: (Contributor) -> Unit) =
        fetchPaged(Contributor::class.java, action) { page -> "https://orkg.org/api/observatories/$id/users?page=$page&size=50" }

    private fun <T> fetchPaged(`class`: Class<T>, action: (T) -> Unit, url: (Int) -> String) {
        var page = 0
        while (true) {
            val request = HttpRequest.newBuilder(URI.create(url(page)))
                .header(USER_AGENT, userAgent)
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            val pageResponse = objectMapper.readTree(response.body()) // We cant parse Page or PageImpl without custom deserializers
            pageResponse.path("content")
                .map { objectMapper.treeToValue(it, `class`) }
                .forEach(action)
            if (pageResponse.path("last").asBoolean(true)) return
            page++
        }
    }
}
