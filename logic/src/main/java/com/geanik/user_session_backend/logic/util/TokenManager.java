package com.geanik.user_session_backend.logic.util;

import com.geanik.user_session_backend.dal.Repositories;
import com.geanik.user_session_backend.domain.SessionToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

// https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
public class TokenManager {

    private static Logger log = LoggerFactory.getLogger(TokenManager.class);

    private final Repositories repositories;

    private final int validityDurationInMin;

    private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String lower = upper.toLowerCase(Locale.ROOT);

    private static final String digits = "0123456789";

    private static final String alphanum = upper + lower + digits;

    private final Random random;

    private final char[] symbols;

    private final char[] buf;

    private TokenManager(Repositories repositories, int validityDurationInMin, int length, Random random, String symbols) {
        this.repositories = repositories;
        this.validityDurationInMin = validityDurationInMin;
        if (length < 1) throw new IllegalArgumentException();
        if (symbols.length() < 2) throw new IllegalArgumentException();
        this.random = Objects.requireNonNull(random);
        this.symbols = symbols.toCharArray();
        this.buf = new char[length];
    }

    /**
     * Create an alphanumeric string generator.
     */
    private TokenManager(Repositories repositories, int validityDurationInMin, int length, Random random) {
        this(repositories, validityDurationInMin, length, random, alphanum);
    }

    /**
     * Create an alphanumeric strings from a secure generator.
     */
    private TokenManager(Repositories repositories, int validityDurationInMin, int length) {
        this(repositories, validityDurationInMin, length, new SecureRandom());
    }

    /**
     * Create session identifiers.
     */
    public TokenManager(Repositories repositories, int validityDurationInMin) {
        this(repositories, validityDurationInMin, 21);
    }

    public SessionToken generateToken(Long userId) {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];

        return new SessionToken(new String(buf), userId, LocalDateTime.now().plusMinutes(validityDurationInMin));
    }

    public boolean validateAndRenewToken(SessionToken sessionToken) {
        // check if token is still valid
        if (LocalDateTime.now().isBefore(sessionToken.getExpireTime())) {
            sessionToken.setExpireTime(LocalDateTime.now().plusMinutes(validityDurationInMin));
            repositories.sessionTokens().save(sessionToken);

            return true;
        } else {
            log.debug("Expired sessionToken");
            return false;
        }
    }

}
