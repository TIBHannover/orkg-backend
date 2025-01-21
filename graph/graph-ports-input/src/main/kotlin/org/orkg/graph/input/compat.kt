package org.orkg.graph.input

import org.orkg.graph.domain.Class

interface ClassUseCases : CreateClassUseCase, RetrieveClassUseCase,
    UpdateClassUseCase, DeleteClassUseCase,
    URIService<Class>

interface LiteralUseCases : CreateLiteralUseCase, RetrieveLiteralUseCase,
    UpdateLiteralUseCase, DeleteLiteralUseCase

interface PredicateUseCases : CreatePredicateUseCase, DeletePredicateUseCase,
    UpdatePredicateUseCase,
    RetrievePredicateUseCase

interface UnsafeResourceUseCases : CreateResourceUseCase, UpdateResourceUseCase, DeleteResourceUseCase

interface ResourceUseCases : CreateResourceUseCase, UpdateResourceUseCase, DeleteResourceUseCase, RetrieveResourceUseCase,
    OtherResourceUseCases

interface StatementUseCases : CreateStatementUseCase,
    RetrieveStatementUseCase, UpdateStatementUseCase,
    DeleteStatementUseCase

interface ListUseCases : CreateListUseCase, RetrieveListUseCase,
    UpdateListUseCase, DeleteListUseCase

interface ClassHierarchyUseCases : CreateClassHierarchyUseCase,
    RetrieveClassHierarchyUseCase, DeleteClassHierarchyUseCase

interface FormattedLabelUseCases : RetrieveFormattedLabelUseCase

interface ImportUseCases : ImportResourceUseCase, ImportPredicateUseCase, ImportClassUseCase

// FIXME: we need to refactor those as well
interface OtherResourceUseCases :
    MarkAsVerifiedUseCase,
    MarkFeaturedService,
    MarkAsUnlistedService,
    GetContributorsQuery
