package it.fabiodezuani.queryengine.dto;

public enum FilterOperation {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    LIKE,
    ILIKE, // Case insensitive LIKE per PostgreSQL
    STARTS_WITH,
    ENDS_WITH,
    CONTAINS,
    IN,
    NOT_IN,
    IS_NULL,
    IS_NOT_NULL,
    BETWEEN,
    DATE_EQUALS,
    DATE_BEFORE,
    DATE_AFTER,
    DATE_BETWEEN
}
