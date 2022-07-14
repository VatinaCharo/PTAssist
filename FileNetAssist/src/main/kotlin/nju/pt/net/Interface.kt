package nju.pt.net

interface FileRouterInterface {
    /**
     * Get packet
     *
     * @param packet 数据包
     * @return 发送回客户端的数据包
     */
    fun getPacket(packet: Packet): Packet

    /**
     * Save packet
     *
     * @param packet 保存至服务端的数据包
     */
    fun savePacket(packet: Packet)
}