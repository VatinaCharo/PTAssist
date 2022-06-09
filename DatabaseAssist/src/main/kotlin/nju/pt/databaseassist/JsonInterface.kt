package nju.pt.databaseassist

import org.slf4j.LoggerFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object JsonInterface {
    fun toJson(teamDataList: TeamDataList,savePath :String){
        val logger = LoggerFactory.getLogger("Save json file to: $savePath")
        logger.info("===================== SavingJsonFile =====================")
        val format = Json { prettyPrint = true }
        File(savePath).writeText(format.encodeToString(teamDataList))
        logger.info("===================== JsonFileSavedSuccessfully =====================")

    }
}