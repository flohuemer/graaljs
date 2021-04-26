# Polyglot - TicTacToe

This branch of the Graal.js project represents a polyglot TicTacToe implementation. While the game logic and command line interface is written in JavaScript, the TicTacToe AI is implemented as Graal.js builtin in Java. 

## JavaScript

The JavaScript part of the implementation can be found in the `tictactoe.js` file inside the `tictactoe` directory of this project. Here, the game loop and the input and output of the game is implemented. 

## Graal.js builtin

The Java part of the implementation can be found in the `com.oracle.truffle.js.builtins.tictactoe` and `com.oracle.truffle.js.runtime.builtins.tictactoe` packages. Here, the logic for the AI (minimax algorithm) and for determining the winner of the game is implemented and provided to JS via a Graal.js builtin.

The TicTacToe logic is implemented in the `SearchTree` class. The implemented nodes are the `TicTacToeNode`, `CheckNode` and `MoveNode`. The `TicTacToeNode` represents the base class which implements the conversion from a JavaScript list to a Java int array with the help of iterator nodes. The `MoveNode` determines the next move of the AI with the help of the `SearchTree` class and represents the implementation of the JavaScript builtin `TicTacToe.move(list)`. The `CheckNode` determines the winner of a game, based on a given game state, and represents the JavaScript builtin `TicTacToe.check(list)`.

## Executing this project

The project can be executed by building this branch with `mx build` and executing `mx js ../tictactoe/tictactoe.js` inside the `graal-js` subdirectory. The tictactoe fields correspond to the numbers on the num pad.