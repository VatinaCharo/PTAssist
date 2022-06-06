package nju.pt.server

import nju.pt.R
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException

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


class RecordData(
    // 用于一条比赛记录
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

//一些配置信息
object ConfigData {

    private val logger = LoggerFactory.getLogger(ConfigData::class.java)

    private val configDataList by lazy { loadConfigFromExcel() }

    val port by lazy { configDataList[0] }
    val judgeCount by lazy { configDataList[1] }
    val roomCount by lazy { configDataList[2] }

    private fun loadConfigFromExcel(): List<Int> {
        var port: Int = R.DEFAULT_PORT
        var judgeCount: Int = 0
        var roomCount: Int = 0
        try {
            //读取excel文件中的配置sheet
            val configSheet = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getSheet(R.CONFIG_SHEET_NAME)
            // 读取每行信息
            configSheet.rowIterator().asSequence().forEach { row ->
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()

                try {
                    when (cellValues[0]) {
//                    Excel单元格默认采用浮点数记录，故读取出的字符串会包含"."，需要截去
                        "端口号" -> {
                            port = cellValues[1].substringBefore(".").toInt()
                            logger.info("port = $port")
                        }
                        "每场比赛裁判个数" -> {
                            judgeCount = cellValues[1].substringBefore(".").toInt()
                            logger.info("judgeCount = $judgeCount")
                        }
                        "会场总个数" -> {
                            roomCount = cellValues[1].substringBefore(".").toInt()
                            logger.info("roomCount = $roomCount")
                        }
                        else -> {
                            logger.error("无法识别，请检查服务端配置信息")
                            throw Exception("无法识别，请检查服务端配置信息！")
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e.message)
                    logger.error(e.stackTraceToString())
//                    没看懂这个地方的异常抛出是什么意思，所有的异常都是这个原因？
                    throw Exception("裁判数和会场数必须是大于零的整数！")
                }
            }
        } catch (e: FileNotFoundException) {
            logger.error("未找到文件: ${e.message}")
            throw Exception("未找到文件: ${e.message}")
        }

        if (judgeCount <= 0) {
            logger.error("裁判数必须是大于零的整数！")
            throw Exception("裁判数必须是大于零的整数！")
        }

        if (roomCount <= 0) {
            logger.error("房间数必须是大于零的整数！")
            throw Exception("房间数必须是大于零的整数！")
        }

        return listOf(port, judgeCount, roomCount)
    }


}


// 用于记录这次比赛的赛题信息
object QuestionData {
    val questionMap by lazy { loadQuestionFromExcel() }

    private fun loadQuestionFromExcel(): Map<Int, String> {
        val qMap: MutableMap<Int, String> = mutableMapOf(0 to "0").apply { this.clear() }

        val questionSheet = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getSheet(R.QUESTIONS_SHEET_NAME)

        questionSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()

                try {
                    qMap += (cellValues[0].substringBefore(".").toInt() to cellValues[1])
                } catch (e: Exception) {
                    throw Exception("赛题信息填写有误！")
                }
            }
        }
        return qMap.toMap()
    }

}

//参赛学校信息
object SchoolData {
    val schoolMap by lazy { loadSchoolFromExcel() }

    private fun loadSchoolFromExcel(): Map<Int, String> {
        val schMap: MutableMap<Int, String> = mutableMapOf(0 to "0").apply { this.clear() }

        val schoolSheet = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getSheet(R.SCHOOL_SHEET_NAME)

        schoolSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()

                try {
                    schMap += (cellValues[0].substringBefore(".").toInt() to cellValues[1])
                } catch (e: Exception) {
                    throw Exception("学校信息填写有误！")
                }
            }
        }
        return schMap.toMap()
    }
}


//object PlayerAndJudgeData{
//
//
//    val playerList: List<PlayerData>
//    val teamList: List<PlayerData>
//    val playerList: List<PlayerData>
//
//}
