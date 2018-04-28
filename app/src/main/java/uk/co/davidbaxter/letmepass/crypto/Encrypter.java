package uk.co.davidbaxter.letmepass.crypto;

import uk.co.davidbaxter.letmepass.crypto.impl.DecryptionException;

/**
 * A class that can encrypt and decrypt binary data
 */
public interface Encrypter {

    /**
     * Encrypts the input data
     * @param input Input data
     * @return Encrypted data (ciphertext)
     */
    byte[] encrypt(byte[] input);

    /**
     * Decrypts ciphertext to retrieve the original plaintext
     * @param ciphertext Ciphertext to decrypt
     * @return The decrypted plaintext
     * @throws Exception If the ciphertext could not be decrypted
     */
    byte[] decrypt(byte[] ciphertext) throws DecryptionException;

}
