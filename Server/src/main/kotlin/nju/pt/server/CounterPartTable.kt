package nju.pt.server

import nju.pt.R
import nju.pt.kotlin.ext.*
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.exp


//对阵表生成
class CounterPartTable {
    val logger = LoggerFactory.getLogger("CounterPartTableLogger")


    val roomCount: Int = Config.roomCount as Int//会场个数

    // TODO: 2022/7/8 要把这个excel的依赖去掉
    val totalTeamNumber: Int = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).getTotalTeamNumber()//总队伍数

    //记录总的对阵表
    val teamTableList = mutableListOf<OneRoundTable>()

    //记录未打乱会场的总的对阵表
    val teamTableListWithoutShuffle = mutableListOf<OneRoundTable>()

    fun generateTableWithoutJudge(turns: Int = 3): MutableList<OneRoundTable> //turns为需要产生对阵表的轮数
    {
        logger.info("===================== generateTableWithoutJudge =====================")
        logger.info("roomCount:${roomCount}")
        logger.info("totalTeamNumber:${totalTeamNumber}")

        //生成第一轮的对阵表
        val oneRoundTable = OneRoundTable(roomCount, totalTeamNumber)

        teamTableListWithoutShuffle.add(oneRoundTable.copy().also {
            logger.info("Round 1 table without shuffle:")
            logger.info("R: ${it.RList}")
            logger.info("O: ${it.OList}")
            logger.info("V: ${it.VList}")
            logger.info("OB: ${it.OBList}")
        })
        teamTableList.add(oneRoundTable.shuffle())


        //生成第二轮及以后的对阵表
        for (turn in 1 until turns) {
            oneRoundTable.roomOffset()
            teamTableListWithoutShuffle.add(oneRoundTable.copy().also {
                logger.info("Round ${turn + 1} table without shuffle:")
                logger.info("R: ${it.RList}")
                logger.info("O: ${it.OList}")
                logger.info("V: ${it.VList}")
                logger.info("OB: ${it.OBList}")
            })
            teamTableList.add(oneRoundTable.shuffle())

        }

        logger.info("CounterpartTable WITHOUT judge generated successfully!")
        return teamTableList
    }

    fun generateTableWithJudge() {
        logger.info("===================== generateTableWithJudge =====================")

        if (teamTableList.size < 1 || teamTableListWithoutShuffle.size < 1) {
            logger.error("请先生成没有裁判的对阵表！")
            throw Exception("请先生成没有裁判的对阵表！")
        }

        //每个会场裁判个数
        val judgeCount = Config.judgeCount as Int
        // TODO: 2022/7/8 解除excel的依赖
        // 裁判序号 to （裁判学校，裁判姓名）
        val judgeMap = mutableMapOf<Int, Pair<String, String>>().apply {
            var i = 0
            WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadJudgeFromExcel().forEach { (schoolName, judgeList) ->
                judgeList.forEach { judgeName ->
                    this[i] = Pair(schoolName, judgeName)
                    i += 1
                }
            }
        }
        // 学校id to 学校名称
        val schoolMap = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadSchoolFromExcel()
        // 队伍抽签号 to 学校名称
        val teamIdMap = WorkbookFactory.create(R.CONFIG_EXCEL_FILE).loadTeamFromExcel().associate {
            it.id to schoolMap[it.schoolId]
        }

        //创建记录不同轮次不同会场的裁判列表 [[第一轮 1,2,3,4,... 会场的裁判],[第二轮 1,2,3,4...会场的裁判],...]
        val judgeTableAllTurns = mutableListOf<MutableList<MutableList<Int>>>()

        //创建总轮次裁判序号 to 已上场次数的字典，用于均衡全部轮次各裁判的上场次数
        val judgeUsedMap = judgeMap.keys.associateWith { 0 }.toMutableMap()

        //遍历不同的轮次
        for (turn in 0 until teamTableList.size) {
            logger.info("-----Round ${turn + 1}-----")

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
                    throw Exception("学校(${playerSchoolNameList})没有足够多的裁判，请补充后再试！")
                }

                for (judgeNumber in 0 until judgeCount) {
                    //若之前选过同学校的老师，则人为地将其下次被选中的概率降低
                    val selectedIndex = selectOne(availableJudgeList.mapNotNull { it ->
                        if (it.second.first !in judgeUsedList.map { judgeMap[it]!!.first }) judgeUsedMap[it.first] else judgeUsedMap[it.first]!! + 5
//
                    }
                    )

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

//        println(judgeTableAllTurns)
        logger.info("CounterPartTable WITH judge generated successfully!")
    }

    private fun selectOne(countList: List<Int>): Int {
        //输入权重列表，返回抽签得到的值的索引

        val probabilityList = MutableList<Double>(countList.size) { index ->
            //先用e指数作用
            exp(-countList[index].toDouble())
        }.apply {
            val sum = this.sum()
            //后归一化
            this.forEachIndexed { index, d ->
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


}

//一轮的参赛队伍对阵表
data class OneRoundTable(
    val roomCount: Int,//会场个数
    val totalTeamNumber: Int //总队伍数
) {

    private val fourPlayersNumber = totalTeamNumber.mod(roomCount) // 本次比赛中存在四个队伍的个数

    // 各个角色的编号列表，其索引号为会场号
    var RList: MutableList<Int>
    var OList: MutableList<Int>
    var VList: MutableList<Int>
    var OBList: MutableList<Int>

    init {
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