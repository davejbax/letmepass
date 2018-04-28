package uk.co.davidbaxter.letmepass.model;

import uk.co.davidbaxter.letmepass.crypto.impl.DecryptionException;

/**
 * Interface for serializing and deserializing databases into/from an encrypted binary format,
 * given the master password.
 */
public interface EncryptedDatabaseSerializer {

    /**
     * Serialize the database into an encrypted binary format.
     *
     * @param db Database to serialize
     * @param mp Master password for encryption
     * @return Encrypted binary data for storage
     * @throws SerializationException If the DB could not be serialized
     */
    byte[] serialize(PasswordDatabase db, String mp) throws SerializationException;

    /**
     * Deserializes the database from an encrypted binary format (i.e. decrypts it).
     *
     * @param encrypted Encrypted data
     * @param mp Master password to decrypt the data
     * @return A PasswordDatabase instance from the decrypted data
     * @throws SerializationException If the data could not be deserialized (e.g. if corrupt)
     * @throws DecryptionException If the data could not be decrypted
     */
    PasswordDatabase deserialize(byte[] encrypted, String mp) throws SerializationException,
            DecryptionException;

}
