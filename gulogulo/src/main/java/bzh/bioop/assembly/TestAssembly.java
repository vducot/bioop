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
        String USAGE = """
                       java assembly.jar <reads file> [perror]
                       reads file : the path of the file containing the reads to assemble
                       perror : percentage of errors accepted in the assembling""";
        // args = new String[1];
        // args[0] = "data/my_reads.txt";
        
        if (args.length < 1) {
            String art = " \u001B[32m.-./`)\u001B[0m  _______  \u001B[32m.-./`)\u001B[0m    .-'''-.    ____      \n" +
				" \u001B[32m\\ .-.')\u001B[0m\\  _____\\ \u001B[32m\\ .-.')\u001B[0m  / _     \\ .'  __ `.   \n" +
				" \u001B[32m/ `-' \\\u001B[0m| |    \\ |\u001B[32m/ `-' \\\u001B[0m \u001B[32m(`' )\u001B[0m/`--'/   '  \\  \\  \n" +
				"  \u001B[32m`-'`\"`\u001B[0m| |____/ / \u001B[32m`-'`\"`\u001B[0m\u001B[32m(_ o _)\u001B[0m.   |___|  /  |  \n" +
				"  .---. |   \u001B[32m_ _\u001B[0m '. .---.  \u001B[32m(_,_)\u001B[0m. '.    _.-`   |  \n" +
				"  |   | |  \u001B[32m( ' )\u001B[0m  \\|   | .---.  \\  :.'   \u001B[32m_\u001B[0m    |  \n" +
				"  |   | | \u001B[32m(_{;}_)\u001B[0m ||   | \\    `-'  ||  \u001B[32m_( )_\u001B[0m  |  \n" +
				"  |   | |  \u001B[32m(_,_)\u001B[0m  /|   |  \\       / \\ \u001B[32m(_ o _)\u001B[0m /  \n" +
				"  '---' /_______.' '---'   `-...-'   '.\u001B[32m(_,_)\u001B[0m.'   \n" +
                " \033[3mInfo & Biology to Improve Sequences Alignement\033[0m \n";
            System.out.println(art);
            String names = " \u001B[34m_____  _  _  _ _____ __   _\u001B[0m   \u001B[33m_    _ _____ __   _ _____  _____ __   _ _____\u001B[0m\n" +
                " \u001B[34m|  ___ |  |  | |____ | \\  |\u001B[0m    \u001B[33m\\  /    |   | \\  | |      |____ | \\  |   |\u001B[0m\n" +
                " \u001B[34m|____| |__|__| |____ |  \\_|\u001B[0m  &  \u001B[33m\\/   __|__ |  \\_| |____  |____ |  \\_|   |\u001B[0m\n" ;

            System.out.println(" By: \n" + names);
            System.err.println(USAGE);
            System.exit(1);
        }

        String readfile = args[0];

        float perror = 0;
        if (args.length > 1) {
            perror = Float.valueOf(args[1]);
        }

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
            if (index == -1) {
                break; // if no read whre overlap > 8
            }
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
