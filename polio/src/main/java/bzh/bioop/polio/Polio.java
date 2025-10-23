package bzh.bioop.polio;

import java.beans.PersistenceDelegate;
import java.util.Random;

/**
 * @author Vincent
 */
public class Polio {

    private Person[][] matrix;
    private int dim;
    private double pDeath;
    private double pSpread;

    public Polio(int citySize, double density, double deathProbability, double spreadProbability, double p_vax)
            throws Exception {
        // Create random map of dim citySize, with density, death probability when sick,
        // spread probability and vaccine coverage
        if (citySize < 10) {
            throw new Exception("city size must be >= 10");
        }
        Random rand = new Random();
        matrix = new Person[citySize][citySize];
        for (int i = 0; i < citySize; i++) {
            for (int j = 0; j < citySize; j++) {
                float x = rand.nextFloat();
                float y = rand.nextFloat();
                if (x < density) {
                    if (y < p_vax) {
                        matrix[i][j] = new Person(Person.State.HEALTHY, true, i, j);
                    } else {
                        matrix[i][j] = new Person(Person.State.HEALTHY, false, i, j);
                    }
                } else {
                    matrix[i][j] = null;
                }
            }
        }
        dim = citySize;
        pDeath = deathProbability;
        pSpread = spreadProbability;
    }

    public Polio(double density, double p_vax) throws Exception {
        // Call the generic constructor
        this(10, density, 0.25, 0.8, p_vax);
    }

    public Polio(double density) throws Exception {
        // Call the generic constructor
        // Vaccination rate is about 75% in the world
        this(10, density, 0.25, 0.8, 0.75);
    }

    public boolean isEndOfTheWorld() {
        // Return True if there is no one is alive
        int n = this.getDim();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Person p = this.matrix[i][j];
                if (p != null) {
                    if (p.getCurrentState() == Person.State.HEALTHY || p.getCurrentState() == Person.State.SICK
                            || p.getCurrentState() == Person.State.CURED) {
                        return false;
                    }

                }
            }
        }
        return true;
    }

    public boolean isOneSick() {
        // Return True if there is at least one person is sick
        int n = getDim();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Person p = this.matrix[i][j];
                if (p != null && p.getCurrentState() == Person.State.SICK) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSick(int i, int j) {
        // Return true if there is a person sick at the given position
        return (this.matrix[i][j] != null && this.matrix[i][j].getCurrentState() == Person.State.SICK);
    }

    public void infect(int i, int j) {
        // Set the person [i][j] sick
        Person p = this.matrix[i][j];
        if (p != null) {
            p.setCurrentState(Person.State.SICK);
            System.out.println("Infected at position : " + p.getPos_i() + " ; "+ p.getPos_j());
        }

    }

    public void infect() {
        // Set a random person sick, if he/she's vaccinated
        Random rand = new Random();
        int i = rand.nextInt(this.getDim());
        int j = rand.nextInt(this.getDim());
        infect(i, j);
    }

    private boolean isHealthy(int i, int j) {
        return (this.matrix[i][j] != null && this.matrix[i][j].getCurrentState() == Person.State.HEALTHY);
    }

    private boolean isCured(int i, int j) {
        return (this.matrix[i][j] != null && this.matrix[i][j].getCurrentState() == Person.State.CURED);
    }

    public boolean hasNeighborSick(int i, int j) {
        // At least one neighbor is sick
        int n = this.getDim();
        
        // up
        if (i - 1 >= 0 && isSick(i - 1, j))
            return true;
        // down
        if (i + 1 >= n - 1 && isSick(i + 1, j))
            return true;
        // left
        if (j - 1 >= 0 && isSick(i, j - 1))
            return true;
        // right
        if (j + 1 >= n - 1 && isSick(i, j + 1))
            return true;

        // Diagonal
        // up left
        if (i - 1 >= 0 && j - 1 >= 0 && isSick(i - 1, j - 1)) {
            return true;
        }
        // down left
        if (i + 1 >= n - 1 && j - 1 >= 0 && isSick(i + 1, j - 1))
            return true;
        // up right
        if (i - 1 >= 0 && j + 1 >= n - 1 && isSick(i - 1, j + 1))
            return true;
        // down right
        if (i + 1 >= n && j + 1 >= n - 1 && isSick(i + 1, j + 1))
            return true;

        return false;
    }

    private Person nextState(int i, int j) {
        // Compute the next state of the cell
        Person p = matrix[i][j];
        if (p != null) { // person
            if (this.hasNeighborSick(i, j) && !p.isVax() && p.getCurrentState() != Person.State.CURED) { 
                // Get sick if a neighbor is sick and the person is not vaccinated and not cured
                Person new_p = new Person(p);
                new_p.setCurrentState(Person.State.SICK);
                return new_p;
            } else if (p.getCurrentState() == Person.State.SICK) { // Get cured or die
                Random rand = new Random();
                double x = rand.nextDouble();
                if (x < this.getpDeath()) { // Die
                    Person new_p = new Person(p);
                    new_p.setCurrentState(Person.State.DEAD);
                } else { // Get cured
                    Person new_p = new Person(p);
                    new_p.setCurrentState(Person.State.CURED);
                } 
            }
        }
        return matrix[i][j];
    }

    private void propagatePolio1() {
        // Compute a whole new state of the matrix
        int n = this.getDim();
        Person[][] new_matrix = new Person[n][n];

        // Copy the current state to a new matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                new_matrix[i][j] = new Person(this.matrix[i][j]);
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

    public void propagatePolio(int n) {
        // Propagate the fire during n periods
        for (int i = 0; i < n; i++) {
            try {
                // System.out.println("Epoch "+i);
                propagatePolio1();
                // this.forestDisplay();
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void cityDisplay() {
        int n = this.getDim();
        String c; // The characters do display at each cell
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Person p = this.matrix[i][j];
                if (p == null) {
                    c = ".";
                    continue;
                }
                switch (p.getCurrentState()) {
                    // EMPTY, HEALTHY, SICK, CURED, DEAD
                    case Person.State.HEALTHY -> c = ":-)";
                    case Person.State.SICK -> c = ":-(";
                    case Person.State.CURED -> c = ":-|";
                    case Person.State.DEAD -> c = "X_X";
                    default -> throw new AssertionError();
                }
                System.out.print(c + "   ");
            }
            System.out.println("");
        }
    }

    public int getDim() {
        return this.dim;
    }

    public double getpDeath() {
        return pDeath;
    }

    public double getpSpread() {
        return pSpread;
    }
}
