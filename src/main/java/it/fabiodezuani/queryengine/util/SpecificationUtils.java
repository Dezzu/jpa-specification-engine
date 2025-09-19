package it.fabiodezuani.queryengine.util;
import it.fabiodezuani.queryengine.dto.FilterCriteria;
import it.fabiodezuani.queryengine.dto.FilterGroup;
import it.fabiodezuani.queryengine.dto.FilterOperation;
import it.fabiodezuani.queryengine.dto.SpecificationRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Classe di utility per semplificare la creazione delle richieste di filtro
 */
public class SpecificationUtils {

    /**
     * Crea un criterio di filtro semplice
     */
    public static FilterCriteria createFilter(String field, FilterOperation operation, Object value) {
        return FilterCriteria.builder()
                .field(field)
                .operation(operation)
                .value(value)
                .build();
    }

    /**
     * Crea un criterio di filtro con multiple valori (per IN, NOT_IN, BETWEEN)
     */
    public static FilterCriteria createFilter(String field, FilterOperation operation, List<Object> values) {
        return FilterCriteria.builder()
                .field(field)
                .operation(operation)
                .values(values)
                .build();
    }

    /**
     * Crea un criterio di filtro EQUALS
     */
    public static FilterCriteria equals(String field, Object value) {
        return createFilter(field, FilterOperation.EQUALS, value);
    }

    /**
     * Crea un criterio di filtro LIKE (case sensitive)
     */
    public static FilterCriteria like(String field, String value) {
        return createFilter(field, FilterOperation.LIKE, value);
    }

    /**
     * Crea un criterio di filtro ILIKE (case insensitive)
     */
    public static FilterCriteria ilike(String field, String value) {
        return createFilter(field, FilterOperation.ILIKE, value);
    }

    /**
     * Crea un criterio di filtro IN
     */
    public static FilterCriteria in(String field, Object... values) {
        return createFilter(field, FilterOperation.IN, Arrays.asList(values));
    }

    /**
     * Crea un criterio di filtro BETWEEN
     */
    public static FilterCriteria between(String field, Object from, Object to) {
        return createFilter(field, FilterOperation.BETWEEN, Arrays.asList(from, to));
    }

    /**
     * Crea un criterio di filtro IS_NULL
     */
    public static FilterCriteria isNull(String field) {
        return FilterCriteria.builder()
                .field(field)
                .operation(FilterOperation.IS_NULL)
                .build();
    }

    /**
     * Crea un criterio di filtro IS_NOT_NULL
     */
    public static FilterCriteria isNotNull(String field) {
        return FilterCriteria.builder()
                .field(field)
                .operation(FilterOperation.IS_NOT_NULL)
                .build();
    }

    /**
     * Crea una richiesta semplice con operatore AND
     */
    public static SpecificationRequest createAndRequest(FilterCriteria... filters) {
        return SpecificationRequest.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(true)
                .build();
    }

    /**
     * Crea una richiesta semplice con operatore OR
     */
    public static SpecificationRequest createOrRequest(FilterCriteria... filters) {
        return SpecificationRequest.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(false)
                .build();
    }

    /**
     * Crea un gruppo di filtri con operatore AND
     */
    public static FilterGroup createAndGroup(FilterCriteria... filters) {
        return FilterGroup.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(true)
                .build();
    }

    /**
     * Crea un gruppo di filtri con operatore OR
     */
    public static FilterGroup createOrGroup(FilterCriteria... filters) {
        return FilterGroup.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(false)
                .build();
    }

    /**
     * Crea una richiesta con gruppi di filtri
     */
    public static SpecificationRequest createGroupRequest(boolean useAndForGroups, FilterGroup... groups) {
        return SpecificationRequest.builder()
                .filterGroups(Arrays.asList(groups))
                .useAndOperatorForGroups(useAndForGroups)
                .build();
    }
}
