package eckofox.efbox.security.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InputValidationService {
    private static final String AUTHORIZED_CHARS_FOR_USER_INPUT =  "^[a-zA-Z0-9 _.-]{1,20}$";
    private static final List<String> POSSIBLE_SQL_INJECTION_CHAR = new ArrayList<>() {{
        add("' OR '1'='1");
        add("\" OR \"1\"=\"1");
        add("' OR 1=1 --");
        add("' OR 'a'='a");
        add("' UNION SELECT");
        add("UNION ALL SELECT");
        add("DROP TABLE");
        add("INSERT INTO");
        add("DELETE FROM");
        add("UPDATE users SET");
        add("xp_cmdshell");
        add("EXEC");
        add("EXECUTE");
        add("SHUTDOWN");
        add("TRUNCATE TABLE");
        add("ALTER TABLE");
        add("CREATE TABLE");
        add("CREATE DATABASE");
        add("CAST(");
        add("CONVERT(");
        add("WAITFOR DELAY");
        add("SLEEP(");
        add("BENCHMARK(");
        add("INFORMATION_SCHEMA");
        add("pg_sleep(");
        add("LOAD_FILE(");
        add("OUTFILE");
        add("INTO OUTFILE");
        add("HAVING 1=1");
        add("'#");
        add("#");
        add(";");
        add("%27");
        add("%22");
        add("%3D");
        add("%2D%2D");
        add("%3B");
        add("0x50");
        add("CHAR(");
        add("' AND 1=1");
        add("' AND 1=2");
        add("' OR TRUE");
        add("' OR FALSE");
        add("' OR SLEEP");
        add("' WAITFOR DELAY '");
        add("--");
        add("/*");
        add("*/");
        add("UNION");
        add("UNION SELECT NULL");
        add("UNION SELECT username; password");
        add("GRANT");
        add("REVOKE");
        add("COMMIT");
        add("ROLLBACK");
    }};

    private static final List<String> OTHER_POSSIBLE_INJECTION = new ArrayList<>() {{
        add("<script>");
        add("</script>");
        add("javascript:");
        add("onerror=");
        add("onload=");
        add("<iframe");
        add("<img");
        add("document.cookie");
        add("alert(");
        add("eval(");
        add("<html>");
        add("<body>");
        add("<form");
        add("<input");
        add("<svg");
        add("<embed");
        add("<object");
        add("&&");
        add("`");
        add("$(");
        add("cmd.exe");
        add("/bin/sh");
        add("/bin/bash");
        add("powershell");
        add("whoami");
        add("wget ");
        add("curl ");
        add("nc ");
        add("netcat");
        add("ping ");
        add("chmod ");
        add("rm -rf");
        add("cat /etc/passwd");
        add("../");
        add("..\\");
        add("%2e%2e%2f");
        add("%252e%252e%252f");
        add("/etc/passwd");
        add("C:\\Windows");
        add("~/");
        add("boot.ini");
        add("*)(");
        add("(|");
        add("(&");
        add("(cn=");
        add("(uid=");
        add("<!ENTITY");
        add("<!DOCTYPE");
        add("SYSTEM");
        add("PUBLIC");
        add("#{");
        add("{{");
        add("}}");
        add("<%");
        add("%>");
        add("$ne");
        add("$gt");
        add("$lt");
        add("$regex");
        add("{ \"$ne\": null }");
        add("%0d");
        add("%0a");
        add("\r");
        add("\n");
        add("<?php");
        add("?>");
        add("Runtime.getRuntime");
        add("ProcessBuilder");
        add("java.lang");
        add(".class");
        add("ysoserial");
        add("ObjectInputStream");
        add("Serialized");
        add("${jndi:");
        add("ldap://");
        add("rmi://");
        add("AAAAAAA");
        add("%%%%%");
        add("../../../");
        add("{{7*7}}");
    }};


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
