const AI = -1;
const PLAYER = 1;
const EMPTY = 0;

let board = [0,0,0,0,0,0,0,0,0];

function game() {
    printBoard();
    let move = readline();
    board[move] = PLAYER;
    printBoard();
    let aiMove = TicTacToe.move(board);
    while(aiMove != -1) {
        board[aiMove] = AI;
        printBoard();
        move = readline();
        board[move] = PLAYER;
        printBoard();
        aiMove = TicTacToe.move(board);
    }
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