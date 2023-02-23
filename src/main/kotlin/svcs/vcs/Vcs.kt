package svcs.vcs

import svcs.ShaCalculator
import svcs.csv.*
import svcs.vcs.VCSConstants.COMMITS_FOLDER_NAME
import svcs.vcs.VCSConstants.CONFIG_FILE_NAME
import svcs.vcs.VCSConstants.INDEX_FILE_NAME
import svcs.vcs.VCSConstants.LOG_FILE_NAME
import svcs.vcs.VCSConstants.VCS_FOLDER_NAME
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime

interface Vcs<T> {
    fun config(args: T): String
    fun add(args: T): String
    fun log(args: T): String
    fun commit(args: T): String
    fun checkout(args: T): String
}

val properties = mapOf(
    "username" to "user.name"
)

class VcsSimulator : Vcs<Array<String>> {

    // Folder paths
    private val vcsPath = Path.of(VCS_FOLDER_NAME)
    private val commitsPath = Path.of(VCS_FOLDER_NAME, COMMITS_FOLDER_NAME)

    // File paths
    private val configPath = Path.of(VCS_FOLDER_NAME, CONFIG_FILE_NAME)
    private val indexPath = Path.of(VCS_FOLDER_NAME, INDEX_FILE_NAME)
    private val logPath = Path.of(VCS_FOLDER_NAME, LOG_FILE_NAME)


    private val indexCSVReadWrite: CSVReadWrite<IndexCSVRecord>
    private val logCSVReadWrite: CSVReadWrite<LogCSVRecord>

    init {
        arrayOf(vcsPath, configPath, indexPath, commitsPath, logPath).map { it.toFile() }.forEach {
            if (it.extension.isEmpty()) it.mkdir() else it.createNewFile()
        }
        indexCSVReadWrite = IndexCSVReadWrite(indexPath)
        logCSVReadWrite = LogCSVReadWrite(logPath)
    }


    private val userNameProperty = properties["username"]!!

    override fun config(args: Array<String>): String {
        if (args.isEmpty()) {
            val userName = extractConfigValue(userNameProperty)
            if (userName.isNotEmpty()) {
                return "The username is $userName."
            }
        } else {
            val userName = args.first()
            setUserNameProperty(configPath, args.first())
            return "The username is $userName."
        }
        return "Please, tell me who you are."
    }

    private fun extractConfigValue(propertyName: String, defaultValue: String = ""): String {
        val lines = configPath.toFile().readLines()
        val userNameIndex = lines.indexOfFirst { it.contains("$propertyName=") }

        return if (userNameIndex == -1 || lines[userNameIndex].trim() == "$propertyName=") {
            defaultValue
        } else {
            val userNameProperty = lines[userNameIndex]
            userNameProperty.substring(userNameProperty.lastIndexOf("=") + 1)
        }
    }

    private fun setUserNameProperty(configPath: Path, userName: String) {
        val lines = configPath.toFile().readLines().toMutableList()
        val userNameIndex = lines.indexOfFirst { it.contains(userNameProperty) }

        val userNameRecord = "$userNameProperty=$userName"
        if (userNameIndex == -1) {
            lines += userNameRecord
        } else {
            lines[userNameIndex] = userNameRecord
        }

        configPath.toFile().writeText(lines.joinToString(separator = System.lineSeparator()))
    }

    override fun add(args: Array<String>): String {
        return if (args.isNotEmpty()) {
            addToIndex(args.map { Path.of(it) }.toTypedArray())
        } else {
            readIndexFile()
        }
    }

    private fun addToIndex(paths: Array<Path>): String {
        val (indexRecords, message) = getUpdatedIndexFileRecords(paths, indexCSVReadWrite.read().toMutableList())
        indexCSVReadWrite.write(indexRecords)
        return message
    }

    private fun getUpdatedIndexFileRecords(
        args: Array<Path>, indexRecords: MutableList<IndexCSVRecord>
    ): Pair<List<IndexCSVRecord>, String> {
        val message = StringBuilder()
        args.map(Path::toFile)
            .filter { message.appendNotFoundFileMessage(it, File::exists) }
            .forEach { file -> message.append(indexRecords.addIndexRecords(file, file.isDirectory)) }
        return indexRecords to message.toString()
    }

    private fun StringBuilder.appendNotFoundFileMessage(file: File, exist: (File) -> Boolean): Boolean {
        if (!exist(file)) {
            this.append("Can't find '${file.path}'.${System.lineSeparator()}")
            return false
        }
        return true
    }

    private fun MutableList<IndexCSVRecord>.addIndexRecords(file: File, isDirectory: Boolean): String {
        val stringBuilder = StringBuilder()
        if (isDirectory) {
            file.walkTopDown()
                .filter { it.isFile }
                .forEach { stringBuilder.append(it.getFileTrackStatus(addIndexRecord(it))) }
        } else {
            stringBuilder.append(file.getFileTrackStatus(addIndexRecord(file)))
        }
        return stringBuilder.toString()
    }

    private fun MutableList<IndexCSVRecord>.addIndexRecord(file: File): Boolean {
        return if (this.any { record -> record.path == file.path }) {
            true
        } else {
            this += IndexCSVRecord(
                file.path,
                ShaCalculator.hashFile(file),
                LocalDateTime.now(),
                LocalDateTime.now()
            )
            false
        }
    }

    private fun File.getFileTrackStatus(alreadyTracked: Boolean) =
        if (alreadyTracked)
            "The file '${this.path}' is already tracked.${System.lineSeparator()}"
        else "The file '${this.path}' is tracked.${System.lineSeparator()}"

    private fun readIndexFile(): String {
        val indexRecords = indexCSVReadWrite.read()
        val stringBuilder = StringBuilder()
        if (indexRecords.isEmpty()) {
            stringBuilder.append("Add a file to the index.${System.lineSeparator()}")
        } else {
            stringBuilder.append("Tracked files:${System.lineSeparator()}")
            stringBuilder.append(indexRecords.joinToString(separator = System.lineSeparator()) {
                it.path
            })
        }
        return stringBuilder.toString()
    }

    override fun log(args: Array<String>): String {
        return if (args.isNotEmpty()) {
            "The log command must not contain any arguments"
        } else {
            getLogMessage()
        }
    }

    private fun getLogMessage(): String {
        val commitRecords = logCSVReadWrite.read()

        if (commitRecords.isEmpty()) return "No commits yet."

        val result = StringBuilder()
        commitRecords
            .reversed()
            .forEach {
                result.append(
                    """
                commit ${it.hashValue}
                Author: ${it.authorName}
                ${it.message}
                
                """.trimIndent()
                )
            }

        return result.toString()
    }

    override fun commit(args: Array<String>): String {
        if (args.isNotEmpty()) {
            return commit(args.first())
        }
        return "Message was not passed."
    }

    private fun commit(commitMessage: String): String {
        val userName = extractConfigValue(userNameProperty)
        if (userName.isBlank()) {
            return "Please, tell me who you are."
        }

        val indexRecords = indexCSVReadWrite.read().toMutableList()
        val indexFiles = indexRecords.map { Path.of(it.path).toFile() }
        val editedFiles = mutableListOf<File>()

        val hashCommitBytes = mutableListOf<ByteArray>()
        indexFiles.forEachIndexed { index, file ->
            if (!file.exists()) {
                indexRecords.removeAt(index)
                editedFiles.add(file)
                return@forEachIndexed
            }
            val indexRecord = indexRecords[index]
            if (file.edited(indexRecord)) {
                editedFiles.add(file)
                indexRecords[index] =
                    IndexCSVRecord(
                        file.path,
                        ShaCalculator.hashFile(file),
                        indexRecord.indexDate,
                        LocalDateTime.now(),
                        false
                    )
            }
            hashCommitBytes += file.readBytes()
        }

        if (editedFiles.isEmpty()) return "Nothing to commit."

        indexCSVReadWrite.write(indexRecords)

        hashCommitBytes += commitMessage.toByteArray()
        val commitHash = ShaCalculator.hashArrayBytes(hashCommitBytes.toTypedArray())

        performCommit(commitHash, indexFiles)

        updateLogFile(LogCSVRecord(commitHash, commitMessage, userName, LocalDateTime.now()))
        return "Changes are committed."
    }

    private fun performCommit(commitHash: String, indexFiles: List<File>) {
        val snapshotPath = commitsPath.resolve(commitHash)
        snapshotPath.toFile().mkdir()
        indexFiles.forEach { it.copyTo(snapshotPath.resolve(it.path).toFile()) }
    }

    private fun updateLogFile(record: LogCSVRecord) {
        val logRecords = logCSVReadWrite.read().toMutableList()
        logRecords += record
        logCSVReadWrite.write(logRecords)
    }

    private fun File.edited(record: IndexCSVRecord) = record.new || ShaCalculator.hashFile(this) != record.hash

    override fun checkout(args: Array<String>): String {
        if (args.isNotEmpty()) {
            return checkout(args.first())
        }
        return "Commit id was not passed."
    }

    private fun checkout(commitId: String): String {
        val snapshotFolder = commitsPath.toFile()
            .walkTopDown()
            .filter { it.isDirectory }
            .firstOrNull { it.name == commitId } ?: return "Commit does not exist."

        val files = snapshotFolder.listFiles() ?: return "Commit doesn't have any committed files."

        files.forEach {
            it.copyTo(Path.of(".", it.path.substringAfter(commitId)).toFile(), true)
        }

        recalculateHashForIndexFiles()

        return "Switched to commit $commitId."
    }

    private fun recalculateHashForIndexFiles() {
        val indexUpdatedRecords = indexCSVReadWrite.read().map {
            IndexCSVRecord(
                it.path,
                ShaCalculator.hashFile(Path.of(it.path).toFile()),
                it.indexDate,
                it.updateDate,
                it.new
            )
        }
        indexCSVReadWrite.write(indexUpdatedRecords)
    }
}