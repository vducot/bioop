package bzh.bioop.polio;

import java.util.Random;

/**
 * Polio epidemic simulation in a city represented by a matrix of Person.
 * 
 * Possible states : HEALTHY, SICK, CURED, DEAD
 * Vaccinated persons have reduced probability of infection. Cured persons
 * cannot be infected again.
 * 
 * @author Vincent & Gwendoline
 */
public class Polio {

    private Person[][] matrix;
    private int dim;
    private double pDeath; // death probability when sick
    private double pSpread; // spread probability
    private double pVaxPolio; // probability a vaccinated person still catches polio
    private double pMove;

    public Polio(int citySize, double density, double deathProbability, double spreadProbability,
            double p_vax, double vaxPolioProb, double moveProbability, boolean clusters)
            throws Exception {
        // Create random map of dim citySize, with density, death probability when sick,
        // spread probability and vaccine coverage
        if (citySize < 10) {
            throw new Exception("city size must be >= 10");
        }
        if (clusters) {
            matrix = initMatrixWithClusters(citySize, density, p_vax);
        } else {
            matrix = initMatrixWithoutClusters(citySize, density, p_vax);
        }
        dim = citySize;
        pDeath = deathProbability;
        pSpread = spreadProbability;
        pVaxPolio = vaxPolioProb;
        pMove = moveProbability;
    }

    public Polio(double density, double p_vax) throws Exception {
        // Call the generic constructor
        this(10, density, 0.25, 0.8, p_vax, 0.05, 0, false);
    }

    public Polio(double density) throws Exception {
        // Call the generic constructor
        // Vaccination rate is about 75% in the world
        this(10, density, 0.65, 0.8, 0.15, 0.05, 0, false);
    }

    private Person[][] initMatrixWithClusters(int citySize, double density, double p_vax) {
        Random rand = new Random();
        Person[][] l_matrix = new Person[citySize][citySize];
        int totalPop = (int) Math.round(citySize * citySize * density);
        // int clusterSize = citySize / 5; // Arbitrary
        // int cluter_nb = totalPop / clusterSize;

        // First, place a few people randomly in the city
        int randomPeople = (int) Math.round(totalPop * 0.1);

        for (int p = 0; p < randomPeople; p++) {
            int x = rand.nextInt(citySize);
            int y = rand.nextInt(citySize);
            if (l_matrix[x][y] == null) {
                float v = rand.nextFloat();
                if (v < p_vax) {
                    l_matrix[x][y] = new Person(Person.State.HEALTHY, true, x, y);
                } else {
                    l_matrix[x][y] = new Person(Person.State.HEALTHY, false, x, y);
                }
            }
        }

        // Then, iterate on the map and try to form clusters
        for (int i = 0; i < citySize; i++) {
            for (int j = 0; j < citySize; j++) {
                float d = rand.nextFloat();
                if (hasNeighbor(i, j)) { // Increase the probability to place a person
                    d = d / 10;
                }
                float v = rand.nextFloat();
                if (d < (density - 0.1)) { // We have already placed 10% of the population
                    if (v < p_vax) {
                        l_matrix[i][j] = new Person(Person.State.HEALTHY, true, i, j);
                    } else {
                        l_matrix[i][j] = new Person(Person.State.HEALTHY, false, i, j);
                    }
                } else {
                    l_matrix[i][j] = null;
                }
            }
        }

        return l_matrix;
    }

    private Person[][] initMatrixWithoutClusters(int citySize, double density, double p_vax) {
        Random rand = new Random();
        Person[][] l_matrix = new Person[citySize][citySize];
        for (int i = 0; i < citySize; i++) {
            for (int j = 0; j < citySize; j++) {
                float d = rand.nextFloat();
                float v = rand.nextFloat();
                if (d < density) {
                    if (v < p_vax) {
                        l_matrix[i][j] = new Person(Person.State.HEALTHY, true, i, j);
                    } else {
                        l_matrix[i][j] = new Person(Person.State.HEALTHY, false, i, j);
                    }
                } else {
                    l_matrix[i][j] = null;
                }
            }
        }
        return l_matrix;
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
        if (p != null && !p.isVax()) { // does not infect vaccinated persons
            p.setCurrentState(Person.State.SICK);
            System.out.println("Infected at position : " + p.getPos_i() + " ; " + p.getPos_j());
        }
    }

    public void infect() {
        // choose a random non-vaccinated person
        Random rand = new Random();
        int i, j;
        do {
            i = rand.nextInt(this.getDim());
            j = rand.nextInt(this.getDim());
        } while (this.matrix[i][j] == null || this.matrix[i][j].isVax());
        infect(i, j);
    }

    private boolean isHealthy(int i, int j) {
        // Return true if there is a person healthy at the given position
        return (this.matrix[i][j] != null && this.matrix[i][j].getCurrentState() == Person.State.HEALTHY);
    }

    private boolean isCured(int i, int j) {
        // Return true if there is a person cured at the given position
        return (this.matrix[i][j] != null && this.matrix[i][j].getCurrentState() == Person.State.CURED);
    }

    public boolean hasNeighbor(int i, int j) {
        // Return true if there is at least one neighbor
        int n = this.getDim();

        int[][] neighbors = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int[] nb : neighbors) {
            int ni = i + nb[0];
            int nj = j + nb[1];
            if (ni >= 0 && ni < n && nj >= 0 && nj < n) {
                Person p = matrix[ni][nj];
                if (p != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasNeighborInfectious(int i, int j) {
        // Return true if at least one neighbor is sick or a carrier
        int n = this.getDim();

        int[][] neighbors = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int[] nb : neighbors) {
            int ni = i + nb[0];
            int nj = j + nb[1];
            if (ni >= 0 && ni < n && nj >= 0 && nj < n) {
                Person p = matrix[ni][nj];
                if (p != null &&
                        (p.getCurrentState() == Person.State.SICK || p.isCarrier())) {
                    return true;
                }
            }
        }
        return false;
    }

    private int[] findEmptyCase() {
        int n = this.getDim();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.matrix[i][j] == null) {
                    int coords[] = new int[2];
                    coords[0] = i;
                    coords[1] = j;
                    return coords;
                }
            }
        }
        return null;
    }

    private int[] findEmptyCase(boolean randomExploration) {
        if (!randomExploration)
            return findEmptyCase();

        Random rand = new Random();
        int n = this.getDim();
        int maxTries = n * n * 2;
        int cpt = 0;
        while (cpt < maxTries) {
            int i = rand.nextInt(n);
            int j = rand.nextInt(n);
            if (this.matrix[i][j] == null) {
                int coords[] = new int[2];
                coords[0] = i;
                coords[1] = j;
                return coords;
            }
        }
        return null;
    }

    private Person nextState(int i, int j) {
        // Compute the next state of the cell
        Person p = matrix[i][j];
        Random rand = new Random();
        if (p != null) { // if person
            Person new_p = new Person(p);
            // if healthy and has an infectious neighbor -> may become sick
            if (p.getCurrentState() == Person.State.HEALTHY && this.hasNeighborInfectious(i, j)) {
                // if vax -> pVaxPolio of being a carrier
                if (p.isVax()) {
                    if (!p.isCarrier() && rand.nextDouble() < this.getpVaxPolio()) {
                        new_p.setCarrier(true);
                    }
                } else {
                    // if not vax -> pSpread of being sick
                    // Randomly decide if this person becomes sick
                    if (rand.nextDouble() < this.getpSpread()) {
                        new_p.setCurrentState(Person.State.SICK);
                    }
                }
            }
            // if sick -> either die or get cured
            else if (p.getCurrentState() == Person.State.SICK) {
                double x = rand.nextDouble();
                if (x < this.getpDeath()) { // Die
                    new_p.setCurrentState(Person.State.DEAD);
                } else { // Get cured
                    new_p.setCurrentState(Person.State.CURED);
                }
            }

            // People move at the end of the turn
            // Each people have a probability to move, if they found an empty spot

            if (this.getpMove() > 0) {
                if (this.matrix[i][j] != null) {
                    double x = rand.nextDouble();
                    if (x < this.getpMove()) {
                        int coords[] = findEmptyCase(true);
                        if (coords != null) { // null means the city is full of people
                            // swap coords
                            this.matrix[i][j] = null;
                            this.matrix[coords[0]][coords[1]] = new_p;
                            new_p.setPos_i(coords[0]);
                            new_p.setPos_j(coords[1]);
                            // System.out.format("Someone move from (%d, %d) to (%d, %d)", i, j, coords[0],
                            // coords[1]);
                        }
                    }
                }
            }
            return new_p;
        }
        return null;
    }

    private void propagatePolio1() {
        // Compute a whole new state of the matrix
        int n = this.getDim();
        Person[][] new_matrix = new Person[n][n];

        // Copy the current state to a new matrix
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Person p = this.matrix[i][j];
                if (p == null) {
                    new_matrix[i][j] = null;
                } else {
                    new_matrix[i][j] = new Person(p);
                }
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
        // Propagate the polio during n periods
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
                } else if (p.isCarrier() && p.isVax()) {
                    // Vaccinated carrier (healthy but infectious)
                    c = "\u001B[33mC\u001B[0m"; // yellow C
                } else {
                    switch (p.getCurrentState()) {
                        // EMPTY, HEALTHY, SICK, CURED, DEAD
                        case Person.State.HEALTHY -> c = ":-)";
                        case Person.State.SICK -> c = ":-(";
                        case Person.State.CURED -> c = ":-|";
                        case Person.State.DEAD -> c = "X_X";
                        default -> throw new AssertionError();
                    }
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

    public double getpVaxPolio() {
        return pVaxPolio;
    }

    public double getpMove() {
        return pMove;
    }
}
