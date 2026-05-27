package eckofox.efbox.security.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InputValidationService {
    private static final String AUTHORIZED_CHARS_FOR_USER_INPUT =  "^[a-zA-Z0-9 _.-]{1,20}$";
    private static final List<String> POSSIBLE_SQL_INJECTION_CHAR = List.of(
            "' OR '1'='1",
            "\" OR \"1\"=\"1",
            "' OR 1=1 --",
            "' OR 'a'='a",
            "' UNION SELECT",
            "UNION ALL SELECT",
            "DROP TABLE",
            "INSERT INTO",
            "DELETE FROM",
            "UPDATE users SET",
            "xp_cmdshell",
            "EXEC",
            "EXECUTE",
            "SHUTDOWN",
            "TRUNCATE TABLE",
            "ALTER TABLE",
            "CREATE TABLE",
            "CREATE DATABASE",
            "CAST(",
            "CONVERT(",
            "WAITFOR DELAY",
            "SLEEP(",
            "BENCHMARK(",
            "INFORMATION_SCHEMA",
            "pg_sleep(",
            "LOAD_FILE(",
            "OUTFILE",
            "INTO OUTFILE",
            "HAVING 1=1",
            "'#",
            "#",
            ";",
            "%27",
            "%22",
            "%3D",
            "%2D%2D",
            "%3B",
            "0x50",
            "CHAR(",
            "' AND 1=1",
            "' AND 1=2",
            "' OR TRUE",
            "' OR FALSE",
            "' OR SLEEP",
            "' WAITFOR DELAY '",
            "--",
            "/*",
            "*/",
            "UNION",
            "UNION SELECT NULL",
            "UNION SELECT username; password",
            "GRANT",
            "REVOKE",
            "COMMIT",
            "ROLLBACK"
    );

    private static final List<String> OTHER_POSSIBLE_INJECTION = List.of(
            "<script>",
            "</script>",
            "javascript:",
            "onerror=",
            "onload=",
            "<iframe",
            "<img",
            "document.cookie",
            "alert(",
            "eval(",
            "<html>",
            "<body>",
            "<form",
            "<input",
            "<svg",
            "<embed",
            "<object",
            "&&",
            "`",
            "$(",
            "cmd.exe",
            "/bin/sh",
            "/bin/bash",
            "powershell",
            "whoami",
            "wget ",
            "curl ",
            "nc ",
            "netcat",
            "ping ",
            "chmod ",
            "rm -rf",
            "cat /etc/passwd",
            "../",
            "..\\",
            "%2e%2e%2f",
            "%252e%252e%252f",
            "/etc/passwd",
            "C:\\Windows",
            "~/",
            "boot.ini",
            "*)(",
            "(|",
            "(&",
            "(cn=",
            "(uid=",
            "<!ENTITY",
            "<!DOCTYPE",
            "SYSTEM",
            "PUBLIC",
            "#{",
            "{{",
            "}}",
            "<%",
            "%>",
            "$ne",
            "$gt",
            "$lt",
            "$regex",
            "{ \"$ne\": null }",
            "%0d",
            "%0a",
            "\r",
            "\n",
            "<?php",
            "?>",
            "Runtime.getRuntime",
            "ProcessBuilder",
            "java.lang",
            ".class",
            "ysoserial",
            "ObjectInputStream",
            "Serialized",
            "${jndi:",
            "ldap://",
            "rmi://",
            "AAAAAAA",
            "%%%%%",
            "../../../",
            "{{7*7}}"
    );


    //NOTE: OWASP recommends an allow-list rather than a forbid-list, any Validation other than Validation.OK
    //is to inform admins of potential threats to the system.
    public Validation isUserInputValidated(String string) {
        if (Pattern.matches(AUTHORIZED_CHARS_FOR_USER_INPUT, string)) {
            return Validation.OK;
        }

        return isInjectionSuspected(string);
    }

    private Validation isInjectionSuspected(String string) {
        for (String sqlCandidate : POSSIBLE_SQL_INJECTION_CHAR) {
            if (string.toLowerCase().contains(sqlCandidate.toLowerCase())) {
                return Validation.SQL_INJECTION_SUSPECTED;
            }
        }

        for (String injectionCandidate : OTHER_POSSIBLE_INJECTION) {
            if (string.toLowerCase().contains(injectionCandidate.toLowerCase())) {
                return Validation.OTHER_INJECTION_SUSPECTED;
            }
        }

        return Validation.NOT_AUTHORIZED;
    }
}
