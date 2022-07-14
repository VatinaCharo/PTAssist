package nju.pt.net

import nju.pt.databaseassist.Data

/**
 * Packet
 *
 * @property roomID 会场编号
 * @property round 比赛轮次
 * @property data 数据
 * @constructor Create Default Packet
 */
@kotlinx.serialization.Serializable
data class Packet(
    val roomID: Int,
    val round: Int,
    val data: Data?
)