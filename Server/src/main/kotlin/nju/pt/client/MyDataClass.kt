package nju.pt.client

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.FileInputStream

class PlayerData(
    var id: Int,
    var name:String,
    var gender : String,
    var teamId : Int
){

}


class TeamData(
    var id : Int,
    var name : String,
    var schoolId : Int
){

}



class RecordData(
    // 用于一条比赛记录
    var teamId: Int,
    var playerId : Int,
    var roomId :Int,
    var round :Int,
    var phase : Int,
    var questionId : Int,
    var role : Int,
    var score : Double,
    var rFactor : Double,
    var oFactor : Double,
    var vFactor : Double
){

}

//一些配置信息
object ConfigData{
    //用于Server文件交互的目录前缀
    const val serverDataPathPrefix = "serverData/"

    private val configDataList by lazy { loadConfigFromExcel() }

    val port by lazy{ configDataList[0] as String }
    val judgeCount by lazy{ configDataList[1] as Int }
    val roomCount by lazy{ configDataList[2] as Int }

    private fun loadConfigFromExcel():List<Any>{
        lateinit var port:String
        var judgeCount :Int = 0
        var roomCount: Int = 0
        //读取excel文件中的配置sheet
        val configSheet = WorkbookFactory.create(
            FileInputStream(serverDataPathPrefix+"serverdata.xlsx")).getSheet("服务端配置")
        // 读取每行信息
        configSheet.rowIterator().asSequence().forEach { row->
            val cellValues = row.cellIterator().asSequence().map{it.toString()}.toList()

            try {
                when(cellValues[0]){
                    "端口号" -> port = cellValues[1]
                    "每场比赛裁判个数" -> judgeCount = cellValues[1].substringBefore(".").toInt()
                    "会场总个数" -> roomCount = cellValues[1].substringBefore(".").toInt()
                    else -> println("无法识别，请检查服务端配置信息")
                }
            }catch (e:Exception){
                throw Exception("裁判数和会场数必须是大于零的整数！")
            }

        }

        if (judgeCount <= 0){
            throw Exception("裁判数必须是大于零的整数！")
        }

        if (roomCount <= 0){
            throw Exception("裁判数必须是大于零的整数！")
        }

        return listOf(port,judgeCount,roomCount)
    }



}


// 用于记录这次比赛的赛题信息
object QuestionData{
    val questionMap by lazy { loadQuestionFromExcel() }

    private fun loadQuestionFromExcel():Map<Int,String>{
        val qMap: MutableMap<Int,String> = mutableMapOf(0 to "0").apply { this.clear() }

        val questionSheet = WorkbookFactory.create(
            FileInputStream(ConfigData.serverDataPathPrefix +"serverdata.xlsx")).getSheet("赛题信息")

        questionSheet.rowIterator().asSequence().forEachIndexed {rowIndex, row->
            //跳过第一行标题行
            if (rowIndex!=0){
                val cellValues = row.cellIterator().asSequence().map{it.toString()}.toList()

                try {
                    qMap += ( cellValues[0].toString().substringBefore(".").toInt() to cellValues[1])
                }catch (e:Exception){
                    throw Exception("赛题信息填写有误！")
                }
            }
        }
        return qMap.toMap()
    }

}

//参赛学校信息
object SchoolData{
    val schoolMap by lazy { loadSchoolFromExcel() }

    private fun loadSchoolFromExcel():Map<Int,String>{
        val schMap :MutableMap<Int,String> = mutableMapOf(0 to "0").apply { this.clear() }

        val schoolSheet = WorkbookFactory.create(
            FileInputStream(ConfigData.serverDataPathPrefix +"serverdata.xlsx")).getSheet("学校信息")

        schoolSheet.rowIterator().asSequence().forEachIndexed {rowIndex, row->
            //跳过第一行标题行
            if (rowIndex!=0){
                val cellValues = row.cellIterator().asSequence().map{it.toString()}.toList()

                try {
                    schMap += ( cellValues[0].toString().substringBefore(".").toInt() to cellValues[1])
                }catch (e:Exception){
                    throw Exception("学校信息填写有误！")
                }
            }
        }
        return schMap.toMap()
    }
}


//object PlayerAndJudgerData{
//
//
//    val playerList: List<PlayerData>
//    val teamList: List<PlayerData>
//    val playerList: List<PlayerData>
//
//}
