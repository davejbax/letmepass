package uk.co.davidbaxter.letmepass.security;

import java.security.SecureRandom;

/**
 * A class to generate new passwords
 */
public class PasswordGenerator {

    private static final char[] UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWER = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] NUMBERS = "0123456789".toCharArray();
    private static final char[] SYMBOLS = "~!@#$%^&*()-_=+[{]}|;:\',<.>/?".toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    private final int minLength;
    private final int maxLength;
    private final char[] chars;

    /**
     * Constructs a new password generator with the given parameters
     * @param minLength Minimum length of generated passwords
     * @param maxLength Maximum length of generated passwords
     * @param symbols Whether to include symbols in generated passwords
     * @param numbers Whether to include numbers in generated passwords
     * @param upper Whether to include uppercase characters in generated passwords
     * @param lower Whether to include lowercase characters in generated passwords
     */
    public PasswordGenerator(int minLength, int maxLength, boolean symbols, boolean numbers,
                             boolean upper, boolean lower) {
        this.minLength = minLength;
        this.maxLength = maxLength;

        int charsLength = (upper ? UPPER.length : 0)
                + (lower ? LOWER.length : 0)
                + (symbols ? SYMBOLS.length : 0)
                + (numbers ? NUMBERS.length : 0);

        this.chars = new char[charsLength];

        if (upper)
            System.arraycopy(UPPER, 0, chars, charsLength -= UPPER.length, UPPER.length);
        if (lower)
            System.arraycopy(LOWER, 0, chars, charsLength -= LOWER.length, LOWER.length);
        if (symbols)
            System.arraycopy(SYMBOLS, 0, chars, charsLength -= SYMBOLS.length, SYMBOLS.length);
        if (numbers)
            System.arraycopy(NUMBERS, 0, chars, charsLength -= NUMBERS.length, NUMBERS.length);
    }

    /**
     * Generates a new password using the parameters set in this generator
     * @return New password string
     */
    public String generate() {
        // Get the length required by randomly generating in the range
        // (bound in nextInt is exclusive, so we add 1)
        int length = minLength + secureRandom.nextInt(maxLength - minLength + 1);
        StringBuilder builder = new StringBuilder();

        // For as many chars as we want, add a random character from our charset
        for (int i = 0; i < length; i++)
            builder.append(chars[secureRandom.nextInt(chars.length)]);

        return builder.toString();
    }

}
