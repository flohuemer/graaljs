package com.oracle.truffle.js.builtins.tictactoe;

import java.util.Arrays;

public class SearchTree {
    private static final int STATE_PLAYER = 1;
    private static final int STATE_AI = -1;
    private static final int STATE_EMPTY = 0;

    private static int[][] getSuccessorStates(int[] board, int length, boolean isAIPlaying) {
        int player = isAIPlaying ? STATE_AI : STATE_PLAYER;
        int[][] successors = new int[length][9];
        int successorIndex = 0;
        for(int i = 0; i < board.length; i++) {
            if(board[i] == STATE_EMPTY) {
                int[] state = Arrays.copyOf(board, 9);
                state[i] = player;
                successors[successorIndex++] = state;
            }
        }
        return successors;
    }

    private static boolean checkLine(int[] board, int i1, int i2, int i3) {
        return Math.abs(board[i1] + board[i2] + board[i3]) == 3;
    }

    public static int getWinner(int[] board) {
        boolean firstRow = checkLine(board, 0, 1, 2);
        boolean secondRow = checkLine(board, 3, 4, 5);
        boolean thirdRow = checkLine(board, 6, 7, 8);

        boolean firstColumn = checkLine(board, 0, 3, 6);
        boolean secondColumn = checkLine(board, 1, 4, 7);
        boolean thirdColumn = checkLine(board, 2, 5, 8);

        boolean diagonal = checkLine(board, 0, 4, 8);
        boolean offDiagonal = checkLine(board, 2, 4, 6);

        if(firstRow || firstColumn) {
            return board[0];
        }
        if(secondRow || secondColumn || diagonal || offDiagonal) {
            return board[4];
        }
        if(thirdRow || thirdColumn) {
            return board[8];
        }
        //draw check
        for(int i = 0; i < board.length; i++) {
            if(board[i] == STATE_EMPTY) {
                return -2;
            }
        }
        return 0;
    }

    private static boolean hasPlayerWon(int[] board, int player) {
        return getWinner(board) == player;
    }

    public static int getNextAction(int[] board) {
        int length = 0;
        for(int i = 0; i < board.length; i++) {
            if(board[i] == STATE_EMPTY) {
                length++;
            }
        }
        if(length == 0) {
            return -1;
        }

        int action = -1;
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < board.length; i++) {
            if(board[i] == STATE_EMPTY) {
                board[i] = STATE_AI;
                int val = maxValue(board,length - 1);
                if(val < min) {
                    min = val;
                    action = i;
                }
                board[i] = STATE_EMPTY;
            }
        }
        return action;
    }

    private static int maxValue(int[] board, int length) {
        if(length == 0) {
            return getWinner(board);
        }
        int[][] successors = getSuccessorStates(board, length, false);
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < successors.length; i++) {
            if(hasPlayerWon(successors[i], STATE_PLAYER)) {
                return STATE_PLAYER;
            }
            max = Math.max(max, minValue(successors[i],length - 1));
        }
        return max;
    }

    private static int minValue(int[] board, int length) {
        if(length == 0) {
            return getWinner(board);
        }
        int[][] successors = getSuccessorStates(board, length, true);
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < successors.length; i++) {
            if(hasPlayerWon(successors[i], STATE_AI)) {
                return STATE_AI;
            }
            min = Math.min(min, maxValue(successors[i], length - 1));
        }
        return min;
    }
}
