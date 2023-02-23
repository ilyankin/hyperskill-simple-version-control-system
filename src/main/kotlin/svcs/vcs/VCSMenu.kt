package svcs.vcs

const val HELP_COMMAND = "--help"
const val CONFIG_COMMAND = "config"
const val ADD_COMMAND = "add"
const val LOG_COMMAND = "log"
const val COMMIT_COMMAND = "commit"
const val CHECKOUT_COMMAND = "checkout"

enum class CommandType(val value: String, val description: String) {
    HELP(HELP_COMMAND, ""),
    CONFIG(CONFIG_COMMAND, "Get and set a username."),
    ADD(ADD_COMMAND, "Add a file to the index."),
    LOG(LOG_COMMAND, "Show commit logs."),
    COMMIT(COMMIT_COMMAND, "Save changes."),
    CHECKOUT(CHECKOUT_COMMAND, "Restore a file."),
    NULL("", "")
    ;

    companion object {
        fun toCommandType(commandName: String): CommandType = when (commandName) {
            "" -> HELP
            else -> when (val c = CommandType.values().firstOrNull { it.value == commandName }) {
                null -> NULL
                else -> c
            }
        }
    }
}

interface Command<T> {
    fun command(): String
    fun description(): String
    fun execute(args: T): String
    fun noArguments(): String
    fun withArguments(args: T): String
}

sealed class VcsCommand<T>(val vcs: Vcs<Array<T>>, val commandType: CommandType) : Command<Array<T>> {
    override fun execute(args: Array<T>) = if (args.isEmpty()) noArguments() else withArguments(args)
}

class HelpVcsCommand(vcs: Vcs<Array<String>>) : VcsCommand<String>(vcs, CommandType.HELP) {
    private val commandTypes = CommandType.values()

    override fun command() = commandType.value

    override fun description() = commandType.description

    override fun noArguments(): String {
        val result = StringBuilder("These are SVCS commands:${System.lineSeparator()}")
        commandTypes
            .map { it.value to it.description }
            .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }
            .forEach {
                result.append("${String.format("%-11s", it.first)} ${it.second}${System.lineSeparator()}")
            }
        return result.toString()
    }

    override fun withArguments(args: Array<String>) = noArguments()
}

class ConfigVcsCommand(vcs: Vcs<Array<String>>) : VcsCommand<String>(vcs, CommandType.CONFIG) {
    override fun command() = commandType.value

    override fun description() = commandType.description

    override fun noArguments() = vcs.config(emptyArray())

    override fun withArguments(args: Array<String>) = applyUser(args.first())

    private fun applyUser(userName: String) = vcs.config(arrayOf(userName))
}

class AddVcsCommand(vcs: Vcs<Array<String>>) : VcsCommand<String>(vcs, CommandType.ADD) {
    override fun command() = commandType.value

    override fun description() = commandType.description

    override fun noArguments() = vcs.add(emptyArray())

    override fun withArguments(args: Array<String>) = vcs.add(args)
}

class LogVcsCommand(vcs: Vcs<Array<String>>) : VcsCommand<String>(vcs, CommandType.LOG) {
    override fun command() = commandType.value

    override fun description() = commandType.description

    override fun noArguments() = vcs.log(emptyArray())

    override fun withArguments(args: Array<String>) = vcs.log(args)
}

class CommitVcsCommand(vcs: Vcs<Array<String>>) : VcsCommand<String>(vcs, CommandType.COMMIT) {
    override fun command() = commandType.value

    override fun description() = commandType.description

    override fun noArguments() = vcs.commit(emptyArray())

    override fun withArguments(args: Array<String>) = vcs.commit(args)
}

class CheckoutVcsCommand(vcs: Vcs<Array<String>>) : VcsCommand<String>(vcs, CommandType.CHECKOUT) {
    override fun command() = commandType.value

    override fun description() = commandType.description

    override fun noArguments() = vcs.checkout(emptyArray())

    override fun withArguments(args: Array<String>) = vcs.checkout(args)
}


class VCSMenu(var vcs: Vcs<Array<String>>) {
    fun executeCommand(input: Array<String>): String {
        val command = if (input.isEmpty()) "" else input[0].trim()
        val args = if (input.size > 1) input.copyOfRange(1, input.size) else emptyArray()
        return executeCommand(
            when (CommandType.toCommandType(command)) {
                CommandType.HELP -> HelpVcsCommand(vcs)
                CommandType.CONFIG -> ConfigVcsCommand(vcs)
                CommandType.ADD -> AddVcsCommand(vcs)
                CommandType.LOG -> LogVcsCommand(vcs)
                CommandType.COMMIT -> CommitVcsCommand(vcs)
                CommandType.CHECKOUT -> CheckoutVcsCommand(vcs)
                CommandType.NULL -> throw IllegalArgumentException("'$command' is not a SVCS command.")
            }, args
        )
    }

    private fun executeCommand(command: Command<Array<String>>, args: Array<String>): String {
        return command.execute(args)
    }
}