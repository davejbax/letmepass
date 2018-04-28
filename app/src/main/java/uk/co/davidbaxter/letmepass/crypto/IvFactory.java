package uk.co.davidbaxter.letmepass.crypto;

/**
 * A factory for generating initialization vectors (IVs). This is useful for counter modes of
 * operation of block ciphers, where a unique IV may be required for each encryption in order to
 * maintain the secrecy of the cipher.
 */
public interface IvFactory {

    /**
     * Gets the current IV
     * @return Current IV
     */
    byte[] getCurrentIv();

    /**
     * Generates and returns a new IV, setting this as the current IV
     * @return New IV
     */
    byte[] generateNewIv();

}