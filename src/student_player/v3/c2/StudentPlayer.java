package student_player.v3.c2;

import boardgame.Board;
import boardgame.Move;
import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoMove;
import pentago_twist.PentagoPlayer;
import student_player.v3.c2.MyTools.Node;
import student_player.v3.c2.MyTools.Tree;

import java.util.ArrayList;

import static student_player.v3.c2.MyTools.TIME_LIMIT;
import static student_player.v3.c2.MyTools.findBestNodeWithUCT;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    static int curPlayer;
    static int opponent;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("C0055-v3");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
        // Find move
        Move myMove = chooseMoveMCTS(boardState);

        // Return your move to be processed by the server.
        return myMove;
    }

    /* ======== Monte Carlo Search Tree Implementation ======== */

    public static Move chooseMoveMCTS(PentagoBoardState boardState) {
        // define the time when search is terminated
        long endTime = System.currentTimeMillis() + TIME_LIMIT;

        // set curPlayer and opponent
        curPlayer = boardState.getTurnPlayer();
        opponent = (curPlayer == PentagoBoardState.WHITE) ? PentagoBoardState.BLACK: PentagoBoardState.WHITE;

        // create new MCTS tree
        Tree tree = new Tree();
        tree.root.state = boardState;

        while (System.currentTimeMillis() < endTime) {
            /* SELECTION : get most promising Node using UCT policy */
            Node promisingNode = selectPromisingNode(tree.root);

            /* EXPANSION : create the children of the selected node */
            if (!promisingNode.state.gameOver()) {
                expandNode(promisingNode);
            }

            /* SIMULATION : select child and simulate to terminal node */
            Node nodeToExplore = promisingNode;
            if (!promisingNode.childArray.isEmpty()) {
                nodeToExplore = promisingNode.getRandomChild();
            }
            int playoutResult = simulateRandomPlayout(nodeToExplore);

            /* BACK PROPAGATION : update the win and visit counts for nodes
                                 on the path to the current node */
            backPropogation(nodeToExplore, playoutResult);
        }

        // Get child with max score and update the tree
        Node selectedNode = tree.root.getChildWithMaxScore();
        tree.root = selectedNode;
        return selectedNode.move;
    }

    private static Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (!node.childArray.isEmpty()) {
            node = findBestNodeWithUCT(node);
        }
        return node;
    }

    private static void expandNode(Node node) {
        ArrayList<PentagoMove> legalMoves = node.state.getAllLegalMoves();

        for (int i = 0; i < legalMoves.size(); i++) {
            PentagoBoardState childState = (PentagoBoardState) node.state.clone();
            childState.processMove(legalMoves.get(i));

            Node child = new Node(legalMoves.get(i), childState, node);
            node.childArray.add(child);
        }
    }

    private static void backPropogation(Node leafNode, int winner) {
        Node tmp = leafNode;

        while (tmp != null) {
            tmp.visitCount++;
            if (winner == curPlayer) {
                tmp.winCount++;
            }
            tmp = tmp.parent;
        }
    }

    private static int simulateRandomPlayout(Node node) {
        PentagoBoardState tmpState = (PentagoBoardState) node.state.clone();
        PentagoMove tmpMove;

        // TODO remove
        // check if game is over and opponent won
        if (opponent == tmpState.getWinner()) {
            node.parent.winCount = Integer.MIN_VALUE;
            return opponent;
        }
        // if game is not over simulate to the end by selecting random moves
        while (tmpState.getWinner() == Board.NOBODY) {
            tmpMove = (PentagoMove) tmpState.getRandomMove();
            tmpState.processMove(tmpMove);
        }
        return tmpState.getWinner();
    }


}