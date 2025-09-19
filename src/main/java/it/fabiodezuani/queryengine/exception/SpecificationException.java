package it.fabiodezuani.queryengine.exception;
/**
 * Custom runtime exception for errors occurring during JPA Specification construction and execution.
 *
 * <p>This exception is thrown when errors occur during the dynamic creation or execution of
 * JPA Specifications, particularly when using {@link it.fabiodezuani.queryengine.builder.SpecificationBuilder} implementations
 * to convert {@link it.fabiodezuani.queryengine.dto.FilterCriteria} into executable query predicates.</p>
 *
 * <p>As an unchecked exception extending {@link RuntimeException}, this exception does not
 * require explicit handling or declaration in method signatures, allowing for cleaner code
 * while still providing meaningful error information when specification building operations fail.</p>
 *
 * <p><b>Common scenarios where this exception is thrown:</b></p>
 * <ul>
 *   <li><b>Invalid field paths:</b> When dot-notation field paths reference non-existent entity fields or relationships</li>
 *   <li><b>Type conversion errors:</b> When filter values cannot be converted to the target field type</li>
 *   <li><b>Unsupported operations:</b> When a {@link it.fabiodezuani.queryengine.dto.FilterOperation} is not supported for a specific field type</li>
 *   <li><b>Invalid operation parameters:</b> When operations like BETWEEN or IN receive incorrect number of values</li>
 *   <li><b>Database constraint violations:</b> When the generated query violates database constraints</li>
 *   <li><b>JPA provider errors:</b> When the underlying JPA implementation encounters errors</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 *
 * <p><b>Throwing with descriptive message:</b></p>
 * <pre>{@code
 * if (criteria.getOperation() == null) {
 *     throw new SpecificationException(
 *         "FilterOperation cannot be null for field: " + criteria.getField());
 * }
 * }</pre>
 *
 * <p><b>Wrapping underlying exceptions:</b></p>
 * <pre>{@code
 * try {
 *     Path<?> fieldPath = getPath(root, criteria.getField());
 *     return createPredicate(fieldPath, criteria, criteriaBuilder);
 * } catch (IllegalArgumentException e) {
 *     throw new SpecificationException(
 *         String.format("Invalid field path '%s' for entity %s",
 *                      criteria.getField(), root.getJavaType().getSimpleName()), e);
 * }
 * }</pre>
 *
 * <p><b>Exception handling in client code:</b></p>
 * <pre>{@code
 * try {
 *     Specification<User> spec = specificationBuilder.build(criteria);
 *     List<User> users = userRepository.findAll(spec);
 * } catch (SpecificationException e) {
 *     log.error("Failed to build specification: {}", e.getMessage(), e);
 *     // Handle the error appropriately (e.g., return error response)
 * }
 * }</pre>
 *
 * <p><b>Design Rationale:</b><br>
 * This exception extends {@code RuntimeException} rather than {@code Exception} because
 * specification building errors typically represent programming errors or configuration
 * issues that should be addressed during development rather than runtime recovery scenarios.
 * This design choice reduces boilerplate code and aligns with modern Java exception handling practices.</p>
 *
 * @author Fabio De Zuani
 * @version 1.0.0
 * @since 19/09/2025
 * @see it.fabiodezuani.queryengine.builder.SpecificationBuilder
 * @see it.fabiodezuani.queryengine.builder.DefaultSpecificationBuilder
 * @see it.fabiodezuani.queryengine.dto.FilterCriteria
 * @see it.fabiodezuani.queryengine.dto.FilterOperation
 */
public class SpecificationException extends RuntimeException {

    /**
     * Constructs a new SpecificationException with the specified detail message.
     *
     * <p>This constructor should be used when the exception is thrown due to a
     * specific condition that can be described with a meaningful error message,
     * but where no underlying cause exception exists.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * if (criteria.getValues() == null || criteria.getValues().size() != 2) {
     *     throw new SpecificationException(
     *         "BETWEEN operation requires exactly 2 values, but got: " +
     *         (criteria.getValues() != null ? criteria.getValues().size() : 0));
     * }
     * }</pre>
     *
     * @param message the detail message explaining the reason for the exception;
     *               should be descriptive and include relevant context information
     * @since 19/09/2025
     */
    public SpecificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new SpecificationException with the specified detail message and cause.
     *
     * <p>This constructor should be used when wrapping an underlying exception that
     * occurred during specification building operations. The original exception is
     * preserved as the cause, allowing for complete stack trace information while
     * providing a more specific, context-aware error message.</p>
     *
     * <p>The cause is typically an exception from the JPA provider, reflection API,
     * or type conversion operations that occurred during specification construction.</p>
     *
     * <p><b>Usage example:</b></p>
     * <pre>{@code
     * try {
     *     return criteriaBuilder.equal(fieldPath, convertValue(criteria.getValue()));
     * } catch (ClassCastException e) {
     *     throw new SpecificationException(
     *         String.format("Cannot convert value '%s' to type required by field '%s'",
     *                      criteria.getValue(), criteria.getField()), e);
     * }
     * }</pre>
     *
     * @param message the detail message explaining the reason for the exception;
     *               should provide context about what operation was being performed
     * @param cause the cause exception that triggered this SpecificationException;
     *             may be null if no underlying cause exists, but typically should be provided
     *             when wrapping other exceptions
     * @since 19/09/2025
     */
    public SpecificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
