package uk.co.davidbaxter.letmepass.crypto.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;

import uk.co.davidbaxter.letmepass.crypto.IvFactory;

/**
 * An {@link IvFactory} that generates unique IVs (nonces) for use in counter modes of encryption.
 * <p>
 * This factory generates an IV consisting of:
 * <ul>
 *     <li>Random bytes (8 bytes)</li>
 *     <li>Counter (4 bytes)</li>
 * </ul>
 * This combination should ensure that an IV is extremely unlikely to be reused: theoretically, it
 * should take 2^48 generations of IVs with this class to have a 50% chance of generating an IV that
 * has already been generated (see: the birthday paradox).
 */
public class HybridIvFactory implements IvFactory {

    private static final int IV_LENGTH = 12; // bytes
    private static final int RAND_LENGTH = 8; // How many bytes of IV is random?
    private static final int COUNT_LENGTH = 4; // How many bytes of IV is counter?

    private byte[] iv = new byte[IV_LENGTH];
    private SecureRandom srand = new SecureRandom(); // We call this few enough times to not re-seed

    /**
     * Creates a new HybridIvFactory with a new initial IV
     */
    public HybridIvFactory() {
        // Generate a new IV -- the default IV is zeroed (see Java Language Spec 4.12.5)
        generateNewIv();
    }

    /**
     * Creates a new HybridIvFactory initialized with the given IV
     * @param initialIv Initial IV to use
     */
    public HybridIvFactory(byte[] initialIv) {
        // Ensure given IV is of correct length
        if (initialIv.length != IV_LENGTH)
            throw new IllegalArgumentException("IV must be of the correct length (" + IV_LENGTH
                    + " bytes)");

        // Copy initial IV to our IV
        System.arraycopy(initialIv, 0, iv, 0, iv.length);
    }

    @Override
    public byte[] getCurrentIv() {
        return iv;
    }

    @Override
    public byte[] generateNewIv() {
        // Generate the random number
        byte[] randomNumber = new byte[RAND_LENGTH];
        srand.nextBytes(randomNumber);

        // Get and increment the counter
        int counter = toInt32BE(iv, RAND_LENGTH);
        counter++;

        // Copy these to the IV
        System.arraycopy(randomNumber, 0, iv, 0, randomNumber.length);
        System.arraycopy(fromInt32BE(counter), 0, iv, randomNumber.length, COUNT_LENGTH);

        return iv;
    }

    private int toInt32BE(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, 4)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();
    }

    private byte[] fromInt32BE(int i) {
        return ByteBuffer.allocate(4)
                .putInt(i)
                .array();
    }
}
