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
 * Implementazione di default per la creazione delle Specification
 */
@Slf4j
@Component
public class DefaultSpecificationBuilder<T> implements SpecificationBuilder<T> {

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

    @Override
    public boolean supports(FilterCriteria criteria) {
        return criteria.getOperation() != null;
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
