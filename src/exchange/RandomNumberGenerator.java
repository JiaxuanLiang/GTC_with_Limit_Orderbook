package exchange;

import java.util.Random;

public class RandomNumberGenerator {
	private static Random RANDOM = new Random();
	private static Long LAST_RANDOM = null;
	public static void Seed( long seed ) {
		LAST_RANDOM = RANDOM.nextLong();
		RANDOM = new Random( seed );
	}
	public static void RestoreRandom() {
		if( LAST_RANDOM != null ) {
			RANDOM = new Random( LAST_RANDOM );
			LAST_RANDOM = null;
		}
	}
	public static Integer NextLong() { return RANDOM.nextInt( 1000 ); } //1000 is the bound for a random number from uniform distribution.
	
	// Print the output to understand the code
	public static void main(String[] args) {
		System.out.println(RANDOM);
		System.out.println(RANDOM.nextLong()); //Random() can generate a random number without a seed.
		System.out.println(RANDOM.nextLong()); // Without seed, it generates a different number each time.
		System.out.println(LAST_RANDOM);
		System.out.println(NextLong());
		System.out.println(NextLong());
		
	    // Why setting seed here? -The return value of NextLong() is fixed.
		Seed(1l);
		System.out.println(RANDOM); 
		System.out.println(LAST_RANDOM);
		System.out.println(NextLong());
		
		Seed(1l);
		System.out.println(RANDOM); 
		System.out.println(LAST_RANDOM);
		System.out.println(NextLong());
				
	}
}
