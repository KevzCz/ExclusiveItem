package net.pixeldreamstudios.exclusiveitem.api;

public enum BindingMode {
    ON_PICKUP(false),
    ON_USE(true);

    private final boolean bindOnUse;

    BindingMode(boolean bindOnUse) {
        this.bindOnUse = bindOnUse;
    }

    public boolean isBindOnUse() {
        return bindOnUse;
    }

    public static BindingMode fromBoolean(boolean bindOnUse) {
        return bindOnUse ? ON_USE : ON_PICKUP;
    }
}
