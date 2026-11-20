package com.nuvio.tv.core.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalProfileSwitchTest {

    @Test
    fun extractProfileIdFromUrl_parsesProfilePathDeepLink() {
        assertEquals(1, ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://profile/1"))
        assertEquals(3, ExternalProfileSwitch.extractProfileIdFromUrl("  NUVIO://PROFILE/3 "))
        assertEquals(6, ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://profile/6?source=hub"))
    }

    @Test
    fun extractProfileIdFromUrl_parsesQueryParametersOnMetaDeepLinks() {
        assertEquals(
            2,
            ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://meta?type=movie&id=tt0133093&profileId=2")
        )
        assertEquals(
            3,
            ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://tmdb/series/1399?profile_id=3")
        )
        assertEquals(
            1,
            ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://detail/movie/tt1234?profile=1")
        )
    }

    @Test
    fun extractProfileIdFromUrl_rejectsInvalidIdsAndOtherSchemes() {
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl(null))
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl(""))
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://profile/0"))
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://profile/7"))
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://profile/abc"))
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl("nuvio://meta?type=movie&id=tt1&profileId=99"))
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl("https://example.com/profile/2?profileId=2"))
        assertNull(ExternalProfileSwitch.extractProfileIdFromUrl("stremio://profile/2"))
    }

    @Test
    fun isProfileOnlyDeepLink_identifiesOnlyNuvioProfileLinks() {
        assertTrue(ExternalProfileSwitch.isProfileOnlyDeepLink("nuvio://profile/2"))
        assertTrue(ExternalProfileSwitch.isProfileOnlyDeepLink("NUVIO://PROFILE/1"))
        assertFalse(ExternalProfileSwitch.isProfileOnlyDeepLink("nuvio://meta?type=movie&id=tt1&profileId=2"))
        assertFalse(ExternalProfileSwitch.isProfileOnlyDeepLink("nuvio://tmdb/movie/550"))
        assertFalse(ExternalProfileSwitch.isProfileOnlyDeepLink(null))
    }

    @Test
    fun extractProfileId_handlesNullIntent() {
        assertNull(ExternalProfileSwitch.extractProfileId(null))
    }
}
