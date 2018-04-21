package uk.co.davidbaxter.letmepass.crypto;

public interface KeyDerivationFunction {

    byte[] derive(String input);

}
