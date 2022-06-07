package nju.pt.server

import nju.pt.R
import nju.pt.kotlin.ext.loadConfigFromExcel
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory

class PlayerData(
    var id: Int,
    var name: String,
    var gender: String,
    var teamId: Int
)


class TeamData(
    var id: Int,
    var name: String,
    var schoolId: Int
)

/**
 * 一条比赛记录
 */
class RecordData(
    var teamId: Int,
    var playerId: Int,
    var roomId: Int,
    var round: Int,
    var phase: Int,
    var questionId: Int,
    var role: Int,
    var score: Double,
    var rFactor: Double,
    var oFactor: Double,
    var vFactor: Double
)

/**
 * 配置信息
 */
object Config {

    private val logger = LoggerFactory.getLogger(Config::class.java)

    private val configDataList by lazy { WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadConfigFromExcel() }

    val port by lazy { configDataList[0] }
    val judgeCount by lazy { configDataList[1] }
    val roomCount by lazy { configDataList[2] }
}
