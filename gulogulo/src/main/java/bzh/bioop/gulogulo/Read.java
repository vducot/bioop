package bzh.bioop.gulogulo;

public class Read implements Sequence {

	private int len;
	private String seq;

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

	public String toString() {
		return seq;
	}

	public int getLength() {
		return len;
	}

	public String getSeq() {
		return seq;
	}

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