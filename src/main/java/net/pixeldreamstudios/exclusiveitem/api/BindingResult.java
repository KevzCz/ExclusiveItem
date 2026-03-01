package net.pixeldreamstudios.exclusiveitem.api;

import net.minecraft.text.Text;

import java.util.Optional;

public class BindingResult {
    private final boolean success;
    private final BindingFailureReason failureReason;
    private final Text message;

    private BindingResult(boolean success, BindingFailureReason failureReason, Text message) {
        this.success = success;
        this.failureReason = failureReason;
        this.message = message;
    }

    public static BindingResult success() {
        return new BindingResult(true, null, null);
    }

    public static BindingResult failure(BindingFailureReason reason, Text message) {
        return new BindingResult(false, reason, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public Optional<BindingFailureReason> getFailureReason() {
        return Optional.ofNullable(failureReason);
    }

    public Optional<Text> getMessage() {
        return Optional.ofNullable(message);
    }

    public static enum BindingFailureReason {
        ALREADY_BOUND,
        NOT_EXCLUSIVE,
        MISSING_REQUIREMENTS,
        PERMISSION_DENIED,
        EVENT_CANCELLED
    }
}
