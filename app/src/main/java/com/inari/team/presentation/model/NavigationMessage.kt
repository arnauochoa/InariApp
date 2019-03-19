package com.inari.team.presentation.model

data class NavigationMessage (
    val svid: Int?,
    val type: Int?,
    val status: Int?,
    val messageId: Int?,
    val submessageId: Int?,
    val data: ByteArray?
)