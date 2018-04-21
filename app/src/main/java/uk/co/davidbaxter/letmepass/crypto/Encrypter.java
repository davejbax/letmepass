package uk.co.davidbaxter.letmepass.crypto;

public interface Encrypter {

    byte[] encrypt(byte[] input);

    byte[] decrypt(byte[] ciphertext) throws Exception;

}
