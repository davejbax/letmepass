package uk.co.davidbaxter.letmepass.model;

import uk.co.davidbaxter.letmepass.crypto.impl.DecryptionException;

public interface EncryptedDatabaseSerializer {

    byte[] serialize(PasswordDatabase db, String mp) throws SerializationException;

    PasswordDatabase deserialize(byte[] encrypted, String mp) throws SerializationException,
            DecryptionException;

}
