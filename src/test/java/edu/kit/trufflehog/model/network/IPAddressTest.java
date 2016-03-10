package edu.kit.trufflehog.model.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * <p>
 *     This class contains all tests for the IPAddress class.
 * </p>
 * @author Mark Giraud
 */
public class IPAddressTest {

    @Test(expected = InvalidIPAddress.class)
    public void IPAddress_constructor_throws_on_0_address() throws Exception {
        new IPAddress(0);
    }

    @Test(expected = InvalidIPAddress.class)
    public void IPAddress_constructor_throws_on_too_large_address() throws Exception {
        new IPAddress(5000000000L);
    }

    @Test
    public void toByteArray_returns_correct_values() throws Exception {
        final IPAddress ipAddress = new IPAddress(2071538313L);

        byte[] bytes = ipAddress.toByteArray();
        assertEquals(0b01111011, bytes[0] & 0xFF);
        assertEquals(0b01111001, bytes[1] & 0xFF);
        assertEquals(0b00101010, bytes[2] & 0xFF);
        assertEquals(0b10001001, bytes[3] & 0xFF);
    }

    @Test
    public void address_is_multicast() throws Exception {
        for (long i = 3758096384L; i <= 4026531839L; i += 100) {
            assertTrue(new IPAddress(i).isMulticast());
        }
    }

    @Test
    public void address_is_not_multicast() throws Exception {
        for (long i = 1; i < 3758096384L; i += 10000) {
            assertFalse(new IPAddress(i).isMulticast());
        }
        for (long i = 4026531840L; i <= 4294967295L; i += 10000) {
            assertFalse(new IPAddress(i).isMulticast());
        }
    }

    @Test
    public void same_addresses_equal_each_other() throws Exception {
        long i = 1;
        while (i <= 4294967295L) {
            assertTrue(new IPAddress(i).equals(new IPAddress(i)));
            i += Math.random() * 10000;
        }
    }

    @Test
    public void different_addresses_do_not_equal_each_other() throws Exception {
        long i = 1;
        while (i <= 4294966294L) {
            assertFalse(new IPAddress(i).equals(new IPAddress(i + (long)(1 + Math.random() * 1000))));
            i += Math.random() * 10000;
        }
    }
}