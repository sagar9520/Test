package com.apkglobal.test.ads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdUnitsTest {

    @Test
    public void placeholdersAreNotConfigured() {
        for (String id : AdUnits.ALL) {
            assertFalse(id, AdUnits.isConfigured(id));
        }
        assertTrue(AdUnits.configuredUnits().isEmpty());
    }

    @Test
    public void realLookingIdsAreConfigured() {
        assertTrue(AdUnits.isConfigured("0123456789abcdef"));
        assertTrue(AdUnits.isConfigured(" 0123456789ABCDEF ")); // pasted with spaces
    }

    @Test
    public void malformedIdsAreRejected() {
        assertFalse(AdUnits.isConfigured(null));
        assertFalse(AdUnits.isConfigured(""));
        assertFalse(AdUnits.isConfigured("   "));
        assertFalse(AdUnits.isConfigured("0123456789abcde"));   // 15 chars
        assertFalse(AdUnits.isConfigured("0123456789abcdef0")); // 17 chars
        assertFalse(AdUnits.isConfigured("0123456789abcd-f"));  // not alphanumeric
        assertFalse(AdUnits.isConfigured("YOUR_0123456789a"));
    }

    @Test
    public void cleanTrims() {
        assertEquals("0123456789abcdef", AdUnits.clean(" 0123456789abcdef\n"));
        assertEquals("", AdUnits.clean(null));
    }
}
