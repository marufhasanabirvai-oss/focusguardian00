package com.focusguardian.app

import com.focusguardian.app.domain.matching.WordMatcher
import org.junit.Assert.*
import org.junit.Test

class WordMatcherTest {

    @Test
    fun testExactWordBoundaryMatch() {
        // True cases
        assertTrue(WordMatcher.matches("I placed a bet yesterday", "bet", isExactMatch = true))
        assertTrue(WordMatcher.matches("BET now!", "bet", isExactMatch = true))
        assertTrue(WordMatcher.matches("Casino, bet.", "bet", isExactMatch = true))

        // False cases: "bet" inside "better" or "alphabet" should NOT trigger when exact = true
        assertFalse(WordMatcher.matches("Things are getting better", "bet", isExactMatch = true))
        assertFalse(WordMatcher.matches("Learn the alphabet today", "bet", isExactMatch = true))
    }

    @Test
    fun testBengaliKeywordMatching() {
        // Exact match with Bengali sentence and punctuation (Dari '।')
        assertTrue(WordMatcher.matches("তিনি গতকাল জুয়া খেলেছেন।", "জুয়া", isExactMatch = true))
        assertTrue(WordMatcher.matches("অনলাইন বাজি ধরা নিষিদ্ধ!", "বাজি", isExactMatch = true))
        assertTrue(WordMatcher.matches("ক্যাসিনো খেলা বন্ধ করুন", "ক্যাসিনো", isExactMatch = true))

        // Partial match
        assertTrue(WordMatcher.matches("পর্নোগ্রাফিক কনটেন্ট দেখা যাবে না", "পর্নোগ্রাফি", isExactMatch = false))
    }

    @Test
    fun testPartialMatchOption() {
        // When user enables partial match, it matches anywhere
        assertTrue(WordMatcher.matches("Things are getting better", "bet", isExactMatch = false))
        assertTrue(WordMatcher.matches("Check out the casino website", "casino", isExactMatch = false))
    }

    @Test
    fun testCaseInsensitivity() {
        assertTrue(WordMatcher.matches("DIS-TRAC-TION and GAMBLING", "gambling", isExactMatch = true))
        assertTrue(WordMatcher.matches("pOrN and adult content", "porn", isExactMatch = true))
    }
}
