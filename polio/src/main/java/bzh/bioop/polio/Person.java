package bzh.bioop.polio;

/**
 * Person class represents a person with its position in the city and health's state.
 * 
 * Possible states : HEALTHY, SICK, CURED, DEAD
 * 
 * @author Vincent & Gwendoline
 */
public class Person {

    /**
     * Health's state
     */
    public enum State {
        HEALTHY, SICK, CURED, DEAD
    }

    private int pos_i;
    private int pos_j;
    private boolean vax;
    private boolean carrier = false; // vaccinated person carrying virus but healthy
    private State currentState;
    private boolean patientZero = false;

    /**
     * Constructor for Person class
     * @param s person's initial state
     * @param vaccinated true if the person is vaccinated
     * @param i The x position in the city
     * @param j The y position in the city
     */
    public Person(State s, boolean vaccinated, int i, int j) {
        this.currentState = s;
        this.vax = vaccinated;
        this.pos_i = i;
        this.pos_j = j;
    }

    /**
     * Call the global constructor. Create a person out of the city
     * @param s person's initial state
     * @param vaccinated true if the person is vaccinated
     */
    public Person(State s, boolean vaccinated) {
        this.currentState = s;
        this.vax = vaccinated;
        // values out of the map if we don't know the exact position
        this.pos_i = -1;
        this.pos_j = -1;
    }

    /**
     * Copy constructor
     * @param p Another Person object
     */
    public Person(Person p) {
        this.pos_i = p.getPos_i();
        this.pos_j = p.getPos_j();
        this.vax = p.isVax();
        this.currentState = p.getCurrentState();
        this.patientZero = p.isPatientZero();
    }

    // Getters and setters

    /**
     * Get the x position in the city map
     * @return the x position 
     */
    public int getPos_i() {
        return pos_i;
    }

    /**
     * Get the y position in the city map
     * @return the y position 
     */
    public int getPos_j() {
        return pos_j;
    }

    /**
     * Get the vaccinated status
     * @return true is the person is vaccinated
     */
    public boolean isVax() {
        return vax;
    }

    /**
     * Get the health status
     * @return the health status
     */
    public State getCurrentState() {
        return currentState;
    }
    
    /**
     * Get the carrier status
     * @return true if the person carry the disease
     */
    public boolean isCarrier() { return carrier; }

    /**
     * Get the patient zero status
     * @return true if the person is patient zero
     */
    public boolean isPatientZero() {
        return patientZero;
    }

    /**
     * Set the x position in the city map
     * @param the x position 
     */
    public void setPos_i(int pos_i) {
        this.pos_i = pos_i;
    }

    /**
     * Set the y position in the city map
     * @param the y position 
     */
    public void setPos_j(int pos_j) {
        this.pos_j = pos_j;
    }
    
    /**
     * Set the carrier status
     * @param the carrier status
     */
    public void setCarrier(boolean carrier) { this.carrier = carrier; }

    /**
     * Set the health status
     * @param the health status
     */
    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

    /**
     * Set the patient zero status
     * @param the patient zero status
     */
    public void setPatientZero(boolean patientZero) {
        this.patientZero = patientZero;
    }
}
