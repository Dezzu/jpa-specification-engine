package it.fabiodezuani.queryengine.builder;

import it.fabiodezuani.queryengine.dto.FilterCriteria;
import org.springframework.data.jpa.domain.Specification;

/**
 * Interfaccia per i builder delle Specification
 * Permette di estendere l'engine con nuovi tipi di operazioni
 */
public interface SpecificationBuilder<T> {

    /**
     * Crea una Specification basata sul criterio di filtro
     *
     * @param criteria il criterio di filtro
     * @return la Specification creata
     */
    Specification<T> build(FilterCriteria criteria);

    /**
     * Verifica se questo builder può gestire l'operazione specificata
     *
     * @param criteria il criterio di filtro
     * @return true se può gestire l'operazione, false altrimenti
     */
    boolean supports(FilterCriteria criteria);
}
