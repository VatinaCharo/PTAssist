package nju.pt.databaseassist

import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

object JsonInterface {
    fun toJson(teamDataList: TeamDataList,savePath :String){
        val logger = LoggerFactory.getLogger("Save json file to: $savePath")
        logger.info("===================== SavingJsonFile =====================")
        val format = Json { prettyPrint = true }
        File(savePath).writeText(format.encodeToString(teamDataList))
        logger.info("===================== JsonFileSavedSuccessfully =====================")

    }

    fun fromJson(readPath:String):TeamDataList{
        val logger = LoggerFactory.getLogger("Read json file from: $readPath")
        logger.info("===================== ReadingJsonFile =====================")
        try {
            val teamDataList =  Json.decodeFromString<TeamDataList>(File(readPath).readText())
            logger.info("===================== JsonFileReadSuccessfully =====================")
            return  teamDataList
        }catch (e: FileNotFoundException) {
            logger.error("未找到文件: ${e.message}")
            throw Exception("未找到文件: ${e.message}")
        }
    }
}