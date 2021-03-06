package student_player;

import pentago_twist.PentagoBoardState;
import pentago_twist.PentagoBoardState.Piece;
import pentago_twist.PentagoMove;

import java.util.*;

public class MyTools {
    public static final long FIRST_MOVE_TIME_LIMIT = 10000;
    public static final long MOVE_TIME_LIMIT = 1950;
    public static final double EXPLORATION_PARAMETER = Math.sqrt(2) / 7;

    private static final Random rand = new Random();


    /* ======== MCTS Node ======== */

    public static class Node {
        PentagoBoardState state;
        PentagoMove move;
        Node parent;
        List<Node> children;
        int visitCount;
        int winCount;

        public Node(PentagoMove move, PentagoBoardState state, Node parent) {
            this.state = state;
            this.move = move;

            this.parent = parent;
            this.children = new ArrayList<>();

            this.visitCount = 0;
            this.winCount = 0;
        }

        public Node() {
            this.state = null;
            this.move = null;

            this.parent = null;
            this.children = new ArrayList<>();

            this.visitCount = 0;
            this.winCount = 0;
        }

        // ----- Getters ----- //

        public Node getRandomChild(){
            if (children.isEmpty()) return null;
            return children.get(rand.nextInt(children.size()));
        }

        public Node getChildWithMaxScore() {
            return Collections.max(this.children, Comparator.comparing(c -> c.winCount));
        }

        public Node getChildWithState(PentagoBoardState state) {
            for (int i = 0; i < children.size(); i++) {
                if (equalsBoard(children.get(i).state, state)){
                    return children.get(i);
                }
            }

            return null;
        }
    }

    /* ======== MCTS Tree ======== */

    public static class Tree {
        Node root;

        public Tree(){
            this.root = new Node();
        }

        public void pruneTree(PentagoBoardState state) {
            this.root = root.getChildWithState(state);
            this.root.parent = null;
        }

        public void pruneTree(Node node) {
            this.root = node;
            this.root.parent = null;
        }
    }

    /* ======== Upper Confidence Tree Functions ======== */

    private static double uctValue(double totalVisit, double nodeWinScore, double nodeVisit) {
        if (nodeVisit == 0) {
            return Integer.MAX_VALUE;
        }
        return (nodeWinScore / nodeVisit)
                + EXPLORATION_PARAMETER * Math.sqrt(Math.log(totalVisit) / nodeVisit);
    }

    public static Node findBestNodeWithUCT(Node node) {
        int parentVisit = node.visitCount;
        return Collections.max(
                node.children,
                Comparator.comparing(c -> uctValue(parentVisit,
                        c.winCount, c.visitCount)));
    }

    /* ======== Helper Functions ======== */

    public static boolean equalsBoard(PentagoBoardState s1, PentagoBoardState s2) {
        if (s1 == s2) {
            return true;
        }

        Piece[][] s1Board = s1.getBoard();
        Piece[][] s2Board = s2.getBoard();
        for (int i = 0; i < s1Board.length; i++) {
            for (int j = 0; j < s1Board[0].length; j++) {
                if (s1Board[i][j] != s2Board[i][j])
                    return false;
            }
        }
        if (s1.getTurnPlayer() != s2.getTurnPlayer())
            return false;
        if (s1.getWinner() != s2.getWinner())
            return false;
        if (s1.getTurnNumber() != s2.getTurnNumber())
            return false;

        return true;
    }

}

