package uk.co.davidbaxter.letmepass.model.impl;

import java.nio.ByteBuffer;

import uk.co.davidbaxter.letmepass.crypto.impl.DecryptionException;
import uk.co.davidbaxter.letmepass.model.PasswordDatabase;
import uk.co.davidbaxter.letmepass.model.EncryptedDatabaseSerializer;
import uk.co.davidbaxter.letmepass.model.SerializationException;

/**
 * An {@link EncryptedDatabaseSerializer} that reads the version of the file format from the
 * header of the data, and chooses the implementation accordingly.
 */
public class VersionedEncryptedDatabaseSerializer implements EncryptedDatabaseSerializer {

    private static final int VERSION_1 = 1;
    private static final int LATEST_VERSION = VERSION_1;
    private EncryptedDatabaseSerializer serializer;

    /**
     * Creates a VersionedEncryptedDatabaseSerializer that will serialize data to the latest
     * version of the DB format.
     */
    public VersionedEncryptedDatabaseSerializer() {
        this(LATEST_VERSION);
    }

    /**
     * Creates a VersionedEncryptedDatabaseSerializer to serialize data to the given version
     * @param version Version of format to serialize to
     */
    public VersionedEncryptedDatabaseSerializer(int version) {
        switch (version) {
            case VERSION_1:
                this.serializer = new V1EncryptedDatabaseSerializer();
                break;
            default:
                throw new IllegalArgumentException("Invalid version");
        }
    }

    @Override
    public byte[] serialize(PasswordDatabase db, String mp) throws SerializationException {
        return this.serializer.serialize(db, mp);
    }

    @Override
    public PasswordDatabase deserialize(byte[] data, String mp) throws SerializationException,
            DecryptionException {
        // Sanity check
        if (data.length < 2)
            throw new SerializationException("Data too short");

        short version = ByteBuffer.wrap(data, 0, 2)
                .getShort();

        switch (version) {
            case LATEST_VERSION:
                return serializer.deserialize(data, mp);
            default:
                throw new IllegalArgumentException(
                        "Unsupported password database version or corrupted file"
                );
        }
    }

}
