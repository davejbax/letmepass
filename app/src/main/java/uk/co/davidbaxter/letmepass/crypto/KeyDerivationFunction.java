package uk.co.davidbaxter.letmepass.crypto;

/**
 * A function that transforms a String (password) to binary data for use in encryption or passwords.
 */
public interface KeyDerivationFunction {

    /**
     * Derives a key from the given input
     * @param input Text from which to derive the key
     * @return Derived key
     */
    byte[] derive(String input);

}
