package it.fabiodezuani.queryengine.builder;

import it.fabiodezuani.queryengine.exception.SpecificationException;
import it.fabiodezuani.queryengine.dto.FilterCriteria;
import it.fabiodezuani.queryengine.dto.FilterOperation;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Default implementation of {@link SpecificationBuilder} that provides comprehensive
 * JPA Criteria API query building capabilities for dynamic filtering operations.
 *
 * <p>This builder supports a wide range of filtering operations including equality,
 * comparison, string matching, null checks, date operations, and collection-based
 * filtering. It handles nested field paths using dot notation and provides
 * case-sensitive/insensitive string operations.</p>
 *
 * <p><b>Supported Operations:</b></p>
 * <ul>
 *   <li>Basic comparisons: EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, etc.</li>
 *   <li>String operations: LIKE, ILIKE, STARTS_WITH, ENDS_WITH, CONTAINS</li>
 *   <li>Collection operations: IN, NOT_IN</li>
 *   <li>Null checks: IS_NULL, IS_NOT_NULL</li>
 *   <li>Range operations: BETWEEN</li>
 *   <li>Date operations: DATE_EQUALS, DATE_BEFORE, DATE_AFTER, DATE_BETWEEN</li>
 * </ul>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * @Autowired
 * DefaultSpecificationBuilder<User> builder;
 *
 * FilterCriteria criteria = FilterCriteria.builder()
 *     .field("profile.firstName")
 *     .operation(FilterOperation.CONTAINS)
 *     .value("John")
 *     .build();
 *
 * Specification<User> spec = builder.build(criteria);
 * List<User> users = userRepository.findAll(spec);
 * }</pre>
 *
 * <p>The builder automatically handles type casting and provides detailed error
 * messages when operations fail. It supports nested entity relationships through
 * dot notation in field names (e.g., "user.profile.email").</p>
 *
 * @param <T> the root entity type for which specifications are built
 * @author Fabio De Zuani
 * @see SpecificationBuilder
 * @see FilterCriteria
 * @see FilterOperation
 * @see org.springframework.data.jpa.domain.Specification
 */
@Slf4j
@Component
public class DefaultSpecificationBuilder<T> implements SpecificationBuilder<T> {

    /**
     * Builds a JPA Specification from the provided filter criteria.
     *
     * <p>This method creates a dynamic JPA Criteria API predicate based on the
     * specified field, operation, and value(s). It supports nested field access
     * using dot notation and handles type conversions automatically.</p>
     *
     * <p>The method will apply negation to the predicate if the criteria's
     * {@code negate} flag is set to true.</p>
     *
     * @param criteria the filter criteria containing field, operation, and value information
     * @return a JPA Specification that can be used with Spring Data JPA repositories
     * @throws SpecificationException if the field path is invalid, the operation is unsupported,
     *                               or the value type is incompatible with the operation
     * @throws IllegalArgumentException if criteria is null
     */
    @Override
    public Specification<T> build(FilterCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            try {
                Path<?> fieldPath = getPath(root, criteria.getField());
                Predicate predicate = createPredicate(fieldPath, criteria, criteriaBuilder);

                return criteria.isNegate() ? criteriaBuilder.not(predicate) : predicate;
            } catch (Exception e) {
                log.error("Error building specification for field: {}, operation: {}",
                        criteria.getField(), criteria.getOperation(), e);
                throw new SpecificationException(
                        String.format("Error building specification for field: %s", criteria.getField()), e);
            }
        };
    }

    private Path<?> getPath(Root<T> root, String field) {
        String[] parts = field.split("\\.");
        Path<?> path = root;

        for (String part : parts) {
            if (path instanceof Root) {
                path = ((Root<?>) path).get(part);
            } else if (path instanceof Join) {
                path = ((Join<?, ?>) path).get(part);
            } else {
                path = path.get(part);
            }
        }

        return path;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate createPredicate(Path<?> fieldPath, FilterCriteria criteria, CriteriaBuilder cb) {
        FilterOperation operation = criteria.getOperation();
        Object value = criteria.getValue();

        switch (operation) {
            case EQUALS:
                return cb.equal(fieldPath, value);

            case NOT_EQUALS:
                return cb.notEqual(fieldPath, value);

            case GREATER_THAN:
                return cb.greaterThan((Path<Comparable>) fieldPath, (Comparable) value);

            case GREATER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo((Path<Comparable>) fieldPath, (Comparable) value);

            case LESS_THAN:
                return cb.lessThan((Path<Comparable>) fieldPath, (Comparable) value);

            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo((Path<Comparable>) fieldPath, (Comparable) value);

            case LIKE:
                return cb.like(cb.lower(cb.toString((Expression<Character>) fieldPath)), "%" + value + "%");

            case ILIKE:
                // PostgreSQL case insensitive LIKE
                return cb.like(cb.lower(cb.toString((Expression<Character>) fieldPath)),
                        ("%" + value + "%").toLowerCase());

            case STARTS_WITH:
                String startPattern = criteria.isCaseSensitive() ?
                        value + "%" : (value.toString().toLowerCase() + "%");
                Expression<String> startPath = criteria.isCaseSensitive() ?
                        (Expression<String>) fieldPath : cb.lower((Path<String>) fieldPath);
                return cb.like(startPath, startPattern);

            case ENDS_WITH:
                String endPattern = criteria.isCaseSensitive() ?
                        "%" + value : ("%" + value.toString().toLowerCase());
                Expression<String> endPath = criteria.isCaseSensitive() ?
                        (Expression<String>) fieldPath : cb.lower((Path<String>) fieldPath);
                return cb.like(endPath, endPattern);

            case CONTAINS:
                String containsPattern = criteria.isCaseSensitive() ?
                        "%" + value + "%" : ("%" + value.toString().toLowerCase() + "%");
                Expression<String> containsPath = criteria.isCaseSensitive() ?
                        (Expression<String>) fieldPath : cb.lower((Path<String>) fieldPath);
                return cb.like(containsPath, containsPattern);

            case IN:
                return fieldPath.in(criteria.getValues());

            case NOT_IN:
                return cb.not(fieldPath.in(criteria.getValues()));

            case IS_NULL:
                return cb.isNull(fieldPath);

            case IS_NOT_NULL:
                return cb.isNotNull(fieldPath);

            case BETWEEN:
                if (criteria.getValues() == null || criteria.getValues().size() != 2) {
                    throw new SpecificationException("BETWEEN operation requires exactly 2 values");
                }
                return cb.between((Path<Comparable>) fieldPath,
                        (Comparable) criteria.getValues().get(0),
                        (Comparable) criteria.getValues().get(1));

            case DATE_EQUALS:
                return createDatePredicate(fieldPath, value, cb, "equals");

            case DATE_BEFORE:
                return createDatePredicate(fieldPath, value, cb, "before");

            case DATE_AFTER:
                return createDatePredicate(fieldPath, value, cb, "after");

            case DATE_BETWEEN:
                if (criteria.getValues() == null || criteria.getValues().size() != 2) {
                    throw new SpecificationException("DATE_BETWEEN operation requires exactly 2 values");
                }
                return cb.between((Path<Comparable>) fieldPath,
                        (Comparable) criteria.getValues().get(0),
                        (Comparable) criteria.getValues().get(1));

            default:
                throw new SpecificationException("Unsupported operation: " + operation);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate createDatePredicate(Path<?> fieldPath, Object value, CriteriaBuilder cb, String operation) {
        if (value instanceof LocalDate) {
            LocalDate dateValue = (LocalDate) value;
            switch (operation) {
                case "equals":
                    return cb.equal((Path<LocalDate>) fieldPath, dateValue);
                case "before":
                    return cb.lessThan((Path<LocalDate>) fieldPath, dateValue);
                case "after":
                    return cb.greaterThan((Path<LocalDate>) fieldPath, dateValue);
            }
        } else if (value instanceof LocalDateTime) {
            LocalDateTime dateTimeValue = (LocalDateTime) value;
            switch (operation) {
                case "equals":
                    return cb.equal((Path<LocalDateTime>) fieldPath, dateTimeValue);
                case "before":
                    return cb.lessThan((Path<LocalDateTime>) fieldPath, dateTimeValue);
                case "after":
                    return cb.greaterThan((Path<LocalDateTime>) fieldPath, dateTimeValue);
            }
        } else if (value instanceof Date) {
            Date dateValue = (Date) value;
            switch (operation) {
                case "equals":
                    return cb.equal((Path<Date>) fieldPath, dateValue);
                case "before":
                    return cb.lessThan((Path<Date>) fieldPath, dateValue);
                case "after":
                    return cb.greaterThan((Path<Date>) fieldPath, dateValue);
            }
        }

        throw new SpecificationException("Unsupported date type: " + value.getClass().getSimpleName());
    }
}
