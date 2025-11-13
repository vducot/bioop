package bzh.bioop.assembly;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class TestAssembly {
    public static void main(String[] args) throws IOException {
        String USAGE = "java assembly.jar <reads file> [perror]";
        args = new String[1];
        args[0] = "gulogulo/data/my_reads.txt";
        
        if (args.length < 1) {
            String art = ".-./`)  _______  .-./`)    .-'''-.    ____      \n" +
                    " \\ .-.')\\  _____  \\ .-.')  / _     \\ .'  __ `.   \n" +
                    "/ `-' \\| |    \\ |/ `-' \\ (`' )/`--'/   '  \\  \\  \n" +
                    " `-'`\"`| |____/ / `-'`\"`(_ o _).   |___|  /  |  \n" +
                    " .---. |   _ _ '. .---.  (_,_). '.    _.-`   |  \n" +
                    " |   | |  ( ' )  \\|   | .---.  \\  :.'   _    |  \n" +
                    " |   | | (_{;}_) ||   | \\    `-'  ||  _( )_  |  \n" +
                    " |   | |  (_,_)  /|   |  \\       / \\ (_ o _) /  \n" +
                    " '---' /_______.' '---'   `-...-'   '.(_,_).'   \n" +
                    "                                                ";
            System.out.println(art);
            String names = "  ______ _  _  _ _______ __   _           _    _ _____ __   _ _______ _______ __   _ _______\n"
                    +
                    " |  ____ |  |  | |______ | \\  |            \\  /    |   | \\  | |       |______ | \\  |    |\n" +
                    " |_____| |__|__| |______ |  \\_|             \\/   __|__ |  \\_| |_____  |______ |  \\_|    |\n" +
                    "                                                                                             ";

            System.out.println(names);

            String project = "__________                   __        __      ________      .__                         .__           \n"
                    +
                    "\\______   \\_______  ____    |__| _____/  |_   /  _____/ __ __|  |   ____      ____  __ __|  |   ____   \n"
                    +
                    " |     ___/\\_  __ \\/  _ \\   |  |/ __ \\   __\\ /   \\  ___|  |  \\  |  /  _ \\    / ___\\|  |  \\  |  /  _ \\  \n"
                    +
                    " |    |     |  | \\ (  <_> )  |  \\  ___/|  |   \\    \\_\\  \\  |  /  |_(  <_> )  / /_/  >  |  /  |_(  <_> ) \n"
                    +
                    " |____|     |__|   \\____/\\__|  |\\___  >__|    \\______  /____/|____/\\____/   \\___  /|____/|____/\\____/  \n"
                    +
                    "                        \\______|    \\/               \\/                    /_____/                     ";

            System.out.println(project);
            System.err.println(USAGE);
            System.exit(1);
        }

        String readfile = args[0];

        float perror = 0;
        if (args.length > 1) {
            perror = Float.valueOf(args[1]);
        }

        // Contig c_nul = new Contig("AZERTYUIOP",0);
        // Read r = new Read("YUIOPQS");
        // int b = c_nul.bestOverlap(r);
        // System.out.println("Best overlap : "+b);
        // System.exit(1);

        // Path filepath = Paths.get("gulogulo/data/my_reads.txt");
        Path filepath = Paths.get(readfile);

        List<Read> list_reads = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath.toAbsolutePath().toString()))) {
            String line;
            while ((line = br.readLine()) != null) {
                Read r1 = new Read(line);
                list_reads.add(r1);
            }
        }

        // Create a Contig with the first sequence of the list
        Contig contig = new Contig(list_reads.get(0));
        list_reads.remove(0);
        // float perror = (float) 0.01;

        // Greedy loop
        while (true) { // loop while still reads with overlap > 8 to assemble
            int index = contig.nextReadWithError((LinkedList<Read>) list_reads, perror); // find the index of read with
                                                                                         // best overlap in the list
            if (index == -1)
                break; // if no read whre overlap > 8
            // remove the best read from the list
            Read chosen = list_reads.remove(index);
            System.out.println("Fusion with " + index + ", still " + (list_reads.size())
                    + " reads to assemble... work in process");
            // fusion the contig (first line) with the chosen read
            contig = contig.fusion(chosen);
        }
        System.out.println("Contig obtained with " + contig.getReadsCount() + " reads");
        System.out.println(contig.fastaFormat());
    }
}
