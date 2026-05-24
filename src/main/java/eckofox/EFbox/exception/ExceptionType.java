package eckofox.EFbox.exception;

public enum ExceptionType {
    ACCESS_EXCEPTION("Access Exception"),
    FILE_VALIDATION_EXCEPTION("File Validation Exception"),
    ILLEGAL_ACCESS_EXCEPTION("Illegal Access Exception"),
    ILLEGAL_ARGUMENT_EXCEPTION("Illegal Argument Exception"),
    ILLEGAL_REGEX_EXCEPTION("Illegal Regex Exception"),
    ILLEGIBLE_PASSWORD_EXCEPTION("Illegible Password Exception"),
    IO_EXCEPTION("Input-Output Exception"),
    LOGIN_EXCEPTION("Login exception"),
    NO_SUCH_ELEMENT_EXCEPTION("No Such Element Exception"),
    SERVLET_EXCEPTION("Servlet Exception"),
    SQL_EXCEPTION("SQL Exception"),
    UNDEFINED_EXCEPTION("Undefined Exception"),
    USER_NOT_FOUND_EXCEPTION("User Not Found Exception"),
    USERNAME_NOT_FOUND_EXCEPTION("Username Not Found Exception"),
    UNSAFE_PASSWORD_EXCEPTION("Unsafe Password Exception");


    private final String description;

    private ExceptionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
