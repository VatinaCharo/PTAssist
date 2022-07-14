package nju.pt.server

import nju.pt.R
import nju.pt.databaseassist.JsonHelper
import nju.pt.kotlin.ext.*
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.math.exp


//对阵表生成
@kotlinx.serialization.Serializable
class CounterPartTable(
    val totalTeamNumber: Int = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getTotalTeamNumber(),
    val JUDGEMAP: Map<String, List<String>> = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadJudgeFromExcel(),
    val SCHOOLMAP: Map<Int, String> = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadSchoolFromExcel(),

    ) {
    val logger = LoggerFactory.getLogger("CounterPartTableLogger")

    val roomCount: Int = Config.roomCount as Int//会场个数

    //记录总的对阵表
    var teamTableList = mutableListOf<OneRoundTable>()

    //记录未打乱会场的总的对阵表
    val teamTableListWithoutShuffle = mutableListOf<OneRoundTable>()

    fun generateTableWithoutJudge(): MutableList<OneRoundTable> //turns为需要产生对阵表的轮数
    {
        val turns: Int = Config.turns as Int
        logger.info("===================== generateTableWithoutJudge =====================")
        logger.info("roomCount:${roomCount}")
        logger.info("totalTeamNumber:${totalTeamNumber}")

        //生成第一轮的对阵表
        val oneRoundTable = OneRoundTable(roomCount, totalTeamNumber).apply { initialize() }

        teamTableListWithoutShuffle.add(oneRoundTable.copy().also {
            logger.info("Round 1 table WITHOUT shuffle:")
            logger.info("R: ${it.RList}")
            logger.info("O: ${it.OList}")
            logger.info("V: ${it.VList}")
            logger.info("OB: ${it.OBList}")
        })
        teamTableList.add(oneRoundTable.shuffle().also {
            logger.info("Round 1 table WITH shuffle:")
            logger.info("R: ${it.RList}")
            logger.info("O: ${it.OList}")
            logger.info("V: ${it.VList}")
            logger.info("OB: ${it.OBList}")
        })


        //生成第二轮及以后的对阵表
        for (turn in 1 until turns) {
            oneRoundTable.roomOffset()
            teamTableListWithoutShuffle.add(oneRoundTable.copy().also {
                logger.info("Round ${turn + 1} table WITHOUT shuffle:")
                logger.info("R: ${it.RList}")
                logger.info("O: ${it.OList}")
                logger.info("V: ${it.VList}")
                logger.info("OB: ${it.OBList}")
            })
            teamTableList.add(oneRoundTable.shuffle().also {
                logger.info("Round ${turn + 1} table WITH shuffle:")
                logger.info("R: ${it.RList}")
                logger.info("O: ${it.OList}")
                logger.info("V: ${it.VList}")
                logger.info("OB: ${it.OBList}")
            })

        }
        JsonHelper.toJson(this, R.COUNTERPART_TABLE_JSON_PATH)
        logger.info("Json Saved to ${R.COUNTERPART_TABLE_JSON_PATH}")
        tableWriteIntoExcel(teamTableList, teamTableListWithoutShuffle)
        logger.info("CounterpartTable WITHOUT judge generated successfully!")
        return teamTableList
    }

    fun generateTableWithJudge() {
        logger.info("===================== generateTableWithJudge =====================")

        // TODO: 2022/7/9 没有json文件的异常已经被JsonHelper捕捉了，怎么提示先生成没有裁判的对阵表?
        teamTableList = JsonHelper.fromJson<CounterPartTable>(R.COUNTERPART_TABLE_JSON_PATH).teamTableList

        //每个会场裁判个数
        val judgeCount = Config.judgeCount as Int
        // 裁判序号 to （裁判学校，裁判姓名）
        val judgeMap = mutableMapOf<Int, Pair<String, String>>().apply {
            var i = 0
            JUDGEMAP.forEach { (schoolName, judgeList) ->
                judgeList.forEach { judgeName ->
                    this[i] = Pair(schoolName, judgeName)
                    i += 1
                }
            }
        }

        // 队伍抽签号 to 学校名称
        val teamIdMap = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadTeamFromExcel().associate {
            it.id to SCHOOLMAP[it.schoolID]
        }


        //由于裁判是较为随机决定的，偶尔会出现由于分配不是很合理，选不出裁判的情况，故默认最大的尝试次数为1001次，若还是得不到结果就报错
        for (attemptNumber in 0..1000) {
            logger.info("===================== Attempt ${attemptNumber + 1} =====================")

            //创建记录不同轮次不同会场的裁判列表 [[第一轮 1,2,3,4,... 会场的裁判],[第二轮 1,2,3,4...会场的裁判],...]
            val judgeTableAllTurns = mutableListOf<MutableList<MutableList<Int>>>()

            //创建总轮次裁判序号 to 已上场次数的字典，用于均衡全部轮次各裁判的上场次数
            val judgeUsedMap = judgeMap.keys.associateWith { 0 }.toMutableMap()

            //遍历不同的轮次
            turnLoop@ for (turn in 0 until teamTableList.size) {
                logger.info("--------------------- Round ${turn + 1} ---------------------")

                //读取本轮队伍对阵表
                val teamTable = teamTableList[turn]

                //创建用于存储本轮所用裁判序号
                val judgeTable = mutableListOf<MutableList<Int>>()

                //创建本轮次已上场裁判的序号，用于避免一个老师在一轮中在多个会场出现
                val judgeUsedList = mutableListOf<Int>()

                //遍历每一个会场的情况
                for (room in 0 until roomCount) {
                    logger.info("room ${room + 1}:")
                    logger.info("R:${teamTable.RList[room]}-${teamIdMap[teamTable.RList[room]]}")
                    logger.info("O:${teamTable.OList[room]}-${teamIdMap[teamTable.OList[room]]}")
                    logger.info("V:${teamTable.VList[room]}-${teamIdMap[teamTable.VList[room]]}")
                    logger.info("OB:${teamTable.OBList[room]}-${teamIdMap[teamTable.OBList[room]]}")

                    //创建用于储存本会场所用裁判序号
                    val judgeTableRoom = mutableListOf<Int>()

                    //得到参赛队伍学校名称列表
                    val playerSchoolNameList = mutableListOf<String>().apply {
                        teamIdMap[teamTable.RList[room]]?.let { this.add(it) }
                        teamIdMap[teamTable.OList[room]]?.let { this.add(it) }
                        teamIdMap[teamTable.VList[room]]?.let { this.add(it) }
                        if (teamTable.OBList[room] != -1) {
                            teamIdMap[teamTable.OBList[room]]?.let { this.add(it) }
                        }
                    }.distinct()

                    logger.info("Team School set: $playerSchoolNameList")

                    //可用裁判的选择规则是 不与参赛队员学校相同，且未当过本轮裁判
                    val availableJudgeList = judgeMap.filter {
                        (it.value.first !in playerSchoolNameList) && (it.key !in judgeUsedList)
                    }.toList().toMutableList()


                    if (availableJudgeList.size < judgeCount) {
                        if (attemptNumber == 1000) {
                            throw Exception("学校(${playerSchoolNameList})没有足够多的裁判，请补充后再试！")
                        } else {
                            break@turnLoop
                        }

                    }

                    for (judgeNumber in 0 until judgeCount) {
                        //若之前选过同学校的老师，则人为地将其下次被选中的概率降低
                        val selectedIndex = selectOne(availableJudgeList.mapNotNull { it ->
                            if (it.second.first !in judgeUsedList.map { judgeMap[it]!!.first }) judgeUsedMap[it.first] else judgeUsedMap[it.first]!! + 5
                        })

                        availableJudgeList[selectedIndex].first.let {
                            //添加到该会场的裁判存储列表
                            judgeTableRoom.add(it)
                            //添加到本轮已出场的裁判列表
                            judgeUsedList.add(it)
                            //该裁判总出场次数+1
                            judgeUsedMap[it] = judgeUsedMap[it]!! + 1

                        }
                        //在可用裁判列表中删除
                        availableJudgeList.removeAt(selectedIndex)
                    }
                    judgeTable.add(judgeTableRoom)

                    logger.info("Judges:${judgeTableRoom.associateWith { judgeMap[it] }}")

                }
                judgeTableAllTurns.add(judgeTable)


            }

            tableWithJudgeWriteIntoExcel(teamTableList,
                judgeTableAllTurns.map { it.map { it.mapNotNull { judgeMap[it] } } }
            )
            logger.info("CounterPartTable WITH judge generated successfully!")


            break

        }


    }

    private fun selectOne(countList: List<Int>): Int {
        //输入权重列表，返回抽签得到的值的索引

        val probabilityList = MutableList<Double>(countList.size) { index ->
            //先用e指数作用
            exp(-countList[index].toDouble())
        }.apply {
            val sum = this.sum()
            //后归一化
            this.forEachIndexed { index, _ ->
                this[index] /= sum
            }
        }

        //0-1的随机数
        val random = Random(System.nanoTime()).nextDouble()

        //轮盘算法，抽签
        var sum: Double = 0.0
        probabilityList.forEachIndexed { index, p ->
            if (sum >= random) {
                return index
            } else {
                sum += p
            }
        }
        return probabilityList.size - 1
    }

    private fun tableWriteIntoExcel(
        playerTableListWithShuffle: MutableList<OneRoundTable>,
        playerTableListWithoutShuffle: MutableList<OneRoundTable>,
    ) {
        val logger = LoggerFactory.getLogger("Export CounterPart Table Without Judge")
        logger.info("===================== ExportCounterPartTableWithoutJudge =====================")

        logger.info("Examining whether the file exists:")
        if (Path(R.COUNTERPART_TABLE_EXCEL_PATH).notExists()) {
            logger.info("Not Exist, creating...")
            XSSFWorkbook().write(FileOutputStream(R.COUNTERPART_TABLE_EXCEL_PATH))
            logger.info("New excel file created successfully!")
        } else {
            logger.info("File already exists, reading...")
        }

        val counterPartTableWorkbook = WorkbookFactory.create(FileInputStream(R.COUNTERPART_TABLE_EXCEL_PATH)).apply {
            val titleStyle = this.getTitleCellStyle()

            logger.info("--------------------- 对阵表 ---------------------")
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            try {
                this.removeSheetAt(this.getSheetIndex("对阵表(无裁判)"))
                logger.info("Exists, deleting and updating...")
            } catch (e: java.lang.Exception) {
                logger.info("Not exists, creating...")
            }

            createSheet("对阵表(无裁判)").apply {
                playerTableListWithoutShuffle.forEachIndexed { turn, oneRoundTableWithoutShuffle ->

                    val oneRoundTableWithShuffle = playerTableListWithShuffle[turn]
                    val everyRoundStartRow = (4 + roomCount) * turn

                    //标题行
                    this.createRow(everyRoundStartRow).apply {
                        createCell(0).apply { setCellValue("第${turn + 1}轮对阵表") }
                    }
                    this.createRow(everyRoundStartRow + 1).apply {
                        createCell(1).apply { setCellValue("已打乱") }
                        createCell(6).apply { setCellValue("未打乱") }
                    }
                    logger.info("--------------------- Round ${turn + 1} ---------------------")

                    this.createRow(everyRoundStartRow + 2).apply {
                        createCell(1).apply { setCellValue("正方");cellStyle = titleStyle }
                        createCell(2).apply { setCellValue("反方");cellStyle = titleStyle }
                        createCell(3).apply { setCellValue("评方");cellStyle = titleStyle }
                        createCell(4).apply { setCellValue("观摩方");cellStyle = titleStyle }

                        createCell(6).apply { setCellValue("正方");cellStyle = titleStyle }
                        createCell(7).apply { setCellValue("反方");cellStyle = titleStyle }
                        createCell(8).apply { setCellValue("评方");cellStyle = titleStyle }
                        createCell(9).apply { setCellValue("观摩方");cellStyle = titleStyle }
                    }

                    //各个会场的对阵表
                    for (room in 0 until roomCount) {
                        this.createRow(everyRoundStartRow + room + 3).apply {
                            createCell(0).apply { setCellValue("会场${room + 1}");cellStyle = titleStyle }
                            logger.info("Room ${room + 1}:")
                            //打乱对阵表情况
                            logger.info("WITH shuffle")
                            createCell(1).setCellValue("${oneRoundTableWithShuffle.RList[room]}")
                            logger.info("R:${oneRoundTableWithShuffle.RList[room]}")
                            createCell(2).setCellValue("${oneRoundTableWithShuffle.OList[room]}")
                            logger.info("O:${oneRoundTableWithShuffle.OList[room]}")
                            createCell(3).setCellValue("${oneRoundTableWithShuffle.VList[room]}")
                            logger.info("V:${oneRoundTableWithShuffle.VList[room]}")
                            createCell(4).setCellValue("${oneRoundTableWithShuffle.OBList[room]}")
                            logger.info("OB:${oneRoundTableWithShuffle.OBList[room]}")

                            //未打乱对阵表
                            logger.info("WITHOUT shuffle")
                            createCell(6).setCellValue("${oneRoundTableWithoutShuffle.RList[room]}")
                            logger.info("R:${oneRoundTableWithoutShuffle.RList[room]}")
                            createCell(7).setCellValue("${oneRoundTableWithoutShuffle.OList[room]}")
                            logger.info("O:${oneRoundTableWithoutShuffle.OList[room]}")
                            createCell(8).setCellValue("${oneRoundTableWithoutShuffle.VList[room]}")
                            logger.info("V:${oneRoundTableWithoutShuffle.VList[room]}")
                            createCell(9).setCellValue("${oneRoundTableWithoutShuffle.OBList[room]}")
                            logger.info("OB:${oneRoundTableWithoutShuffle.OBList[room]}")

                        }
                    }

                }
            }

        }

        try {
            val fileOutputStream = FileOutputStream(R.COUNTERPART_TABLE_EXCEL_PATH)
            counterPartTableWorkbook.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export CounterPartTable successfully to ${R.COUNTERPART_TABLE_EXCEL_PATH} !")
        } catch (e: FileNotFoundException) {
            logger.error(e.message)
            throw Exception("文件 ${R.COUNTERPART_TABLE_EXCEL_PATH} 正被另一个程序占用，无法访问，请关闭！")
        }


    }

    private fun tableWithJudgeWriteIntoExcel(
        playerTableList: MutableList<OneRoundTable>,
        // 裁判列表，第一级是轮数，第二级是会场，第三级是裁判列表，Pair提供(裁判学校,裁判姓名)
        judgeTableList: List<List<List<Pair<String, String>>>>,
    ) {
        val logger = LoggerFactory.getLogger("Export CounterPart Table With Judge")
        logger.info("===================== ExportCounterPartTableWithJudge =====================")

        // TODO: 2022/7/9 解除Excel依赖
        // 队伍抽签号 to (队伍名称,学校名称)
        val teamIdMap = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadTeamFromExcel().associate {
            it.id to Pair(it.name, SCHOOLMAP[it.schoolID])
        }

        //若文件不存在，则创建
        logger.info("Examining whether the file exists:")
        if (Path(R.COUNTERPART_TABLE_EXCEL_PATH).notExists()) {
            logger.info("Not Exist, creating...")
            XSSFWorkbook().write(FileOutputStream(R.COUNTERPART_TABLE_EXCEL_PATH))
            logger.info("New excel file created successfully!")
        } else {
            logger.info("File already exists, reading...")
        }


        val counterPartTableWorkbook = WorkbookFactory.create(FileInputStream(R.COUNTERPART_TABLE_EXCEL_PATH)).apply {
            val titleStyle = this.getTitleCellStyle()

            logger.info("--------------------- 对阵表 ---------------------")
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            try {
                this.removeSheetAt(this.getSheetIndex("对阵表"))
                logger.info("Exists, deleting and updating...")
            } catch (e: java.lang.Exception) {
                logger.info("Not exists, creating...")
            }

            createSheet("对阵表").apply {
                playerTableList.forEachIndexed { turn, oneRoundTable ->
                    //标题行
                    this.createRow((3 + roomCount) * turn).apply {
                        createCell(0).apply { setCellValue("第${turn + 1}轮对阵表") }
                    }
                    logger.info("--------------------- Round ${turn + 1} ---------------------")

                    this.createRow((3 + roomCount) * turn + 1).apply {
                        createCell(1).apply { setCellValue("正方");cellStyle = titleStyle }
                        createCell(2).apply { setCellValue("反方");cellStyle = titleStyle }
                        createCell(3).apply { setCellValue("评方");cellStyle = titleStyle }
                        createCell(4).apply { setCellValue("观摩方");cellStyle = titleStyle }
                        createCell(5).apply { setCellValue("裁判们") }
                    }

                    //各个会场的对阵表
                    for (room in 0 until roomCount) {
                        this.createRow((3 + roomCount) * turn + room + 2).apply {
                            createCell(0).apply { setCellValue("会场${room + 1}");cellStyle = titleStyle }
                            logger.info("Room ${room + 1}:")
                            //队伍情况
                            createCell(1).setCellValue("${oneRoundTable.RList[room]}")
                            logger.info("R:${oneRoundTable.RList[room]}")
                            createCell(2).setCellValue("${oneRoundTable.OList[room]}")
                            logger.info("O:${oneRoundTable.OList[room]}")
                            createCell(3).setCellValue("${oneRoundTable.VList[room]}")
                            logger.info("V:${oneRoundTable.VList[room]}")
                            createCell(4).setCellValue("${oneRoundTable.OBList[room]}")
                            logger.info("OB:${oneRoundTable.OBList[room]}")

                            //裁判情况
                            judgeTableList[turn][room].forEachIndexed { judgeNumber, pair ->
                                createCell(5 + judgeNumber).setCellValue(pair.second)
                            }
                            logger.info("Judges:${judgeTableList[turn][room]}")

                        }
                    }

                }
            }

            logger.info("--------------------- 对阵表（含学校名） ---------------------")
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            try {
                this.removeSheetAt(this.getSheetIndex("对阵表（含学校名）"))
                logger.info("Exists, deleting and updating...")
            } catch (e: java.lang.Exception) {
                logger.info("Not exists, creating...")
            }

            createSheet("对阵表（含学校名）").apply {
                playerTableList.forEachIndexed { turn, oneRoundTable ->
                    //标题行
                    this.createRow((3 + roomCount) * turn).apply {
                        createCell(0).apply { setCellValue("第${turn + 1}轮对阵表") }
                    }
                    logger.info("--------------------- Round ${turn + 1} ---------------------")

                    this.createRow((3 + roomCount) * turn + 1).apply {
                        createCell(1).apply { setCellValue("正方");cellStyle = titleStyle }
                        createCell(2).apply { setCellValue("反方");cellStyle = titleStyle }
                        createCell(3).apply { setCellValue("评方");cellStyle = titleStyle }
                        createCell(4).apply { setCellValue("观摩方");cellStyle = titleStyle }
                        createCell(5).apply { setCellValue("裁判们") }
                    }

                    //各个会场的对阵表
                    for (room in 0 until roomCount) {
                        this.createRow((3 + roomCount) * turn + room + 2).apply {
                            createCell(0).apply { setCellValue("会场${room + 1}");cellStyle = titleStyle }
                            logger.info("Room ${room + 1}:")
                            //队伍情况
                            createCell(1).setCellValue("${teamIdMap[oneRoundTable.RList[room]]}")
                            logger.info("R:${teamIdMap[oneRoundTable.RList[room]]}")
                            createCell(2).setCellValue("${teamIdMap[oneRoundTable.OList[room]]}")
                            logger.info("O:${teamIdMap[oneRoundTable.OList[room]]}")
                            createCell(3).setCellValue("${teamIdMap[oneRoundTable.VList[room]]}")
                            logger.info("V:${teamIdMap[oneRoundTable.VList[room]]}")
                            createCell(4).setCellValue(
                                "${teamIdMap.getOrElse(oneRoundTable.OBList[room]) { "-1" }}"
                            )
                            logger.info("OB:${teamIdMap.getOrElse(oneRoundTable.OBList[room]) { "-1" }}")

                            //裁判情况
                            judgeTableList[turn][room].forEachIndexed { judgeNumber, pair ->
                                createCell(5 + judgeNumber).setCellValue("$pair")
                            }
                            logger.info("Judges:${judgeTableList[turn][room]}")

                        }
                    }

                }
            }

        }

        try {
            val fileOutputStream = FileOutputStream(R.COUNTERPART_TABLE_EXCEL_PATH)
            counterPartTableWorkbook.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export CounterPartTable successfully to ${R.COUNTERPART_TABLE_EXCEL_PATH} !")
        } catch (e: FileNotFoundException) {
            logger.error(e.message)
            throw Exception("文件 ${R.COUNTERPART_TABLE_EXCEL_PATH} 正被另一个程序占用，无法访问，请关闭！")
        }

    }
}

//一轮的参赛队伍对阵表
@kotlinx.serialization.Serializable
data class OneRoundTable(
    val roomCount: Int,//会场个数
    val totalTeamNumber: Int //总队伍数
) {

    private val fourPlayersNumber = totalTeamNumber.mod(roomCount) // 本次比赛中存在四个队伍的个数

    // 各个角色的编号列表，其索引号为会场号
    lateinit var RList: MutableList<Int>
    lateinit var OList: MutableList<Int>
    lateinit var VList: MutableList<Int>
    lateinit var OBList: MutableList<Int>

    fun initialize() {
        RList = (1 until roomCount + 1).toMutableList()
        OList = (roomCount + 1 until roomCount * 2 + 1).toMutableList()
        VList = (roomCount * 2 + 1 until roomCount * 3 + 1).toMutableList()
        OBList = MutableList(roomCount) { index ->
            if (index < fourPlayersNumber) roomCount * 3 + index + 1 else -1
        }
    }

    fun roomOffset(): OneRoundTable {
        //反方右移1格，评方右移2格，观摩方右移3格，改变原对象

        //燕哥别移出去！
        fun offset(list: MutableList<Int>, offset: Int) = MutableList(list.size) { index ->
            list[(index - offset).mod(list.size)]
        }

        return this.apply {
            OList = offset(OList, 1)
            VList = offset(VList, 2)
            OBList = offset(OBList, 3)
        }
    }


    fun shuffle(): OneRoundTable {
        //返回打乱会场的单次对阵表对象，且不改变原对象

        //按照indexList排序
        fun sortByIndexList(list: MutableList<Int>, indexList: List<Int>) = MutableList(list.size) { index ->
            list[indexList[index]]
        }

        //创建用于打乱顺序的索引列表
        val indexList = (0 until roomCount).toList().shuffled()

        return this.copy().apply {
            //按照同一索引列表将所有角色顺序打乱
            this.RList = sortByIndexList(RList, indexList)
            this.OList = sortByIndexList(OList, indexList)
            this.VList = sortByIndexList(VList, indexList)
            this.OBList = sortByIndexList(OBList, indexList)
        }
    }

    fun copy(): OneRoundTable {
        return OneRoundTable(this.roomCount, this.totalTeamNumber).apply {
            RList = this@OneRoundTable.RList
            OList = this@OneRoundTable.OList
            VList = this@OneRoundTable.VList
            OBList = this@OneRoundTable.OBList
        }
    }
}