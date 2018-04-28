package uk.co.davidbaxter.letmepass.crypto.impl;

/**
 * An exception indicating that something went wrong while decrypting a database. This class has
 * a root cause, and is generally used as a catch-all for decryption errors.
 */
public class DecryptionException extends Exception {

    /**
     * Constructs a new DecryptionException
     * @param cause The cause of this exception
     */
    public DecryptionException(Throwable cause) {
        super(cause);
    }

}
