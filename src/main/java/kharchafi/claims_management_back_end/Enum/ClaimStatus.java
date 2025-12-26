package kharchafi.claims_management_back_end.Enum;

public enum ClaimStatus {
    SUBMITTED("submitted"),
    RECEIVED("received"),
    ASSIGNED("assigned"),
    IN_PROGRESS("in_progress"),
    PENDING_INFO("pending_info"),
    RESOLVED("resolved"),
    REJECTED("rejected");

    private final String value;

    ClaimStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ClaimStatus fromValue(String value) {
        for (ClaimStatus status : ClaimStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
