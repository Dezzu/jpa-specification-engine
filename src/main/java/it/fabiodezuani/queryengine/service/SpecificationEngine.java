package it.fabiodezuani.queryengine.service;

import it.fabiodezuani.queryengine.builder.SpecificationBuilder;
import it.fabiodezuani.queryengine.dto.FilterCriteria;
import it.fabiodezuani.queryengine.dto.FilterGroup;
import it.fabiodezuani.queryengine.dto.FilterOperation;
import it.fabiodezuani.queryengine.dto.SpecificationRequest;
import it.fabiodezuani.queryengine.exception.SpecificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Engine principale per la creazione delle JPA Specifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificationEngine {

    private final List<SpecificationBuilder<?>> builders;

    /**
     * Crea una Specification basata sulla richiesta
     *
     * @param request la richiesta contenente i filtri
     * @param <T> il tipo dell'entità
     * @return la Specification creata
     */
    public <T> Specification<T> createSpecification(SpecificationRequest request) {
        log.debug("Creating specification for request with {} filters and {} groups",
                request.getFilters().size(), request.getFilterGroups().size());

        Specification<T> specification = Specification.unrestricted();

        // Processa i filtri diretti
        if (!request.getFilters().isEmpty()) {
            Specification<T> filtersSpec = createSpecificationFromFilters(
                    request.getFilters(), request.isUseAndOperator());
            specification = specification.and(filtersSpec);
        }

        // Processa i gruppi di filtri
        if (!request.getFilterGroups().isEmpty()) {
            Specification<T> groupsSpec = createSpecificationFromGroups(
                    request.getFilterGroups(), request.isUseAndOperatorForGroups());

            if (request.getFilters().isEmpty()) {
                specification = groupsSpec;
            } else {
                specification = request.isUseAndOperatorForGroups() ?
                        specification.and(groupsSpec) : specification.or(groupsSpec);
            }
        }

        return specification;
    }

    /**
     * Crea una Specification da una lista di filtri
     *
     * @param filters lista dei filtri
     * @param useAndOperator true per AND, false per OR
     * @param <T> tipo dell'entità
     * @return la Specification creata
     */
    public <T> Specification<T> createSpecificationFromFilters(List<FilterCriteria> filters, boolean useAndOperator) {
        if (filters.isEmpty()) {
            return Specification.unrestricted();
        }

        Specification<T> specification = createSingleSpecification(filters.get(0));

        for (int i = 1; i < filters.size(); i++) {
            Specification<T> nextSpec = createSingleSpecification(filters.get(i));
            specification = useAndOperator ?
                    specification.and(nextSpec) : specification.or(nextSpec);
        }

        return specification;
    }

    /**
     * Crea una Specification da una lista di gruppi di filtri
     *
     * @param groups lista dei gruppi
     * @param useAndOperator true per AND, false per OR
     * @param <T> tipo dell'entità
     * @return la Specification creata
     */
    public <T> Specification<T> createSpecificationFromGroups(List<FilterGroup> groups, boolean useAndOperator) {
        if (groups.isEmpty()) {
            return Specification.unrestricted();
        }

        Specification<T> specification = createSpecificationFromFilters(
                groups.get(0).getFilters(), groups.get(0).isUseAndOperator());

        for (int i = 1; i < groups.size(); i++) {
            Specification<T> nextSpec = createSpecificationFromFilters(
                    groups.get(i).getFilters(), groups.get(i).isUseAndOperator());
            specification = useAndOperator ?
                    specification.and(nextSpec) : specification.or(nextSpec);
        }

        return specification;
    }

    /**
     * Crea una singola Specification da un criterio di filtro
     *
     * @param criteria il criterio di filtro
     * @param <T> tipo dell'entità
     * @return la Specification creata
     */
    @SuppressWarnings("unchecked")
    public <T> Specification<T> createSingleSpecification(FilterCriteria criteria) {
        log.debug("Creating specification for field: {}, operation: {}",
                criteria.getField(), criteria.getOperation());

        Optional<SpecificationBuilder<?>> builderOpt = builders.stream()
                .filter(builder -> builder.supports(criteria))
                .findFirst();

        if (!builderOpt.isPresent()) {
            throw new SpecificationException(
                    String.format("No builder found for operation: %s", criteria.getOperation()));
        }

        SpecificationBuilder<T> builder = (SpecificationBuilder<T>) builderOpt.get();
        return builder.build(criteria);
    }

    /**
     * Crea una Specification semplice per un singolo campo
     * Metodo di utilità per casi semplici
     */
    public <T> Specification<T> createSimpleSpecification(String field, Object value) {
        FilterCriteria criteria = FilterCriteria.builder()
                .field(field)
                .operation(FilterOperation.EQUALS)
                .value(value)
                .build();

        return createSingleSpecification(criteria);
    }
}