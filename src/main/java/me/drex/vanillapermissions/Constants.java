package me.drex.vanillapermissions;

public class Constants {

    public static final String COMMAND = permission();

    protected static String permission() {
        return build("minecraft", "command.%s");
    }

    public static String build(String... parts) {
        return String.join(".", parts);
    }
}
