package student_player.swap.min_max;

import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoPlayer;
import student_player.swap.PentagoSimpleHeuristics;;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("SWAP_AB");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState pentagoBoardState) {

        final boolean DEBUG = false;
        PentagoSimpleHeuristics simpleHeuristics = new PentagoSimpleHeuristics();
        Move winningMove = simpleHeuristics.getNextMove(pentagoBoardState);

        long start = System.currentTimeMillis();
        if (winningMove != null) {
            //noinspection ConstantConditions
            if (DEBUG) {
                System.out.println(String.format("Time for Move (s): %f", (System.currentTimeMillis() - start) / 1000f));
                pentagoBoardState.printBoard();
            }
            return winningMove;
        }

        final int DEPTH = 8;
        ABPruningOptimizer optimizer = new ABPruningOptimizer();
        Move myMove = optimizer.getNextBestMove(DEPTH, pentagoBoardState, pentagoBoardState.getTurnPlayer());
        float timeElapsed = (System.currentTimeMillis() - start) / 1000f;

        if (DEBUG) {
            System.out.println(myMove.toPrettyString());
            pentagoBoardState.printBoard();
            System.out.println(String.format("Time for Move (s): %f", timeElapsed));
        }

        return myMove;
    }
}
