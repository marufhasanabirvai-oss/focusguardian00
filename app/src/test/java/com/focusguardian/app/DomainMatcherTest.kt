package com.focusguardian.app

import com.focusguardian.app.domain.matching.DomainMatcher
import org.junit.Assert.*
import org.junit.Test

class DomainMatcherTest {

    @Test
    fun testExactDomainMatch() {
        assertTrue(DomainMatcher.isHostBlocked("example.com", "example.com"))
        assertTrue(DomainMatcher.isHostBlocked("http://example.com", "example.com"))
        assertTrue(DomainMatcher.isHostBlocked("https://example.com/", "example.com"))
    }

    @Test
    fun testSubdomainMatch() {
        assertTrue(DomainMatcher.isHostBlocked("sub.example.com", "example.com"))
        assertTrue(DomainMatcher.isHostBlocked("api.v2.example.com", "example.com"))
        assertTrue(DomainMatcher.isHostBlocked("www.example.com", "example.com"))
    }

    @Test
    fun testUnrelatedDomainNotBlocked() {
        // Must NOT block unrelated websites
        assertFalse(DomainMatcher.isHostBlocked("notexample.com", "example.com"))
        assertFalse(DomainMatcher.isHostBlocked("anotherexample.com", "example.com"))
        assertFalse(DomainMatcher.isHostBlocked("example.org", "example.com"))
        assertFalse(DomainMatcher.isHostBlocked("google.com", "example.com"))
    }

    @Test
    fun testPortAndPathNormalization() {
        assertEquals("example.com", DomainMatcher.normalizeDomain("https://example.com:8080/path/to/page"))
        assertEquals("sub.domain.com", DomainMatcher.normalizeDomain("http://sub.domain.com/login?param=1"))
    }
}
