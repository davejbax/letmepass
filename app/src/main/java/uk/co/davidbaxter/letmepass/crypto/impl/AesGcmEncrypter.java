package uk.co.davidbaxter.letmepass.crypto.impl;

import android.util.Log;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import uk.co.davidbaxter.letmepass.crypto.Encrypter;
import uk.co.davidbaxter.letmepass.crypto.IvFactory;
import uk.co.davidbaxter.letmepass.crypto.KeyDerivationFunction;

/**
 * An {@link Encrypter} implementation that uses AES in Galois/Counter Mode (GCM)
 * <p>
 * This implementation makes use of several abstractions to ease encryption/decryption and reduce
 * the potential for errors. These include:
 * <ul>
 *     <li>
 *         Master password - this is passed to a key derivation function on encryption & decryption.
 *         The {@link KeyDerivationFunction} is passed as a parameter on construction; the key size
 *         of this class depends on the size returned by this {@link KeyDerivationFunction}
 *     </li>
 *     <li>
 *         IV generation - an {@link IvFactory} is passed in construction, which is used to generate
 *         and retrieve IV for encryption and decryption, respectively. The use of this is to avoid
 *         redundant calls to the class to re-set the IV, for instance. It should be noted that GCM
 *         <b>requires a unique IV on each encryption</b>, and each IV should only be used once,
 *         hence the use of this abstraction.
 *     </li>
 * </ul>
 */
public class AesGcmEncrypter implements Encrypter {

    private static final int DEFAULT_MAC_SIZE = 128; // NIST says 128, 120, 112, 104, 96 generally.

    private final KeyDerivationFunction kdf;
    private String mp = null; // TODO: store as char[] & wipe?
    private IvFactory ivFactory;
    private int macSizeBits;

    /**
     * Constructs an AesGcmEncrypter with the given KDF and IvFactory, and the default MAC size of
     * 128 bits.
     *
     * @param kdf Key derivation function to derive a key from the set master password
     * @param ivFactory IvFactory to generate and retrieve IVs for encryption/decryption
     */
    public AesGcmEncrypter(KeyDerivationFunction kdf, IvFactory ivFactory) {
        this(kdf, ivFactory, DEFAULT_MAC_SIZE);
    }

    /**
     * Constructs an AesGcmEncrypter with the given KDF, IvFactory, and MAC size. If there is no
     * need for a specific MAC size, it is recommended to use the default MAC size by calling
     * instead {@link #AesGcmEncrypter(KeyDerivationFunction, IvFactory)}.
     *
     * @param kdf Key derivation function to derive a key from the set master password
     * @param ivFactory IvFactory to generate and retrieve IVs for encryption/decryption
     * @param macSizeBits The size of the MAC (GCM tag); should be 128, 120, 112, 104, 96, usually.
     */
    public AesGcmEncrypter(KeyDerivationFunction kdf, IvFactory ivFactory, int macSizeBits) {
        this.kdf = kdf;
        this.ivFactory = ivFactory;
        this.macSizeBits = macSizeBits;
    }

    public void setIvFactory(IvFactory ivFactory) {
        this.ivFactory = ivFactory;
    }

    /**
     * Sets the master password of this encrypter and returns the same encrypter (for builder-like
     * pattern).
     *
     * @param mp Master password to use for encryption & decryption
     * @return This
     */
    public AesGcmEncrypter setMasterPassword(String mp) {
        this.mp = mp;
        return this;
    }

    private byte[] generateMp() {
        return kdf.derive(mp);
    }

    private byte[] crypt(byte[] input, boolean encrypt) throws DecryptionException {
        assert mp != null;

        // If we're decrypting, we don't want to be re-generating the IV.
        // Otherwise, we MUST re-generate the IV! GCM requires a unique IV (nonce)
        byte[] nonce = encrypt ? ivFactory.generateNewIv() : ivFactory.getCurrentIv();

        // Setup AES cipher in GCM mode
        // N.B. - AESFastEngine has potential side-channel attacks; we do not care about these.
        GCMBlockCipher gcmCipher = new GCMBlockCipher(new AESFastEngine());
        KeyParameter keyParam = new KeyParameter(generateMp());
        AEADParameters aeadParams = new AEADParameters(keyParam, macSizeBits, nonce, new byte[]{});

        // Initialize the cipher with our key, MAC size, and nonce (IV)
        byte[] output = new byte[gcmCipher.getOutputSize(input.length)];
        gcmCipher.init(encrypt, aeadParams);

        // Process the bytes and finalize encryption (write MAC)
        int written = gcmCipher.processBytes(input, 0, input.length, output, 0);

        // Ensure our buffer is still large enough
        int newBuffSize = gcmCipher.getOutputSize(input.length);
        if (newBuffSize > output.length) {
            byte[] newBuff = new byte[newBuffSize];
            System.arraycopy(output, 0, newBuff, 0, output.length);
            output = newBuff;
        }

        try {
            // Do the final operation
            written += gcmCipher.doFinal(output, written); // `written` is offset to write MAC
        } catch (InvalidCipherTextException e) {
            // This should only be thrown on decryption, when the MAC is invalid or some other error
            // occurred.
            throw new DecryptionException(e);
        }

        if (output.length > written) {
            byte[] onlyCipherText = new byte[written];
            System.arraycopy(output, 0, onlyCipherText, 0, written);
            return onlyCipherText;
        }

        // Return the ciphertext if all went well
        return output;
    }

    @Override
    public byte[] encrypt(byte[] input) {
        try {
            return crypt(input, true);
        } catch (DecryptionException e) {
            // This should NEVER happen
            throw new RuntimeException("Decryption exception during encryption", e.getCause());
        }
    }

    @Override
    public byte[] decrypt(byte[] cipherText) throws DecryptionException {
        return crypt(cipherText, false);
    }
}
