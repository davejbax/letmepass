package uk.co.davidbaxter.letmepass.crypto.impl;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import uk.co.davidbaxter.letmepass.crypto.KeyDerivationFunction;

public class Argon2KeyDerivationFunction implements KeyDerivationFunction {

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private SecureRandom saltRandom = new SecureRandom();
    private Argon2Advanced argon;
    private int iterations;
    private int memory;
    private int parallelism;
    private byte[] salt;

    public Argon2KeyDerivationFunction(int iterations, int memory, int parallelism, int saltLen,
                                       int hashLen) {
        this(iterations, memory, parallelism, new byte[saltLen], hashLen);
        this.regenerateSalt();
    }

    public Argon2KeyDerivationFunction(int iterations, int memory, int parallelism, byte[] salt,
                                       int hashLen) {
        this.argon = Argon2Factory.createAdvanced(
                Argon2Factory.Argon2Types.ARGON2i,
                salt.length,
                hashLen);
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
        this.salt = salt;
    }

    public int getIterations() {
        return iterations;
    }

    public int getMemory() {
        return memory;
    }

    public int getParallelism() {
        return parallelism;
    }

    public int getSaltLength() {
        return salt.length;
    }

    public byte[] getSalt() {
        byte[] out = new byte[salt.length];
        System.arraycopy(salt, 0, out, 0, salt.length);
        return out;
    }

    public void setSalt(byte[] salt) {
        if (this.salt.length != salt.length)
            throw new IllegalArgumentException("Salt length must be the same");

        System.arraycopy(salt, 0, this.salt, 0, salt.length);
    }

    public void regenerateSalt() {
        this.saltRandom.nextBytes(this.salt);
    }

    @Override
    public byte[] derive(String input) {
        return argon.rawHash(iterations, memory, parallelism, input, CHARSET_UTF8, salt);
    }

}
