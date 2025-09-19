package it.fabiodezuani.queryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO che rappresenta un singolo criterio di filtro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {

    /**
     * Nome del campo su cui applicare il filtro
     * Supporta la navigazione con dot notation (es: "user.name", "address.city")
     */
    private String field;

    /**
     * Operazione da applicare
     */
    private FilterOperation operation;

    /**
     * Valore da confrontare
     * Può essere null per operazioni IS_NULL/IS_NOT_NULL
     */
    private Object value;

    /**
     * Lista di valori per operazioni IN/NOT_IN/BETWEEN
     */
    private List<Object> values;

    /**
     * Indica se il campo è case sensitive (per operazioni stringa)
     * Default: false
     */
    @Builder.Default
    private boolean caseSensitive = false;

    /**
     * Indica se negare l'intera condizione
     * Default: false
     */
    @Builder.Default
    private boolean negate = false;
}
