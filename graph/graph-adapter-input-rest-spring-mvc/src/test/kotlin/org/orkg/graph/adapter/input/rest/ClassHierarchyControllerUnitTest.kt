package org.orkg.graph.adapter.input.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import java.util.*
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.common.exceptions.ExceptionHandler
import org.orkg.common.json.CommonJacksonModule
import org.orkg.graph.domain.ChildClass
import org.orkg.graph.domain.ClassHierarchyEntry
import org.orkg.graph.domain.ClassNotFound
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidSubclassRelation
import org.orkg.graph.domain.ParentClassAlreadyExists
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ClassHierarchyUseCases
import org.orkg.graph.input.ClassUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.testing.FixedClockConfig
import org.orkg.testing.MockUserId
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.orkg.testing.pageOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Import(SecurityTestConfiguration::class)
@ContextConfiguration(classes = [ClassHierarchyController::class, ExceptionHandler::class, CommonJacksonModule::class, FixedClockConfig::class])
@WebMvcTest(controllers = [ClassHierarchyController::class])
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
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var classHierarchyService: ClassHierarchyUseCases

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder>(springSecurity())
            .build()
    }

    @Test
    fun `Given a parent class id, when searched for its children, then status is 200 OK and children class ids are returned`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val response = ChildClass(createClass(id = childId), 1)

        every { classHierarchyService.findChildren(parentId, any()) } returns PageImpl(listOf(response))
        every { statementService.findAllDescriptions(any()) } returns emptyMap()

        mockMvc.perform(get("/api/classes/{id}/children", parentId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].class.id").value(response.`class`.id.value))
            .andExpect(jsonPath("$.content[0].child_count").value(response.childCount))
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { statementService.findAllDescriptions(any()) }
    }

    @Test
    fun `Given a parent class id, when service reports the parent class cannot be found while searching for its children, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")

        every { classHierarchyService.findChildren(parentId, any()) } throws ClassNotFound.withThingId(parentId)

        mockMvc.perform(get("/api/classes/{id}/children", parentId))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a child class id, when searched for its parent, then status is 200 OK and parent class is returned`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")

        every { classHierarchyService.findParent(childId) } returns Optional.of(createClass(id = parentId))
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = parentId,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        mockMvc.perform(get("/api/classes/{id}/parent", childId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(parentId.value))

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = parentId,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    fun `Given a child class id, when service reports the child class cannot be found while searching for its parent, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.findParent(childId) } throws ClassNotFound.withThingId(childId)

        mockMvc.perform(get("/api/classes/{id}/parent", childId))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a child class id, when the parent class cannot be found, then status is 204 NO CONTENT`() {
        val childId = ThingId("child")

        every { classHierarchyService.findParent(childId) } returns Optional.empty()

        mockMvc.perform(get("/api/classes/{id}/parent", childId))
            .andExpect(status().isNoContent)
    }

    @Test
    fun `Given a child class id, when searched for its root, then status is 200 OK and root class is returned`() {
        val rootId = ThingId("root")
        val childId = ThingId("child")

        every { classHierarchyService.findRoot(childId) } returns Optional.of(createClass(id = rootId))
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = rootId,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        mockMvc.perform(get("/api/classes/{id}/root", childId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(rootId.value))

        verify(exactly = 1) {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = rootId,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        }
    }

    @Test
    fun `Given a child class id, when service reports the child class cannot be found while searching for its root, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.findRoot(childId) } throws ClassNotFound.withThingId(childId)

        mockMvc.perform(get("/api/classes/{id}/root", childId))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `Given a child class id, when searched for its root but it has no parent class, then status is 204 NO CONTENT`() {
        val childId = ThingId("child")

        every { classHierarchyService.findRoot(childId) } returns Optional.empty()

        mockMvc.perform(get("/api/classes/{id}/root", childId))
            .andExpect(status().isNoContent)
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when a relation is created, then status is 201 CREATED`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true) } returns Unit

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/children", parentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated)
            .andExpect(header().string("location", endsWith("/api/classes/$parentId/children")))

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports input class does not exist, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true)
        } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/children", parentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound)
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports input classes are the same, then status is 400 BDA REQUEST`() {
        val classId = ThingId("parent")
        val request = mapOf("child_ids" to setOf(classId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), true)
        } throws InvalidSubclassRelation(classId, classId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/children", classId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports child class already has a parent class, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parent")
        val otherParentId = ThingId("other")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/children", parentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when a relation is created for a patch request, then status is 200 OK`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) } returns Unit

        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/api/classes/{id}/children", parentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk)
            .andExpect(header().string("location", endsWith("/api/classes/$parentId/children")))

        verify(exactly = 1) {
            classHierarchyService.create(
                ContributorId(MockUserId.CURATOR),
                parentId,
                setOf(childId),
                false
            )
        }
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports input class does not exist for a patch request, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/api/classes/{id}/children", parentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound)
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports input classes are the same for a patch request, then status is 400 BDA REQUEST`() {
        val classId = ThingId("parent")
        val request = mapOf("child_ids" to setOf(classId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), false)
        } throws InvalidSubclassRelation(classId, classId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/api/classes/{id}/children", classId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports child class already has a parent class for a patch request, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parent")
        val otherParentId = ThingId("other")
        val childId = ThingId("child")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.patch("/api/classes/{id}/children", parentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id, when deleting its subclass relation, then status is 200 OK`() {
        val childId = ThingId("child")

        every { classHierarchyService.delete(childId) } returns Unit

        mockMvc
            .perform(MockMvcRequestBuilders.delete("/api/classes/{id}/parent", childId))
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))

        verify(exactly = 1) { classHierarchyService.delete(childId) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id, when service reports input class does not exist while deleting its subclass relation, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.delete(childId) } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(MockMvcRequestBuilders.delete("/api/classes/{id}/parent", childId))
            .andExpect(status().isNotFound)

        verify(exactly = 1) { classHierarchyService.delete(childId) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id and a parent class id, when a relation is created, then status is 200 OK`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("parent_id" to parentId)

        every { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) } returns Unit

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/parent", childId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated)
            .andExpect(header().string("location", endsWith("/api/classes/$childId/parent")))

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id and a parent class id, when service reports input class does not exist, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parent")
        val childId = ThingId("child")
        val request = mapOf("parent_id" to parentId)

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ClassNotFound.withThingId(childId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/parent", childId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound)
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id and a parent class id, when service reports input classes are the same, then status is 400 BDA REQUEST`() {
        val classId = ThingId("parent")
        val request = mapOf("parent_id" to classId)

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), false)
        } throws InvalidSubclassRelation(classId, classId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/parent", classId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id and a parent class id, when service reports child class already has a parent class, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parent")
        val otherParentId = ThingId("other")
        val childId = ThingId("child")
        val request = mapOf("parent_id" to parentId)

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/classes/{id}/parent", childId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `Given a class id, when counting class instances, then status is 200 OK`() {
        val id = ThingId("C123")

        every { classHierarchyService.countClassInstances(id) } returns 5

        mockMvc.perform(get("/api/classes/{id}/count", id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(5))
    }

    @Test
    fun `Given a class id, when service reports missing class while counting class instances, then status is 404 NOT FOUND`() {
        val id = ThingId("C123")

        every { classHierarchyService.countClassInstances(id) } throws ClassNotFound.withThingId(id)

        mockMvc.perform(get("/api/classes/{id}/count", id))
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
        every { statementService.findAllDescriptions(any()) } returns emptyMap()

        mockMvc.perform(get("/api/classes/{id}/hierarchy", childId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].class.id").value(childId.value))
            .andExpect(jsonPath("$.content[0].parent_id").value(parentId.value))
            .andExpect(jsonPath("$.totalElements").value(1))

        verify(exactly = 1) { statementService.findAllDescriptions(any()) }
    }

    @Test
    fun `Given a class id, when service reports missing class while fetched the class hierarchy, then status is 404 NOT FOUND`() {
        val childId = ThingId("child")

        every { classHierarchyService.findClassHierarchy(childId, any()) } throws ClassNotFound.withThingId(childId)

        mockMvc.perform(get("/api/classes/{id}/hierarchy", childId))
            .andExpect(status().isNotFound)
    }
}
