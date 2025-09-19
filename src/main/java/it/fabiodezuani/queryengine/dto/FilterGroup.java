package it.fabiodezuani.queryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object representing a logical group of filter criteria with a specified operator.
 *
 * <p>This class allows combining multiple {@link FilterCriteria} instances using either
 * AND or OR logical operators to create complex filtering conditions. Filter groups
 * can be used to build sophisticated queries with multiple conditions that need to
 * be evaluated together.</p>
 *
 * <p>The class supports two logical operators:</p>
 * <ul>
 *   <li><b>AND operator:</b> All filter criteria must be satisfied (default behavior)</li>
 *   <li><b>OR operator:</b> At least one filter criteria must be satisfied</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // AND group - user must be active AND have admin role
 * FilterGroup andGroup = FilterGroup.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder()
 *             .field("status")
 *             .operation(FilterOperation.EQUALS)
 *             .value("ACTIVE")
 *             .build(),
 *         FilterCriteria.builder()
 *             .field("role")
 *             .operation(FilterOperation.EQUALS)
 *             .value("ADMIN")
 *             .build()
 *     ))
 *     .useAndOperator(true)  // default
 *     .build();
 *
 * // OR group - find users by email OR username
 * FilterGroup orGroup = FilterGroup.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder()
 *             .field("email")
 *             .operation(FilterOperation.CONTAINS)
 *             .value("john")
 *             .build(),
 *         FilterCriteria.builder()
 *             .field("username")
 *             .operation(FilterOperation.CONTAINS)
 *             .value("john")
 *             .build()
 *     ))
 *     .useAndOperator(false)  // OR logic
 *     .build();
 *
 * // Complex query with nested conditions
 * FilterGroup complexGroup = FilterGroup.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder()
 *             .field("department")
 *             .operation(FilterOperation.IN)
 *             .values(Arrays.asList("IT", "FINANCE"))
 *             .build(),
 *         FilterCriteria.builder()
 *             .field("salary")
 *             .operation(FilterOperation.GREATER_THAN)
 *             .value(50000)
 *             .build(),
 *         FilterCriteria.builder()
 *             .field("startDate")
 *             .operation(FilterOperation.DATE_AFTER)
 *             .value(LocalDate.of(2020, 1, 1))
 *             .build()
 *     ))
 *     .useAndOperator(true)
 *     .build();
 * }</pre>
 *
 * <p><b>SQL Query Translation:</b></p>
 * <ul>
 *   <li><b>AND group:</b> {@code WHERE (condition1 AND condition2 AND condition3)}</li>
 *   <li><b>OR group:</b> {@code WHERE (condition1 OR condition2 OR condition3)}</li>
 * </ul>
 *
 * <p>Filter groups can be processed by {@link it.fabiodezuani.queryengine.builder.SpecificationBuilder} implementations
 * to create compound JPA Specifications. Empty filter lists are handled gracefully
 * and typically result in no filtering conditions being applied.</p>
 *
 * <p>This class uses Lombok annotations for automatic generation of getters, setters,
 * constructors, and builder pattern implementation. The filters list is initialized
 * as an empty ArrayList to prevent null pointer exceptions.</p>
 *
 * @author Fabio De Zuani
 * @see FilterCriteria
 * @see it.fabiodezuani.queryengine.builder.SpecificationBuilder
 * @see org.springframework.data.jpa.domain.Specification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterGroup {

    /**
     * The list of filter criteria to be combined using the specified logical operator.
     *
     * <p>Each {@link FilterCriteria} in this list represents an individual filtering
     * condition. The list can contain any number of criteria (including zero), and
     * all criteria will be combined using either AND or OR logic based on the
     * {@code useAndOperator} flag.</p>
     *
     * <p><b>Behavior with empty list:</b><br>
     * When the list is empty, no filtering conditions are applied, effectively
     * resulting in no restrictions on the query results.</p>
     *
     * <p><b>Default value:</b> Empty {@code ArrayList<FilterCriteria>} to prevent
     * null pointer exceptions during list operations.</p>
     *
     * <p><b>Thread Safety:</b><br>
     * The default ArrayList implementation is not thread-safe. If concurrent
     * modification is expected, consider using thread-safe alternatives or
     * external synchronization.</p>
     *
     */
    @Builder.Default
    private List<FilterCriteria> filters = new ArrayList<>();

    /**
     * Flag determining the logical operator used to combine filter criteria.
     *
     * <p>This boolean flag controls how multiple {@link FilterCriteria} within
     * the {@code filters} list are logically combined:</p>
     *
     * <ul>
     *   <li><b>{@code true} (default):</b> Use AND operator - all conditions must be satisfied</li>
     *   <li><b>{@code false}:</b> Use OR operator - at least one condition must be satisfied</li>
     * </ul>
     *
     * <p><b>Default value:</b> {@code true} (AND operator)</p>
     *
     * <p><b>Logical behavior examples:</b></p>
     * <ul>
     *   <li><b>AND (true):</b> {@code condition1 AND condition2 AND condition3}</li>
     *   <li><b>OR (false):</b> {@code condition1 OR condition2 OR condition3}</li>
     * </ul>
     *
     * <p><b>Single filter behavior:</b><br>
     * When only one filter exists in the list, this flag has no effect as there
     * are no multiple conditions to combine.</p>
     *
     * <p><b>Empty filter behavior:</b><br>
     * When the filters list is empty, this flag has no effect as there are no
     * conditions to evaluate.</p>
     *
     */
    @Builder.Default
    private boolean useAndOperator = true;
}
