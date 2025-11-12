public class Read implements Sequence{

	private int len ;
	private String seq ;
	
	public static void main(String[] args) {
		Read r1 = new Read("azertyuiop");
		Read r2 = new Read("ertyuiopuui");
		//System.out.println(r1.bestOverlap(r2));
	}
	
	public Read() {
		seq = "azertyuiopqsdfghjklmwxcvbnazertyuiopdfghjklmqsdfghjklmllllkjhgfdsqsdfgaaaaaaaaaaacccccccccccccccccccccctttttttttttttttttttddddddddddddddddddhjklm"; 
		len = seq.length();
	}

	public Read(String s) {
		seq = s; 
		len = seq.length();
	}
	
	public String toString() {
		return seq ;
	}

	public int getLength() {
		return len ;
	}

	public String getSeq() {
		return seq ;
	}

	public String fastaFormat(){
		StringBuilder sb = new StringBuilder();
		int lineLength = 60;
		for(int i = 0; i < seq.length(); i += lineLength){
			if(i + lineLength < seq.length()){
				sb.append(seq, i, i + lineLength).append("\n");
			} else {
				sb.append(seq.substring(i)).append("\n");
			}
		}
		return sb.toString();
	}

}