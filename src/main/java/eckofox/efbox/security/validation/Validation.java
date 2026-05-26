package eckofox.efbox.security.validation;

import lombok.Getter;

@Getter
public enum Validation {
    FILE,
    FILE_NOT_VALIDATED,
    NOT_AUTHORIZED,
    OK,
    OTHER_INJECTION_SUSPECTED,
    SQL_INJECTION_SUSPECTED;

}
