package nju.pt.net

import nju.pt.databaseassist.Data

/**
 * Packet 数据包
 *
 * @property roomID 房间号
 * @property phase 总阶段数
 * @property teamIDMatchList 对阵表，由队伍id构成，第一位是第一阶段的正方，以此类推
 * @property data 数据库
 * @constructor Create empty Packet
 */
@kotlinx.serialization.Serializable
data class Packet(
    val roomID: Int,
    val round: Int,
    val phase: Int,
    val teamIDMatchList: List<Int>,
    val data: Data
)