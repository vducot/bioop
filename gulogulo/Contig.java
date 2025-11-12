
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Contig implements Sequence{

	private int len ;
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
		nb_fusions = 1;
	}

	public String toString() {
		return contig ;
	}

	public int getLength() {
		return len ;
	}

	public String getSeq() {
		return contig ;
	}

	public int getReadsCount() {
        return nb_fusions;
    }

	public String fastaFormat(){
		StringBuilder sb = new StringBuilder();
		int lineLength = 60;
		for(int i = 0; i < contig.length(); i += lineLength){
			if(i + lineLength < contig.length()){
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
	public int bestOverlap(Read r){
		String rseq = r.getSeq(); // read sequence
		int maxOverlap = Math.min(this.getLength(), rseq.length()); // maximum possible overlap

		for(int i = maxOverlap; i >= 0; i--){ // from maxOverlap to 0
			String suffix = contig.substring(contig.length() - i); // suffix du contig
            String prefix = rseq.substring(0, i); // prefix du read
            if (suffix.equals(prefix)) { // check si they match
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
		int bestIndex = -1;
		int bestOverlap = 0;
		for (int i = 0; i < l.size(); i++) {
			Read r = l.get(i);
			int overlap = bestOverlap(r);
			if (overlap > bestOverlap) {
				bestOverlap = overlap;
				bestIndex = i;
			}
		}
		if (bestOverlap < 8) {
			return -1;
		}else {
			return bestIndex;
		}
	}

	/**
	 * returns new contig that is the fusion of
	 * the read r and the contig using their best overlap
	 */
	public Contig fusion(Read r) {
		int overlap = bestOverlap(r);
		String rseq = r.getSeq();
		String newContigSeq;
		if (overlap ==0){
			newContigSeq = contig + rseq;
		} else {
			newContigSeq = contig + rseq.substring(overlap);
		}
		return new Contig(newContigSeq, nb_fusions + 1); //add 1 to nb_fusions?
	}
	
	public static void main ( String [] args ) throws IOException {
		// System . out . println ( System . getProperty (" user . dir") ) ;
		// String filename = "/ src/ assembly / my_reads .txt" ;
		// File monFichierTexte = new File ( System . getProperty (" user . dir") +
		// filename ) ;
		String monFichierTexte = "C:/Users/iborr/OneDrive/Desktop/M2_2025.26/POG/cestALGouquoi/my_reads.txt" ;
		// Simple test to verify that the file exists .
		// if ( monFichierTexte . exists () ) {
		// 	System . out . println ("The file " + filename + " is present in the given directory \n") ;
		// } else {
		// 	System . out . println ("The file " + filename + " is NOT present in the given directory ") ;
		// }
		List <Read> list_reads = new LinkedList <>();
		try (BufferedReader br = new BufferedReader (new FileReader (monFichierTexte))) {
			String line;
			while((line = br.readLine()) != null) {
				Read r1 = new Read(line);
				list_reads.add(r1);
			}
			br.close();
		}
		// Create a Contig with the first sequence of the list
		Contig contig = new Contig(list_reads.get(0));
		System.out.println("Contig initial : " +contig);
		// Greedy loop
		while (true) { //loop while still reads with overlap > 8 to assemble 
			int index = contig.nextRead((LinkedList<Read>) list_reads); // find the index of read with best overlap in the list
			if (index == -1) break; // if no read whre overlap >8
			// remove the best read from the list
			Read chosen = list_reads.remove(index);
			System.out.println("Fusion with " + index + ", still " + (list_reads.size()) + " reads to assemble... work in process");
			// fusion the contig (first line) with the chosen read
			contig = contig.fusion(chosen);
		}
		System.out.println("Contig obtained with " + contig.getReadsCount() + " reads");
        System.out.println(contig.fastaFormat());
	}
}