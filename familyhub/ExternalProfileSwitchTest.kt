package com.nuvio.tv.core.profile

import com.nuvio.tv.core.player.StreamAutoPlaySelector
import com.nuvio.tv.data.local.StreamAutoPlayMode
import com.nuvio.tv.data.local.StreamAutoPlaySource
import com.nuvio.tv.domain.model.Stream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalProfileSwitchTest {

    private fun testStream(
        addonName: String,
        url: String? = "https://stream.local/video.mkv",
        externalUrl: String? = null,
        name: String? = "1080p",
    ) = Stream(
        name = name,
        title = name,
        description = null,
        url = url,
        ytId = null,
        infoHash = null,
        fileIdx = null,
        externalUrl = externalUrl,
        behaviorHints = null,
        addonName = addonName,
        addonLogo = null,
    )

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
        assertFalse(ExternalProfileSwitch.extractAutoPlayFirst(null))
        assertNull(ExternalProfileSwitch.extractPreferAddon(null))
    }

    @Test
    fun oneClickFlagParsers_handleVariants() {
        assertTrue(ExternalProfileSwitch.parseAutoPlayFirstValue(true))
        assertTrue(ExternalProfileSwitch.parseAutoPlayFirstValue("true"))
        assertTrue(ExternalProfileSwitch.parseAutoPlayFirstValue("1"))
        assertTrue(ExternalProfileSwitch.parseAutoPlayFirstValue(1))
        assertFalse(ExternalProfileSwitch.parseAutoPlayFirstValue(false))
        assertFalse(ExternalProfileSwitch.parseAutoPlayFirstValue("false"))
        assertFalse(ExternalProfileSwitch.parseAutoPlayFirstValue(null))

        assertEquals("aiostreams", ExternalProfileSwitch.parsePreferAddonValue("  aiostreams "))
        assertNull(ExternalProfileSwitch.parsePreferAddonValue("   "))
        assertNull(ExternalProfileSwitch.parsePreferAddonValue(null))
    }

    @Test
    fun selectFirstPreferredAddonStream_prefersAioStreamsAndSkipsExternal() {
        val other = testStream(addonName = "Torrentio", name = "Torrentio First")
        val aioExt = testStream(addonName = "AIOStreams", url = null, externalUrl = "https://example.com/cfg", name = "Configure")
        val aioOk = testStream(addonName = "AIOStreams", name = "AIOStreams Stream 1")

        val picked = StreamAutoPlaySelector.selectFirstPreferredAddonStream(
            streams = listOf(other, aioExt, aioOk),
            installedAddonNames = setOf("Torrentio", "AIOStreams"),
            preferredAddonKeyword = "aiostreams",
        )
        assertEquals(aioOk, picked)
    }

    @Test
    fun selectAutoPlayStream_fallsBackToOtherInstalledAddonWhenPreferredHasNoStreams() {
        val other = testStream(addonName = "Comet", name = "Comet 1080p")
        val picked = StreamAutoPlaySelector.selectAutoPlayStream(
            streams = listOf(other),
            mode = StreamAutoPlayMode.FIRST_STREAM,
            regexPattern = "",
            source = StreamAutoPlaySource.INSTALLED_ADDONS_ONLY,
            installedAddonNames = setOf("Comet", "AIOStreams"),
            selectedAddons = emptySet(),
            selectedPlugins = emptySet(),
            preferredAddonKeyword = "aiostreams",
        )
        assertEquals(other, picked)
    }
}
