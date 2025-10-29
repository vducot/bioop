package bzh.bioop.polio;

public class TestPolio {
    public static void main(String[] args) throws Exception {

        Polio po = new Polio(0.8);
        System.out.println(" Is there any disease " + po.isOneSick());
        System.out.println(" Is everybody dead yet ? " + po.isEndOfTheWorld());

        po.infect(1,1);
        po.cityDisplay();
        System.out.println(po.hasNeighborInfectious(0,0));
        System.out.println(po.hasNeighborInfectious(0, 1));
        System.out.println(po.hasNeighborInfectious(1, 0));
        po.propagatePolio(5);
        po.cityDisplay();
        // System.out.println(po.hasNeighborSick(0, 2));
        // System.out.println(po.hasNeighborSick(1, 2));
        // System.out.println(po.hasNeighborSick(2, 1));
        // System.out.println(po.hasNeighborSick(2, 2));
        // System.out.println(po.hasNeighborSick(2, 3));

        // // --- City creation ---
        // // System.out.println("=== Realistic Polio Simulation ===");
        // // Polio po = new Polio(10, 0.8, 0.02, 0.3, 0.75, 0.05);
        // System.out.println("=== Testing Simulation ===");
        // Polio po = new Polio(10, 0.8, 0.25, 0.8, 0.5, 0.1);
        // System.out.println("Initial city:");
        // po.cityDisplay();

        // // Random infection of a non-vaccinated person
        // po.infect();
        // System.out.println("\nAfter first infection:");
        // po.cityDisplay();

        // // --- Simulation ---
        // int step = 0;
        // while (po.isOneSick() && !po.isEndOfTheWorld()) {
        //     step++;
        //     System.out.println("\n--- Step " + step + " ---");
        //     po.propagatePolio(1);
        //     po.cityDisplay();
        // }

        // System.out.println("\nSimulation ended after " + step + " steps.");
        // if (po.isEndOfTheWorld()) {
        //     System.out.println("Everyone is dead.");
        // } else {
        //     System.out.println("Epidemic is over (no one sick).");
        // }
        
    }
}
