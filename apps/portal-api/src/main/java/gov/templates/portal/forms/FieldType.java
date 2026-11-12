package gov.templates.portal.forms;

public enum FieldType {
    TEXT(200),
    TEXTAREA(4000),
    EMAIL(254),
    PHONE(30),
    SELECT(100),
    CHECKBOX(0);

    private final int defaultMaxLength;

    FieldType(int defaultMaxLength) {
        this.defaultMaxLength = defaultMaxLength;
    }

    public int defaultMaxLength() {
        return defaultMaxLength;
    }
}
