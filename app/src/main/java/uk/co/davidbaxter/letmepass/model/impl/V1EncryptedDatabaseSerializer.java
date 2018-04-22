package uk.co.davidbaxter.letmepass.model.impl;

import com.google.android.gms.common.util.IOUtils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import uk.co.davidbaxter.letmepass.crypto.impl.AesGcmEncrypter;
import uk.co.davidbaxter.letmepass.crypto.impl.Argon2KeyDerivationFunction;
import uk.co.davidbaxter.letmepass.crypto.impl.DecryptionException;
import uk.co.davidbaxter.letmepass.crypto.impl.HybridIvFactory;
import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.model.EncryptedDatabaseSerializer;
import uk.co.davidbaxter.letmepass.model.SerializationException;

public class V1EncryptedDatabaseSerializer implements EncryptedDatabaseSerializer {

    private static final short VERSION = 1;
    private static final int ARGON_ITERATIONS = 10;
    private static final int ARGON_MEMORY = 65536;
    private static final int ARGON_PARALLELISM = 2;
    private static final int ARGON_SALT_LEN = 16;
    private static final int AES_KEY_LEN_BYTES = 32; // 256-bit

    private HybridIvFactory newIvFactory;
    private Argon2KeyDerivationFunction kdf;

    public V1EncryptedDatabaseSerializer() {
        this.newIvFactory = new HybridIvFactory();
        this.kdf = new Argon2KeyDerivationFunction(ARGON_ITERATIONS, ARGON_MEMORY,
                ARGON_PARALLELISM, ARGON_SALT_LEN, AES_KEY_LEN_BYTES);
    }

    @Override
    public byte[] serialize(PasswordDatabase db, String mp) throws SerializationException {
        if (!(db instanceof JsonPasswordDatabase))
            throw new IllegalArgumentException("Database is not of correct type");

        byte[] payload = db.serialize();

        // GZIP the payload
        try {
            payload = gzip(payload);
        } catch (IOException e) {
            throw new SerializationException("Failed to GZIP payload", e);
        }

        // Re-generate IV now, so we can use it in encryption AND store it in our header before
        // we encrypt: we cannot encrypt now because we don't have the header to hash & encrypt!
        newIvFactory.generateNewIv();

        // Generate header
        byte[] header = new Header(this, payload.length).toByteArray();

        // Get a digest of the header
        byte[] headerHash = getSha256Hash(header);

        // Copy the header hash & payload to the text to be encrypted
        byte[] textToEncrypt = new byte[headerHash.length + payload.length];
        System.arraycopy(headerHash, 0, textToEncrypt, 0, headerHash.length);
        System.arraycopy(payload, 0, textToEncrypt, headerHash.length, payload.length);

        // Encrypt what needs to be encrypted
        AesGcmEncrypter encrypter = new AesGcmEncrypter(kdf, newIvFactory);
        encrypter.setMasterPassword(mp);
        byte[] encrypted = encrypter.encrypt(textToEncrypt);

        // Finally, copy the header and encrypted text to a single array and return
        byte[] out = new byte[header.length + encrypted.length];
        System.arraycopy(header, 0, out, 0, header.length);
        System.arraycopy(encrypted, 0, out, header.length, encrypted.length);
        return out;
    }

    @Override
    public PasswordDatabase deserialize(byte[] data, String mp) throws SerializationException,
            DecryptionException {
        // Create buffer of data and read header
        ByteBuffer encBuff = ByteBuffer.wrap(data);
        Header header = new Header(encBuff);

        // Read remainder of buffer into an encrypted buffer
        byte[] encrypted = new byte[encBuff.remaining()];
        encBuff.get(encrypted);

        // Use an IV factory based on the data's stored IV, so we can decrypt and increment
        // correctly in future
        HybridIvFactory decIv = new HybridIvFactory(header.iv);
        this.newIvFactory = decIv;

        // Create a KDF from the parameters
        Argon2KeyDerivationFunction kdf = new Argon2KeyDerivationFunction(
                header.argonIterations,
                header.argonMemory,
                header.argonParallelism,
                header.argonSalt,
                AES_KEY_LEN_BYTES
        );

        // Decrypt the encrypted payload using the KDF and IV factory for it;
        // we don't use our stored KDF because this has different params; same for IV factory, but
        // we do update our IV factory so that we can generate new IVs sequentially
        AesGcmEncrypter encrypter = new AesGcmEncrypter(kdf, decIv);
        encrypter.setMasterPassword(mp);
        byte[] decrypted;
        try {
            decrypted = encrypter.decrypt(encrypted);
        } catch (DecryptionException e) {
            throw e;
        }

        // Get the decrypted header digest, then get the actual header digest and compare
        byte[] digest = new byte[32];
        ByteBuffer decBuff = ByteBuffer.wrap(decrypted)
                .get(digest);
        byte[] actualDigest = getSha256Hash(header.toByteArray());

        // Ensure that the header has not been tampered with or corrupted
        if (!Arrays.equals(digest, actualDigest))
            throw new DecryptionException(new Exception("Header digest invalid"));

        // Get the payload and decompress
        byte[] payload = new byte[decBuff.remaining()];
        decBuff.get(payload);
        try {
            return JsonPasswordDatabase.deserialize(payload, true); // true = should gunzip
        } catch (JsonIOException | JsonSyntaxException e) {
            throw new SerializationException("Failed to deserialize JSON", e);
        } catch (IOException e) {
            throw new SerializationException("Failed to deflate GZIP payload", e);
        }
    }

    private byte[] gzip(byte[] payload) throws IOException {
        GZIPOutputStream gzipOut = null;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            gzipOut = new GZIPOutputStream(out);
            gzipOut.write(payload, 0, payload.length);
            gzipOut.finish();
            payload = out.toByteArray();
        } catch (IOException e) {
            throw e;
        } finally {
            if (gzipOut != null)
                IOUtils.closeQuietly(gzipOut);
        }

        // Return payload (we've set it to the ByteArrayOutputStream output)
        return payload;
    }

    private byte[] getSha256Hash(byte[] message) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return md.digest(message);
    }

    /**
     * Header with the following structure:
     * <pre>
     * uint16_t version;
     * uint32_t payload_length;
     * [uint16_t iv_length: fixed at 96 bits]
     * uint8_t iv[12];
     * uint32_t argon_iterations;
     * uint32_t argon_memory;
     * uint32_t argon_parallelism;
     * [uint16_t argon_hash_len: fixed at 256 bits];
     * uint16_t argon_salt_len;
     * uint8_t* argon_salt; // salt_len bytes
     * </pre>
     */
    private static class Header {

        private static final short IV_LENGTH = HybridIvFactory.IV_LENGTH; // 96 bits
        private static final int LENGTH_EXCEPT_SALT = 20 + IV_LENGTH;

        short version;
        int payloadLength;
        byte[] iv;
        int argonIterations;
        int argonMemory;
        int argonParallelism;
        /* private short argonSaltLength; -- implicit/not required: argonSalt has length */
        byte[] argonSalt;

        /** Constructs a header, setting parameters based on the given serializer & payload len */
        Header(V1EncryptedDatabaseSerializer serializer, int payloadLength) {
            this.version = VERSION;
            this.payloadLength = payloadLength;
            this.iv = serializer.newIvFactory.getCurrentIv();
            this.argonIterations = serializer.kdf.getIterations();
            this.argonMemory = serializer.kdf.getMemory();
            this.argonParallelism = serializer.kdf.getParallelism();
            this.argonSalt = serializer.kdf.getSalt();
            if (this.argonSalt.length > Short.MAX_VALUE)
                throw new IllegalArgumentException("KDF salt too long");
        }

        /** Constructs a header, deserializing the given buffer into the class */
        Header(ByteBuffer buff) throws SerializationException {
            // Read from the Buffer into the class
            buff.order(ByteOrder.BIG_ENDIAN);
            this.version = buff.getShort();
            if (this.version != VERSION)
                throw new SerializationException("Invalid version");

            this.payloadLength = buff.getInt();
            if (this.payloadLength < 0)
                throw new SerializationException("Payload length negative");

            this.iv = new byte[IV_LENGTH];
            buff.get(this.iv);

            this.argonIterations = buff.getInt();
            if (this.argonIterations < 0)
                throw new SerializationException("Invalid KDF parameters (iterations)");

            this.argonMemory = buff.getInt();
            if (this.argonMemory <= 0)
                throw new SerializationException("Invalid KDF parameters (memory)");

            this.argonParallelism = buff.getInt();
            if (this.argonParallelism <= 0)
                throw new SerializationException("Invalid KDF parameters (parallelism)");

            short len = buff.getShort();
            if (len <= 0)
                throw new SerializationException("Invalid salt length");

            this.argonSalt = new byte[len];
            buff.get(this.argonSalt);
        }

        /** Serializes this header to a byte array */
        byte[] toByteArray() {
            int headerLen = LENGTH_EXCEPT_SALT + argonSalt.length;
            byte[] header = new byte[headerLen];

            ByteBuffer buff = ByteBuffer.allocate(headerLen)
                    .order(ByteOrder.BIG_ENDIAN)
                    .putShort(VERSION)
                    .putInt(this.payloadLength)
                    .put(this.iv)
                    .putInt(this.argonIterations)
                    .putInt(this.argonMemory)
                    .putInt(this.argonParallelism)
                    .putShort((short) this.argonSalt.length);

            System.out.println(buff.capacity() + ", " + buff.remaining());
            buff.put(this.argonSalt, 0, this.argonSalt.length);
            buff.rewind();
            buff.get(header);

            return header;
        }

    }

}
