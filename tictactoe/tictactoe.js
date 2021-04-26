// AI, Player, and empty state on the board.
const AI = -1;
const PLAYER = 1;
const EMPTY = 0;

const DRAW = 0;

// The game is played via the num pad. This list represents a
// mapping from num pad numbers to board indices.
let numPadMapping = [10, 6, 7, 8, 3, 4, 5, 0, 1, 2];

let board = [0, 0, 0, 0, 0, 0, 0, 0, 0];
let player = PLAYER;

/**
 * Executes the game loop and determines the winner with the help of the
 * TicTacToe.check Graal.js builtin implemented in Java. 
 */
function game() {
    printBoard();
    do {
        if (player == AI) {
            aiInput();
            player = PLAYER;
        } else {
            playerInput();
            player = AI;
        }
        printBoard();
    } while (TicTacToe.check(board) == -2);
    let winner = TicTacToe.check(board);
    if (winner == AI) {
        print("AI wins.");
    }
    if (winner == DRAW) {
        print("Draw.")
    }
    //since AI plays optimal, player can not win.
}

/**
 * Asks the player for the next move and updates the board state.
 */
function playerInput() {
    print("Make your move:");
    let move = numPadMapping[readline()];
    while (board[move] != EMPTY) {
        print("Invalid move. Make your move:");
        move = numPadMapping[readline()];
    }
    board[move] = PLAYER;
}

/**
 * Uses the developed Graal.js builtin TicTacToe.move to get the next move from the AI.
 * The logic of the TicTacToe object is implemented in Java inside the Graal.js engine.
 */
function aiInput() {
    let move = TicTacToe.move(board);
    board[move] = AI;
}

/**
 * Print the board, representing the game state, onto the command line.
 */
function printBoard() {
    print("-------");
    for (let i = 0; i < 3; i++) {
        let str = "|";
        for (let j = 0; j < 3; j++) {
            if (board[i * 3 + j] == EMPTY) {
                str += " |";
            }
            if (board[i * 3 + j] == AI) {
                str += "o|";
            }
            if (board[i * 3 + j] == PLAYER) {
                str += "x|";
            }
        }
        print(str);
    }
    print("-------");
}

// Execute the main game loop.
game();