package nju.pt.kotlin.ext


import nju.pt.R
import nju.pt.databaseassist.*
import nju.pt.server.ConfigData
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.*
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.io.FileOutputStream

fun Workbook.checkConfigExcel() {
    //用于程序启动时的检查，通过加载一遍来检查服务端配置、题目，裁判信息，学校有没有写错
    //由于队伍信息中可能抽签号还没有决定，不检查
    val logger = LoggerFactory.getLogger("Config Check Loader")
    logger.info("Checking config:")
    this.loadConfigFromExcel()

    logger.info("Checking questions:")
    this.loadQuestionFromExcel()

    logger.info("Checking schools & judges:")
    this.loadJudgeFromExcel()

}

fun Workbook.loadConfigFromExcel(): ConfigData {
    val logger = LoggerFactory.getLogger("Config Loader")
    logger.info("===================== loadConfigFromExcel =====================")
    var port = 0
    var judgeCount = 0
    var roomCount = 0
    var turns = 0
    var rWeight: Double = 0.0
    var oWeight: Double = 0.0
    var vWeight: Double = 0.0
    try {
        // 读取excel文件中的配置sheet
        val configSheet: Sheet
        try {
            configSheet = this@loadConfigFromExcel.getSheet(R.CONFIG_SHEET_NAME)
        } catch (e: NullPointerException) {
            logger.error("未找到sheet：" + e.message)
            throw Exception("未找到sheet，请检查sheet名称")
        }

        // 读取每行信息
        configSheet.rowIterator().asSequence().forEach { row ->
            val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
            if (cellValues.size > 1) {
                logger.info("cellValues = $cellValues")

                when (cellValues[0]) {
                    //  Excel单元格默认采用浮点数记录，故读取出的字符串会包含"."，需要用substringBefore截去
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
                    "比赛轮数" -> {
                        turns = cellValues[1].substringBefore(".").toInt()
                        logger.info("turns = $roomCount")
                    }
                    "正方分数权重" -> {
                        rWeight = cellValues[1].toDouble()
                        logger.info("roomCount = $rWeight")
                    }
                    "反方分数权重" -> {
                        oWeight = cellValues[1].toDouble()
                        logger.info("roomCount = $oWeight")
                    }
                    "评方分数权重" -> {
                        vWeight = cellValues[1].toDouble()
                        logger.info("roomCount = $vWeight")
                    }
                    else -> {
                        logger.error("无法识别，请检查服务端配置信息！")
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
        throw Exception("裁判数和会场数必须是大于零的整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    } catch (e: NullPointerException) {
        logger.error("配置信息不得有空：" + e.message)
        throw Exception("配置信息不得有空，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    } catch (e: Exception) {
        logger.error(e.message)
        throw Exception("服务端信息配置信息有误,请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }

    if (judgeCount <= 0) {
        logger.error("裁判数必须是大于零的整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        throw Exception("裁判数必须是大于零的整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }
    if (port <= 1024) {
        logger.error("端口号必须是大于1024的整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        throw Exception("端口号必须是大于1024的整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }

    if (roomCount <= 0) {
        logger.error("房间数必须是大于零的整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        throw Exception("房间数必须是大于零的整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }
    return ConfigData(
        port,
        judgeCount,
        roomCount,
        turns,
        rWeight,
        oWeight,
        vWeight,
    )
}

fun Workbook.loadQuestionFromExcel() = mutableMapOf<Int, String>().apply {
    val logger = LoggerFactory.getLogger("Question Data Loader")
    logger.info("===================== loadQuestionFromExcel =====================")
    var qCount = 0
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
                    qCount += 1
                }
            }
        }
        //判断题号是否有重复
        if (qCount != this.size) {
            logger.error("题号有重复，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
            throw Exception("题号有重复，请检查题号！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        }
        //判断题目号是否有重复
        if (qCount != this.values.distinct().size) {
            logger.error("题目名称有重复，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
            throw Exception("题号有重复，请检查题号！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        }
    } catch (e: NullPointerException) {
        logger.error("配置信息不得有空：" + e.message)
        throw Exception("配置信息不得有空，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    } catch (e: Exception) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("赛题信息填写有误！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }
}.entries.sortedBy { it.key }.associateBy({ it.key }, { it.value })

fun Workbook.loadSchoolFromExcel() = mutableMapOf<Int, String>().apply {
    // 学校id to 学校名称
    val logger = LoggerFactory.getLogger("School Data Loader")
    logger.info("===================== loadSchoolFromExcel =====================")

    try {
        val juedgeSheet: Sheet
        val teamSheet: Sheet
        try {
            juedgeSheet = this@loadSchoolFromExcel.getSheet(R.JUDGE_SHEET_NAME)
        } catch (e: NullPointerException) {
            logger.error("未找到sheet${R.JUDGE_SHEET_NAME}}：" + e.message)
            throw Exception("未找到sheet${R.JUDGE_SHEET_NAME}，请检查sheet名称，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        }
        try {
            teamSheet = this@loadSchoolFromExcel.getSheet(R.TEAM_SHEET_NAME)
        } catch (e: NullPointerException) {
            logger.error("未找到sheet${R.JUDGE_SHEET_NAME}}：" + e.message)
            throw Exception("未找到sheet${R.JUDGE_SHEET_NAME}，请检查sheet名称，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        }


        // 读取sheet内容
        var schoolIndex = 1
        logger.info("load school from judgeSheet")
        juedgeSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
                if (cellValues.size > 1) {
                    logger.info("cellValues = $cellValues")
                    this += (schoolIndex to cellValues[0])
                    logger.info("School Map = $this")
                    schoolIndex += 1
                }
            }
        }
        logger.info("load school from teamSheet")
        teamSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()

                if (cellValues.size > 1 && !this.values.contains(cellValues[0])) {
                    logger.info("cellValues = $cellValues")
                    this += (schoolIndex to cellValues[0])
                    logger.info("School Map = $this")
                    schoolIndex += 1
                }
            }
        }
    } catch (e: NullPointerException) {
        logger.error("未找到sheet：" + e.message)
        throw Exception("未找到sheet，请检查sheet名称，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    } catch (e: Exception) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("学校信息填写有误！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }

}.toMap()

fun Workbook.loadJudgeFromExcel() = mutableMapOf<String, List<String>>().apply {
    //返回 学校名称：裁判列表 的字典
    val schoolMap = this@loadJudgeFromExcel.loadSchoolFromExcel()

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
                    //检测裁判学校是否在提供的学校列表内
                    if (!schoolMap.values.contains(cellValues[0])) {
                        logger.error("${cellValues[0]}并未在提供的学校信息内！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
                        throw Exception("${cellValues[0]}并未在提供的学校信息内！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
                    }

                    logger.info("cellValues = $cellValues")

                    this += (cellValues[0] to cellValues.subList(
                        1,
                        cellValues.size
                    )).also { logger.info("School Map = $it") }


                }
            }
        }
    } catch (e: NullPointerException) {
        logger.error("裁判信息不完整：" + e.message)
        throw Exception("裁判信息不完整，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    } catch (e: Exception) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("裁判信息填写有误！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }

}.toMap()


fun Workbook.loadTeamFromExcel() = mutableListOf<TeamData>().apply {
    //[teamData1, teamData2, ...]

    val logger = LoggerFactory.getLogger("Team Data Loader")

    try {
        val teamSheet: Sheet
        try {
            teamSheet = this@loadTeamFromExcel.getSheet(R.TEAM_SHEET_NAME)
        } catch (e: NullPointerException) {
            logger.error("未找到sheet：" + e.message)
            throw Exception("未找到sheet，请检查sheet名称，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        }

        val reversedSchoolMap = this@loadTeamFromExcel.loadSchoolFromExcel().entries.associate { (k, v) -> v to k }
        logger.info("===================== loadTeamFromExcel =====================")

        var playerId = 1
        // 读取sheet内容
        teamSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
                if (cellValues.size > 1) {
                    logger.info("cellValues = $cellValues")
                    //判断队员名称-姓名是否齐全
                    if ((cellValues.size - 3) % 2 != 0) {
                        println(cellValues)
                        logger.error("第${rowIndex}行队伍/队员信息不全，请检查！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
                        throw Exception("第${rowIndex}行队伍/队员信息不全，请检查！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
                    }

                    //检测队伍学校是否在提供的学校列表内
                    if (!reversedSchoolMap.containsKey(cellValues[0])) {
                        logger.error("${cellValues[0]}并未在提供的学校信息内！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
                        throw Exception("${cellValues[0]}并未在提供的学校信息内！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
                    }

                    this += TeamData(
                        id = cellValues[2].substringBefore(".").toInt(),
                        name = cellValues[1],
                        schoolID = reversedSchoolMap[cellValues[0]]!!,
                        playerDataList = mutableListOf<PlayerData>().apply {
                            val subCellValues = cellValues.subList(3, cellValues.size)
                            for (index in subCellValues.indices step 2) {
                                this.add(
                                    PlayerData(
                                        // 队员id的命名规则为 学校id*1000 + 队伍抽签号 *10 + 队内序号
                                        id = playerId,
                                        name = subCellValues[index],
                                        gender = subCellValues[index + 1]
                                    )
                                )
                                playerId += 1
                            }
                        },
                        //recordDataList 默认为空列表
                    )

                }
            }
        }
        //检测抽签号是否有重复
        if (this.size != this.map { it.id }.distinct().size) {
            logger.error("抽签号有重复，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
            throw Exception("抽签号有重复，请检查队伍抽签号！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        }

    } catch (e: NullPointerException) {
        logger.error("队伍信息不完整：" + e.message)
        throw Exception("队伍信息不完整，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    } catch (e: NumberFormatException) {
        logger.error(e.message)
        logger.error(e.stackTraceToString())
        throw Exception("抽签号必须是整数！请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }
//    catch (e: Exception) {
//        logger.error(e.message)
//        logger.error(e.stackTraceToString())
//        throw Exception("队伍信息填写有误！")
//    }
}.toList()

fun Workbook.getTotalTeamNumber(): Int {
    val logger = LoggerFactory.getLogger("Tot team number Logger")
    logger.info("===================== getTotalTeamNumberFromExcel =====================")
    try {
        //考虑到有标题行的存在，故初始值记为-1
        var totalTeamNumber = -1

        this.getSheet(R.TEAM_SHEET_NAME).rowIterator().asSequence().forEach {
            if (it.cellIterator().asSequence().toList().size > 1) {
                totalTeamNumber += 1
            }
        }
        logger.info("total team number: $totalTeamNumber")
        return totalTeamNumber

    } catch (e: FileNotFoundException) {
        logger.error("未找到文件，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        throw Exception("未找到文件，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    } catch (e: NullPointerException) {
        logger.error("未找到sheet，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        throw Exception("未找到sheet，请检查sheet名称，请检查${R.CONFIG_EXCEL_PATH}配置文件！")

    }
}

// TODO: 2022/7/17 导入题库，初始化json
fun Workbook.loadQuestionBankFromExcel() = mutableListOf<Triple<String, String, List<Int>>>().apply {
    // [学校名，队伍名，题库列表]
    val logger = LoggerFactory.getLogger("QuestionBankLoader")

    try {
        val qBankSheet: Sheet
        try {
            qBankSheet = this@loadQuestionBankFromExcel.getSheet(R.QUESTIONBANK_SHEET_NAME)
        } catch (e: NullPointerException) {
            logger.error("未找到sheet${R.QUESTIONBANK_SHEET_NAME}：" + e.message)
            throw Exception("未找到sheet${R.QUESTIONBANK_SHEET_NAME}，请检查sheet名称，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
        }


        logger.info("===================== loadQuestionBankFromExcel =====================")
        // 读取sheet内容
        qBankSheet.rowIterator().asSequence().forEachIndexed { rowIndex, row ->
            //跳过第一行标题行
            if (rowIndex != 0) {
                val cellValues = row.cellIterator().asSequence().map { it.toString() }.toList()
                if (cellValues.size > 1) {
                    logger.info("cellValues = $cellValues")

                    this += Triple(
                        cellValues[0], cellValues[1],
                        cellValues[2].replace('，', ',').split(',').map { it.toInt() }
                    )
                }
            }
        }

    } catch (e: NullPointerException) {
        logger.error("题库信息不完整：" + e.message)
        throw Exception("题库信息不完整，请检查${R.CONFIG_EXCEL_PATH}配置文件！")
    }
}


fun Workbook.initializeJson() {
    val logger = LoggerFactory.getLogger("JsonInitialize Logger")

    val teamDataList = this.loadTeamFromExcel()
    val schoolMap = this.loadSchoolFromExcel()
    val questionMap = this.loadQuestionFromExcel()
    val questionBankList = this.loadQuestionBankFromExcel()

    //增加拒题题库
    if (questionBankList.isNotEmpty()) {
        logger.info("QuestionBank NOT empty")
        teamDataList.forEach { teamData ->
            questionBankList.first { it.second == teamData.name && it.first == schoolMap[teamData.schoolID] }
                .let { triple ->
                    logger.info("${triple.first}学校${triple.second}队伍题库为：${triple.third}")

                    val bannedQuestionList =
                        questionMap.keys.toMutableList().apply { this.removeIf { triple.third.contains(it) } }
                    logger.info("Ban题为：${bannedQuestionList}")

                    teamData.recordDataList = MutableList(bannedQuestionList.size) { index ->
                        RecordData(0, 0, 0, bannedQuestionList[index], 0, "B", 0.0, 0.0)
                    }
                }

        }
    } else {
        logger.info("QuestionBank empty")
    }

    JsonHelper.toJson(
        Data(
            teamDataList = teamDataList,
            questionMap = questionMap,
            schoolMap = schoolMap
        ),
        savePath = R.DATA_JSON_PATH
    )
}


fun Workbook.getTitleCellStyle(): CellStyle = this.createCellStyle().apply {
    //黄色，诸位边框实线

    //颜色填充
    fillForegroundColor = IndexedColors.YELLOW.index
    fillPattern = FillPatternType.SOLID_FOREGROUND

    //边界颜色
    borderBottom = (BorderStyle.THIN)
    bottomBorderColor = IndexedColors.BLACK.index
    borderLeft = (BorderStyle.THIN)
    leftBorderColor = IndexedColors.BLACK.index
    borderRight = (BorderStyle.THIN)
    rightBorderColor = IndexedColors.BLACK.index
    borderTop = (BorderStyle.THIN)
    topBorderColor = IndexedColors.BLACK.index

}

fun Workbook.initializeExcel() {
    val logger = LoggerFactory.getLogger("Server_ConfigInitializeLogger")
    logger.info("=====================Initialize Server Config Excel=====================")
    val titleStyle = this.getTitleCellStyle()

    this.apply {
        createSheet("软件配置").apply {
            createRow(0).createCell(0).apply {
                setCellValue("端口号");cellStyle = titleStyle.apply {
                defaultColumnWidth = 17
            }
            }
            createRow(1).createCell(0).apply {
                setCellValue("每场比赛裁判个数");cellStyle = titleStyle.apply {
                defaultColumnWidth = 17
            }
            }
            createRow(2).createCell(0).apply {
                setCellValue("会场总个数");cellStyle = titleStyle.apply {
                defaultColumnWidth = 17
            }
            }
            createRow(3).createCell(0).apply {
                setCellValue("比赛轮数");cellStyle = titleStyle.apply {
                defaultColumnWidth = 17
            }
            }
            createRow(4).createCell(0).apply {
                setCellValue("正方分数权重");cellStyle = titleStyle.apply {
                defaultColumnWidth = 17
            }
            }
            createRow(5).createCell(0).apply {
                setCellValue("反方分数权重");cellStyle = titleStyle.apply {
                defaultColumnWidth = 17
            }
            }
            createRow(6).createCell(0).apply {
                setCellValue("评方分数权重");cellStyle = titleStyle.apply {
                defaultColumnWidth = 17
            }
            }
        }
        createSheet("赛题信息").apply {
            createRow(0).apply {
                createCell(0).apply {
                    setCellValue("题号");cellStyle = titleStyle
                }
                createCell(1).apply {
                    setCellValue("题名");cellStyle = titleStyle
                }
            }

            createRow(1).apply {
                createCell(0).apply {
                    setCellValue("1")
                }
                createCell(1).apply {
                    setCellValue("你来发明")
                }
            }
        }
        createSheet("队伍信息").apply {
            createRow(0).apply {
                createCell(0).apply {
                    setCellValue("学校名");cellStyle = titleStyle
                }
                createCell(1).apply {
                    setCellValue("队伍名");cellStyle = titleStyle
                }
                createCell(2).apply {
                    setCellValue("抽签号");cellStyle = titleStyle
                }
                createCell(3).apply {
                    setCellValue("队员姓名");cellStyle = titleStyle
                }
                createCell(4).apply {
                    setCellValue("队员性别(男/女)");cellStyle = titleStyle
                }
                createCell(5).apply {
                    setCellValue("队员姓名");cellStyle = titleStyle
                }
                createCell(6).apply {
                    setCellValue("队员性别(男/女)");cellStyle = titleStyle
                }
            }

            createRow(1).apply {
                createCell(0).apply {
                    setCellValue("南京大学")
                }
                createCell(1).apply {
                    setCellValue("啦啦啦")
                }
                createCell(2).apply {
                    setCellValue("1")
                }
                createCell(3).apply {
                    setCellValue("张三")
                }
                createCell(4).apply {
                    setCellValue("男")
                }
                createCell(5).apply {
                    setCellValue("李四")
                }
                createCell(6).apply {
                    setCellValue("女")
                }
            }
        }
        createSheet("裁判信息").apply {
            createRow(0).apply {
                createCell(0).apply {
                    setCellValue("学校名");cellStyle = titleStyle
                }
                createCell(1).apply {
                    setCellValue("裁判们");cellStyle = titleStyle
                }
            }
            createRow(1).apply {
                createCell(0).apply {
                    setCellValue("南京大学")
                }
                createCell(1).apply {
                    setCellValue("裁判1")
                }
                createCell(2).apply {
                    setCellValue("裁判2")
                }
                createCell(3).apply {
                    setCellValue("裁判3")
                }
            }
        }

        createSheet("队伍题库").apply {
            createRow(0).apply {
                createCell(0).apply {
                    setCellValue("学校名");cellStyle = titleStyle
                }
                createCell(1).apply {
                    setCellValue("队伍名");cellStyle = titleStyle
                }
                createCell(2).apply {
                    setCellValue("题库");cellStyle = titleStyle
                }
                createCell(3).apply {
                    setCellValue("注：此表单为队伍的题库表单，用于不采用拒题而选择直接给出题库的比赛规则。若不需要此功能则不需要填写任何内容，也不要删除此表单。题库输入规则为题号用逗号隔开，例如：1,2,10 此处逗号半角圆角都可以")
                    cellStyle = this@initializeExcel.createCellStyle().apply {
                        setFont(
                            this@initializeExcel.createFont().apply {
                                color = HSSFColor.HSSFColorPredefined.RED.index
                            }
                        )
                    }
                }
            }
        }
        try {
            val fileOutputStream = FileOutputStream(R.CONFIG_EXCEL_PATH)
            this.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export team score successfully to ${R.CONFIG_EXCEL_PATH} !")
        } catch (e: FileNotFoundException) {
            logger.error(e.message)
            throw Exception("文件 ${R.CONFIG_EXCEL_PATH} 正被另一个程序占用，无法访问，请关闭！")
        }

    }

}

