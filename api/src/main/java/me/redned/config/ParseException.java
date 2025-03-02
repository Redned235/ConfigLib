package me.redned.config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParseException extends Exception {
    private Path sourceFile;
    private boolean userError = false;
    private Cause cause = Cause.UNKNOWN;
    private Class<?> type;

    private final Map<String, String> context = new LinkedHashMap<>();

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException sourceFile(Path sourceFile) {
        this.sourceFile = sourceFile;
        return this;
    }

    public ParseException userError() {
        this.userError = true;
        return this;
    }

    public ParseException cause(Cause cause) {
        this.cause = cause;
        return this;
    }

    public ParseException context(String key, String value) {
        this.context.put(key, value);
        return this;
    }

    public ParseException type(Class<?> type) {
        this.type = type;
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public static void handle(ParseException exception) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("-".repeat(60));
        builder.append("\n");
        if (exception.userError) {
            builder.append("A value inside one of your configuration files is configured incorrectly.\n");
        } else {
            builder.append("An internal error occurred while parsing a file.\n");
        }

        builder.append("\nError: ")
                .append(exception.cause.message)
                .append(": ")
                .append(exception.getMessage())
                .append("\n");

        if (!exception.context.isEmpty()) {
            builder.append("\nAdditional Context:\n");
            for (Map.Entry<String, String> entry : exception.context.entrySet()) {
                builder.append("- ")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }
        }

        if (exception.sourceFile != null) {
            builder.append("\n");
            builder.append("Please see the following file causing the reported error: ");
            builder.append(exception.sourceFile.toAbsolutePath());
            builder.append("\n");
        }

        builder.append("\n");
        boolean printedStacktrace = false;
        if (!exception.userError || exception.cause == Cause.UNKNOWN) {
            builder.append("It is likely that this error is due to a problem or misconfiguration in the plugin. ");
            builder.append("Please report the following stacktrace to Redned!\n");
            builder.append("Stacktrace:\n");

            StringWriter writer = new StringWriter();
            exception.printStackTrace(new PrintWriter(writer));

            builder.append(writer);
            printedStacktrace = true;
        }

        if (!printedStacktrace) {
            builder.append("Stacktrace (Debug Mode):\n");

            StringWriter writer = new StringWriter();
            (exception.getCause() != null ? exception.getCause() : exception).printStackTrace(new PrintWriter(writer));

            builder.append(writer);
        }

        builder.append("-".repeat(60));
        Config.onError(builder.toString());
    }

    public enum Cause {
        INVALID_VALUE("An invalid value was provided"),
        MISSING_VALUE("A required value was not provided"),
        MISSING_SECTION("A required configuration section was not provided"),
        INVALID_FORMAT("The provided value was in an invalid format"),
        INVALID_TYPE("The provided value was not the expected type"),
        INVALID_OPTION("The provided option was not valid"),
        INTERNAL_ERROR("An internal error occurred while parsing the file"),
        UNKNOWN("An unknown error occurred. See the stacktrace for more information");

        private final String message;

        Cause(String message) {
            this.message = message;
        }
    }
}
