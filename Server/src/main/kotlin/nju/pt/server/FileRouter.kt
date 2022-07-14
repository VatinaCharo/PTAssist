package nju.pt.server

import nju.pt.R
import nju.pt.databaseassist.Data
import nju.pt.databaseassist.JsonHelper
import nju.pt.net.FileRouterInterface
import nju.pt.net.Packet
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

class FileRouter : FileRouterInterface {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Get packet
     *
     * @param packet 空数据体数据包
     * @return 发送回客户端的数据包
     */
    override fun getPacket(packet: Packet): Packet {
        val roomID = packet.roomID
        val round = packet.round
        var data: Data? = null
        try {
            data = JsonHelper.fromJson<Data>("${R.SERVER_SEND_FILE_DIR_PATH}/Round$round/Room${roomID}.json")
        } catch (e: Exception) {
            logger.warn("服务端无法找到文件或该文件已损坏：${e.message}")
        }
        return Packet(roomID, round, data)
    }

    /**
     * Save packet
     *
     * @param packet 保存至服务端的数据包
     */
    override fun savePacket(packet: Packet) {
        val roomID = packet.roomID
        val round = packet.round
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(Date().time)
        JsonHelper.toJson(
            packet.data,
            "${R.SERVER_ACCEPT_FILE_TEMP_DIR_PATH}/Round${round}-Room${roomID}[$timeStamp].json"
        )
    }
}