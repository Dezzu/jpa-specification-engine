package it.fabiodezuani.queryengine.dto;

/**
 * Enumeration defining all supported filter operations for dynamic JPA query construction.
 *
 * <p>This enum represents the complete set of filtering operations that can be applied
 * to entity fields when building dynamic queries with JPA Criteria API. Each operation
 * corresponds to a specific type of comparison or matching logic that will be translated
 * into appropriate SQL predicates.</p>
 *
 * <p>The operations are categorized into several groups based on their functionality:</p>
 *
 * <p><b>Basic Comparison Operations:</b></p>
 * <ul>
 *   <li>{@link #EQUALS} - Exact equality comparison</li>
 *   <li>{@link #NOT_EQUALS} - Inequality comparison</li>
 *   <li>{@link #GREATER_THAN} - Greater than comparison for comparable values</li>
 *   <li>{@link #GREATER_THAN_OR_EQUAL} - Greater than or equal comparison</li>
 *   <li>{@link #LESS_THAN} - Less than comparison for comparable values</li>
 *   <li>{@link #LESS_THAN_OR_EQUAL} - Less than or equal comparison</li>
 * </ul>
 *
 * <p><b>String Pattern Matching Operations:</b></p>
 * <ul>
 *   <li>{@link #LIKE} - SQL LIKE pattern matching (case-sensitive)</li>
 *   <li>{@link #ILIKE} - Case-insensitive LIKE (PostgreSQL specific)</li>
 *   <li>{@link #STARTS_WITH} - String starts with pattern matching</li>
 *   <li>{@link #ENDS_WITH} - String ends with pattern matching</li>
 *   <li>{@link #CONTAINS} - String contains substring matching</li>
 * </ul>
 *
 * <p><b>Collection Operations:</b></p>
 * <ul>
 *   <li>{@link #IN} - Value exists in a collection</li>
 *   <li>{@link #NOT_IN} - Value does not exist in a collection</li>
 * </ul>
 *
 * <p><b>Null Check Operations:</b></p>
 * <ul>
 *   <li>{@link #IS_NULL} - Field value is null</li>
 *   <li>{@link #IS_NOT_NULL} - Field value is not null</li>
 * </ul>
 *
 * <p><b>Range Operations:</b></p>
 * <ul>
 *   <li>{@link #BETWEEN} - Value falls within a specified range</li>
 * </ul>
 *
 * <p><b>Date-Specific Operations:</b></p>
 * <ul>
 *   <li>{@link #DATE_EQUALS} - Date equality (ignoring time components)</li>
 *   <li>{@link #DATE_BEFORE} - Date before comparison</li>
 *   <li>{@link #DATE_AFTER} - Date after comparison</li>
 *   <li>{@link #DATE_BETWEEN} - Date falls within a date range</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Basic comparison
 * FilterCriteria.builder()
 *     .field("age")
 *     .operation(FilterOperation.GREATER_THAN)
 *     .value(18)
 *     .build();
 *
 * // String pattern matching
 * FilterCriteria.builder()
 *     .field("name")
 *     .operation(FilterOperation.CONTAINS)
 *     .value("John")
 *     .caseSensitive(false)
 *     .build();
 *
 * // Collection operation
 * FilterCriteria.builder()
 *     .field("status")
 *     .operation(FilterOperation.IN)
 *     .values(Arrays.asList("ACTIVE", "PENDING"))
 *     .build();
 *
 * // Date operation
 * FilterCriteria.builder()
 *     .field("createdDate")
 *     .operation(FilterOperation.DATE_AFTER)
 *     .value(LocalDate.of(2023, 1, 1))
 *     .build();
 * }</pre>
 *
 * <p><b>Database Compatibility Notes:</b></p>
 * <ul>
 *   <li><b>ILIKE:</b> Specific to PostgreSQL database. For other databases,
 *       use LIKE with caseSensitive=false in FilterCriteria</li>
 *   <li><b>Date operations:</b> Behavior may vary across different database vendors
 *       regarding time zone handling and precision</li>
 * </ul>
 *
 * @author Fabio De Zuani
 * @see FilterCriteria
 * @see it.fabiodezuani.queryengine.builder.SpecificationBuilder
 * @see it.fabiodezuani.queryengine.builder.DefaultSpecificationBuilder
 */
public enum FilterOperation {

    /**
     * Exact equality comparison operation.
     * Matches values that are exactly equal to the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field = value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> All comparable types</p>
     */
    EQUALS,

    /**
     * Inequality comparison operation.
     * Matches values that are not equal to the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field != value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> All comparable types</p>
     */
    NOT_EQUALS,

    /**
     * Greater than comparison operation.
     * Matches values that are greater than the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field > value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> Comparable types (numbers, dates, strings)</p>
     */
    GREATER_THAN,

    /**
     * Greater than or equal comparison operation.
     * Matches values that are greater than or equal to the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field >= value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> Comparable types (numbers, dates, strings)</p>
     */
    GREATER_THAN_OR_EQUAL,

    /**
     * Less than comparison operation.
     * Matches values that are less than the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field < value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> Comparable types (numbers, dates, strings)</p>
     */
    LESS_THAN,

    /**
     * Less than or equal comparison operation.
     * Matches values that are less than or equal to the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field <= value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> Comparable types (numbers, dates, strings)</p>
     */
    LESS_THAN_OR_EQUAL,

    /**
     * SQL LIKE pattern matching operation with case sensitivity.
     * Uses SQL LIKE syntax with % and _ wildcards.
     *
     * <p><b>SQL equivalent:</b> {@code field LIKE '%value%'}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> String types</p>
     * <p><b>Case sensitivity:</b> Respects FilterCriteria.caseSensitive flag</p>
     */
    LIKE,

    /**
     * Case-insensitive LIKE operation (PostgreSQL specific).
     * Performs case-insensitive pattern matching using PostgreSQL's ILIKE operator.
     *
     * <p><b>SQL equivalent:</b> {@code field ILIKE '%value%'}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> String types</p>
     * <p><b>Database support:</b> PostgreSQL only</p>
     */
    ILIKE,

    /**
     * String starts with pattern matching operation.
     * Matches strings that begin with the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field LIKE 'value%'}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> String types</p>
     * <p><b>Case sensitivity:</b> Respects FilterCriteria.caseSensitive flag</p>
     */
    STARTS_WITH,

    /**
     * String ends with pattern matching operation.
     * Matches strings that end with the specified value.
     *
     * <p><b>SQL equivalent:</b> {@code field LIKE '%value'}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> String types</p>
     * <p><b>Case sensitivity:</b> Respects FilterCriteria.caseSensitive flag</p>
     */
    ENDS_WITH,

    /**
     * String contains substring matching operation.
     * Matches strings that contain the specified value as a substring.
     *
     * <p><b>SQL equivalent:</b> {@code field LIKE '%value%'}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> String types</p>
     * <p><b>Case sensitivity:</b> Respects FilterCriteria.caseSensitive flag</p>
     */
    CONTAINS,

    /**
     * Collection membership operation.
     * Matches values that exist within the specified collection.
     *
     * <p><b>SQL equivalent:</b> {@code field IN (value1, value2, value3)}</p>
     * <p><b>Required fields:</b> {@code values} (list with one or more elements)</p>
     * <p><b>Supported types:</b> All comparable types</p>
     */
    IN,

    /**
     * Collection non-membership operation.
     * Matches values that do not exist within the specified collection.
     *
     * <p><b>SQL equivalent:</b> {@code field NOT IN (value1, value2, value3)}</p>
     * <p><b>Required fields:</b> {@code values} (list with one or more elements)</p>
     * <p><b>Supported types:</b> All comparable types</p>
     */
    NOT_IN,

    /**
     * Null value check operation.
     * Matches fields that have null values.
     *
     * <p><b>SQL equivalent:</b> {@code field IS NULL}</p>
     * <p><b>Required fields:</b> None (value and values are ignored)</p>
     * <p><b>Supported types:</b> All types</p>
     */
    IS_NULL,

    /**
     * Non-null value check operation.
     * Matches fields that have non-null values.
     *
     * <p><b>SQL equivalent:</b> {@code field IS NOT NULL}</p>
     * <p><b>Required fields:</b> None (value and values are ignored)</p>
     * <p><b>Supported types:</b> All types</p>
     */
    IS_NOT_NULL,

    /**
     * Range membership operation.
     * Matches values that fall within the specified range (inclusive).
     *
     * <p><b>SQL equivalent:</b> {@code field BETWEEN value1 AND value2}</p>
     * <p><b>Required fields:</b> {@code values} (list with exactly 2 elements: start and end)</p>
     * <p><b>Supported types:</b> Comparable types (numbers, dates, strings)</p>
     */
    BETWEEN,

    /**
     * Date equality operation (ignoring time components).
     * Matches dates that are equal to the specified date, typically ignoring time.
     *
     * <p><b>SQL equivalent:</b> {@code DATE(field) = DATE(value)}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> LocalDate, LocalDateTime, Date</p>
     */
    DATE_EQUALS,

    /**
     * Date before comparison operation.
     * Matches dates that are before the specified date.
     *
     * <p><b>SQL equivalent:</b> {@code field < value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> LocalDate, LocalDateTime, Date</p>
     */
    DATE_BEFORE,

    /**
     * Date after comparison operation.
     * Matches dates that are after the specified date.
     *
     * <p><b>SQL equivalent:</b> {@code field > value}</p>
     * <p><b>Required fields:</b> {@code value}</p>
     * <p><b>Supported types:</b> LocalDate, LocalDateTime, Date</p>
     */
    DATE_AFTER,

    /**
     * Date range operation.
     * Matches dates that fall within the specified date range (inclusive).
     *
     * <p><b>SQL equivalent:</b> {@code field BETWEEN date1 AND date2}</p>
     * <p><b>Required fields:</b> {@code values} (list with exactly 2 elements: start and end date)</p>
     * <p><b>Supported types:</b> LocalDate, LocalDateTime, Date</p>
     */
    DATE_BETWEEN
}
