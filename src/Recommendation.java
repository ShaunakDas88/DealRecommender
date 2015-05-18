public class Recommendation {

	/**
	 * This is the constructor for Recommendation class
	 */
	public Recommendation(){}
	
	/**
	 * Main method, will be involved in creating an instance
	 * of Members class.
	 * 
	 * @param args
	 */
	
	public static void main(String[] args){
		System.out.println("This is the main method!\n");
		
		// NOTE 1: We do NOT want to create a new instance of the following Matrices class EVERY time
		//         we create a new instance of Recommendation class. I have simply done this for
		//		   stand-alone code.
		// NOTE 2: The parameters passed in are names of JSON files that we receive
				// after performing 3 GETs into Team 10's database (1 for each JSON)
		Matrices M = new Matrices("Deals.json", "Members.json", "Transactions.json");
		
		// create an instance of Member class
		// NOTE: The first parameter passed in is the name of the JSON we receive from Proximity, 
		//		   and has been hard-coded for the time-being
		Member member = new Member("FromProximity.json", M);
	}
	
	/**
	 * Not sure if we need this method for this class.
	 * This depends on how we fit everything together.
	 */
	public static void SendDeals(){}
	
}
