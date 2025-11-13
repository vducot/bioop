package bzh.bioop.assembly;

import java.util.*;

/**
 * Represents a contig, ie an assembly of reads
 */
public class Contig implements Sequence {

	private final int len;
	private final String contig;
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

	@Override
	public String toString() {
		return contig;
	}

    @Override
	public int getLength() {
		return len;
	}

    @Override
	public String getSeq() {
		return contig;
	}

	public int getReadsCount() {
		return nb_fusions;
	}

	/**
	 * Format the sequence like Fasta, 60 nucleotides max per line
	 * 
	 * @return the formatted sequence
	 */
    @Override
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
	 * @param the read to align with the contig
	 * @return the length of the best match read-contig
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
	 * @param the read to align with the contig
	 * @return the length of the best match read-contig
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
	 * @param a list of reads
	 * @return the index of the read with the largest overlap
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
	 * Return the index of the read with the largest overlap and max perror pourcentage of errors
	 * if overlap < 8, return -1
	 * @param a list of reads
	 * @return the index of the read with the largest overlap
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
	 * @param the read to merge
	 * @return the fusionned new contig
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
		return new Contig(newContigSeq, nb_fusions + 1); // add 1 to nb_fusions
	}
}