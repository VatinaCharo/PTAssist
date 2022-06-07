package nju.pt.kotlin.ext

import nju.pt.R
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import kotlin.math.log

fun Workbook.loadConfigFromExcel() = mutableListOf<Int>().apply {
    val logger = LoggerFactory.getLogger("Config Loader")
    logger.info("===================== loadConfigFromExcel =====================")
    var port: Int
    var judgeCount: Int = 0
    var roomCount: Int = 0
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