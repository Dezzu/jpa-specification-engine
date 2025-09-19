package it.fabiodezuani.queryengine.service;

import it.fabiodezuani.queryengine.builder.SpecificationBuilder;
import it.fabiodezuani.queryengine.dto.FilterCriteria;
import it.fabiodezuani.queryengine.dto.FilterGroup;
import it.fabiodezuani.queryengine.dto.FilterOperation;
import it.fabiodezuani.queryengine.dto.SpecificationRequest;
import it.fabiodezuani.queryengine.exception.SpecificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Main engine for dynamic JPA Specification creation and management.
 *
 * <p>This service acts as the central orchestrator for converting complex filter requests
 * into executable JPA Specifications. It coordinates multiple {@link SpecificationBuilder}
 * implementations and handles the logical combination of individual filters and filter groups
 * according to specified operators.</p>
 *
 * <p>The engine supports sophisticated querying scenarios including:</p>
 * <ul>
 *   <li><b>Individual filter processing:</b> Direct conversion of {@link FilterCriteria} to Specifications</li>
 *   <li><b>Filter group processing:</b> Handling of {@link FilterGroup} objects with internal logic</li>
 *   <li><b>Mixed query construction:</b> Combining individual filters with filter groups</li>
 *   <li><b>Multi-level logical operators:</b> Support for different AND/OR logic at different levels</li>
 *   <li><b>Builder delegation:</b> Automatic selection of appropriate {@link SpecificationBuilder} for each operation</li>
 * </ul>
 *
 * <p><b>Architecture:</b><br>
 * The engine uses a list of {@code SpecificationBuilder} implementations that are automatically
 * injected by Spring's dependency injection. Each builder can declare support for specific
 * operations through the {@code supports(FilterCriteria)} method. The engine selects the
 * first matching builder for each filter criterion.</p>
 *
 * <p><b>Query Construction Process:</b></p>
 * <ol>
 *   <li>Process individual filters using the specified logical operator</li>
 *   <li>Process filter groups, respecting each group's internal logic</li>
 *   <li>Combine individual filters and groups using the top-level operator</li>
 *   <li>Return the final compound Specification</li>
 * </ol>
 *
 * <p><b>Usage Examples:</b></p>
 *
 * <p><b>Simple filter processing:</b></p>
 * <pre>{@code
 * @Autowired
 * private SpecificationEngine engine;
 *
 * @Autowired
 * private UserRepository userRepository;
 *
 * // Create request with individual filters
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
 *
 * Specification<User> spec = engine.createSpecification(request);
 * List<User> users = userRepository.findAll(spec);
 * }</pre>
 *
 * <p><b>Complex grouped filter processing:</b></p>
 * <pre>{@code
 * // Create filter groups
 * FilterGroup nameGroup = FilterGroup.builder()
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder().field("firstName").operation(FilterOperation.CONTAINS).value("John").build(),
 *         FilterCriteria.builder().field("lastName").operation(FilterOperation.CONTAINS).value("Smith").build()
 *     ))
 *     .useAndOperator(false) // OR within group
 *     .build();
 *
 * SpecificationRequest complexRequest = SpecificationRequest.builder()
 *     .filterGroups(Arrays.asList(nameGroup))
 *     .filters(Arrays.asList(
 *         FilterCriteria.builder().field("isActive").operation(FilterOperation.EQUALS).value(true).build()
 *     ))
 *     .useAndOperator(true)
 *     .useAndOperatorForGroups(true)
 *     .build();
 *
 * Specification<User> complexSpec = engine.createSpecification(complexRequest);
 * }</pre>
 *
 * <p><b>Simple utility usage:</b></p>
 * <pre>{@code
 * // Quick specification for simple equality checks
 * Specification<User> simpleSpec = engine.createSimpleSpecification("username", "john.doe");
 * Optional<User> user = userRepository.findOne(simpleSpec);
 * }</pre>
 *
 * <p>This service is thread-safe and can be used concurrently in multi-threaded environments.
 * It automatically integrates with Spring Boot's auto-configuration and is available as a
 * Spring service component.</p>
 *
 * @author Fabio De Zuani
 * @version 1.0.0
 * @since 19/09/2025
 * @see SpecificationBuilder
 * @see it.fabiodezuani.queryengine.builder.DefaultSpecificationBuilder
 * @see SpecificationRequest
 * @see FilterCriteria
 * @see FilterGroup
 * @see org.springframework.data.jpa.domain.Specification
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificationEngine {

    /**
     * List of specification builders used for creating individual filter specifications.
     *
     * <p>This list is automatically populated by Spring's dependency injection with all
     * available {@link SpecificationBuilder} implementations in the application context.
     * The engine uses these builders to delegate the creation of specific filter types
     * based on each builder's {@code supports(FilterCriteria)} method.</p>
     *
     * <p><b>Builder Selection:</b> When processing a filter criterion, the engine iterates
     * through this list and selects the first builder that declares support for the
     * given criteria. Builders should be ordered by specificity if multiple builders
     * might support the same criteria.</p>
     *
     * @since 19/09/2025
     */
    private final List<SpecificationBuilder<?>> builders;

    /**
     * Creates a complete JPA Specification from a specification request.
     *
     * <p>This is the main entry point for the engine. It processes both individual filters
     * and filter groups according to their respective logical operators and combines them
     * into a single executable Specification.</p>
     *
     * <p>The method handles empty requests gracefully by returning an unrestricted specification
     * that matches all records. Complex requests with multiple levels of logical grouping
     * are processed according to the specified operator precedence.</p>
     *
     * <p><b>Processing Logic:</b></p>
     * <ol>
     *   <li>Start with an unrestricted specification</li>
     *   <li>Process individual filters using {@code useAndOperator}</li>
     *   <li>Process filter groups using {@code useAndOperatorForGroups}</li>
     *   <li>Combine individual filters and groups based on {@code useAndOperatorForGroups}</li>
     * </ol>
     *
     * @param request the specification request containing filters and groups; must not be null
     * @param <T> the entity type for which the specification will be created
     * @return a JPA Specification representing the complete filter logic; never null
     * @throws SpecificationException if any filter criterion cannot be processed by available builders
     * @throws IllegalArgumentException if the request parameter is null
     * @since 19/09/2025
     */
    public <T> Specification<T> createSpecification(SpecificationRequest request) {
        log.debug("Creating specification for request with {} filters and {} groups",
                request.getFilters().size(), request.getFilterGroups().size());

        Specification<T> specification = Specification.unrestricted();

        // Process individual filters
        if (!request.getFilters().isEmpty()) {
            Specification<T> filtersSpec = createSpecificationFromFilters(
                    request.getFilters(), request.isUseAndOperator());
            specification = specification.and(filtersSpec);
        }

        // Process filter groups
        if (!request.getFilterGroups().isEmpty()) {
            Specification<T> groupsSpec = createSpecificationFromGroups(
                    request.getFilterGroups(), request.isUseAndOperatorForGroups());

            if (request.getFilters().isEmpty()) {
                specification = groupsSpec;
            } else {
                specification = request.isUseAndOperatorForGroups() ?
                        specification.and(groupsSpec) : specification.or(groupsSpec);
            }
        }

        return specification;
    }

    /**
     * Creates a JPA Specification from a list of individual filter criteria.
     *
     * <p>This method processes a list of {@link FilterCriteria} objects and combines them
     * using the specified logical operator. Each filter criterion is processed independently
     * and then combined with the others to form a compound specification.</p>
     *
     * <p>Empty filter lists are handled gracefully by returning an unrestricted specification.
     * Single-item lists return the specification for that single criterion without
     * additional logical operators.</p>
     *
     * @param filters the list of filter criteria to process; must not be null but may be empty
     * @param useAndOperator {@code true} to combine filters with AND logic, {@code false} for OR logic
     * @param <T> the entity type for which the specification will be created
     * @return a JPA Specification combining all filter criteria with the specified operator; never null
     * @throws SpecificationException if any filter criterion cannot be processed by available builders
     * @throws IllegalArgumentException if the filters parameter is null
     * @since 19/09/2025
     */
    public <T> Specification<T> createSpecificationFromFilters(List<FilterCriteria> filters, boolean useAndOperator) {
        if (filters.isEmpty()) {
            return Specification.unrestricted();
        }

        Specification<T> specification = createSingleSpecification(filters.get(0));

        for (int i = 1; i < filters.size(); i++) {
            Specification<T> nextSpec = createSingleSpecification(filters.get(i));
            specification = useAndOperator ?
                    specification.and(nextSpec) : specification.or(nextSpec);
        }

        return specification;
    }

    /**
     * Creates a JPA Specification from a list of filter groups.
     *
     * <p>This method processes a list of {@link FilterGroup} objects, respecting each group's
     * internal logical operator, and then combines the groups using the specified operator.
     * Each group is processed as an independent unit with its own internal logic.</p>
     *
     * <p>Empty group lists are handled gracefully by returning an unrestricted specification.
     * Each group's filters are processed according to that group's {@code useAndOperator} setting
     * before being combined with other groups.</p>
     *
     * @param groups the list of filter groups to process; must not be null but may be empty
     * @param useAndOperator {@code true} to combine groups with AND logic, {@code false} for OR logic
     * @param <T> the entity type for which the specification will be created
     * @return a JPA Specification combining all filter groups with the specified operator; never null
     * @throws SpecificationException if any filter criterion within any group cannot be processed
     * @throws IllegalArgumentException if the groups parameter is null
     * @since 19/09/2025
     */
    public <T> Specification<T> createSpecificationFromGroups(List<FilterGroup> groups, boolean useAndOperator) {
        if (groups.isEmpty()) {
            return Specification.unrestricted();
        }

        Specification<T> specification = createSpecificationFromFilters(
                groups.getFirst().getFilters(), groups.getFirst().isUseAndOperator());

        for (int i = 1; i < groups.size(); i++) {
            Specification<T> nextSpec = createSpecificationFromFilters(
                    groups.get(i).getFilters(), groups.get(i).isUseAndOperator());
            specification = useAndOperator ?
                    specification.and(nextSpec) : specification.or(nextSpec);
        }

        return specification;
    }

    /**
     * Creates a single JPA Specification from an individual filter criterion.
     *
     * <p>This method delegates the actual specification creation to the appropriate
     * {@link SpecificationBuilder} implementation. It finds the first builder in the
     * {@code builders} list that declares support for the given criteria and uses
     * that builder to create the specification.</p>
     *
     * <p>The method performs automatic type casting to ensure type safety while
     * maintaining flexibility with the generic builder system.</p>
     *
     * @param criteria the filter criterion to process; must not be null
     * @param <T> the entity type for which the specification will be created
     * @return a JPA Specification representing the single filter criterion; never null
     * @throws SpecificationException if no builder supports the given criteria or if specification creation fails
     * @throws IllegalArgumentException if the criteria parameter is null
     */
    @SuppressWarnings("unchecked")
    public <T> Specification<T> createSingleSpecification(FilterCriteria criteria) {
        log.debug("Creating specification for field: {}, operation: {}",
                criteria.getField(), criteria.getOperation());

        Optional<SpecificationBuilder<?>> builderOpt = builders.stream()
                .findFirst();

        if ( builderOpt.isEmpty() ) {
            throw new SpecificationException(
                    String.format("No builder found for operation: %s", criteria.getOperation()));
        }

        SpecificationBuilder<T> builder = (SpecificationBuilder<T>) builderOpt.get();
        return builder.build(criteria);
    }

    /**
     * Creates a simple equality-based JPA Specification for a single field and value.
     *
     * <p>This convenience method provides a quick way to create simple specifications
     * without constructing full {@link FilterCriteria} objects. It's particularly useful
     * for common use cases where only equality comparison is needed.</p>
     *
     * <p>The method internally creates a {@code FilterCriteria} with {@link FilterOperation#EQUALS}
     * and delegates to the standard specification building process. Case sensitivity and
     * negation flags use their default values (case-insensitive, non-negated).</p>
     *
     * <p><b>Use cases:</b></p>
     * <ul>
     *   <li>Simple entity lookups by ID or unique fields</li>
     *   <li>Basic filtering in REST endpoints</li>
     *   <li>Quick prototyping and testing scenarios</li>
     * </ul>
     *
     * @param field the entity field name to filter on; must not be null or empty
     * @param value the value to match exactly; may be null for null equality checks
     * @param <T> the entity type for which the specification will be created
     * @return a JPA Specification for equality comparison of the specified field and value; never null
     * @throws SpecificationException if the field name is invalid or specification creation fails
     * @throws IllegalArgumentException if the field parameter is null or empty
     * @since 19/09/2025
     */
    public <T> Specification<T> createSimpleSpecification(String field, Object value) {
        FilterCriteria criteria = FilterCriteria.builder()
                .field(field)
                .operation(FilterOperation.EQUALS)
                .value(value)
                .build();

        return createSingleSpecification(criteria);
    }
}
