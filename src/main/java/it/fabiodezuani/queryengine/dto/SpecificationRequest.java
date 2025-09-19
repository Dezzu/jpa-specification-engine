package it.fabiodezuani.queryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO che rappresenta una richiesta completa di filtri
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationRequest {

    /**
     * Lista dei criteri di filtro
     */
    @Builder.Default
    private List<FilterCriteria> filters = new ArrayList<>();

    /**
     * Operatore logico per combinare i filtri
     * true = AND, false = OR
     * Default: true (AND)
     */
    @Builder.Default
    private boolean useAndOperator = true;

    /**
     * Lista di gruppi di filtri per query complesse
     * Ogni gruppo viene combinato internamente con l'operatore specificato
     * I gruppi vengono poi combinati tra loro
     */
    @Builder.Default
    private List<FilterGroup> filterGroups = new ArrayList<>();

    /**
     * Operatore per combinare i gruppi di filtri
     * true = AND, false = OR
     * Default: true (AND)
     */
    @Builder.Default
    private boolean useAndOperatorForGroups = true;

    public void addFilterGroup(FilterGroup filterGroup) {
        this.filterGroups.add(filterGroup);
    }

    public void addFilter(FilterCriteria filterCriteria) {
        this.filters.add(filterCriteria);
    }
    public void addFilters(List<FilterCriteria> filterCriteriaList) {
        this.filters.addAll(filterCriteriaList);
    }

}