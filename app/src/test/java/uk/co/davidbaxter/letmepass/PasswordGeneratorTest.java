package uk.co.davidbaxter.letmepass;

import org.junit.Test;

import uk.co.davidbaxter.letmepass.security.PasswordGenerator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class PasswordGeneratorTest {

    @Test
    public void validates_AllParamsFalse() {
        try {
            PasswordGenerator gen = new PasswordGenerator.Builder()
                    .lower(false)
                    .upper(false)
                    .symbols(false)
                    .numbers(false)
                    .create();
            fail("Password generator should not allow construction with no valid chars");
        } catch (Exception e) {}
    }

    @Test
    public void validates_MinGreaterThanMax() {
        try {
            PasswordGenerator gen = new PasswordGenerator.Builder()
                    .minLength(10)
                    .maxLength(1)
                    .create();
            fail("Password generator should not allow construction with minimum > maximum length");
        } catch (Exception e) {}
    }

    @Test
    public void validates_MaxZero() {
        try {
            PasswordGenerator gen = new PasswordGenerator.Builder()
                    .minLength(0)
                    .maxLength(0)
                    .create();
            fail("Password generator should not allow construction with maximum length of 0");
        } catch (Exception e) {}
    }

    @Test
    public void generates_AllSettings() {
        PasswordGenerator.Builder builder = new PasswordGenerator.Builder();

        // Loop through all possible combinations of upper, lower, numbers, and symbols flags
        // (i.e. from b0001 to b1111; exclude 0 because we need at least 1 flag set)
        for (int i = 1; i < 0x10; i++) {
            boolean upper = ((i & 0x1) != 0);
            boolean lower = ((i & 0x2) != 0);
            boolean numbers = ((i & 0x4) != 0);
            boolean symbols = ((i & 0x8) != 0);

            // Create password generator for this configuration
            PasswordGenerator gen = builder.upper(upper)
                    .lower(lower)
                    .numbers(numbers)
                    .symbols(symbols)
                    .create();

            // Ensure that across 100 generations, we do not violate our stated parameters
            for (int j = 0; j < 100; j++) {
                String s = gen.generate();

                // We cannot -guarantee- that a charset will be used, since generation is random.
                // However, we can guarantee that it won't be used.
                if (!upper) assertFalse(s.matches("[A-Z]"));
                if (!lower) assertFalse(s.matches("[a-z]"));
                if (!numbers) assertFalse(s.matches("[0-9]"));
                if (!symbols) assertFalse(s.matches("[^a-zA-Z0-9]"));
            }
        }
    }

}
