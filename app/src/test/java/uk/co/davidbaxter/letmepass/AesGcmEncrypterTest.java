package uk.co.davidbaxter.letmepass;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import uk.co.davidbaxter.letmepass.crypto.IvFactory;
import uk.co.davidbaxter.letmepass.crypto.KeyDerivationFunction;
import uk.co.davidbaxter.letmepass.crypto.impl.AesGcmEncrypter;
import uk.co.davidbaxter.letmepass.crypto.impl.DecryptionException;

public class AesGcmEncrypterTest {

    private final static Object[][] TEST_DATA = new Object[][] {
            { // McGrew/Viega Test Case 3
                // Master password (String)
                "feffe9928665731c6d6a8f9467308308",

                // Nonce
                Hex.decode("cafebabefacedbaddecaf888"),

                // Plaintext
                Hex.decode(
                    "d9313225f88406e5a55909c5aff5269a" +
                    "86a7a9531534f7da2e4c303d8a318a72" +
                    "1c3c0c95956809532fcf0e2449a6b525" +
                    "b16aedf5aa0de657ba637b391aafd255"
                ),

                // Ciphertext
                Hex.decode(
                    "42831ec2217774244b7221b784d0d49c" +
                    "e3aa212f2c02a4e035c17e2329aca12e" +
                    "21d514b25466931c7d8f6a5aac84aa05" +
                    "1ba30b396a0aac973d58e091473f5985" +
                    "4d5c2af327cd64a62cf35abd2ba6fab4"
                )
            },
            { // McGrew/Viega Test Case 7
                // Master password (String)
                "00000000000000000000000000000000" +
                "0000000000000000",

                // Nonce
                Hex.decode("000000000000000000000000"),

                // Plaintext
                Hex.decode(
                    ""
                ),

                // Ciphertext
                Hex.decode(
                    "cd33b28ac773f74ba00ed1f312572435"
                )
            },
            { // McGrew/Viega Test Case 8
                // Master password (String)
                "00000000000000000000000000000000" +
                "0000000000000000",

                // Nonce
                Hex.decode("000000000000000000000000"),

                // Plaintext
                Hex.decode(
                    "00000000000000000000000000000000"
                ),

                // Ciphertext
                Hex.decode(
                    "98e7247c07f0fe411c267e4384b0f600" +
                    "2ff58d80033927ab8ef4d4587514f0fb"
                )
            },
            { // McGrew/Viega Test Case 15
                // Master password (String)
                "feffe9928665731c6d6a8f9467308308" +
                "feffe9928665731c6d6a8f9467308308",

                // Nonce
                Hex.decode("cafebabefacedbaddecaf888"),

                // Plaintext
                Hex.decode(
                    "d9313225f88406e5a55909c5aff5269a" +
                    "86a7a9531534f7da2e4c303d8a318a72" +
                    "1c3c0c95956809532fcf0e2449a6b525" +
                    "b16aedf5aa0de657ba637b391aafd255"
                ),

                // Ciphertext
                Hex.decode(
                    "522dc1f099567d07f47f37a32a84427d" +
                    "643a8cdcbfe5c0c97598a2bd2555d1aa" +
                    "8cb08e48590dbb3da7b08b1056828838" +
                    "c5f61e6393ba7a0abcc9f662898015ad" +
                    "b094dac5d93471bdec1a502270e3cc6c"
                )
            }
    };

    private KeyDerivationFunction mockDerivation = new KeyDerivationFunction() {
        @Override
        public byte[] derive(String input) {
            return Hex.decode(input);
        }
    };

    private MockIvFactory mockIvFactory = new MockIvFactory();

    @Test
    public void encrypts_NormalData() throws DecryptionException {
        // Create the encrypter
        AesGcmEncrypter encrypter = new AesGcmEncrypter(
                mockDerivation,
                mockIvFactory,
                128); // MAC size = 128 bits

        // Run our test cases
        for (Object[] testCase : TEST_DATA) {
            // Get our test case variables
            String mp = (String) testCase[0];
            byte[] nonce = (byte[]) testCase[1];
            byte[] plainText = (byte[]) testCase[2];
            byte[] cipherText = (byte[]) testCase[3];

            // Setup the encrypter (MP & IV) and encrypt & decrypt
            encrypter.setMasterPassword(mp);
            mockIvFactory.setIv(nonce);
            byte[] ourCipherText = encrypter.encrypt(plainText);
            byte[] ourPlainText = encrypter.decrypt(cipherText);

            // Check that the decrypted ciphertext and the encrypted plaintext are correct
            assertThat(Hex.toHexString(plainText), equalTo(Hex.toHexString(ourPlainText)));
            assertThat(Hex.toHexString(cipherText), equalTo(Hex.toHexString(ourCipherText)));
        }
    }

    private class MockIvFactory implements IvFactory {
        private byte[] currentIv = new byte[12];

        public MockIvFactory setIv(byte[] iv) {
            System.arraycopy(iv, 0, currentIv, 0, 12);
            return this;
        }

        @Override
        public byte[] getCurrentIv() {
            return currentIv;
        }

        @Override
        public byte[] generateNewIv() {
            return currentIv;
        }
    }

}
