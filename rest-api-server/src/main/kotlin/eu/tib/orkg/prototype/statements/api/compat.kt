package eu.tib.orkg.prototype.statements.api

import eu.tib.orkg.prototype.statements.domain.model.URIService

interface ClassUseCases : CreateClassUseCase, RetrieveClassUseCase, UpdateClassUseCase, DeleteClassUseCase,
    URIService<ClassRepresentation>

interface LiteralUseCases : CreateLiteralUseCase, RetrieveLiteralUseCase, UpdateLiteralUseCase, DeleteLiteralUseCase

interface PredicateUseCases : CreatePredicateUseCase, DeletePredicateUseCase, UpdatePredicateUseCase,
    RetrievePredicateUseCase

interface ResourceUseCases : CreateResourceUseCase, RetrieveResourceUseCase, UpdateResourceUseCase,
    DeleteResourceUseCase

interface StatementUseCases : CreateStatementUseCase, RetrieveStatementUseCase, UpdateStatementUseCase,
    DeleteStatementUseCase
