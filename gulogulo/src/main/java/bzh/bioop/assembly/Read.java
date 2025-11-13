package bzh.bioop.assembly;

/**
 * Represents a Read
 */
public class Read implements Sequence {

	private final int len;
	private final String seq;

	public static void main(String[] args) {
		Read r1 = new Read("azertyuiop");
		// Read r2 = new Read("azertyuiopjdg");
		Read r2 = new Read("ertyuiopui");
		float perror = (float)0.9;
		System.out.println(nearlyEquals(r1.getSeq(),r2.getSeq(), perror));

	}

	public Read() {
		seq = "azertyuiopqsdfghjklmwxcvbnazertyuiopdfghjklmqsdfghjklmllllkjhgfdsqsdfgaaaaaaaaaaacccccccccccccccccccccctttttttttttttttttttddddddddddddddddddhjklm";
		len = seq.length();
	}

	public Read(String s) {
		seq = s;
		len = seq.length();
	}

	/**
	 * Check if two strings are equal, with one error max
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return true if the two strings are different of maximum one character
	 */
	public static boolean nearlyEquals(String s1, String s2) {
		int minLength = Math.min(s1.length(), s2.length());
		int maxLength = Math.max(s1.length(), s2.length());
		int diffCount = 0;
		int i = 0;
		for (i = 0; i < minLength; i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				diffCount++;
			}
		}
		diffCount += (maxLength - i);
		return diffCount <= 1;
	}

	/**
	 * Check if two strings are equal, accepting a pourcentage of error
	 * @param s1 the first string
	 * @param s2 the second string
	 * @return true if the two strings are different of maximum perror pourcentage of errors
	 */
	public static boolean nearlyEquals(String s1, String s2, float perror) {
		int minLength = Math.min(s1.length(), s2.length());
		int maxLength = Math.max(s1.length(), s2.length());
		int diffCount = 0;
		int i = 0;
		for (i = 0; i < minLength; i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				diffCount++;
			}
		}
		diffCount += (maxLength - i);
		return (float)diffCount/minLength <= perror;
	}

    @Override
	public String toString() {
		return seq;
	}

    @Override
	public int getLength() {
		return len;
	}

    @Override
	public String getSeq() {
		return seq;
	}

	/**
	 * Format the sequence like Fasta, 60 nucleotides max per line
	 * @return the formatted sequence
	 */
    @Override
	public String fastaFormat() {
		StringBuilder sb = new StringBuilder();
		int lineLength = 60;
		for (int i = 0; i < seq.length(); i += lineLength) {
			if (i + lineLength < seq.length()) {
				sb.append(seq, i, i + lineLength).append("\n");
			} else {
				sb.append(seq.substring(i)).append("\n");
			}
		}
		return sb.toString();
	}

}