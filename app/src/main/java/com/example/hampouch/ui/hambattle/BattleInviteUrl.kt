package com.example.hampouch.ui.hambattle

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal const val BATTLE_INVITE_BASE_URL = "https://invite.hampouch.com/battles/invite"
private const val BATTLE_INVITE_PATH_SEGMENT_COUNT = 3

internal fun buildBattleInviteUrl(battleCode: String): String? {
    val normalizedCode = battleCode.trim().takeIf(String::isNotEmpty) ?: return null
    val encodedCode = URLEncoder.encode(normalizedCode, StandardCharsets.UTF_8)
        .replace("+", "%20")
    return "$BATTLE_INVITE_BASE_URL/$encodedCode"
}

internal fun extractBattleCodeFromInviteUrl(url: String?): String? = runCatching {
    val uri = URI(url?.trim().orEmpty())
    require(uri.scheme.equals("https", ignoreCase = true))
    require(uri.host.equals("invite.hampouch.com", ignoreCase = true))

    val pathSegments = uri.rawPath.orEmpty().trim('/').split('/').filter(String::isNotBlank)
    require(pathSegments.size == BATTLE_INVITE_PATH_SEGMENT_COUNT)
    require(pathSegments[0] == "battles" && pathSegments[1] == "invite")

    URLDecoder.decode(pathSegments[2], StandardCharsets.UTF_8).takeIf(String::isNotBlank)
}.getOrNull()

internal fun extractBattleCodeFromInviteValue(value: String): String? =
    extractBattleCodeFromInviteUrl(value)
        ?: value.trim().takeIf { it.isNotBlank() && '/' !in it }

internal fun normalizeBattleInviteUrl(value: String): String? =
    extractBattleCodeFromInviteValue(value)?.let(::buildBattleInviteUrl)

@HiltViewModel
internal class BattleInviteViewModel @Inject constructor() : ViewModel() {
    private val _pendingBattleCode = MutableStateFlow<String?>(null)
    val pendingBattleCode = _pendingBattleCode.asStateFlow()

    fun acceptUrl(url: String?) {
        extractBattleCodeFromInviteUrl(url)?.let { _pendingBattleCode.value = it }
    }

    fun consume(battleCode: String) {
        if (_pendingBattleCode.value == battleCode) _pendingBattleCode.value = null
    }
}
