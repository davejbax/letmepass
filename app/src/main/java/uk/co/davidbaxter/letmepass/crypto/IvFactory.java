package uk.co.davidbaxter.letmepass.crypto;

public interface IvFactory {

    byte[] getCurrentIv();

    byte[] generateNewIv();

}