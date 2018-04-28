package uk.co.davidbaxter.letmepass.crypto.impl;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import de.mkammerer.argon2.Argon2Advanced;
import de.mkammerer.argon2.Argon2Factory;
import uk.co.davidbaxter.letmepass.crypto.KeyDerivationFunction;

/**
 * An implementation of a {@link KeyDerivationFunction} using the Argon2(i) hashing function (the
 * winner of the Password Hashing Contest).
 * <p>
 * This class relies on a library that utilizes native binaries. In particular, the binaries for
 * libargon2 and libjnidispatch <b>must</b> be bundled with the final application.
 * <p>
 * Argon2d is used in this class due to its resistance to GPU cracking attacks, and the lack of a
 * need for prevention of timing attacks (since we are not a server).
 */
public class Argon2KeyDerivationFunction implements KeyDerivationFunction {

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private SecureRandom saltRandom = new SecureRandom();
    private Argon2Advanced argon;
    private int iterations;
    private int memory;
    private int parallelism;
    private byte[] salt;

    /**
     * Creates a new Argon2 KDF using the provided parameters, generating the salt randomly
     *
     * @param iterations Number of iterations to perform
     * @param memory Amount of memory in KiB to use
     * @param parallelism Degree of parallelism (number of threads)
     * @param saltLen Length of salt in bytes
     * @param hashLen Length of hash to generate in bytes
     */
    public Argon2KeyDerivationFunction(int iterations, int memory, int parallelism, int saltLen,
                                       int hashLen) {
        this(iterations, memory, parallelism, new byte[saltLen], hashLen);
        this.regenerateSalt();
    }

    /**
     * Creates a new Argon2 KDF using the provided parameters and an initial salt
     *
     * @param iterations Number of iterations to perform
     * @param memory Amount of memory in KiB to use
     * @param parallelism Degree of parallelism (number of threads)
     * @param salt The salt to use
     * @param hashLen Length of hash to generate in bytes
     */
    public Argon2KeyDerivationFunction(int iterations, int memory, int parallelism, byte[] salt,
                                       int hashLen) {
        this.argon = Argon2Factory.createAdvanced(
                Argon2Factory.Argon2Types.ARGON2d,
                salt.length,
                hashLen);
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
        this.salt = salt;
    }

    /**
     * Gets the set number of iterations
     * @return Iterations
     */
    public int getIterations() {
        return iterations;
    }

    /**
     * Gets the set amount of memory to use in KiB
     * @return Memory
     */
    public int getMemory() {
        return memory;
    }

    /**
     * Gets the degree of parallelism (i.e. # threads)
     * @return Parallelism
     */
    public int getParallelism() {
        return parallelism;
    }

    /**
     * Gets the length of the salt
     * @return Salt length
     */
    public int getSaltLength() {
        return salt.length;
    }

    /**
     * Gets (a copy of) the salt in use
     * @return Salt byte array
     */
    public byte[] getSalt() {
        byte[] out = new byte[salt.length];
        System.arraycopy(salt, 0, out, 0, salt.length);
        return out;
    }

    /**
     * Sets the salt to the given value
     * @param salt New salt
     */
    public void setSalt(byte[] salt) {
        if (this.salt.length != salt.length)
            throw new IllegalArgumentException("Salt length must be the same");

        System.arraycopy(salt, 0, this.salt, 0, salt.length);
    }

    /**
     * Regenerates the salt randomly
     */
    public void regenerateSalt() {
        this.saltRandom.nextBytes(this.salt);
    }

    @Override
    public byte[] derive(String input) {
        return argon.rawHash(iterations, memory, parallelism, input, CHARSET_UTF8, salt);
    }

}
