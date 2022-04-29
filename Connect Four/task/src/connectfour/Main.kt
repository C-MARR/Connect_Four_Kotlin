package connectfour

object ScoreBoard {
    var gameOver = false
    var firstPlayerTurn = true
    var winner = ' '
    var numberOfGames = 0
    var currentGame = 1
    var firstPlayerScore = 0
    var secondPlayerScore = 0
    var multipleGames = false
    var rows = 6
    var columns = 7
}

fun main() = gameStart()

fun gameStart() {
    println("""
        Connect Four
        First player's name:""".trimIndent().trimMargin())
    val firstPlayer = readLine()!!
    println("Second player's name:")
    val secondPlayer = readLine()!!
    val validInput = Regex(".?.x..?")
    outerLoop@ while (true) {
        println("""
        Set the board dimensions (Rows x Columns)
        Press Enter for default (6 x 7)""".trimIndent())
        val input = readLine()!!.filter{ !it.isWhitespace() } .lowercase()
        if (validInput.matches(input) || input == "") {
            try {
                if (input == "") break@outerLoop
                val rowsColumns = input.split("x").map { it.toInt() }
                when (rowsColumns[0]) {
                    !in 5..9 -> {
                        println("Board rows should be from 5 to 9")
                        continue@outerLoop
                    }
                    in 5..9 -> ScoreBoard.rows = 6
                    else -> {
                        println("Invalid input")
                        continue@outerLoop
                    }
                }
                when (rowsColumns[1]) {
                    !in 5..9 -> {
                        println("Board columns should be from 5 to 9")
                        continue@outerLoop
                    }
                    in 5..9 -> {
                        ScoreBoard.rows = rowsColumns[0]
                        ScoreBoard.columns = rowsColumns[1]
                        break
                    }
                    else -> {
                        println("Invalid input")
                        continue@outerLoop
                    }
                }
            } catch (e: Exception) {
                println("Invalid input")
                continue@outerLoop
            }
        } else {
            println("Invalid input")
            continue
        }
    }
    val board = createBoard(ScoreBoard.rows, ScoreBoard.columns)
    singleGameOrElse(ScoreBoard.rows, ScoreBoard.columns, firstPlayer, secondPlayer)
    drawBoard(ScoreBoard.rows, ScoreBoard.columns, board)
    gameMoves(ScoreBoard.rows, ScoreBoard.columns, firstPlayer, secondPlayer,createBoard(ScoreBoard.rows, ScoreBoard.columns))
}

fun createBoard(rows: Int, columns: Int): MutableList<MutableList<Char>> {
    val board = MutableList(columns * 2 + 1) { MutableList(rows) {' '} }
    for (i in rows - 1 downTo 0) {
        for (j in board.indices step 2) {
            board[j][i] = '║'
            board[columns * 2][i] = '║'
        }
    }
    return board
}

fun drawBoard(rows: Int, columns: Int, board: MutableList<MutableList<Char>>) {
    val cols = columns * 2
    for (col in 1 .. columns) {
        print(" $col")
    }
    println()
    for (row in rows - 1 downTo 0) {
        for (col in 0 .. cols) {
            print(board[col][row])
        }
        println()
    }
    print("╚═")
    for (col in 1 until columns) {
        print("╩═")
    }
    println("╝")
}

fun gameMoves(rows: Int, columns: Int, firstPlayer: String,
              secondPlayer: String, board: MutableList<MutableList<Char>>) {
    val verticalCheck = listOf(0,1,0,2,0,3)
    val horizontalCheck = listOf(2,0,4,0,6,0)
    val diagonalCheck = listOf(2,1,4,2,6,3)
    val reverseDiagonalCheck = listOf(2,-1,4,-2,6,-3)
    start@ while (!ScoreBoard.gameOver) {
        println(if (!ScoreBoard.firstPlayerTurn) "$secondPlayer's turn:" else "$firstPlayer's turn:")
        val text = readLine()!!
        if (text == "end") {
            println("Game Over!")
            ScoreBoard.gameOver = true
            continue
        }
        val input = text.toIntOrNull()
        when (input) {
            null -> {
                println("Incorrect column number")
                continue
            }
            !in 1..columns -> {
                println("The column number is out of range (1 - $columns)")
                continue
            }
            in 1..columns -> {
                if (board[input * 2 - 1][rows - 1] == ' ') {
                    for (row in 0 until rows) {
                        if (board[input * 2 - 1][row] != ' ') {
                            continue
                        } else {
                            board[input * 2 - 1][row] = (if (!ScoreBoard.firstPlayerTurn) '*' else 'o')
                            checkForWin(board,verticalCheck)
                            checkForWin(board,horizontalCheck)
                            checkForWin(board,diagonalCheck)
                            checkForWin(board,reverseDiagonalCheck)
                            drawBoard(rows, columns, board)
                            when (ScoreBoard.winner) {
                                'o' -> {
                                    println("Player $firstPlayer won")
                                    if (ScoreBoard.multipleGames) {
                                        postGame(firstPlayer, secondPlayer, createBoard(ScoreBoard.rows, ScoreBoard.columns))
                                        if (ScoreBoard.currentGame > ScoreBoard.numberOfGames) {
                                            ScoreBoard.gameOver = true
                                            break@start
                                        }
                                    } else {
                                        exit()
                                        return
                                    }
                                }
                                '*' -> {
                                    println("Player $secondPlayer won")
                                    if (ScoreBoard.multipleGames) {
                                        postGame(firstPlayer, secondPlayer, createBoard(ScoreBoard.rows, ScoreBoard.columns))
                                        if (ScoreBoard.currentGame > ScoreBoard.numberOfGames) {
                                            ScoreBoard.gameOver = true
                                            break@start
                                        }
                                    } else {
                                        exit()
                                        break@start
                                    }
                                }
                                'X' -> {
                                    println("It is a draw")
                                    if (ScoreBoard.multipleGames) {
                                        ScoreBoard.firstPlayerScore += 1
                                        ScoreBoard.secondPlayerScore += 1
                                        postGame(firstPlayer, secondPlayer, createBoard(ScoreBoard.rows, ScoreBoard.columns))
                                        if (ScoreBoard.currentGame > ScoreBoard.numberOfGames) {
                                            ScoreBoard.gameOver = true
                                            break@start
                                        }
                                    } else {
                                        exit()
                                        break@start
                                    }
                                }
                                ' ' -> {
                                    ScoreBoard.firstPlayerTurn = !ScoreBoard.firstPlayerTurn
                                    break
                                }
                            }
                        }
                    }
                } else {
                    println("Column $input is full")
                    continue
                }
            }
        }
    }
}

fun checkForWin(board: MutableList<MutableList<Char>>, check: List<Int>) {
    val drawList = mutableListOf<Char>()
    val rowSize = board.size / 2
    for (column in 1 until board.size step 2) {
        if (board[column][board[column].size - 1] != ' ') drawList.add(board[column][board[column].size - 1])
    }
    if (drawList.size >= rowSize) {
        ScoreBoard.winner = 'X'
        return
    }
    start@ for (column in 1 until board.size step 2) {
        for (row in board[column].indices) {
            if (board[column][row] == '*' || board[column][row] == 'o') {
                if (column + check[0] in 1 until board.size && row + check[1] in board[column].indices &&
                    board[column][row] == board[column + check[0]][row + check[1]]) {
                    if (column + check[2] in 1 until board.size && row + check[3] in board[column].indices &&
                        board[column][row] == board[column + check[2]][row + check[3]]) {
                        if (column + check[4] in 1 until board.size && row + check[5] in board[column].indices &&
                            board[column][row] == board[column + check[4]][row + check[5]]) {
                            if (board[column][row] == 'o') {
                                ScoreBoard.winner = 'o'
                                break@start
                            } else if (board[column][row] == '*') {
                                ScoreBoard.winner = '*'
                                break@start
                            }
                        }
                    }
                }
            }
        }
    }
}

fun singleGameOrElse(rows: Int, columns: Int, firstPlayer: String,
                     secondPlayer: String) {
    start@while (ScoreBoard.numberOfGames < 1) {
        println("""Do you want to play single or multiple games?
    For a single game, input 1 or press Enter
    Input a number of games:""".trimMargin())
        val input = readLine()!!
        if (input == "" || input == "1") {
            println(
                """
        $firstPlayer VS $secondPlayer
        $rows X $columns board
        Single Game""".trimIndent())
            return
        } else {
            val numberOfGames = input.toIntOrNull()
            if (numberOfGames == null || numberOfGames < 1) {
                println("Invalid input")
            } else {
                ScoreBoard.numberOfGames = numberOfGames
                println(
                    """
        $firstPlayer VS $secondPlayer
        $rows X $columns board""".trimIndent()
                )
                println("""Total $numberOfGames games
                    |Game #1
                """.trimMargin())

                ScoreBoard.multipleGames = true
                return
            }
        }
    }
}

fun postGame(firstPlayer: String, secondPlayer: String, board: MutableList<MutableList<Char>>) {
    if (ScoreBoard.firstPlayerTurn) {
        ScoreBoard.firstPlayerTurn = false
    } else if (!ScoreBoard.firstPlayerTurn) {
        ScoreBoard.firstPlayerTurn = true
    }
    when (ScoreBoard.winner) {
        'o' -> {
            ScoreBoard.firstPlayerScore += 2
        }
        '*' -> {
            ScoreBoard.secondPlayerScore += 2
        }
    }
    println("""Score
        |$firstPlayer: ${ScoreBoard.firstPlayerScore} $secondPlayer: ${ScoreBoard.secondPlayerScore}
    """.trimMargin())
    ScoreBoard.currentGame += 1
    if (ScoreBoard.currentGame > ScoreBoard.numberOfGames) {
        ScoreBoard.multipleGames = false
        exit()
        return
    } else {
        ScoreBoard.winner = ' '
        println("Game #${ScoreBoard.currentGame}")
        val newBoard = createBoard(ScoreBoard.rows, ScoreBoard.columns)
        drawBoard(ScoreBoard.rows, ScoreBoard.columns, newBoard)
        gameMoves(ScoreBoard.rows, ScoreBoard.columns, firstPlayer, secondPlayer, newBoard)
    }
    return
}

fun exit() {
    println("Game over!")
}