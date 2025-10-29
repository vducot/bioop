package bzh.bioop.polio;

public class Person {

    public enum State {
        HEALTHY, SICK, CURED, DEAD
    }

    private int pos_i;
    private int pos_j;
    private boolean vax;
    private boolean carrier = false; // vaccinated person carrying virus but healthy
    private State currentState;

    public Person(State s, boolean vaccinated, int i, int j) {
        this.currentState = s;
        this.vax = vaccinated;
        this.pos_i = i;
        this.pos_j = j;
    }

    public Person(State s, boolean vaccinated) {
        this.currentState = s;
        this.vax = vaccinated;
        // values out of the map if we don't know the exact position
        this.pos_i = -1;
        this.pos_j = -1;
    }

    public Person(Person s) {
        this.pos_i = s.getPos_i();
        this.pos_j = s.getPos_j();
        this.vax = s.isVax();
        this.currentState = s.getCurrentState();
    }

    public int getPos_i() {
        return pos_i;
    }

    public int getPos_j() {
        return pos_j;
    }

    public boolean isVax() {
        return vax;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void setPos_i(int pos_i) {
        this.pos_i = pos_i;
    }

    public void setPos_j(int pos_j) {
        this.pos_j = pos_j;
    }

    public void setVax(boolean vax) {
        this.vax = vax;
    }

    public boolean isCarrier() { return carrier; }
    
    public void setCarrier(boolean carrier) { this.carrier = carrier; }

    public void setCurrentState(State currentState) {
        this.currentState = currentState;
    }

}
