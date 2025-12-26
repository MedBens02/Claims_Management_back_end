package kharchafi.claims_management_back_end.Enum;

public enum SenderType {
    USER("user"),
    SERVICE("service"),
    SYSTEM("system");

    private final String value;

    SenderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SenderType fromValue(String value) {
        for (SenderType type : SenderType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown sender type: " + value);
    }
}
