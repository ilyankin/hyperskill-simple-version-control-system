# Version Control System

## Introduction

This project is a primitive implementation of version control system on Kotlin.
The [task](https://hyperskill.org/projects/177) was taken from the JetBrains Academy educational platform (Hyperskill).

## Description

A **version control system** is software that can keep track of the changes that were implemented to a program.
A version control system remembers **who** changed the file, when it was done, and **why**.
It allows you to **roll back** to the previous versions as well.

List of supported commands:

1. `config` sets or outputs the name of a commit author;
2. `--help` prints the help page;
3. `add` adds a file to the list of tracked files or outputs this list;
4. `log` shows all commits;
5. `commit` saves file changes and the author name;
6. `checkout` allows you to switch between commits and restore a previous file state.

## Features

### 1. `config` command

- Take one argument as arguments of command.
- If an argument is missing, print `Please, tell me who you are.` if a username is not set,
  or `The username is [user name]`.
- If a user wants to set a new name, the old will be overwritten by its.

**Example 1:** the `config` argument

`Please, tell me who you are.`

**Example 2:** the `config John` argument

`The username is John.`
**Example 3:** the `config` argument

`The username is John.`

**Example 4:** the `config Max` argument

`The username is Max.`

### 2. `--help` command

- Take one argument as a command.
- If an argument is missing, or it is `--help`, print the entire help page.
- If a command exists, the program should output a description of the command.
- If the command is wrong, print '[passed argument]' is not a SVCS command.

**Example 1:** the `--help` argument

```
These are SVCS commands:
config     Get and set a username.
add        Add a file to the index.
log        Show commit logs.
commit     Save changes.
checkout   Restore a file.
```

**Example 2:** no arguments

```
These are SVCS commands:
config     Get and set a username.
add        Add a file to the index.
log        Show commit logs.
commit     Save changes.
checkout   Restore a file.
```

**Example 3:** the config argument

`Get and set a username.`

**Example 4:** the wrong argument

`'wrong' is not a SVCS command.`

### 3. `add` command

- Take one or more arguments as arguments of command. (Arguments are the path to a file or folder)
- If an argument is missing and no tracked files, print `Add a file to the index.`,
  or if there are such files, print all list of tracked files.
- If an argument of command exists, add files or only one file into `./vcs/index.csv` and
  print `The file '[file path]' is tracked.`
- If an argument of command is wrong, print `Can't find '[file path]'`.

**Example 1:** the `add` argument.

`Add a file to the index.`

**Example 2:** the `add file.txt` arguments

`The file 'file.txt' is tracked.`

**Example 3:** the `add` argument

```
Tracked files:
file.txt
```

**Example 4:** the` add new_file.txt` argument

`The file 'new_file.txt' is tracked.`

**Example 5:** the `add` argument

```
Tracked files:
file.txt
new_file.txt
```

**Example 6:** the `add not_exists_file.txt` argument

`Can't find 'not_exists_file.txt'.`

### 4. `log` command

- Take one argument as a command.
- If there aren't any commits in `./vcs/log.csv`, print `No commits yet.`, then show all the commits in reverse order.

**Example 1:** the log argument

`No commits yet.`

**Example 2:** the `log` argument

```
commit 2853da19f31cfc086cd5c40915253cb28d5eb01c
Author: Ilya
Changed several lines of code in the file2.txt

commit 0b4f05fcd3e1dcc47f58fed4bb189196f99da89a
Author: Ilya
Added several lines of code to the file1.txt
```

### 4. `commit` command

- Take one argument as arguments of command. (Argument is a commit message).
- If an argument is missing, print `Message was not passed.`.
- If there are argument and changes to commit, add a commit record into `./vcs/log.csv` and create a snapshot of
  the current state of all tracked files into `./vcs/commits/[commit hash]`.
- If an argument and there aren't any changes to commit, print `No commits yet.`.

**Example 1:** the `log` argument

`No commits yet.`

**Example 2:** the `commit "Added several lines of code to the file1.txt"` argument

`Changes are committed.`

**Example 3:** the `commit "Files were not changed"` argument

`Nothing to commit.`

**Example 4:** the `commit` argument

`Message was not passed.`

### 5. `checkout` command

- Take one argument as arguments of command. (Argument is a commit hash).
- If an argument is missing, print `Commit id was not passed.`.
- If such a commit id does not exist, print `Commit id was not passed.`.
- If there's an argument and such commit id exists the contents of the tracked file should be restored in accordance
  with this commit and print `Switched to commit [commit id].`.

**Example 1:** the `checkout 0b4f05fcd3e1dcc47f58fed4bb189196f99da89a` argument

`Switched to commit 0b4f05fcd3e1dcc47f58fed4bb189196f99da89a.`

**Example 2:** the `checkout fb92cc1be7f60c8d9acf74cbd4a67841d8d2e844` argument

`Commit does not exist.`

**Example 3:** the `checkout` argument

`Commit id was not passed.`