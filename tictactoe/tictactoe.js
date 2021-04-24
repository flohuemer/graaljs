const AI = -1;
const PLAYER = 1;
const EMPTY = 0;

let numPadMapping = [10,6,7,8,3,4,5,0,1,2];
let board = [0,0,0,0,0,0,0,0,0];
let player = PLAYER;

function game() {
    printBoard();
    do {
        if(player == AI) {
            aiInput();
            player = PLAYER;
        } else {
            playerInput();
            player = AI;
        }
        printBoard();
    } while(TicTacToe.check(board) == -2);
    let winner = TicTacToe.check(board);
    if(winner == AI) {
        print("AI wins.");
    }
    //draw
    if(winner == 0) {
        print("Draw.")
    }
    //since AI plays optimal, player can not win.
}

function playerInput() {
    print("Make your move:");
    let move = numPadMapping[readline()];
    while(board[move] != EMPTY) {
        print("Invalid move. Make your move:");
        move = numPadMapping[readline()];
    }
    board[move] = PLAYER;
}

function aiInput() {
    let move = TicTacToe.move(board);
    board[move] = AI;
}

function printBoard() {
    print("-------");
    for(let i = 0; i < 3; i++) {
        let str = "|";
        for(let j = 0; j < 3; j++) {
            if(board[i * 3 + j] == EMPTY) {
                str += " |";
            }
            if(board[i * 3 + j] == AI) {
                str += "o|";
            }
            if(board[i * 3 + j] == PLAYER) {
                str += "x|";
            }
        }
        print(str);
    }
    print("-------");
}

game();