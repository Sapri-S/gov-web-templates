package gov.templates.portal.common;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Creates hard-to-guess reference numbers such as SR-7KQ4MZ2PXA. The alphabet leaves
 * out 0, 1, I, L, O and U so numbers are easy to read aloud over the phone.
 */
@Component
public class ReferenceNumberGenerator {

    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    public String next(String prefix) {
        StringBuilder sb = new StringBuilder(prefix.length() + 1 + LENGTH).append(prefix).append('-');
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    /** Returns the normalized number, or null when the input can't be one of ours. */
    public static String normalize(String prefix, String input) {
        if (input == null) {
            return null;
        }
        String candidate = input.trim().toUpperCase(Locale.ROOT);
        Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix) + "-[" + ALPHABET + "]{" + LENGTH + "}$");
        return pattern.matcher(candidate).matches() ? candidate : null;
    }
}
