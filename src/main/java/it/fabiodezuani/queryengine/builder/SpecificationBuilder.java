package it.fabiodezuani.queryengine.builder;

import it.fabiodezuani.queryengine.dto.FilterCriteria;
import org.springframework.data.jpa.domain.Specification;

/**
 * Strategy interface for building JPA Specifications from filter criteria.
 *
 * <p>This interface defines the contract for converting {@link FilterCriteria} objects
 * into JPA {@link Specification} instances that can be used with Spring Data JPA
 * repositories for dynamic query construction.</p>
 *
 * <p>Implementations of this interface should handle specific types of filter
 * operations and provide appropriate JPA Criteria API predicates. The interface
 * follows the Strategy design pattern, allowing different builders to handle
 * different filtering scenarios or entity types.</p>
 *
 * <p><b>Example implementation usage:</b></p>
 * <pre>{@code
 * @Service
 * public class UserFilterService {
 *
 *     @Autowired
 *     private SpecificationBuilder<User> specificationBuilder;
 *
 *     @Autowired
 *     private UserRepository userRepository;
 *
 *     public List<User> findUsers(FilterCriteria criteria) {
 *         Specification<User> spec = specificationBuilder.build(criteria);
 *         return userRepository.findAll(spec);
 *     }
 * }
 * }</pre>
 *
 * <p><b>Example custom implementation:</b></p>
 * <pre>{@code
 * @Component
 * public class CustomUserSpecificationBuilder implements SpecificationBuilder<User> {
 *
 *     @Override
 *     public Specification<User> build(FilterCriteria criteria) {
 *         return (root, query, criteriaBuilder) -> {
 *             // Custom specification logic
 *             return criteriaBuilder.equal(root.get(criteria.getField()), criteria.getValue());
 *         };
 *     }
 * }
 * }</pre>
 *
 * <p>Implementations should be thread-safe as they may be used concurrently
 * in multi-threaded environments. They should also handle edge cases gracefully
 * and provide meaningful error messages when operations cannot be completed.</p>
 *
 * @param <T> the root entity type for which specifications will be built
 * @author Fabio De Zuani
 * @see FilterCriteria
 * @see org.springframework.data.jpa.domain.Specification
 * @see DefaultSpecificationBuilder
 */
public interface SpecificationBuilder<T> {

    /**
     * Builds a JPA Specification from the provided filter criteria.
     *
     * <p>This method transforms filter criteria containing field names, operations,
     * and values into a JPA Specification that can be executed against a database
     * using Spring Data JPA repositories.</p>
     *
     * <p>The returned Specification should be null-safe and handle edge cases
     * appropriately. If the criteria cannot be processed, implementations should
     * throw a descriptive exception rather than returning null.</p>
     *
     * <p><b>Implementation responsibilities:</b></p>
     * <ul>
     *   <li>Validate the provided filter criteria</li>
     *   <li>Handle type conversions between criteria values and entity field types</li>
     *   <li>Create appropriate JPA Criteria API predicates</li>
     *   <li>Provide meaningful error messages for invalid operations</li>
     *   <li>Support the entity's field structure and relationships</li>
     * </ul>
     *
     * @param criteria the filter criteria containing field, operation, and value information;
     *                must not be null
     * @return a JPA Specification that represents the filtering logic defined by the criteria;
     *         never null
     * @throws it.fabiodezuani.queryengine.exception.SpecificationException if the criteria cannot be converted to a valid specification
     * @throws IllegalArgumentException if the criteria parameter is null or invalid
     */
    Specification<T> build(FilterCriteria criteria);

}
