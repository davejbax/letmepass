package uk.co.davidbaxter.letmepass.model;

/**
 * Password security flags to distill the 'security' of a password
 * <p>
 * This class contains public 'flags' which can be read to determine whether a password is
 * considered secure. The exact setting of these flags is dependent on the producer of them: some
 * flags are subjective, for instance (e.g. good length) and so it is best to consult the
 * documentation of the producing classes in this case.
 */
public class PasswordFlags {

    /**
     * Constructs a new password flags instance with all flags set to false.
     */
    public PasswordFlags() {}

    /**
     * Whether the password is of a 'good' length, i.e. has sufficient entropy by virtue of its
     * length.
     */
    public boolean goodLength;

    /**
     * Whether the password contains mixed characters (lowercase, uppercase, and numbers)
     */
    public boolean hasMixedChars;

    /**
     * Whether the password contains symbols (non-alphanumeric characters)
     */
    public boolean hasSymbols;

    /**
     * Whether the password is not on any blacklist
     */
    public boolean notBlacklisted;

}
