package it.fabiodezuani.queryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data transfer object representing filtering criteria for dynamic JPA query construction.
 *
 * <p>This class encapsulates all the information needed to build a single filter condition,
 * including the target field, the operation to perform, the value(s) to compare against,
 * and additional behavioral flags for case sensitivity and negation.</p>
 *
 * <p>The class is designed to work seamlessly with {@link it.fabiodezuani.queryengine.builder.SpecificationBuilder} implementations
 * to create dynamic, type-safe JPA Criteria API predicates. It supports both single-value
 * and multi-value operations through the {@code value} and {@code values} fields respectively.</p>
 *
 * <p><b>Field Path Support:</b><br>
 * The {@code field} parameter supports dot notation for navigating entity relationships:</p>
 * <ul>
 *   <li>{@code "name"} - Direct field access</li>
 *   <li>{@code "user.email"} - Single level relationship</li>
 *   <li>{@code "order.customer.profile.firstName"} - Multiple level relationships</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Simple equality filter
 * FilterCriteria criteria = FilterCriteria.builder()
 *     .field("username")
 *     .operation(FilterOperation.EQUALS)
 *     .value("john.doe")
 *     .build();
 *
 * // Case-insensitive string contains filter
 * FilterCriteria criteria = FilterCriteria.builder()
 *     .field("firstName")
 *     .operation(FilterOperation.CONTAINS)
 *     .value("John")
 *     .caseSensitive(false)
 *     .build();
 *
 * // IN operation with multiple values
 * FilterCriteria criteria = FilterCriteria.builder()
 *     .field("status")
 *     .operation(FilterOperation.IN)
 *     .values(Arrays.asList("ACTIVE", "PENDING"))
 *     .build();
 *
 * // Negated range filter
 * FilterCriteria criteria = FilterCriteria.builder()
 *     .field("age")
 *     .operation(FilterOperation.BETWEEN)
 *     .values(Arrays.asList(18, 65))
 *     .negate(true)
 *     .build();
 * }</pre>
 *
 * <p><b>Value vs Values Usage:</b></p>
 * <ul>
 *   <li><b>Use {@code value}:</b> For single-value operations (EQUALS, LIKE, GREATER_THAN, etc.)</li>
 *   <li><b>Use {@code values}:</b> For multi-value operations (IN, NOT_IN, BETWEEN, etc.)</li>
 * </ul>
 *
 * <p>This class uses Lombok annotations for automatic generation of getters, setters,
 * constructors, and builder pattern implementation. All fields are mutable to support
 * dynamic modification of filter criteria.</p>
 *
 * @author Fabio De Zuani
 * @see FilterOperation
 * @see it.fabiodezuani.queryengine.builder.SpecificationBuilder
 * @see org.springframework.data.jpa.domain.Specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterCriteria {

    /**
     * The target field name for the filter operation.
     *
     * <p>Supports dot notation for navigating entity relationships. For example,
     * {@code "user.profile.email"} will navigate from the root entity through
     * the {@code user} relationship, then through the {@code profile} relationship
     * to access the {@code email} field.</p>
     *
     * <p>Field names must correspond to actual JPA entity field names or relationship
     * names. Case sensitivity depends on the underlying JPA implementation and
     * database configuration.</p>
     *
     */
    private String field;

    /**
     * The filtering operation to perform on the specified field.
     *
     * <p>Defines how the field value should be compared against the criteria value(s).
     * The operation determines whether to use the {@code value} or {@code values} field
     * for the comparison.</p>
     *
     * @see FilterOperation
     */
    private FilterOperation operation;
    /**
     * The single value to use for the filter operation.
     *
     * <p>Used for operations that compare against a single value such as EQUALS,
     * NOT_EQUALS, GREATER_THAN, LESS_THAN, LIKE, CONTAINS, etc.</p>
     *
     * <p>The value type should be compatible with the target field type.
     * Type conversion will be handled by the {@link it.fabiodezuani.queryengine.builder.SpecificationBuilder} implementation.</p>
     *
     * <p><b>Mutual exclusivity:</b> Either {@code value} or {@code values} should be set,
     * but not both, depending on the operation requirements.</p>
     *
     */
    private Object value;

    /**
     * The list of values to use for multi-value filter operations.
     *
     * <p>Used for operations that require multiple values such as IN, NOT_IN,
     * BETWEEN, DATE_BETWEEN, etc. The number of values required depends on
     * the specific operation:</p>
     * <ul>
     *   <li><b>IN, NOT_IN:</b> Any number of values (1 or more)</li>
     *   <li><b>BETWEEN, DATE_BETWEEN:</b> Exactly 2 values (start and end)</li>
     * </ul>
     *
     * <p><b>Mutual exclusivity:</b> Either {@code value} or {@code values} should be set,
     * but not both, depending on the operation requirements.</p>
     *
     */
    private List<Object> values;

    /**
     * Flag indicating whether string operations should be case-sensitive.
     *
     * <p>This flag affects string-based operations such as LIKE, CONTAINS,
     * STARTS_WITH, and ENDS_WITH. When set to {@code false} (default), string
     * comparisons will be case-insensitive. When set to {@code true}, comparisons
     * will respect character case.</p>
     *
     * <p><b>Default value:</b> {@code false} (case-insensitive)</p>
     *
     * <p><b>Example behavior:</b></p>
     * <ul>
     *   <li><b>caseSensitive = false:</b> "John" matches "john", "JOHN", "John"</li>
     *   <li><b>caseSensitive = true:</b> "John" matches only "John"</li>
     * </ul>
     *
     */
    @Builder.Default
    private boolean caseSensitive = false;

    /**
     * Flag indicating whether the filter condition should be negated.
     *
     * <p>When set to {@code true}, the entire predicate built from this criteria
     * will be wrapped with a NOT condition. This provides a convenient way to
     * create inverse filters without defining separate "NOT_*" operations.</p>
     *
     * <p><b>Default value:</b> {@code false} (no negation)</p>
     *
     * <p><b>Example behavior:</b></p>
     * <ul>
     *   <li><b>negate = false:</b> {@code field = 'value'}</li>
     *   <li><b>negate = true:</b> {@code NOT(field = 'value')} which is equivalent to {@code field != 'value'}</li>
     * </ul>
     *
     * <p>This flag is applied after the base predicate is constructed, allowing
     * for easy negation of complex operations like BETWEEN, IN, or string matching.</p>
     *
     */
    @Builder.Default
    private boolean negate = false;
}
