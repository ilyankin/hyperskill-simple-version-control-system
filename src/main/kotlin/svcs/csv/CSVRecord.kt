package svcs.csv

import java.time.LocalDateTime

interface CSVRecord

data class IndexCSVRecord(
    val path: String,
    val hash: String,
    val indexDate: LocalDateTime,
    val updateDate: LocalDateTime,
    val new: Boolean = true
) : CSVRecord


data class LogCSVRecord(
    val hashValue: String,
    val message: String,
    val authorName: String,
    val commitDate: LocalDateTime
) : CSVRecord
