package bzh.bioop.polio;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class TestPolio {
    public static void main(String[] args) throws Exception {

        //Polio po = new Polio(10, 0.6, 0.65, 0.8, 0.15, 0.05, 0.1, false);
        Polio po = new Polio(20, 0.7, 0.65, 0.8, 0.15, 0.05, 0.1, true);
        System.out.println(" Is there any disease " + po.isOneSick());
        System.out.println(" Is everybody dead yet ? " + po.isEndOfTheWorld());

        po.infect();
        po.infect();
        po.cityDisplay();
        // System.out.println(po.hasNeighborInfectious(0,0));
        // System.out.println(po.hasNeighborInfectious(0, 1));
        // System.out.println(po.hasNeighborInfectious(1, 0));
        po.propagatePolio(2);
        System.out.println();
        po.cityDisplay();

 
        // System.out.println(po.hasNeighborSick(0, 2));
        // System.out.println(po.hasNeighborSick(1, 2));
        // System.out.println(po.hasNeighborSick(2, 1));
        // System.out.println(po.hasNeighborSick(2, 2));
        // System.out.println(po.hasNeighborSick(2, 3));

        // // === Simulation Parameters ===
        // int citySize = 10;          
        // double density = 0.8;              // pop density
        // double pDeath = 0.25; //0.02       // prob of diying when sick
        // double pSpread = 0.8; //0.3        // prob of spreding polio
        // double pVax = 0.3; //0.75          // fraction of vaccinated people
        // double pVaxPolio = 0.1; //0.01     // prop for vax people to transmit polio
        // boolean enableDisplay = true;     // graphical display

        // // --- City creation ---
        // Polio po = new Polio(citySize, density, pDeath, pSpread, pVax, pVaxPolio);
        // // System.out.println("Initial city:");
        // // po.cityDisplay();

        // // Random infection of a non-vaccinated person
        // po.infect();

        // // === Optional Graphical setup ===
        // EpidemicGrid.EpidemicPanel panel = null;
        // if (enableDisplay) {
        //     // initialization
        //     panel = new EpidemicGrid.EpidemicPanel(citySize, citySize);
        //     // mark patient zero
        //     boolean marked = false;
        //     for (int i = 0; i < citySize && !marked; i++) {
        //         for (int j = 0; j < citySize && !marked; j++) {
        //             Person p = po.getMatrix()[i][j];
        //             if (p != null && p.getCurrentState() == Person.State.SICK) {
        //                 panel.setPatientZero(i, j);
        //                 marked = true;
        //             }
        //         }
        //     }
        //     // create JFrame
        //     JFrame frame = new JFrame("Polio Epidemic Simulation");
        //     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //     frame.getContentPane().add(new JScrollPane(panel));
        //     frame.pack();
        //     frame.setLocationRelativeTo(null);
        //     frame.setVisible(true);
        // }

        // // === Run the simulation loop ===
        // int step = 0;
        // while (po.isOneSick() && !po.isEndOfTheWorld()) {
        //     step++;
        //     po.propagatePolio(1);
        //     // Console display
        //     System.out.println("\n--- Step " + step + " ---");
        //     po.cityDisplay();

        //     // update the display if enabled
        //     if (enableDisplay && panel != null) {
        //         int[][] stateMatrix = new int[citySize][citySize];
        //         Person[][] matrix = po.getMatrix();
        //         // convert states to integer for EpidemicGrid
        //         for (int i = 0; i < citySize; i++) {
        //             for (int j = 0; j < citySize; j++) {
        //                 Person p = matrix[i][j];
        //                 if (p == null) {
        //                     stateMatrix[i][j] = -1; // empty cell
        //                 } else {
        //                     if (p.isVax()) {
        //                         stateMatrix[i][j] = EpidemicGrid.VAX;
        //                     } else {
        //                         switch (p.getCurrentState()) {
        //                             case HEALTHY -> stateMatrix[i][j] = EpidemicGrid.HEALTHY;
        //                             case SICK    -> stateMatrix[i][j] = EpidemicGrid.SICK;
        //                             case DEAD    -> stateMatrix[i][j] = EpidemicGrid.DEAD;
        //                             case CURED   -> stateMatrix[i][j] = EpidemicGrid.CURED;
        //                             default      -> stateMatrix[i][j] = -1; // unknown state
        //                         }
        //                     }
        //                 }
        //             }
        //         }
        //         panel.setGrid(stateMatrix); // update
        //         // pause to visualize steps
        //         try {
        //             Thread.sleep(500);
        //         } catch (InterruptedException e) {
        //             Thread.currentThread().interrupt();
        //         }
        //     }
        // }

        // // === Simulation finished ===
        // System.out.println("\n=== Simulation ended after " + step + " steps ===");
        // if (po.isEndOfTheWorld()) {
        //     System.out.println("Everyone is dead.");
        // } else {
        //     System.out.println("Epidemic is over (no one is sick).");
        // }
    }
}
