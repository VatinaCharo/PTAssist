package nju.pt.server

import nju.pt.databaseassist.Data
import nju.pt.kotlin.ext.getTitleCellStyle
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import kotlin.io.path.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists


class ExportExcel(private val data: Data, saveDirPath: String) {

    init {
        val logger = LoggerFactory.getLogger("Judge Directory Path:$saveDirPath")
        logger.info("=====================Judging Directory Path =====================")
        if (Path(saveDirPath).isDirectory()) {
            logger.info("导出excel文件夹路径正确")
        } else {
            logger.error("导出excel文件夹路径错误！")
            throw Exception("导出excel文件夹路径错误！")
        }
        logger.info("data:$data")
    }



    //获知该数据中有多少轮，以此来命名输出文件名
    private val roundName =
        data.teamDataList.asSequence().map { it.recordDataList }.flatten().map { it.round }.toSortedSet().let { it ->
            var string = ""
            it.forEach {
                string += "$it&"
            }
            string.dropLast(1)
        }

    //导出文件路径
    val savePath = when (saveDirPath.endsWith("/")) {
        true -> saveDirPath + "第${roundName}轮成绩.xlsx"
        else -> saveDirPath + "/第${roundName}轮成绩.xlsx"
    }

    fun exportTeamScore() {
        val logger = LoggerFactory.getLogger("Export Team Score")
        logger.info("===================== ExportTeamScore =====================")
        //若文件不存在，则创建
        logger.info("Examining whether the file exists:")
        if (Path(savePath).notExists()) {
            logger.info("Not Exist, creating...")
            XSSFWorkbook().write(FileOutputStream(savePath))
            logger.info("New excel file created successfully!")
        } else {
            logger.info("File already exists, reading...")
        }


        val teamScoreWorkbook = WorkbookFactory.create(FileInputStream(savePath)).apply {
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            val titleStyle = this.getTitleCellStyle()
            try {
                this.removeSheetAt(this.getSheetIndex("队伍总得分"))
                logger.info("Exists, deleting and updating...")
            } catch (e: java.lang.Exception) {
                logger.info("Not exists, creating...")
            }

            createSheet("队伍总得分").apply {
                this.createRow(0).apply {
                    createCell(0).apply {
                        setCellValue("学校名")
                        cellStyle = titleStyle
                    }
                    createCell(1).apply {
                        setCellValue("队伍名")
                        cellStyle = titleStyle
                    }
                    createCell(2).apply {
                        setCellValue("总得分")
                        cellStyle = titleStyle
                    }

                }
                //由于有标题行，生成的行序号是index+1
                data.getTeamScore().forEachIndexed { index, data ->
                    logger.info("写入数据：${data}")
                    this.createRow(index + 1).apply {
                        createCell(0).setCellValue(data.first)
                        createCell(1).setCellValue(data.second)
                        createCell(2).setCellValue(data.third)

                    }
                }
            }

        }
        try {
            val fileOutputStream = FileOutputStream(savePath)
            teamScoreWorkbook.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export team score successfully to $savePath !")
        } catch (e: FileNotFoundException) {
            logger.error(e.message)
            throw Exception("文件 $savePath 正被另一个程序占用，无法访问，请关闭！")
        }
    }


    fun exportReviewTable() {
        val logger = LoggerFactory.getLogger("Export Review Table")
        logger.info("===================== ExportReviewTable =====================")
        //若文件不存在，则创建
        logger.info("Examining whether the file exists:")
        if (Path(savePath).notExists()) {
            logger.info("Not Exist, creating...")
            XSSFWorkbook().write(FileOutputStream(savePath))
            logger.info("New excel file created successfully!")
        } else {
            logger.info("File already exists, reading...")
        }


        val teamScoreWorkbook = WorkbookFactory.create(FileInputStream(savePath)).apply {
            val titleStyle = this.getTitleCellStyle()
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            try {
                this.removeSheetAt(this.getSheetIndex("回顾表"))
                logger.info("Exists, deleting and updating...")
            } catch (e: java.lang.Exception) {
                logger.info("Not exists, creating...")
            }
            //题号 ： 表中的列号
            val qIndexMap: MutableMap<Int, Int> = mutableMapOf()
            var qColumnIndex = 2
            createSheet("回顾表").apply {
                //标题行
                this.createRow(0).apply {
                    createCell(0).apply {
                        setCellValue("学校名")
                        cellStyle = titleStyle
                    }
                    createCell(1).apply {
                        setCellValue("队伍名")
                        cellStyle = titleStyle
                    }

                    data.questionMap.forEach { (qId, qName) ->
                        createCell(qColumnIndex).apply {
                            setCellValue("${qId}${qName}")
                            cellStyle = titleStyle
                        }
                        qIndexMap[qId] = qColumnIndex
                        qColumnIndex += 1
                    }

                }
                //由于有标题行，生成的行序号是index+1
                data.getReviewTable().forEachIndexed { index, triple ->
                    logger.info("写入数据：${triple}")
                    this.createRow(index + 1).apply {
                        createCell(0).setCellValue(triple.first)
                        createCell(1).setCellValue(triple.second)
                        triple.third.forEach { (qId, role) ->
                            qIndexMap[qId]?.let { createCell(it).setCellValue(role) }
                        }

                    }
                }
            }

        }
        try {
            val fileOutputStream = FileOutputStream(savePath)
            teamScoreWorkbook.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export review table successfully to $savePath !")
        } catch (e: FileNotFoundException) {
            logger.error(e.message)
            throw Exception("文件 $savePath 正被另一个程序占用，无法访问，请关闭！")
        }
    }


    fun exportPlayerScore() {
        val logger = LoggerFactory.getLogger("Export Player Score")
        logger.info("===================== ExportPlayerScore =====================")
        //若文件不存在，则创建
        logger.info("Examining whether the file exists:")
        if (Path(savePath).notExists()) {
            logger.info("Not Exist, creating...")
            XSSFWorkbook().write(FileOutputStream(savePath))
            logger.info("New excel file created successfully!")
        } else {
            logger.info("File already exists, reading...")
        }


        val playerScoreWorkbook = WorkbookFactory.create(FileInputStream(savePath)).apply {
            val titleStyle = this.getTitleCellStyle()
            //检查sheet是否存在
            logger.info("Examining whether the sheet exists:")
            try {
                this.removeSheetAt(this.getSheetIndex("个人得分"))
                logger.info("Exists, deleting and updating...")
            } catch (e: java.lang.Exception) {
                logger.info("Not exists, creating...")
            }


            createSheet("个人得分").apply {
                //标题行
                this.createRow(0).apply {
                    createCell(0).apply {
                        setCellValue("学校名")
                        cellStyle = titleStyle
                    }
                    createCell(1).apply {
                        setCellValue("队伍名")
                        cellStyle = titleStyle
                    }
                    createCell(2).apply {
                        setCellValue("队员名")
                        cellStyle = titleStyle
                    }
                    createCell(3).apply {
                        setCellValue("队员性别")
                        cellStyle = titleStyle
                    }
                    createCell(4).apply {
                        setCellValue("正方得分情况")
                        cellStyle = titleStyle
                    }
                    createCell(5).apply {
                        setCellValue("正方平均分")
                        cellStyle = titleStyle
                    }
                    createCell(6).apply {
                        setCellValue("反方得分情况")
                        cellStyle = titleStyle
                    }
                    createCell(7).apply {
                        setCellValue("反方平均分")
                        cellStyle = titleStyle
                    }
                    createCell(8).apply {
                        setCellValue("评方得分情况")
                        cellStyle = titleStyle
                    }
                    createCell(9).apply {
                        setCellValue("评方平均分")
                        cellStyle = titleStyle
                    }
                }
                var index = 1
                data.getPlayerScore().forEach { triple ->
                    logger.info("写入数据：${triple}")

                    triple.third.forEach { (name, dataList) ->
                        this.createRow(index).apply {
                            createCell(0).setCellValue(triple.first)
                            createCell(1).setCellValue(triple.second)
                            createCell(2).setCellValue(name)
                            createCell(3).setCellValue(dataList[0] as String)
                            for (i in 0..2) {
                                //得分情况
                                createCell(2*i + 4).setCellValue(dataList[i +1] as String)
                                //平均分
                                createCell(2*i + 5).setCellValue((dataList[i + 4] as Double))
                            }

                        }
                        index += 1
                    }
                }
            }

        }
        try {
            val fileOutputStream = FileOutputStream(savePath)
            playerScoreWorkbook.write(fileOutputStream)
            fileOutputStream.close()
            logger.info("Export player score successfully to $savePath !")
        } catch (e: FileNotFoundException) {
            logger.error(e.message)
            throw Exception("文件 $savePath 正被另一个程序占用，无法访问，请关闭！")
        }
    }


}

