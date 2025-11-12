package bzh.bioop.gulogulo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Contig implements Sequence {

	private int len;
	private String contig;
	private int nb_fusions; // number of fusion to create the final contig

	public Contig() {
		contig = "azertyuiopqsdfghjklmwxcvbnazertyuiopdfghjklmqsdfghjklmllllkjhgfdsqsdfgaaaaaaaaaaacccccccccccccccccccccctttttttttttttttttttddddddddddddddddddhjklm";
		len = contig.length();
		nb_fusions = 0;
	}

	public Contig(String s, int nb_fusions) {
		contig = s;
		len = contig.length();
		this.nb_fusions = nb_fusions;
	}

	public Contig(Read r) {
		contig = r.getSeq();
		len = contig.length();
		nb_fusions = 0;
	}

	public String toString() {
		return contig;
	}

	public int getLength() {
		return len;
	}

	public String getSeq() {
		return contig;
	}

	public int getReadsCount() {
		return nb_fusions;
	}

	public String fastaFormat() {
		StringBuilder sb = new StringBuilder();
		int lineLength = 60;
		for (int i = 0; i < contig.length(); i += lineLength) {
			if (i + lineLength < contig.length()) {
				sb.append(contig, i, i + lineLength).append("\n");
			} else {
				sb.append(contig.substring(i)).append("\n");
			}
		}
		return sb.toString();
	}

	// --- Assembly methods ---
	/**
	 * Returns the length of the maximum suffix-prefix exact match
	 * between this contig and read r.
	 */
	public int bestOverlap(Read r) {
		String rseq = r.getSeq(); // read sequence
		int maxOverlap = Math.min(this.getLength(), rseq.length()); // maximum possible overlap
		String l_contig = this.getSeq();
		for (int i = maxOverlap; i >= 0; i--) { // from maxOverlap to 0
			String suffix = l_contig.substring(l_contig.length() - i); // suffix du contig
			String prefix = rseq.substring(0, i); // prefix du read
			// System.out.println("Suffix : "+suffix+" prefix : "+prefix+" \n");
			if (suffix.equals(prefix)) { // check si they match
				return i; // return la taille de l'overlap
			}
		}
		return 0;
	}

	/**
	 * Returns the length of the maximum suffix-prefix with some errors
	 * between this contig and read r.
	 */
	public int bestOverlapWithError(Read r, float perror) {
		String rseq = r.getSeq(); // read sequence
		int maxOverlap = Math.min(this.getLength(), rseq.length()); // maximum possible overlap
		String l_contig = this.getSeq();
		for (int i = maxOverlap; i >= 0; i--) { // from maxOverlap to 0
			String suffix = l_contig.substring(l_contig.length() - i); // suffix du contig
			String prefix = rseq.substring(0, i); // prefix du read
			// System.out.println("Suffix : "+suffix+" prefix : "+prefix+" \n");
			if (Read.nearlyEquals(suffix, prefix, perror)) { // check si they match
				return i; // return la taille de l'overlap
			}
		}
		return 0;
	}

	/**
	 * Return the index of the read with the largest overlap
	 * if overlap < 8, return -1
	 */
	public int nextRead(LinkedList<Read> l) {
		int bestOverlap = 0;
		int currentOverlap;
		int bestIndex = 0;
		int index = 0;
		for (Read r : l) {
			currentOverlap = this.bestOverlap(r);
			if (currentOverlap > bestOverlap) {
				bestOverlap = currentOverlap;
				bestIndex = index;
			}
			index++;
		}
		if (bestOverlap >= 8) {
			return bestIndex;
		}
		return -1; // overlap < 8 or empty list
	}

	/**
	 * Return the index of the read with the largest overlap and some errors
	 * if overlap < 8, return -1
	 */
	public int nextReadWithError(LinkedList<Read> l, float perror) {
		int bestOverlap = 0;
		int currentOverlap;
		int bestIndex = 0;
		int index = 0;
		for (Read r : l) {
			currentOverlap = this.bestOverlapWithError(r, perror);
			if (currentOverlap > bestOverlap) {
				bestOverlap = currentOverlap;
				bestIndex = index;
			}
			index++;
		}
		if (bestOverlap >= 8) {
			return bestIndex;
		}
		return -1; // overlap < 8 or empty list
	}

	/**
	 * returns new contig that is the fusion of
	 * the read r and the contig using their best overlap
	 */
	public Contig fusion(Read r) {
		int overlap = bestOverlap(r);
		String rseq = r.getSeq();
		String newContigSeq;
		if (overlap == 0) {
			newContigSeq = contig + rseq;
		} else {
			newContigSeq = contig + rseq.substring(overlap);
		}
		return new Contig(newContigSeq, nb_fusions + 1); // add 1 to nb_fusions?
	}

	public static void main(String[] args) throws IOException {

		// Contig c_nul = new Contig("AZERTYUIOP",0);
		// Read r = new Read("YUIOPQS");
		// int b = c_nul.bestOverlap(r);
		// System.out.println("Best overlap : "+b);
		// System.exit(1);

		Path filepath = Paths.get("gulogulo/data/my_reads.txt");
		// Path filepath =
		// Paths.get("gulogulo/data/my_reads_with_sequencing_errors.txt");

		List<Read> list_reads = new LinkedList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filepath.toAbsolutePath().toString()))) {
			String line;
			while ((line = br.readLine()) != null) {
				Read r1 = new Read(line);
				list_reads.add(r1);
			}
		}
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
		String names =
            "  ______ _  _  _ _______ __   _           _    _ _____ __   _ _______ _______ __   _ _______\n" +
            " |  ____ |  |  | |______ | \\  |            \\  /    |   | \\  | |       |______ | \\  |    |\n" +
            " |_____| |__|__| |______ |  \\_|             \\/   __|__ |  \\_| |_____  |______ |  \\_|    |\n" +
            "                                                                                             ";

        System.out.println(names);

		String project =
            "__________                   __        __      ________      .__                         .__           \n" +
            "\\______   \\_______  ____    |__| _____/  |_   /  _____/ __ __|  |   ____      ____  __ __|  |   ____   \n" +
            " |     ___/\\_  __ \\/  _ \\   |  |/ __ \\   __\\ /   \\  ___|  |  \\  |  /  _ \\    / ___\\|  |  \\  |  /  _ \\  \n" +
            " |    |     |  | \\ (  <_> )  |  \\  ___/|  |   \\    \\_\\  \\  |  /  |_(  <_> )  / /_/  >  |  /  |_(  <_> ) \n" +
            " |____|     |__|   \\____/\\__|  |\\___  >__|    \\______  /____/|____/\\____/   \\___  /|____/|____/\\____/  \n" +
            "                        \\______|    \\/               \\/                    /_____/                     ";

        System.out.println(project);

		// Create a Contig with the first sequence of the list
		Contig contig = new Contig(list_reads.get(0));
		// System.out.println("Contig initial : " +contig);
		list_reads.remove(0);
		float perror = (float) 0.01;
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
	}}