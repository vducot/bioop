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
    private double pMove; // probability for people to move at each turn

    /**
     * Constructor for Polio object
     * 
     * @param citySize          Length of a side of the square representing the city
     * @param density           Wanted density of people in the city, between 0 and
     *                          1
     * @param deathProbability  Death probability when sick
     * @param spreadProbability Spread probability when sick
     * @param p_vax             Vaccine coverage, as a probability to be vaccinated
     * @param vaxPolioProb      Probability a vaccinated person still catches polio
     * @param moveProbability   Probability for people to move at each turn
     * @param clusters          True if the city is mostly populated with clusters
     *                          of people
     * @throws Exception
     */
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

    /**
     * Call the global Polio constructor with default values
     * 
     * @param density Wanted density of people in the city, between 0 and 1
     * @param p_vax   Vaccine coverage, as a probability to be vaccinated
     * @throws Exception
     */
    public Polio(double density, double p_vax) throws Exception {
        // Call the generic constructor
        this(10, density, 0.25, 0.8, p_vax, 0.05, 0, false);
    }

    /**
     * Call the global Polio constructor with default values
     * 
     * @param density Wanted density of people in the city, between 0 and 1
     * @throws Exception
     */
    public Polio(double density) throws Exception {
        // Call the generic constructor
        // Vaccination rate is about 75% in the world
        this(10, density, 0.65, 0.8, 0.15, 0.05, 0, false);
    }

    /**
     * Create a city with people mostly grouped by clusters
     * 
     * @param citySize Length of a side of the square representing the city
     * @param density  Wanted density of people in the city, between 0 and 1
     * @param p_vax    Vaccine coverage, as a probability to be vaccinated
     * @return the initialized city map
     */
    private Person[][] initMatrixWithClusters(int citySize, double density, double p_vax) {
        Random rand = new Random();
        Person[][] l_matrix = new Person[citySize][citySize];
        int totalPop = (int) Math.round(citySize * citySize * density);

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

    /**
     * Create a city with people randomly placed
     * 
     * @param citySize Length of a side of the square representing the city
     * @param density  Wanted density of people in the city, between 0 and 1
     * @param p_vax    Vaccine coverage, as a probability to be vaccinated
     * @return the initialized city map
     */
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

    /**
     * Check if no one is alive in the city
     * 
     * @return false if at least one person is alive, else true
     */
    public boolean isEndOfTheWorld() {
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

    /**
     * Check if at least one person is sick
     * 
     * @return true is at least one person is sick, else false
     */
    public boolean isOneSick() {
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

    /**
     * Infect a person at the position (i, j). Nothing happen if the case is empty.
     * 
     * @param i The x position in the city
     * @param j The y position in the city
     */
    public void infect(int i, int j) {
        Person p = this.matrix[i][j];
        if (p != null) {
            if (p.isVax()) {
                Random rand = new Random();
                Float x = rand.nextFloat();
                if (x < 0.15) { // 15% proba to be infected when vaccinated
                    p.setCurrentState(Person.State.SICK);
                    System.out.println("Infected at position : " + p.getPos_i() + " ; " + p.getPos_j());
                }
            } else { // Not vaccinated, always get sick
                p.setCurrentState(Person.State.SICK);
                System.out.println("Infected at position : " + p.getPos_i() + " ; " + p.getPos_j());
            }
        }
    }

    /**
     * Infect a person at a random position. Nothing happen if the case is empty.
     */
    public void infect() {
        Random rand = new Random();
        int i, j;
        do {
            i = rand.nextInt(this.getDim());
            j = rand.nextInt(this.getDim());
        } while (this.matrix[i][j] == null);
        infect(i, j);
    }

    /**
     * Check if the specified case has at least one person as neighbor
     * 
     * @param i The x position in the city
     * @param j The y position in the city
     * @return true if there is at least one neighbor
     */
    public boolean hasNeighbor(int i, int j) {
        int n = this.getDim();

        // Represents the shifts from the given case
        int[][] neighbors = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        //for (int[] nb : neighbors) {
        for (int nb = 0; nb < neighbors.length - 1 ; nb++) {
            int ni = i + neighbors[nb][0];
            int nj = j + neighbors[nb][1];
            if (ni >= 0 && ni < n && nj >= 0 && nj < n) {
                Person p = matrix[ni][nj];
                if (p != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if the specified case has at least one infected neighbor (sick or
     * carrier)
     * 
     * @param i The x position in the city
     * @param j The y position in the city
     * @return true if there is at least one infected neighbor
     */
    private boolean hasNeighborInfectious(int i, int j) {
        int n = this.getDim();

        // Represents the shifts from the given case
        int[][] neighbors = {
                { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 },
                { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 }
        };

        for (int nb = 0; nb < neighbors.length - 1 ; nb++) {
            int ni = i + neighbors[nb][0];
            int nj = j + neighbors[nb][1];
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

    /**
     * Find the first empty case in the city
     * 
     * @return The coordinates (x,y) of the first empty case found, or null if no
     *         case is empty
     */
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

    /**
     * Find an empty case in the city when exploring it randomly
     * 
     * @return The coordinates (x,y) of an empty case found, or null if no case is
     *         empty (or the function made too much random moves)
     */
    private int[] findEmptyCase(boolean randomExploration) {
        if (!randomExploration)
            return findEmptyCase();

        Random rand = new Random();
        int n = this.getDim();
        int maxTries = n * n * 2; // Limit the random exploration, so the function can end.
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

    /**
     * Compute the next state of a case
     * 
     * @param i The x position in the city
     * @param j The y position in the city
     * @return A Person with updated status, or null if the case is empty.
     */
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

    /**
     * Compute a whole new state of the matrix
     */
    private void propagatePolio1() {
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

    /**
     * Propagate the polio during n periods
     * 
     * @param n Number of steps to run
     */
    public void propagatePolio(int n) {
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

    /**
     * Print the whole city
     */
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

    // Getters and setters

    /**
     * Getter for the city size
     * 
     * @return the size of a city's side
     */
    public int getDim() {
        return this.dim;
    }

    /**
     * Getter for death probability
     * 
     * @return the probability of death when sick
     */
    public double getpDeath() {
        return pDeath;
    }

    /**
     * Getter for spread probability
     * 
     * @return the probability of spread the disease when sick
     */
    public double getpSpread() {
        return pSpread;
    }

    /**
     * Getter for vaccinated probability
     * 
     * @return the probability of being vaccinated
     */
    public double getpVaxPolio() {
        return pVaxPolio;
    }

    /**
     * Getter for probability of moving
     * 
     * @return the probability of moving
     */
    public double getpMove() {
        return pMove;
    }

    public Person[][] getMatrix() {
        // Return a copy of the matrix
        int n = this.dim;
        Person[][] copy = new Person[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (this.matrix[i][j] != null) {
                    copy[i][j] = new Person(this.matrix[i][j]);
                }
            }
        }
        return copy;
    }
}
