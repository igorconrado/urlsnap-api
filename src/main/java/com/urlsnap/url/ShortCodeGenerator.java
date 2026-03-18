package com.urlsnap.url;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();

    public String generateCode() {
        var sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public boolean isUnique(String code, UrlRepository repo) {
        return !repo.existsByShortCode(code);
    }

    public String generate(UrlRepository repo) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = generateCode();
            if (isUnique(code, repo)) {
                return code;
            }
        }
        throw new IllegalStateException("Failed to generate unique short code after " + MAX_ATTEMPTS + " attempts");
    }
}
