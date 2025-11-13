package bzh.bioop.assembly;

/**
 * Interface that represents a sequence
 */
public interface Sequence {
	
	/**
	 * Implement toString method to display a Sequence
	 * @return the string representing the object
	 */
    @Override
	public abstract String toString();

	/**
	 * Accessor for sequence length
	 * @return the sequence length
	 */
	public abstract int getLength();

	/**
	 * Accessor for sequence string
	 * @return the sequence as a string
	 */
	public abstract String getSeq();
	
	/**
	 * Format the sequence like Fasta, 60 nucleotides max per line
	 * @return the formatted sequence
	 */
	public String fastaFormat();
	
}