package it.fabiodezuani.queryengine.util;
import it.fabiodezuani.queryengine.dto.FilterCriteria;
import it.fabiodezuani.queryengine.dto.FilterGroup;
import it.fabiodezuani.queryengine.dto.FilterOperation;
import it.fabiodezuani.queryengine.dto.SpecificationRequest;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class providing factory methods and convenience builders for creating filter requests.
 *
 * <p>This utility class simplifies the creation of {@link FilterCriteria}, {@link FilterGroup},
 * and {@link SpecificationRequest} objects by providing concise factory methods with sensible
 * defaults. It reduces boilerplate code and improves readability when building complex filter
 * queries programmatically.</p>
 *
 * <p>The class follows the static utility pattern and provides methods for:</p>
 * <ul>
 *   <li><b>Filter Criteria Creation:</b> Factory methods for common filter operations</li>
 *   <li><b>Filter Group Creation:</b> Convenience methods for grouping related filters</li>
 *   <li><b>Specification Request Building:</b> High-level builders for complete filter requests</li>
 *   <li><b>Common Operation Shortcuts:</b> Specialized methods for frequently used operations</li>
 * </ul>
 *
 * <p><b>Usage Benefits:</b></p>
 * <ul>
 *   <li>Reduces verbose builder syntax for simple cases</li>
 *   <li>Provides type-safe method signatures</li>
 *   <li>Improves code readability and maintainability</li>
 *   <li>Offers consistent API for common filtering patterns</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 *
 * <p><b>Simple filter creation:</b></p>
 * <pre>{@code
 * // Traditional approach
 * FilterCriteria criteria = FilterCriteria.builder()
 *     .field("username")
 *     .operation(FilterOperation.EQUALS)
 *     .value("john.doe")
 *     .build();
 *
 * // Using utility method
 * FilterCriteria criteria = SpecificationUtils.equals("username", "john.doe");
 * }</pre>
 *
 * <p><b>Complex query construction:</b></p>
 * <pre>{@code
 * // Create filter groups
 * FilterGroup nameGroup = SpecificationUtils.createOrGroup(
 *     SpecificationUtils.like("firstName", "%John%"),
 *     SpecificationUtils.like("lastName", "%Smith%")
 * );
 *
 * FilterGroup statusGroup = SpecificationUtils.createAndGroup(
 *     SpecificationUtils.equals("isActive", true),
 *     SpecificationUtils.in("role", "ADMIN", "MANAGER")
 * );
 *
 * // Combine into final request
 * SpecificationRequest request = SpecificationUtils.createGroupRequest(true, nameGroup, statusGroup);
 * }</pre>
 *
 * <p><b>Quick request building:</b></p>
 * <pre>{@code
 * // AND request with multiple conditions
 * SpecificationRequest andRequest = SpecificationUtils.createAndRequest(
 *     SpecificationUtils.equals("department", "IT"),
 *     SpecificationUtils.between("salary", 50000, 100000),
 *     SpecificationUtils.isNotNull("email")
 * );
 *
 * // OR request for alternative conditions
 * SpecificationRequest orRequest = SpecificationUtils.createOrRequest(
 *     SpecificationUtils.equals("priority", "HIGH"),
 *     SpecificationUtils.equals("urgent", true)
 * );
 * }</pre>
 *
 * <p><b>Thread Safety:</b><br>
 * All methods in this class are static and stateless, making them inherently thread-safe
 * and suitable for use in concurrent environments.</p>
 *
 * <p><b>Design Pattern:</b><br>
 * This class implements the Static Factory Method pattern, providing alternative constructors
 * with more expressive names than traditional constructors or builders.</p>
 *
 * @author Fabio De Zuani
 * @version 1.0.0
 * @since 19/09/2025
 * @see FilterCriteria
 * @see FilterGroup
 * @see SpecificationRequest
 * @see FilterOperation
 */
public class SpecificationUtils {

    /**
     * Creates a simple filter criterion with a single value.
     *
     * <p>This is the base factory method for creating {@link FilterCriteria} objects
     * with single values. It's used internally by other utility methods and can be
     * used directly when the specific operation shortcut methods are not available.</p>
     *
     * @param field the entity field name to filter on; must not be null or empty
     * @param operation the filter operation to perform; must not be null
     * @param value the value to use in the filter operation; may be null for null-based operations
     * @return a new FilterCriteria instance with the specified parameters
     * @throws IllegalArgumentException if field is null/empty or operation is null
     * @since 19/09/2025
     */
    public static FilterCriteria createFilter(String field, FilterOperation operation, Object value) {
        return FilterCriteria.builder()
                .field(field)
                .operation(operation)
                .value(value)
                .build();
    }

    /**
     * Creates a filter criterion with multiple values for collection-based operations.
     *
     * <p>This factory method is specifically designed for operations that require multiple
     * values such as {@link FilterOperation#IN}, {@link FilterOperation#NOT_IN}, and
     * {@link FilterOperation#BETWEEN}. The values list is directly assigned to the
     * {@code values} field of the FilterCriteria.</p>
     *
     * @param field the entity field name to filter on; must not be null or empty
     * @param operation the filter operation to perform; should be a multi-value operation
     * @param values the list of values for the operation; must not be null but may be empty
     * @return a new FilterCriteria instance with the specified parameters
     * @throws IllegalArgumentException if field is null/empty, operation is null, or values is null
     * @since 19/09/2025
     */
    public static FilterCriteria createFilter(String field, FilterOperation operation, List<Object> values) {
        return FilterCriteria.builder()
                .field(field)
                .operation(operation)
                .values(values)
                .build();
    }

    /**
     * Creates an equality filter criterion.
     *
     * <p>This convenience method creates a filter for exact value matching using
     * {@link FilterOperation#EQUALS}. It's the most commonly used filter type
     * and provides a clean, readable syntax for equality comparisons.</p>
     *
     * @param field the entity field name to filter on; must not be null or empty
     * @param value the value to match exactly; may be null for null equality checks
     * @return a FilterCriteria for equality comparison
     * @since 19/09/2025
     */
    public static FilterCriteria equals(String field, Object value) {
        return createFilter(field, FilterOperation.EQUALS, value);
    }

    /**
     * Creates a case-sensitive LIKE filter criterion.
     *
     * <p>This method creates a pattern matching filter using {@link FilterOperation#LIKE}.
     * The LIKE operation supports SQL wildcard characters (% and _) and is case-sensitive.
     * For case-insensitive matching, use {@link #ilike(String, String)} instead.</p>
     *
     * @param field the entity field name to filter on; must not be null or empty and should be a string field
     * @param value the pattern to match; may include SQL wildcards (% for multiple characters, _ for single character)
     * @return a FilterCriteria for case-sensitive pattern matching
     * @since 19/09/2025
     */
    public static FilterCriteria like(String field, String value) {
        return createFilter(field, FilterOperation.LIKE, value);
    }

    /**
     * Creates a case-insensitive LIKE filter criterion (PostgreSQL specific).
     *
     * <p>This method creates a case-insensitive pattern matching filter using
     * {@link FilterOperation#ILIKE}. This operation is specific to PostgreSQL
     * databases. For other databases, consider using {@link #like(String, String)}
     * with appropriate case conversion or the {@code caseSensitive} flag in FilterCriteria.</p>
     *
     * @param field the entity field name to filter on; must not be null or empty and should be a string field
     * @param value the pattern to match case-insensitively; may include SQL wildcards
     * @return a FilterCriteria for case-insensitive pattern matching
     * @since 19/09/2025
     */
    public static FilterCriteria ilike(String field, String value) {
        return createFilter(field, FilterOperation.ILIKE, value);
    }

    /**
     * Creates an IN filter criterion for collection membership testing.
     *
     * <p>This method creates a filter that matches records where the field value
     * exists within the provided collection of values. It uses varargs for
     * convenience, allowing values to be passed directly as method arguments.</p>
     *
     * @param field the entity field name to filter on; must not be null or empty
     * @param values the values to check for membership; at least one value should be provided
     * @return a FilterCriteria for collection membership testing
     * @since 19/09/2025
     */
    public static FilterCriteria in(String field, Object... values) {
        return createFilter(field, FilterOperation.IN, Arrays.asList(values));
    }

    /**
     * Creates a BETWEEN filter criterion for range testing.
     *
     * <p>This method creates a filter that matches records where the field value
     * falls within the specified range (inclusive). The range is defined by
     * {@code from} and {@code to} parameters, where both boundaries are included
     * in the match.</p>
     *
     * @param field the entity field name to filter on; must not be null or empty and should be a comparable field
     * @param from the lower boundary of the range (inclusive); must be comparable with the field type
     * @param to the upper boundary of the range (inclusive); must be comparable with the field type
     * @return a FilterCriteria for range testing
     * @since 19/09/2025
     */
    public static FilterCriteria between(String field, Object from, Object to) {
        return createFilter(field, FilterOperation.BETWEEN, Arrays.asList(from, to));
    }

    /**
     * Creates an IS_NULL filter criterion for null value testing.
     *
     * <p>This method creates a filter that matches records where the specified
     * field has a null value. No value parameter is needed since the operation
     * specifically tests for null conditions.</p>
     *
     * @param field the entity field name to test for null; must not be null or empty
     * @return a FilterCriteria for null value testing
     * @since 19/09/2025
     */
    public static FilterCriteria isNull(String field) {
        return FilterCriteria.builder()
                .field(field)
                .operation(FilterOperation.IS_NULL)
                .build();
    }

    /**
     * Creates an IS_NOT_NULL filter criterion for non-null value testing.
     *
     * <p>This method creates a filter that matches records where the specified
     * field has a non-null value. This is useful for filtering out records
     * with missing or undefined field values.</p>
     *
     * @param field the entity field name to test for non-null; must not be null or empty
     * @return a FilterCriteria for non-null value testing
     * @since 19/09/2025
     */
    public static FilterCriteria isNotNull(String field) {
        return FilterCriteria.builder()
                .field(field)
                .operation(FilterOperation.IS_NOT_NULL)
                .build();
    }

    /**
     * Creates a specification request with AND logic for combining filters.
     *
     * <p>This convenience method creates a {@link SpecificationRequest} where all
     * provided filter criteria must be satisfied (AND logic). This is the most
     * common type of filter request for restrictive queries.</p>
     *
     * @param filters the filter criteria to combine with AND logic; may be empty
     * @return a SpecificationRequest with AND logic for the provided filters
     * @since 19/09/2025
     */
    public static SpecificationRequest createAndRequest(FilterCriteria... filters) {
        return SpecificationRequest.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(true)
                .build();
    }

    /**
     * Creates a specification request with OR logic for combining filters.
     *
     * <p>This convenience method creates a {@link SpecificationRequest} where any
     * of the provided filter criteria can be satisfied (OR logic). This is useful
     * for broad queries that match multiple alternative conditions.</p>
     *
     * @param filters the filter criteria to combine with OR logic; may be empty
     * @return a SpecificationRequest with OR logic for the provided filters
     * @since 19/09/2025
     */
    public static SpecificationRequest createOrRequest(FilterCriteria... filters) {
        return SpecificationRequest.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(false)
                .build();
    }

    /**
     * Creates a filter group with AND logic for internal filter combination.
     *
     * <p>This method creates a {@link FilterGroup} where all filters within the
     * group must be satisfied. Filter groups are useful for creating nested
     * logical conditions in complex queries.</p>
     *
     * @param filters the filter criteria to group with AND logic; may be empty
     * @return a FilterGroup with internal AND logic
     * @since 19/09/2025
     */
    public static FilterGroup createAndGroup(FilterCriteria... filters) {
        return FilterGroup.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(true)
                .build();
    }

    /**
     * Creates a filter group with OR logic for internal filter combination.
     *
     * <p>This method creates a {@link FilterGroup} where any filter within the
     * group can be satisfied. This is particularly useful for creating alternative
     * condition groups within larger query structures.</p>
     *
     * @param filters the filter criteria to group with OR logic; may be empty
     * @return a FilterGroup with internal OR logic
     * @since 19/09/2025
     */
    public static FilterGroup createOrGroup(FilterCriteria... filters) {
        return FilterGroup.builder()
                .filters(Arrays.asList(filters))
                .useAndOperator(false)
                .build();
    }

    /**
     * Creates a specification request with filter groups.
     *
     * <p>This method creates a {@link SpecificationRequest} that operates on
     * filter groups rather than individual filters. The groups are combined
     * using the specified logical operator, allowing for complex nested
     * query structures.</p>
     *
     * @param useAndForGroups {@code true} to combine groups with AND logic, {@code false} for OR logic
     * @param groups the filter groups to combine; may be empty
     * @return a SpecificationRequest with the specified group combination logic
     * @since 19/09/2025
     */
    public static SpecificationRequest createGroupRequest(boolean useAndForGroups, FilterGroup... groups) {
        return SpecificationRequest.builder()
                .filterGroups(Arrays.asList(groups))
                .useAndOperatorForGroups(useAndForGroups)
                .build();
    }
}

