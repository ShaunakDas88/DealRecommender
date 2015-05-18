import java.util.*;
//for file processing
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
// forJSON processing
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
// for matrices
import org.apache.commons.math3.linear.*;

public class Member {	
	// data fields for this class
	public static String member_id;
	public static ArrayList<String> vendors = new ArrayList<String>();
	public static ArrayList<String> new_deals = new ArrayList<String>();		
	public static ArrayList<String> unredeemed_deals = new ArrayList<String>();
	public static ArrayList<String> by_preference = new ArrayList<String>();
	public static ArrayList<String> by_personality = new ArrayList<String>();
	
	/**
	 * This is the constructor for the Member class.
	 * This will store all pertinent recommendation for a given member
	 * who is within distance of a list of nearby vendors.
	 * 
	 * @param fromProximity: String of json filename passed to us from Proximity team 
	 * @param M: instance of Matrices class
	 */
	public Member(String fromProximity, Matrices M){
	  System.out.println("This is the constructor for Member class. Initializing data fields!");	
	  
	  // initialize data fields via JSON Parsing
	  try{
    	  // this is JSON processing
		  JSONObject proxJSON;
		  JSONArray proxArray;
		  JSONArray nearby;
		  int num_vendors;
	      
		  JSONParser parser = new JSONParser();
    	  Object object = parser.parse(new FileReader(fromProximity));
    	  proxJSON = (JSONObject)object;
    	  proxArray = (JSONArray)proxJSON.get("items");
    	  proxJSON = (JSONObject)proxArray.get(0);
    	  member_id = proxJSON.get("member_id").toString();
    	  nearby = (JSONArray)proxJSON.get("vendors");
    	  num_vendors = nearby.size();
    	  // setting the vendors data field
    	  for(int i = 0; i < num_vendors; i++){
    		  vendors.add(nearby.get(i).toString());
    	  }    	  
      }
      catch (FileNotFoundException e) {
  		e.printStackTrace();
      }
  	  catch (IOException e) {
  		e.printStackTrace();
  	  } 
      catch (ParseException e) {
  		e.printStackTrace();
  	  }
      
      // SEE NOTES IN THE APPROPRIATE METHODS
	  //GetNewDeals(M.D);
	  //GetUnredeemedDeals();
	  
      // this method will initialize the by_preference data field
      RecommendByPreference(M);
      //System.out.println(M.deals_map);
      //System.out.println(by_preference);
      
	}
	
	/**
	 * This method will fill the new_deals data field
	 * of the class with appropriate information
	 * 
	 */
	private static void GetNewDeals(){
		System.out.println("\nThis is GetNewDeals method.\n");
	
		/*NOTE: Looking at Team 10 database, it is possible we do not have
		to write this method, since we can query by the fields sent and
		vendor_id. New deals will be those which have never been sent
		and which have vendor_id velonging in the list Proximity gives us. 
		
		Our only work MAY simply involve parsing the JSON that is returned
		to us from Team 10 when we make this query, and ranking these new deals.
		This may involve a call to this method from an instance of Recommendation
		class, in which case we do not make this method private, and we pass
		it in the appropriate JSON (from Team 10) as a parameter.
		*/
	};
	
	
	/**
	 * This method will fill the unredeemed_deals
	 * data field of the class with appropriate information
	 */
	private static void GetUnredeemedDeals(){
		System.out.println("This is teh GetUnredeemedDeals method.\n");
	
		/*NOTE: Looking at Team 10 database, it is possible we do not have
		to write this method, since we can query by the fields sent and
		vendor_id. New deals will be those which have never been sent
		and which have vendor_id velonging in the list Proximity gives us. 
		
		Our only work MAY simply involve parsing the JSON that is returned
		to us from Team 10 when we make this query, and ranking these 
		unredeemed deals. This may involve a call to this method from an instance 
		of Recommendation class, in which case we do not make this method private, 
		and we pass it in the appropriate JSON (from Team 10) as a parameter.*/	
	};
	
	
	/**
	 * This method will take in the transactions matrix, make sure
	 * that enough sample data points have been obtained for each
	 * member/column. 
	 * 
	 * If there have been, we use the matrix Pref = DC, and if not, we use
	 * the matrix Pref = DJ.
	 * 
	 * We then use HashMap members_map (of class Matrices that is passed
	 * into the constructor of this class) in order to figure out which
	 * column of Pref corresponds to our member of interest.
	 * 
	 * We then get the indices of this column in a list, ordered by
	 * the corresponding entries in the column being put in descending order.
	 * 
	 * Last, we use the deals_map of the Matrices class to fill up by_preference
	 * array with deal_id's in the correct order.
	 * 
	 * This will initialize the following data field of the class:
	 * - by_preference
	 */
	private static void RecommendByPreference(Matrices M){
		System.out.println("\nThis is the RecommendByPreference method.\n");
	
		// go through all rows/users, we make sure they have enough ( > 10) non-zero entries.
		// A non-zero entry in a column corresponds to a sample point for that user, since it
		// it means they were sent the deal (corresponding to the row), and either accepted or
		// rejected that deal.
		
		boolean enough_points = true;	// sample points flag
		int num_members = M.D.getColumnDimension();
		int num_deals = M.D.getRowDimension();
		//System.out.println(num_deals);
		//System.out.println(num_members);
		int curr_points = 0;
		RealMatrix Pref;
		RealVector curr_column;
		int max_index;
		int[] descending_indices = new int[num_deals];	
		// this will store list of indices, where values at these indices are in DESCENDING order
		
		// loop through columns of M.D, checking quantity of sample points for each user
		for(int j = 0; j < num_members; j++){
			if(enough_points == false){
				//System.out.printf("Not enough sample points for column %d.\n", j);
				break;
			}
			// the case that we haven't run into user with too few sample points
			else{
				curr_column = (M.D).getColumnVector(j);
				//System.out.println(curr_column);
				// go through the rows of column j, tally up non-zero entries/sample points
				for(int i = 0; i < num_deals; i++){
					if(M.D.getEntry(i,j) != 0){
						curr_points = curr_points+1;
					}
				}
				//System.out.println(curr_points);
				// NOTE: the value 5 has been chosen arbitrarily for right now
				if(curr_points < 5){
					enough_points = false;	//set flag to false, will break out of loop
				} //close if
				else{
					curr_points = 0;	//re-set to 0
				} //close else
			}
	    } //close for loop
		
		// if there are enough sample points per user, use correlation matrix
		if(enough_points == true){
			Pref = M.DC;	
		}
		// if NOT enough sample points per user, use the Jaccard-similarity matrix
		else{
			Pref = M.DJ;
		}
		
		// use the HashMap to locate column for our member, and then obtain that column vector
		int column_index = (int)M.members_map.get(member_id.toString());
		curr_column = Pref.getColumnVector(column_index);
		//System.out.println(curr_column);
		
		// Order the obtained column in descending order, get appropriate ordered indices
		for(int i = 0; i < num_deals; i++){
			max_index = curr_column.getMaxIndex();
			//System.out.println(max_index);
			descending_indices[i] = max_index; //ading this index into ArrayList
			curr_column.addToEntry(max_index, -(i+5*num_members+1)); // I will explain this value later 
		} 
		// now fill up the by_preference data field
		int n = descending_indices.length;
		
		// now get corresponding deal_id's using deals_list data field of M
		for(int i = 0; i < n; i++){
			by_preference.add(M.deals_list[descending_indices[i]]);
		}
		//System.out.println(by_preference);
	}
	
	/**
	 * TO DO: This involves the bigfive 
	 */
	private static void RecommendByPersonality(){	
		//System.out.println("\nThis is the RecommendByPersonality method.");
	};
}