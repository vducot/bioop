package bioop.fire;

import java.io.FileWriter;

public class TestAutomata {
    public static void main(String[] args) throws Exception {
        // Test the effect of density on fire propagation
        FileWriter wr_time = new FileWriter("complexity.csv");
        FileWriter wr = new FileWriter("razed_diag.csv");
        wr.write("Density;Nb_razed\n");
        //for (double d=0.05 ; d <= 1; d+=0.05) {
        for (int n=100;n<500;n+=20) {
            long startTime = System.currentTimeMillis();
            int c = 0;
            for (int j = 0 ; j < 100 ; j++) {
                Automata iAC = new Automata(n, 0.3);
                iAC.putFire();
                iAC.propagateFire(36); // how many steps do we need ?
                if (iAC.isRazed()) {
                    c++;
                }
            }
            // System.out.println("With density "+String.format("%.2f",d)+", "+c+" forests completely razed");
            // wr.write(String.format("%.2f",n)+";"+c+"\n");
           
            long estimatedTime = System.currentTimeMillis() - startTime;
            wr_time.write(n+";"+estimatedTime+"\n");
        }
        wr.close();
        wr_time.close();


        // Automata iAC = new Automata(0.3);
        // System.out.println(" Is the forest on fire ? " + iAC.isOnFire());
        // System.out.println(" Is the forest completely rased ? " + iAC.isRazed());

        // iAC.putFire(1, 1);
        // iAC.forestDisplay();
        // System.out.println(iAC.hasNeighborOnFire(0,0));
        // System.out.println(iAC.hasNeighborOnFire(0, 1));
        // System.out.println(iAC.hasNeighborOnFire(1, 0));
        // System.out.println(iAC.hasNeighborOnFire(0, 2));
        // System.out.println(iAC.hasNeighborOnFire(1, 2));
        // System.out.println(iAC.hasNeighborOnFire(2, 1));
        // System.out.println(iAC.hasNeighborOnFire(2, 2));
        // System.out.println(iAC.hasNeighborOnFire(2, 3));
        

        // Automata iAC = new Automata(5, 0.8);
        // iAC.putFire(2, 2);
        // iAC.forestDisplay();
        // System.out.println("Propagate ! \n\n");
        // iAC.propagateFire(4);
        // iAC.forestDisplay();
        // System.out.println(" Is the forest completely rased ? " + iAC.isRazed());
        // Automata jAC = new Automata(25, 0.8);
        // System.out.println(" Is the forest on fire ? " + jAC.isOnFire());
        // System.out.println(" Is the forest completely rased ? " + jAC.isRazed());
        // jAC.forestDisplay();

        // Automata kAC = new Automata(20, 0.7);
        // kAC.putFire(20,20);
        // kAC.putFire();
        // System.out.println(" Is the forest on fire ? " + kAC.isOnFire());
        // System.out.println(" Is the forest completely rased ? " + kAC.isRazed());
        // kAC.forestDisplay();

        // // Should throw an exception
        // Automata zAC = new Automata(2, 0.5);
    }
}
