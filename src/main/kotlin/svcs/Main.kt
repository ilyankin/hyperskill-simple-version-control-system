package svcs

import svcs.vcs.VCSMenu
import svcs.vcs.VcsSimulator

fun main(args: Array<String>) {
    val gitMenu = VCSMenu(VcsSimulator())
    try {
        println(gitMenu.executeCommand(args))
    } catch (e: Exception) {
        println(e.message)
    }
}