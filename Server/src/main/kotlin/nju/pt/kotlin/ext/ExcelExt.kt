package nju.pt.kotlin.ext

import nju.pt.R
import nju.pt.databaseassist.JsonInterface
import nju.pt.databaseassist.PlayerData
import nju.pt.databaseassist.TeamData
import nju.pt.databaseassist.TeamDataList
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException


fun Workbook.loadConfigFromExcel() = mutableListOf<Any>().apply {
    val logger = LoggerFactory.getLogger("Config Loader")
    logger.info("===================== loadConfigFromExcel =====================")
    var port: Int
    var judgeCount = 0
    var roomCount = 0
    var rWeight: Double
    var oWeight: Double
    var vWeight: Double
    try {
        // 读取excel文件中的配置sheet
        val configSheet = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getSheet(R.CONFIG_SHEET_NAME)
        // 读取每行信息
        configSheet.rowIterator().asSequence().forEach { row ->
            val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
            if (cellValues.size > 1) {
                logger.info("cellValues = $cellValues")

                when (cellValues[0]) {
                    //  Excel单元格默认采用浮点数记录，故读取出的字符串会包含"."，需要用substringBefore截去
                    "端口号" -> {
                        port = cellValues[1].substringBefore(".").toInt()
                        this.add(0, port)
                        logger.info("port = $port")
                    }
                    "每场比赛裁判个数" -> {
                        judgeCount = cellValues[1].substringBefore(".").toInt()
                        this.add(1, judgeCount)
                        logger.info("judgeCount = $judgeCount")
                    }
                    "会场总个数" -> {
                        roomCount = cellValues[1].substringBefore(".").toInt()
                        this.add(2, roomCount)
                        logger.info("roomCount = $roomCount")
                    }
                    "正方分数权重" -> {
                        rWeight = cellValues[1].toDouble()
                        this.add(3, rWeight)
                        logger.info("roomCount = $rWeight")
                    }
                    "反方分数权重" -> {
                        oWeight = cellValues[1].toDouble()
                        this.add(4, oWeight)
                        logger.info("roomCount = $oWeight")
                    }
                    "评方分数权重" -> {
                        vWeight = cellValues[1].toDouble()
                        this.add(5, vWeight)
                        logger.info("roomCount = $vWeight")
                    }
                    else -> {
                        logger.error("无法识别，请检查服务端配置信息")
                        throw Exception("无法识别，请检查服务端配置信息！")
                    }
                }
            }
        }
    } catch (e: FileNotFoundException) {
        logger.error("未找到文件: ${e.message}")
        throw Exception("未找到文件: ${e.message}")
    } catch (e: NumberFormatException) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("裁判数和会场数必须是大于零的整数！")
    } catch (e: NullPointerException) {
        logger.error("未找到sheet：" + e.message)
        throw Exception("未找到sheet，请检查sheet名称")
    } catch (e: Exception) {
        logger.error(e.message)
        throw Exception("服务端信息配置信息有误")
    }

    if (judgeCount <= 0) {
        logger.error("裁判数必须是大于零的整数！")
        throw Exception("裁判数必须是大于零的整数！")
    }

    if (roomCount <= 0) {
        logger.error("房间数必须是大于零的整数！")
        throw Exception("房间数必须是大于零的整数！")
    }
}.toList()

fun Workbook.loadQuestionFromExcel() = mutableMapOf<Int, String>().apply {
    val logger = LoggerFactory.getLogger("Question Data Loader")
    logger.info("===================== loadQuestionFromExcel =====================")
    try {
        val questionSheet = this@loadQuestionFromExcel.getSheet(R.QUESTIONS_SHEET_NAME)

        // 读取sheet内容
        questionSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
                if (cellValues.size > 1) {
                    logger.info("cellValues = $cellValues")
                    this += (cellValues[0].substringBefore(".").toInt() to cellValues[1])
                    logger.info("Question Map = $this")
                }
            }
        }
    } catch (e: NullPointerException) {
        logger.error("未找到sheet：" + e.message)
        throw Exception("未找到sheet，请检查sheet名称")
    } catch (e: Exception) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("赛题信息填写有误！")
    }


}.toMap()

fun Workbook.loadSchoolFromExcel() = mutableMapOf<Int, String>().apply {
    val logger = LoggerFactory.getLogger("School Data Loader")
    logger.info("===================== loadSchoolFromExcel =====================")

    try {
        val schoolSheet = this@loadSchoolFromExcel.getSheet(R.SCHOOL_SHEET_NAME)

        // 读取sheet内容
        schoolSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
                if (cellValues.size > 1) {
                    logger.info("cellValues = $cellValues")
                    this += (cellValues[0].substringBefore(".").toInt() to cellValues[1])
                    logger.info("School Map = $this")
                }
            }
        }
    } catch (e: NullPointerException) {
        logger.error("未找到sheet：" + e.message)
        throw Exception("未找到sheet，请检查sheet名称")
    } catch (e: Exception) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("学校信息填写有误！")
    }

}.toMap()

fun Workbook.loadJudgeFromExcel() = mutableMapOf<String, List<String>>().apply {
    val logger = LoggerFactory.getLogger("Judge Data Loader")
    logger.info("===================== loadJudgeFromExcel =====================")

    try {
        val judgeSheet = this@loadJudgeFromExcel.getSheet(R.JUDGE_SHEET_NAME)

        // 读取sheet内容
        judgeSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
                if (cellValues.size > 1) {
                    logger.info("cellValues = $cellValues")
                    this += (cellValues[0] to cellValues.subList(1, cellValues.size))
                    logger.info("School Map = $this")
                }
            }
        }
    } catch (e: NullPointerException) {
        logger.error("未找到sheet：" + e.message)
        throw Exception("未找到sheet，请检查sheet名称")
    } catch (e: Exception) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("裁判信息填写有误！")
    }

}.toMap()


fun Workbook.loadTeamFromExcel() = mutableListOf<TeamData>().apply {
    val logger = LoggerFactory.getLogger("Team Data Loader")


    try {
        val teamSheet = this@loadTeamFromExcel.getSheet(R.TEAM_SHEET_NAME)
        val reversedSchoolMap = this@loadTeamFromExcel.loadSchoolFromExcel().entries.associate { (k, v) -> v to k }
        logger.info("===================== loadTeamFromExcel =====================")
        // 读取sheet内容
        teamSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
                if (cellValues.size > 1) {
                    logger.info("cellValues = $cellValues")
                    if ((cellValues.size - 3) % 2 != 0) {
                        logger.error("队员信息不全！")
                        throw Exception("队员信息不全！")
                    }


                    this += TeamData(
                        id = cellValues[2].substringBefore(".").toInt(),
                        name = cellValues[1],
                        schoolId = reversedSchoolMap[cellValues[0]]!!,
                        playerDataList = mutableListOf<PlayerData>().apply {
                            val subCellValues = cellValues.subList(3, cellValues.size)
                            for (index in subCellValues.indices step 2) {
                                this.add(
                                    PlayerData(
                                        id = reversedSchoolMap[cellValues[0]]!! * 100 + index,
                                        name = subCellValues[index],
                                        gender = subCellValues[index + 1]
                                    )
                                )
                            }
                        },
                        recordDataList = null
                    )

                }
            }
        }
    } catch (e: NullPointerException) {
        logger.error("未找到sheet：" + e.message)
        throw Exception("未找到sheet，请检查sheet名称")
    } catch (e: Exception) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("队伍信息填写有误！")
    } catch (e: NumberFormatException) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("抽签号必须是整数！")
    }

}.toList()


fun Workbook.initializeJson() {
    JsonInterface.toJson(
        TeamDataList(
            this.loadTeamFromExcel(),
            this.loadQuestionFromExcel()
        ),
        R.DATA_JSON_PATH
    )


}