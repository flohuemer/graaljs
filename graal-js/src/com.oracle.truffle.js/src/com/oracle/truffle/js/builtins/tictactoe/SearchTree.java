package com.oracle.truffle.js.builtins.tictactoe;

import java.util.Arrays;

public class SearchTree {
    public static final int STATE_X = 0;
    public static final int STATE_O = 1;

    public static int[][] getSuccessorStates(int[] board, int length, int player) {
        if ((player >> 1) != 0) {
            return new int[0][0];
        }
        int[][] successors = new int[length][9];
        int successorIndex = 0;
        for(int i = 0; i < board.length; i++) {
            if(board[i] == -1) {
                int[] state = Arrays.copyOf(board,9);
                state[i] = player;
                successors[successorIndex++] = state;
            }
        }
        return successors;
    }

    public static int getWinner(int[] board) {
        boolean firstRow = board[0] == board[1] && board[1] == board[2];
        boolean secondRow = board[3] == board[4] && board[4] == board[5];
        boolean thirdRow = board[6] == board[7] && board[7] == board[8];

        boolean firstColumn = board[0] == board[3] && board[3] == board[6];
        boolean secondColumn = board[1] == board[4] && board[4] == board[7];
        boolean thirdColumn = board[2] == board[5] && board[5] == board[8];

        boolean diagonal = board[0] == board[4] && board[4] == board[8];
        boolean offDiagonal = board[2] == board[4] && board[4] == board[6];

        if(firstRow || firstColumn) {
            return board[0];
        }
        if(secondRow || secondColumn || diagonal || offDiagonal) {
            return board[4];
        }
        if(thirdRow || thirdColumn) {
            return board[8];
        }
        return -1;
    }
}
