package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.ChildClass
import org.orkg.graph.domain.ClassHierarchyEntry
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.InvalidSubclassRelation
import org.orkg.graph.domain.ParentClassAlreadyExists
import org.orkg.graph.input.ClassHierarchyUseCases
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.FixedClockConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

private const val CONTRIBUTOR_ID = "f2d66c90-3cbf-4d4f-951f-0fc470f682c4"

@ContextConfiguration(classes = [ClassHierarchyController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ClassHierarchyController::class])
@DisplayName("Given a Class controller")
internal class ClassHierarchyControllerUnitTest {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var context: WebApplicationContext

    @MockkBean
    private lateinit var classService: ClassUseCases

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var classHierarchyService: ClassHierarchyUseCases

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `Given a parent class id, when searched for its children, then status is 200 OK and children class ids are returned`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val response = ChildClass(createClass(id = childId), 1)

        every { classHierarchyService.findChildren(parentId, any()) } returns PageImpl(listOf(response))

        get("/api/classes/$parentId/children")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].class.id").value(response.`class`.id.value))
            .andExpect(jsonPath("$.content[0].child_count").value(response.childCount))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `Given a parent class id, when service reports the parent class cannot be found while searching for its children, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")

        every { classHierarchyService.findChildren(parentId, any()) } throws ClassNotFound.withThingId(parentId)

        get("/api/classes/$parentId/children")
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a child class id, when searched for its parent, then status is 200 OK and parent class is returned`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classHierarchyService.findParent(childId) } returns Optional.of(createClass(id = parentId))

        get("/api/classes/$childId/parent")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(parentId.value))
    }

    @Test
    fun `Given a child class id, when service reports the child class cannot be found while searching for its parent, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.findParent(childId) } throws ClassNotFound.withThingId(childId)

        get("/api/classes/$childId/parent")
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a child class id, when the parent class cannot be found, then status is 204 NO CONTENT`() {
        val childId = ThingId("child")

        every { classHierarchyService.findParent(childId) } returns Optional.empty()

        get("/api/classes/$childId/parent")
            .andExpect(status().isNoContent)
    }

    @Test
    fun `Given a child class id, when searched for its root, then status is 200 OK and root class is returned`() {
        val rootId = ThingId("root")
        val childId = ThingId("child")

        every { classHierarchyService.findRoot(childId) } returns Optional.of(createClass(id = rootId))

        get("/api/classes/$childId/root")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(rootId.value))
    }

    @Test
    fun `Given a child class id, when service reports the child class cannot be found while searching for its root, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.findRoot(childId) } throws ClassNotFound.withThingId(childId)

        get("/api/classes/$childId/root")
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a child class id, when searched for its root but it has no parent class, then status is 204 NO CONTENT`() {
        val childId = ThingId("child")

        every { classHierarchyService.findRoot(childId) } returns Optional.empty()

        get("/api/classes/$childId/root")
            .andExpect(status().isNoContent)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when a relation is created, then status is 201 CREATED`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every { classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), true) } returns Unit

        mockMvc
            .perform(performPost("/api/classes/$parentId/children", request))
            .andExpect(status().isCreated)
            .andExpect(header().string("location", endsWith("/api/classes/$parentId/children")))

        verify(exactly = 1) { classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), true) }
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when service reports input class does not exist, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), true)
        } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(performPost("/api/classes/$parentId/children", request))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when service reports input classes are the same, then status is 400 BDA REQUEST`() {
        val classId = ThingId("parent")
        val request = mapOf("child_ids" to setOf(classId))

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), classId, setOf(classId), true)
        } throws InvalidSubclassRelation(classId, classId)

        mockMvc
            .perform(performPost("/api/classes/$classId/children", request))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when service reports child class already has a parent class, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parent")
        val otherParentId = ThingId("other")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), true)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        mockMvc
            .perform(performPost("/api/classes/$parentId/children", request))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when a relation is created for a patch request, then status is 200 OK`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every { classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), false) } returns Unit

        mockMvc
            .perform(performPatch("/api/classes/$parentId/children", request))
            .andExpect(status().isOk)
            .andExpect(header().string("location", endsWith("/api/classes/$parentId/children")))

        verify(exactly = 1) {
            classHierarchyService.create(
                ContributorId(CONTRIBUTOR_ID),
                parentId,
                setOf(childId),
                false
            )
        }
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when service reports input class does not exist for a patch request, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), false)
        } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(performPatch("/api/classes/$parentId/children", request))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when service reports input classes are the same for a patch request, then status is 400 BDA REQUEST`() {
        val classId = ThingId("parent")
        val request = mapOf("child_ids" to setOf(classId))

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), classId, setOf(classId), false)
        } throws InvalidSubclassRelation(classId, classId)

        mockMvc
            .perform(performPatch("/api/classes/$classId/children", request))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a parent class id and a child class id, when service reports child class already has a parent class for a patch request, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parent")
        val otherParentId = ThingId("other")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), false)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        mockMvc
            .perform(performPatch("/api/classes/$parentId/children", request))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Given a child class id, when deleting its subclass relation, then status is 200 OK`() {
        val childId = ThingId("child")

        every { classHierarchyService.delete(childId) } returns Unit

        mockMvc
            .perform(MockMvcRequestBuilders.delete("/api/classes/$childId/parent"))
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))

        verify(exactly = 1) { classHierarchyService.delete(childId) }
    }

    @Test
    fun `Given a child class id, when service reports input class does not exist while deleting its subclass relation, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.delete(childId) } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(MockMvcRequestBuilders.delete("/api/classes/$childId/parent"))
            .andExpect(status().isNotFound)

        verify(exactly = 1) { classHierarchyService.delete(childId) }
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a child class id and a parent class id, when a relation is created, then status is 200 OK`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("parent_id" to parentId)

        every { classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), false) } returns Unit

        mockMvc
            .perform(performPost("/api/classes/$childId/parent", request))
            .andExpect(status().isCreated)
            .andExpect(header().string("location", endsWith("/api/classes/$childId/parent")))

        verify(exactly = 1) { classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), false) }
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a child class id and a parent class id, when service reports input class does not exist, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("parent_id" to parentId)

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), false)
        } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(performPost("/api/classes/$childId/parent", request))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a child class id and a parent class id, when service reports input classes are the same, then status is 400 BDA REQUEST`() {
        val classId = ThingId("parent")
        val request = mapOf("parent_id" to classId)

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), classId, setOf(classId), false)
        } throws InvalidSubclassRelation(classId, classId)

        mockMvc
            .perform(performPost("/api/classes/$classId/parent", request))
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = CONTRIBUTOR_ID)
    fun `Given a child class id and a parent class id, when service reports child class already has a parent class, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parent")
        val otherParentId = ThingId("other")
        val childId = ThingId("child")
        val request = mapOf("parent_id" to parentId)

        every {
            classHierarchyService.create(ContributorId(CONTRIBUTOR_ID), parentId, setOf(childId), false)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        mockMvc
            .perform(performPost("/api/classes/$childId/parent", request))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Given a class id, when counting class instances, then status is 200 OK`() {
        val id = ThingId("C123")

        every { classHierarchyService.countClassInstances(id) } returns 5

        get("/api/classes/$id/count")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(5))
    }

    @Test
    fun `Given a class id, when service reports missing class while counting class instances, then status is 404 NOT FOUND`() {
        val id = ThingId("C123")

        every { classHierarchyService.countClassInstances(id) } throws ClassNotFound.withThingId(id)

        get("/api/classes/$id/count")
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a class id, when the class hierarchy is fetched, then status is 200 OK`() {
        val childId = ThingId("child")
        val parentId = ThingId("parent")
        val childClass = createClass(id = childId)

        every { classHierarchyService.findClassHierarchy(childId, any()) } returns PageImpl(
            listOf(
                ClassHierarchyEntry(childClass, parentId)
            )
        )

        get("/api/classes/$childId/hierarchy")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].class.id").value(childId.value))
            .andExpect(jsonPath("$.content[0].parent_id").value(parentId.value))
            .andExpect(jsonPath("$.totalElements").value(1))
    }

    @Test
    fun `Given a class id, when service reports missing class while fetched the class hierarchy, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.findClassHierarchy(childId, any()) } throws ClassNotFound.withThingId(childId)

        get("/api/classes/$childId/hierarchy")
            .andExpect(status().isNotFound)
    }

    private fun performPost(uri: String, body: Map<out Any, Any>) =
        MockMvcRequestBuilders.post(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))

    private fun performPatch(uri: String, body: Map<out Any, Any>) =
        MockMvcRequestBuilders.patch(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .content(objectMapper.writeValueAsString(body))

    private fun get(uri: String) = mockMvc.perform(MockMvcRequestBuilders.get(uri))
}
