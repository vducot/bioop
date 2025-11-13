package bzh.bioop.polio;

import javax.swing.*;

/**
 * TestPolio.java
 * 
 * This class runs a Polio epidemic simulation in a city represented by a matrix.
 * Optionally, it displays the epidemic using a graphical interface (EpidemicGrid).
 * 
 * Requirements:
 * - Polio.java
 * - Person.java 
 * - EpidemicGrid.java (optioal for graphical display)
 */
public class TestPolio {

    public static void main(String[] args) throws Exception {

        // === Simulation Parameters ===
        int citySize = 16;          
        double density = 0.7;              // pop density
        double pDeath = 0.2; //0.02       // prob of diying when sick
        double pSpread = 0.75; //0.3        // prob of spreding polio
        double pVax = 0.3; //0.75          // fraction of vaccinated people
        double pVaxPolio = 0.2; //0.01     // prop for vax people to transmit polio
        double pMove = 0.1;                // prob of moving each step
        boolean enableCluster = false;     // enable people clustering
        boolean enableDisplay = false;      // graphical display

        // === Create the Polio simulation ===
        Polio po = new Polio(citySize, density, pDeath, pSpread, pVax, pVaxPolio, pMove, enableCluster);
        //System.out.println("=== Initial city ===");
        //po.cityDisplay();  // console display

        // Infect a random non-vaccinated person
        po.infect();
        //System.out.println("\n=== After first infection ===");
        //po.cityDisplay();
        boolean patientZeroMarked = false;
        Person[][] matrix = po.getMatrix();
        for (int i = 0; i < citySize && !patientZeroMarked; i++) {
            for (int j = 0; j < citySize && !patientZeroMarked; j++) {
                Person p = matrix[i][j];
                if (p != null && p.getCurrentState() == Person.State.SICK) {
                    p.setPatientZero(true);
                    patientZeroMarked = true;
                }
            }
        }

        // === Optional Graphical setup ===
        EpidemicGrid.EpidemicPanel panel = null;
        if (enableDisplay) {
            // initialization
            panel = new EpidemicGrid.EpidemicPanel(citySize, citySize);
            panel.setMatrix(matrix);
            // create JFrame
            JFrame frame = new JFrame("Polio Epidemic Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JScrollPane(panel));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        // === Run the simulation loop ===
        int step = 0;
        while (po.isOneSick() && !po.isEndOfTheWorld()) {
            step++;
            po.propagatePolio(1);
            // Console display
            System.out.println("\n--- Step " + step + " ---");
            po.cityDisplay();

            // update the display if enabled
            // Update the display if enabled
            if (enableDisplay && panel != null) {
                panel.setMatrix(po.getMatrix());  // matrix already updated in po.getMatrix()
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // === Simulation finished ===
        System.out.println("\n=== Simulation ended after " + step + " steps ===");
        if (po.isEndOfTheWorld()) {
            System.out.println("Everyone is dead.");
        } else {
            System.out.println("Epidemic is over (no one is sick).");
        }
    }
}
