package it.fabiodezuani.queryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO che rappresenta un gruppo di filtri
 * Utile per creare query complesse come: (A AND B) OR (C AND D)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterGroup {

    /**
     * Lista dei criteri di filtro del gruppo
     */
    @Builder.Default
    private List<FilterCriteria> filters = new ArrayList<>();

    /**
     * Operatore logico per combinare i filtri del gruppo
     * true = AND, false = OR
     * Default: true (AND)
     */
    @Builder.Default
    private boolean useAndOperator = true;
}
