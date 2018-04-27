package uk.co.davidbaxter.letmepass;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import uk.co.davidbaxter.letmepass.crypto.impl.HybridIvFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class HybridIvFactoryTest {

    private int getIntBE(byte[] bytes, int offset) {
        return ByteBuffer.wrap(bytes, offset, 4)
                .order(ByteOrder.BIG_ENDIAN)
                .getInt();
    }

    private void printBytes(byte[] fourBytes) {
        for (byte b : fourBytes)
            System.out.printf("%02x", b);
        System.out.println();
    }

    @Test
    public void generates_SequentialPart() {
        HybridIvFactory factory = new HybridIvFactory();

        byte[] iv = factory.getCurrentIv();
        int prev = getIntBE(iv, 8);

        // Try 10000 iterations
        for (int i = 0; i < 10000; i++) {
            // Generate a new IV and get the 'sequential' part of it (i.e. the counter)
            iv = factory.generateNewIv();

            int newPart = getIntBE(iv, 8);

            // Ensure that we incremented correctly
            assertThat(newPart, is(prev + 1));

            // Update previous value
            prev = newPart;
        }
    }

    @Test
    public void generates_RandomPart() {
        HybridIvFactory factory = new HybridIvFactory();

        byte[] prev = Arrays.copyOf(factory.getCurrentIv(), factory.getCurrentIv().length);
        byte[] iv;

        // Try 10000 iterations
        for (int i = 0; i < 10000; i++) {
            // Generate a new IV and get the 'sequential' part of it (i.e. the counter)
            iv = factory.generateNewIv();

            // Ensure that we incremented correctly
            assertFalse("Random parts duplicated; this could occur normally", Arrays.equals(prev, iv));

            // Update previous value
            prev = Arrays.copyOf(iv, iv.length);
        }
    }

    @Test
    public void generates_UniqueOnMultipleInstantiations() {
        // Create two separate factories
        HybridIvFactory factory1 = new HybridIvFactory();
        HybridIvFactory factory2 = new HybridIvFactory();

        // Ensure initial IVs are unique
        assertFalse("Multiple instantiations should NOT produce same initial IV",
                Arrays.equals(factory1.getCurrentIv(), factory2.getCurrentIv()));
    }

}
