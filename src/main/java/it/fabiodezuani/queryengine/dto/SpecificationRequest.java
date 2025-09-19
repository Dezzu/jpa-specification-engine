package it.fabiodezuani.queryengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object representing a complete filter request for dynamic JPA query construction.
 *
 * <p>This class serves as a comprehensive container for complex filtering requirements,
 * supporting both individual filter criteria and grouped filter conditions. It enables
 * the construction of sophisticated queries with multiple levels of logical grouping
 * and different operators at each level.</p>
 *
 * <p>The class supports two levels of filter organization:</p>
 * <ul>
 *   <li><b>Individual filters:</b> Direct {@link FilterCriteria} that are combined using
 *       the {@code useAndOperator} flag</li>
 *   <li><b>Filter groups:</b> Collections of {@link FilterGroup} objects that can have
 *       their own internal logic and are combined using {@code useAndOperatorForGroups}</li>
 * </ul>
 *
 * <p><b>Query Structure:</b><br>
 * The final query combines individual filters and filter groups according to this structure:</p>
 * <pre>{@code
 * (filter1 AND/OR filter2 AND/OR filter3) AND/OR
 * (group1) AND/OR (group2) AND/OR (group3)
 * }</pre>
 *
 * <p><b>Usage Examples:</b></p>
 *
 * <p><b>Simple filter request:</b></p>
 * <pre>{@code
 * SpecificationRequest request = SpecificationRequest.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder()
 *             .field("status")
 *             .operation(FilterOperation.EQUALS)
 *             .value("ACTIVE")
 *             .build(),
 *         FilterCriteria.builder()
 *             .field("age")
 *             .operation(FilterOperation.GREATER_THAN)
 *             .value(18)
 *             .build()
 *     ))
 *     .useAndOperator(true)
 *     .build();
 * // Results in: status = 'ACTIVE' AND age > 18
 * }</pre>
 *
 * <p><b>Complex grouped filter request:</b></p>
 * <pre>{@code
 * // Group 1: (firstName LIKE 'John%' OR lastName LIKE 'Smith%')
 * FilterGroup nameGroup = FilterGroup.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder()
 *             .field("firstName")
 *             .operation(FilterOperation.STARTS_WITH)
 *             .value("John")
 *             .build(),
 *         FilterCriteria.builder()
 *             .field("lastName")
 *             .operation(FilterOperation.STARTS_WITH)
 *             .value("Smith")
 *             .build()
 *     ))
 *     .useAndOperator(false) // OR logic within group
 *     .build();
 *
 * // Group 2: (department = 'IT' AND salary > 50000)
 * FilterGroup jobGroup = FilterGroup.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder()
 *             .field("department")
 *             .operation(FilterOperation.EQUALS)
 *             .value("IT")
 *             .build(),
 *         FilterCriteria.builder()
 *             .field("salary")
 *             .operation(FilterOperation.GREATER_THAN)
 *             .value(50000)
 *             .build()
 *     ))
 *     .useAndOperator(true) // AND logic within group
 *     .build();
 *
 * SpecificationRequest complexRequest = SpecificationRequest.builder()
 *     .filterGroups(Arrays.asList(nameGroup, jobGroup))
 *     .useAndOperatorForGroups(true) // AND between groups
 *     .build();
 *
 * // Results in: (firstName LIKE 'John%' OR lastName LIKE 'Smith%') AND
 * //             (department = 'IT' AND salary > 50000)
 * }</pre>
 *
 * <p><b>Mixed filter request:</b></p>
 * <pre>{@code
 * SpecificationRequest mixedRequest = SpecificationRequest.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder()
 *             .field("isActive")
 *             .operation(FilterOperation.EQUALS)
 *             .value(true)
 *             .build()
 *     ))
 *     .filterGroups(Arrays.asList(nameGroup))
 *     .useAndOperator(true)          // AND for individual filters
 *     .useAndOperatorForGroups(true) // AND between groups and individual filters
 *     .build();
 *
 * // Results in: (isActive = true) AND (firstName LIKE 'John%' OR lastName LIKE 'Smith%')
 * }</pre>
 *
 * <p>The class provides convenience methods for dynamically building filter requests
 * and supports method chaining through the builder pattern. All collections are
 * initialized as empty lists to prevent null pointer exceptions.</p>
 *
 * @author Fabio De Zuani
 * @see FilterCriteria
 * @see FilterGroup
 * @see FilterOperation
 * @see it.fabiodezuani.queryengine.builder.SpecificationBuilder
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationRequest {

    /**
     * List of individual filter criteria to be applied directly to the query.
     *
     * <p>These filters represent individual filtering conditions that are combined
     * using the logical operator specified by {@code useAndOperator}. Individual
     * filters are processed before filter groups and are combined with the groups
     * using {@code useAndOperatorForGroups}.</p>
     *
     * <p><b>Default value:</b> Empty {@code ArrayList<FilterCriteria>} to prevent
     * null pointer exceptions during list operations.</p>
     *
     * <p><b>Combination logic:</b><br>
     * Individual filters are combined as: {@code filter1 AND/OR filter2 AND/OR filter3}
     * based on the {@code useAndOperator} flag.</p>
     *
     * @see FilterCriteria
     */
    @Builder.Default
    private List<FilterCriteria> filters = new ArrayList<>();

    /**
     * Logical operator for combining individual filter criteria.
     *
     * <p>This flag determines how multiple {@link FilterCriteria} in the
     * {@code filters} list are logically combined with each other:</p>
     *
     * <ul>
     *   <li><b>{@code true} (default):</b> Use AND operator - all individual filters must be satisfied</li>
     *   <li><b>{@code false}:</b> Use OR operator - at least one individual filter must be satisfied</li>
     * </ul>
     *
     * <p><b>Default value:</b> {@code true} (AND operator)</p>
     *
     * <p><b>Scope:</b> This operator only affects the combination of individual filters.
     * It does not affect how filter groups are combined internally or how groups
     * are combined with individual filters.</p>
     *
     */
    @Builder.Default
    private boolean useAndOperator = true;

    /**
     * List of filter groups for complex query construction.
     *
     * <p>Each {@link FilterGroup} represents a collection of related filter criteria
     * that are combined using their own internal logical operator. Filter groups
     * enable the creation of nested logical conditions and complex query structures.</p>
     *
     * <p>Filter groups are combined with each other and with individual filters using
     * the operator specified by {@code useAndOperatorForGroups}. Each group maintains
     * its internal logical structure while participating in the overall query logic.</p>
     *
     * <p><b>Default value:</b> Empty {@code ArrayList<FilterGroup>} to prevent
     * null pointer exceptions during list operations.</p>
     *
     * <p><b>Processing order:</b><br>
     * Filter groups are processed after individual filters and combined according to
     * the overall query structure.</p>
     *
     * @see FilterGroup
     */
    @Builder.Default
    private List<FilterGroup> filterGroups = new ArrayList<>();

    /**
     * Logical operator for combining filter groups and individual filters.
     *
     * <p>This flag determines how filter groups are combined with each other and
     * with individual filters to form the final query predicate:</p>
     *
     * <ul>
     *   <li><b>{@code true} (default):</b> Use AND operator - all groups and individual filters must be satisfied</li>
     *   <li><b>{@code false}:</b> Use OR operator - at least one group or individual filter must be satisfied</li>
     * </ul>
     *
     * <p><b>Default value:</b> {@code true} (AND operator)</p>
     *
     * <p><b>Query structure example:</b></p>
     * <ul>
     *   <li><b>AND (true):</b> {@code (individualFilters) AND (group1) AND (group2)}</li>
     *   <li><b>OR (false):</b> {@code (individualFilters) OR (group1) OR (group2)}</li>
     * </ul>
     *
     * <p><b>Scope:</b> This operator affects the top-level combination of all filter
     * elements (individual filters and groups) but does not affect internal group logic.</p>
     *
     */
    @Builder.Default
    private boolean useAndOperatorForGroups = true;

    /**
     * Adds a filter group to the current specification request.
     *
     * <p>This convenience method allows for dynamic addition of filter groups
     * during request construction. The added group will be combined with existing
     * filters and groups using the operator specified by {@code useAndOperatorForGroups}.</p>
     *
     * @param filterGroup the filter group to add; must not be null
     * @throws IllegalArgumentException if filterGroup is null
     */
    public void addFilterGroup(FilterGroup filterGroup) {
        this.filterGroups.add(filterGroup);
    }

    /**
     * Adds an individual filter criterion to the current specification request.
     *
     * <p>This convenience method allows for dynamic addition of individual filter
     * criteria during request construction. The added filter will be combined with
     * existing individual filters using the operator specified by {@code useAndOperator}.</p>
     *
     * @param filterCriteria the filter criterion to add; must not be null
     * @throws IllegalArgumentException if filterCriteria is null
     */
    public void addFilter(FilterCriteria filterCriteria) {
        this.filters.add(filterCriteria);
    }

    /**
     * Adds multiple filter criteria to the current specification request.
     *
     * <p>This convenience method allows for bulk addition of filter criteria
     * during request construction. All added filters will be combined with existing
     * individual filters using the operator specified by {@code useAndOperator}.</p>
     *
     * <p><b>Note:</b> This method performs a shallow copy of the provided list.
     * Modifications to the original list after this method call will not affect
     * the specification request.</p>
     *
     * @param filterCriteriaList the list of filter criteria to add; must not be null
     * @throws IllegalArgumentException if filterCriteriaList is null
     */
    public void addFilters(List<FilterCriteria> filterCriteriaList) {
        this.filters.addAll(filterCriteriaList);
    }
}