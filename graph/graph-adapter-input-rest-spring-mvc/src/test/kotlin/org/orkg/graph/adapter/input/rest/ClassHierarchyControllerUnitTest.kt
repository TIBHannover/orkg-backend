package org.orkg.graph.adapter.input.rest

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
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
import org.orkg.testing.MockUserId
import org.orkg.testing.andExpectClass
import org.orkg.testing.annotations.TestWithMockCurator
import org.orkg.testing.configuration.ExceptionTestConfiguration
import org.orkg.testing.configuration.FixedClockConfig
import org.orkg.testing.configuration.SecurityTestConfiguration
import org.orkg.testing.pageOf
import org.orkg.testing.spring.MockMvcBaseTest
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectErrorStatus
import org.orkg.testing.spring.MockMvcExceptionBaseTest.Companion.andExpectType
import org.orkg.testing.spring.restdocs.pagedResponseFields
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@ContextConfiguration(
    classes = [
        ClassHierarchyController::class,
        ExceptionTestConfiguration::class,
        CommonJacksonModule::class,
        FixedClockConfig::class,
        SecurityTestConfiguration::class
    ]
)
@WebMvcTest(controllers = [ClassHierarchyController::class])
internal class ClassHierarchyControllerUnitTest : MockMvcBaseTest("class-hierarchies") {
    @MockkBean
    private lateinit var classService: ClassUseCases

    @MockkBean
    private lateinit var resourceService: ResourceUseCases

    @MockkBean
    private lateinit var statementService: StatementUseCases

    @MockkBean
    private lateinit var classHierarchyService: ClassHierarchyUseCases

    @Test
    @DisplayName("Given a parent class id, when searching for its children, then status is 200 OK and children class ids are returned")
    fun findChildren() {
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")
        val response = ChildClass(createClass(id = childId), 1)

        every { classHierarchyService.findAllChildrenByAncestorId(parentId, any()) } returns PageImpl(listOf(response))
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/classes/{id}/children", parentId)
            .perform()
            .andExpect(status().isOk)
            .andExpectClass("$.content[*].class")
            .andExpect(jsonPath("$.content[0].class.id").value(response.`class`.id.value))
            .andExpect(jsonPath("$.content[0].child_count").value(response.childCount))
            .andExpect(jsonPath("$.page.total_elements").value(1))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the parent class.")
                    ),
                    pagedResponseFields(
                        subsectionWithPath("class").description("The child <<classes,class>>."),
                        fieldWithPath("child_count").description("The count of child classes of the child class."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.findAllChildrenByAncestorId(parentId, any()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
    }

    @Test
    fun `Given a parent class id, when service reports the parent class cannot be found while searching for its children, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parentId")

        every { classHierarchyService.findAllChildrenByAncestorId(parentId, any()) } throws ClassNotFound.withThingId(parentId)

        get("/api/classes/{id}/children", parentId)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.findAllChildrenByAncestorId(parentId, any()) }
    }

    @Test
    @DisplayName("Given a child class id, when searched for its parent, then status is 200 OK and parent class is returned")
    fun findParentRelation() {
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")

        every { classHierarchyService.findParentByChildId(childId) } returns Optional.of(createClass(id = parentId))
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = parentId,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        documentedGetRequestTo("/api/classes/{id}/parent", childId)
            .perform()
            .andExpect(status().isOk)
            .andExpectClass()
            .andExpect(jsonPath("$.id").value(parentId.value))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the child class.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.findParentByChildId(childId) }
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
        val childId = ThingId("childId")

        every { classHierarchyService.findParentByChildId(childId) } throws ClassNotFound.withThingId(childId)

        get("/api/classes/{id}/parent", childId)
            .perform()
            .andExpect(status().isNotFound)

        verify(exactly = 1) { classHierarchyService.findParentByChildId(childId) }
    }

    @Test
    fun `Given a child class id, when the parent class cannot be found, then status is 204 NO CONTENT`() {
        val childId = ThingId("childId")

        every { classHierarchyService.findParentByChildId(childId) } returns Optional.empty()

        get("/api/classes/{id}/parent", childId)
            .perform()
            .andExpect(status().isNoContent)

        verify(exactly = 1) { classHierarchyService.findParentByChildId(childId) }
    }

    @Test
    @DisplayName("Given a child class id, when searched for its root, then status is 200 OK and root class is returned")
    fun findRoot() {
        val rootId = ThingId("root")
        val childId = ThingId("childId")

        every { classHierarchyService.findRootByDescendantId(childId) } returns Optional.of(createClass(id = rootId))
        every {
            statementService.findAll(
                pageable = PageRequests.SINGLE,
                subjectId = rootId,
                predicateId = Predicates.description,
                objectClasses = setOf(Classes.literal)
            )
        } returns pageOf()

        documentedGetRequestTo("/api/classes/{id}/root", childId)
            .perform()
            .andExpect(status().isOk)
            .andExpectClass()
            .andExpect(jsonPath("$.id").value(rootId.value))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the class.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.findRootByDescendantId(childId) }
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
        val childId = ThingId("childId")

        every { classHierarchyService.findRootByDescendantId(childId) } throws ClassNotFound.withThingId(childId)

        get("/api/classes/{id}/root", childId)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.findRootByDescendantId(childId) }
    }

    @Test
    fun `Given a child class id, when searched for its root but it has no parent class, then status is 204 NO CONTENT`() {
        val childId = ThingId("childId")

        every { classHierarchyService.findRootByDescendantId(childId) } returns Optional.empty()

        get("/api/classes/{id}/root", childId)
            .perform()
            .andExpect(status().isNoContent)

        verify(exactly = 1) { classHierarchyService.findRootByDescendantId(childId) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given a parent class id and a child class id, when a relation is created, then status is 201 CREATED")
    fun createChildRelations() {
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")
        val request = mapOf("child_ids" to setOf(childId))

        every { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true) } returns Unit

        documentedPostRequestTo("/api/classes/{id}/children", parentId)
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("location", endsWith("/api/classes/$parentId/children")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the parent class.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated list of child classes can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("child_ids").description("The list of child class ids.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports input class does not exist, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true)
        } throws ClassNotFound.withThingId(childId)

        post("/api/classes/{id}/children", parentId)
            .content(request)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports input classes are the same, then status is 400 BAD REQUEST`() {
        val classId = ThingId("parentId")
        val request = mapOf("child_ids" to setOf(classId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), true)
        } throws InvalidSubclassRelation(classId, classId)

        post("/api/classes/{id}/children", classId)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_subclass_relation")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), true) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports child class already has a parent class, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parentId")
        val otherParentId = ThingId("other")
        val childId = ThingId("childId")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        post("/api/classes/{id}/children", parentId)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:parent_class_already_exists")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), true) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given a parent class id and a child class id, when a relation is created for a patch request, then status is 204 NO CONTENT")
    fun updateChildRelations() {
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")
        val request = mapOf("child_ids" to setOf(childId))

        every { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) } returns Unit

        documentedPatchRequestTo("/api/classes/{id}/children", parentId)
            .content(request)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(header().string("location", endsWith("/api/classes/$parentId/children")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the parent class.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated list of child classes can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("child_ids").description("The updated list of child class ids.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

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
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ClassNotFound.withThingId(childId)

        patch("/api/classes/{id}/children", parentId)
            .content(request)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports input classes are the same for a patch request, then status is 400 BAD REQUEST`() {
        val classId = ThingId("parentId")
        val request = mapOf("child_ids" to setOf(classId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), false)
        } throws InvalidSubclassRelation(classId, classId)

        patch("/api/classes/{id}/children", classId)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_subclass_relation")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), false) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a parent class id and a child class id, when service reports child class already has a parent class for a patch request, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parentId")
        val otherParentId = ThingId("other")
        val childId = ThingId("childId")
        val request = mapOf("child_ids" to setOf(childId))

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        patch("/api/classes/{id}/children", parentId)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:parent_class_already_exists")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given a child class id, when deleting its subclass relation, then status is 204 NO CONTENT")
    fun deleteParentRelation() {
        val childId = ThingId("childId")

        every { classHierarchyService.deleteByChildId(childId) } returns Unit

        documentedDeleteRequestTo("/api/classes/{id}/parent", childId)
            .perform()
            .andExpect(status().isNoContent)
            .andExpect(content().string(""))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the child class.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.deleteByChildId(childId) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id, when service reports input class does not exist while deleting its subclass relation, then status is 404 NOT FOUND`() {
        val childId = ThingId("childId")

        every { classHierarchyService.deleteByChildId(childId) } throws ClassNotFound.withThingId(childId)

        delete("/api/classes/{id}/parent", childId)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.deleteByChildId(childId) }
    }

    @Test
    @TestWithMockCurator
    @DisplayName("Given a child class id and a parent class id, when a relation is created, then status is 201 CREATED")
    fun createParentRelation() {
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")
        val request = mapOf("parent_id" to parentId)

        every { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) } returns Unit

        documentedPostRequestTo("/api/classes/{id}/parent", childId)
            .content(request)
            .perform()
            .andExpect(status().isCreated)
            .andExpect(header().string("location", endsWith("/api/classes/$childId/parent")))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the child class.")
                    ),
                    responseHeaders(
                        headerWithName("Location").description("The uri path where the updated list of child classes can be fetched from.")
                    ),
                    requestFields(
                        fieldWithPath("parent_id").description("The id of the parent class.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id and a parent class id, when service reports input class does not exist, then status is 404 NOT FOUND`() {
        val parentId = ThingId("parentId")
        val childId = ThingId("childId")
        val request = mapOf("parent_id" to parentId)

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ClassNotFound.withThingId(childId)

        post("/api/classes/{id}/parent", childId)
            .content(request)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id and a parent class id, when service reports input classes are the same, then status is 400 BAD REQUEST`() {
        val classId = ThingId("parentId")
        val request = mapOf("parent_id" to classId)

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), false)
        } throws InvalidSubclassRelation(classId, classId)

        post("/api/classes/{id}/parent", classId)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:invalid_subclass_relation")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), classId, setOf(classId), false) }
    }

    @Test
    @TestWithMockCurator
    fun `Given a child class id and a parent class id, when service reports child class already has a parent class, then status is 400 BAD REQUEST`() {
        val parentId = ThingId("parentId")
        val otherParentId = ThingId("other")
        val childId = ThingId("childId")
        val request = mapOf("parent_id" to parentId)

        every {
            classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false)
        } throws ParentClassAlreadyExists(childId, otherParentId)

        post("/api/classes/{id}/parent", childId)
            .content(request)
            .perform()
            .andExpectErrorStatus(BAD_REQUEST)
            .andExpectType("orkg:problem:parent_class_already_exists")

        verify(exactly = 1) { classHierarchyService.create(ContributorId(MockUserId.CURATOR), parentId, setOf(childId), false) }
    }

    @Test
    @DisplayName("Given a class id, when counting class instances, then status is 200 OK")
    fun countClassInstances() {
        val id = ThingId("C123")

        every { classHierarchyService.countClassInstances(id) } returns 5

        documentedGetRequestTo("/api/classes/{id}/count", id)
            .perform()
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.count").value(5))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the class.")
                    ),
                    responseFields(
                        fieldWithPath("count").description("The count of class instances including subclass instances.")
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.countClassInstances(id) }
    }

    @Test
    fun `Given a class id, when service reports missing class while counting class instances, then status is 404 NOT FOUND`() {
        val id = ThingId("C123")

        every { classHierarchyService.countClassInstances(id) } throws ClassNotFound.withThingId(id)

        get("/api/classes/{id}/count", id)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.countClassInstances(id) }
    }

    @Test
    @DisplayName("Given a class id, when the class hierarchy is fetched, then status is 200 OK")
    fun findHierarchy() {
        val childId = ThingId("childId")
        val parentId = ThingId("parentId")
        val childClass = createClass(id = childId)

        every { classHierarchyService.findClassHierarchy(childId, any()) } returns PageImpl(
            listOf(
                ClassHierarchyEntry(childClass, parentId)
            )
        )
        every { statementService.findAllDescriptionsById(any()) } returns emptyMap()

        documentedGetRequestTo("/api/classes/{id}/hierarchy", childId)
            .perform()
            .andExpect(status().isOk)
            .andExpectClass("$.content[*].class")
            .andExpect(jsonPath("$.content[0].class.id").value(childId.value))
            .andExpect(jsonPath("$.content[0].parent_id").value(parentId.value))
            .andExpect(jsonPath("$.page.total_elements").value(1))
            .andDo(
                documentationHandler.document(
                    pathParameters(
                        parameterWithName("id").description("The identifier of the class.")
                    ),
                    pagedResponseFields(
                        subsectionWithPath("class").description("The <<classes,class>> in the hierarchy."),
                        fieldWithPath("parent_id").description("The parent id of the class."),
                    )
                )
            )
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.findClassHierarchy(childId, any()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(any()) }
    }

    @Test
    fun `Given a class id, when service reports missing class while fetched the class hierarchy, then status is 404 NOT FOUND`() {
        val childId = ThingId("childId")

        every { classHierarchyService.findClassHierarchy(childId, any()) } throws ClassNotFound.withThingId(childId)

        get("/api/classes/{id}/hierarchy", childId)
            .perform()
            .andExpectErrorStatus(NOT_FOUND)
            .andExpectType("orkg:problem:class_not_found")

        verify(exactly = 1) { classHierarchyService.findClassHierarchy(childId, any()) }
    }

    @Test
    @DisplayName("Given a root class, when listing all root classes, then status is 200 OK and root classes are returned")
    fun findAllRoots() {
        val rootId = ThingId("root")

        every { classHierarchyService.findAllRoots(any()) } returns pageOf(createClass(id = rootId))
        every { statementService.findAllDescriptionsById(setOf(rootId)) } returns mapOf()

        documentedGetRequestTo("/api/classes/roots")
            .perform()
            .andExpect(status().isOk)
            .andExpectClass("$.content[*]")
            .andDo(generateDefaultDocSnippets())

        verify(exactly = 1) { classHierarchyService.findAllRoots(any()) }
        verify(exactly = 1) { statementService.findAllDescriptionsById(setOf(rootId)) }
    }
}
