package nju.pt.databaseassist

import kotlinx.serialization.decodeFromString
import org.slf4j.LoggerFactory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException

object JsonHelper {
    inline fun <reified T> toJson(data: T, savePath: String) {
        val logger = LoggerFactory.getLogger("JSON Saver")
        logger.info("===================== SavingJsonFile =====================")
        val path = if (savePath.lowercase().startsWith("file:")) savePath.substringAfter(":") else savePath
        logger.info("save json file to: ${File(path).absolutePath}")
        val format = Json { prettyPrint = true }
        File(path).writeText(format.encodeToString(data))
        logger.info("JsonFileSavedSuccessfully!")

    }

    inline fun <reified T> fromJson(readPath: String): T {
        val logger = LoggerFactory.getLogger("JSON Reader")
        logger.info("===================== ReadingJsonFile =====================")
        logger.info("read json form ${File(readPath).absolutePath}")
        try {
            val path = if (readPath.lowercase().startsWith("file:")) readPath.substringAfter(":") else readPath

            val data = Json.decodeFromString<T>(File(path).readText())
            logger.info("JsonFileReadSuccessfully!")
            return data
        } catch (e: FileNotFoundException) {
            logger.error("未找到文件${readPath}: ${e.message}")
            throw FileNotFoundException("未找到文件${readPath}: ${e.message}")
        }
    }
}