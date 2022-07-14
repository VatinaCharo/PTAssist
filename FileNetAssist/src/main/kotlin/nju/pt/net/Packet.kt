package nju.pt.net

import nju.pt.databaseassist.Data

@kotlinx.serialization.Serializable
data class Packet(
    val roomID: Int,
    val data: Data
)