package bioop.fire;

import java.util.Random;

/**
 * @author Vincent
 */
public class Automata {

    private int[][] matrix;
    private int dim;

    public Automata(int n, double p) throws Exception {
        // Create random forest of dim n, with density p
        if (n < 3) {
            throw new Exception("n must be >= 10");
        }
        Random rand = new Random();
        matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                float x = rand.nextFloat();
                if (x < p) {
                    matrix[i][j] = 1;
                }
            }
        }
        dim = n;
    }

    public Automata(double p) throws Exception {
        // Call the generic constructor
        this(10, p);
    }

    public boolean isRazed() {
        // Return True if there is no tree in the forest
        int n = this.getDim();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.matrix[i][j] >= 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isOnFire() {
        // Return True if there is at least one tree on fire
        int n = getDim();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.matrix[i][j] == 5) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnFire(int i, int j) {
        // Return true if there is a tree on fire at the given position
        return (this.matrix[i][j] == 5);
    }

    public void putFire(int i, int j) {
        // Set the cell [i][j] on fire
        this.matrix[i][j] = 5;
    }

    public void putFire() {
        // Set a random cell on fire
        Random rand = new Random();
        int i = rand.nextInt(this.getDim());
        int j = rand.nextInt(this.getDim());
        this.matrix[i][j] = 5;
    }

    private boolean isTree(int i, int j) {
        return (this.matrix[i][j] == 1);
    }

    public boolean hasNeighborOnFire(int i, int j) {
        int[] di = { -1, 1, 0, 0 }; // vertical
        int[] dj = { 0, 0, -1, 1 }; // horizontal
        int n = this.getDim();

        for (int k = 0; k < 4; k++) {
            int ni = i + di[k];
            int nj = j + dj[k];

            if (ni >= 0 && ni < n && nj >= 0 && nj < n) {
                if (this.isOnFire(ni, nj)) {
                    return true;
                }
            }
        }
        // Diagonal
        int [] diagi = { -1, -1, 1, 1 };
        int [] diagj = { -1, 1, -1, 1};
        for (int k = 0; k < 4; k++) {
            int ni = i + diagi[k];
            int nj = j + diagj[k];

            if (ni >= 0 && ni < n && nj >= 0 && nj < n) {
                if (this.isOnFire(ni, nj)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int nextState(int i, int j) {
        // Compute the next state of the cell
        if (this.isTree(i, j)) { // tree
            if (this.hasNeighborOnFire(i, j)) {
                return 5;
            }
        } else if (this.isOnFire(i, j)) {
            return -1;
        }
        return matrix[i][j];
    }

    private  void propagateFire1() {
        // Compute a whole new state of the matrix
        int n = this.getDim();
        int [][] new_matrix = new int[n][n];

        // Copy the current state to a new matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                new_matrix[i][j] = this.matrix[i][j];
            }
        }

        // Compute the new state of each cell
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // next_state reads the current matrix
                new_matrix[i][j] = this.nextState(i, j);
            }
        }
        this.matrix = new_matrix;
    }

    public void propagateFire(int n) {
        // Propagate the fire during n periods
        for (int i = 0; i < n; i++) {
            try {
                //System.out.println("Epoch "+i);
                propagateFire1();
                //this.forestDisplay();
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    public void forestDisplay() {
        int n = this.getDim();
        char c; // The character do display at each cell
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                switch (this.matrix[i][j]) {
                    case 0 -> c = '.';
                    case 1 -> c = 'T';
                    case 5 -> c = 'O';
                    case -1 -> c = '_';
                    default -> throw new AssertionError();
                }
                System.out.print(c + " ");
            }
            System.out.println("");
        }
    }

    public int getDim() {
        return this.dim;
    }
}
