//for file processing
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
//for JSON parsing
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
// for statistics
import org.apache.commons.*;
import org.apache.commons.math3.stat.correlation.*;
// for matrices
import org.apache.commons.math3.linear.*;

public class Matrices{ 
   
   // data fields
   public static int num_deals;		// number of rows for transaction matrix
   public static int num_members;	// number of columns for transaction matrix
   
   // will store aprropriate JSON filenames
   
   // maps will be used for matrix navigation
   public static Map deals_map = new HashMap();		// know which row is which deal
   public static Map members_map = new HashMap();	// know which column is which deal
   public static String[] deals_list;				// need this since maps only work in key->value direction
   // matrices that will be filled
   public static RealMatrix F = new Array2DRowRealMatrix(new double[][]{{0,0},{0,0}}); // need initial matrix to use createMatrix method
   public static RealMatrix D;	// this is the transaction matrix
   public static RealMatrix J;	// this is the Jaccard-similarity matrix
   public static RealMatrix P;	// this is the Personality-similarity matrix
   public static RealMatrix C;	// this is the correlation-coefficient matrix
   public static RealMatrix DJ;	// this is a product of matrices D and J
   public static RealMatrix DP;	// this is a product of matrices D and P
   public static RealMatrix DC; // this is a product of matrices D and C
   
   
   /**
    * Constructor for the LoadData class
    */
   public Matrices(String deals_file, String members_file, String transactions_file){
	  System.out.println("This is the constructor for the Matrices class.\n");
	   
	  // call to method for deal tables
      ProcessDeals(deals_file);
      
      // call to method for members table; will initialize J and P appropriately
      ProcessMembers(members_file);
      
      // initialized dimensions of D appropriately
      D = F.createMatrix(num_deals, num_members);
      // call to method to fill transaction matrix D
      ProcessTransactions(transactions_file);
      //System.out.println("Entry in matrix D:");
      //System.out.println(D.getEntry(0,8)); // This for testing D was correctly filled
      
      // initialize dimensions of C appropriately
      C = F.createMatrix(num_members, num_members);
      // fill correlation matrix      
      FillCorrelation();
      //System.out.println(C.getEntry(6,6)); //this for testing C was correctly filled
      
      // do matrix multiplications, store these for ranking
      DJ = D.multiply(J);
      //DP = D.multiply(P);
      DC = D.multiply(C);  
      
      System.out.println("All matrices have been initialized...\n");
   }
   
   
   /**
    * This method takes in a filename corresponding to Deals JSON,
    * and processes it appropriately.
    * 
    * Sets the following two data fields of the class:
    * 	num_deals
    *   deals_map  
    *   
    *   @param filename (String)
    */
   private static void ProcessDeals(String filename) { 
      //System.out.println("\nThis is the ProcessDeals method.");   
      //System.out.printf("Processing the following JSON: %s\n", filename);
      JSONObject curr_deal;
	  
      try{
    	  // this is JSON processing
    	  JSONParser parser = new JSONParser();
    	  Object object = parser.parse(new FileReader(filename));
    	  JSONObject dealsJSON = (JSONObject) object;
    	  JSONArray deals_array = (JSONArray)dealsJSON.get("items");
    	  num_deals = deals_array.size();
    	  deals_list = new String[num_deals];	//initialize length
    	  
    	  // fill up the HashMap and deals_list
    	  for(int i = 0; i < num_deals; i++){
    		  curr_deal = (JSONObject)deals_array.get(i);
    		  deals_map.put(curr_deal.get("deal_id"), i);
    		  deals_list[i] = curr_deal.get("deal_id").toString();
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
   }
   
   /**
    * This private method takes in a filename corresponding to a Members JSON,
    * and processes it appropriately.  
    * 
    * Sets the following data fields of the class:
    *  num_members	
    *  members_map		
    *  Jaccard matrix J (via calls to JaccardIndex helper method)
    *  Personality matrix P (DO THIS ONE LATER!)
    */
   private static void ProcessMembers(String filename) { 
      //System.out.println("\nThis is the ProcessMembers method.");   
      //System.out.printf("Processing the following JSON: %s\n", filename);
      JSONObject curr_member;
      int row;					// get the correct row of matrix J or P
      int column;				// get the correct column of matrix J or P
      float curr_value;			// this will store the value to be placed inside an entry of matrix J or P
      JSONObject curr_member1;	// this is for filling matrices
      JSONObject curr_member2;  // this is for filling matrices
      JSONArray preference1;	// this is for filling matrix J
      JSONArray preference2;	// this is for filling matrix J
      JSONObject bigfive1;		// this is for filling matrix D
      JSONObject bigfive2;		// this is for filling matrix D
      
      try{
    	  // this is JSON processing
    	  JSONParser parser = new JSONParser();
    	  Object object = parser.parse(new FileReader(filename));
    	  JSONObject membersJSON = (JSONObject) object;
    	  JSONArray members_array = (JSONArray)membersJSON.get("items");
    	  num_members = members_array.size();
    	  
    	  // fill up the HashMap
    	  for(int i = 0; i < num_members; i++){
    		  curr_member = (JSONObject)members_array.get(i);
    		  //System.out.println(curr_member);
    		  members_map.put(curr_member.get("user_id").toString(), i);
    	  }
    	  
    	  // set dimensions of matrices J and D; these will eventually be a symmetric matrices
    	  J = F.createMatrix(num_members, num_members);
    	  P = F.createMatrix(num_members, num_members);
    	  
    	  // fill up the two similarities matrices J and P
    	  for(int i = 0; i < num_members; i++){
    		  curr_member1 = (JSONObject)members_array.get(i);
    		  row = (int)members_map.get(curr_member1.get("user_id").toString()); // get the right row using HashMap
    		  //System.out.println("Current row:");
    		  //System.out.println(row);
    		  //System.out.println(curr_member1.get("preferences"));
    		  preference1 = (JSONArray)curr_member1.get("preferences");
    		  for(int j = 0; j < num_members; j++){
    			  //System.out.println("Current column");
    			  curr_member2 = (JSONObject)members_array.get(j);
    			  column = (int)members_map.get(curr_member2.get("user_id").toString()); //get the right column using HashMap
    			  //System.out.println(column);
    			  preference2 = (JSONArray)curr_member2.get("preferences");
    			  //System.out.println(preference1);
    			  //System.out.println(preference1.get(0));
    			  //System.out.println(preference2);
    			  
    			  // computation of Jaccard index between members i and j, using helper method
    			  curr_value = JaccardIndex(preference1, preference2);
    			  //System.out.println("The return value from JaccardIndex method: ");
    			  //System.out.println(curr_value);
    			  //System.out.println();  			  
    			  J.addToEntry(row, column,  curr_value); // set entry of matrix J to this curr_value
    			  
    			  // computation of Personality-similarity index between members i and j, using helper method
    			  // TO DO LATER! RIGHT NOW, WE HAVE JACCARD WORKING.
    		   }    		  
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
   }
   
   /**
    * This helper method takes in two JSONArray objects as
    * parameters, converts them to Set objects, computes
    * the intersection and union of the sets, and then
    * forms the ratio of the size of these two quantities, and
    * returns this float value. 
    * 
    * @param array1 (JSONArray)
    * @param array2 (JSONArray)
    * @return Jaccard_index (float)
    */
   
   private static float JaccardIndex(JSONArray array1, JSONArray array2){
	   //System.out.println("\nThis is the JaccardIndex method.");
	   
	   float Jaccard_index;
	   Set<String> set1 = new HashSet<String>(array1);
	   Set<String> temp_set1 = new HashSet<String>(array1); // for union
	   Set<String> set2 = new HashSet<String>(array2);
	   //System.out.println(set1);
	   //System.out.println(set2);
	   set1.retainAll(set2);	// set1 is now intersection of array1 and array2
	   //System.out.println(set1);
	   set2.addAll(temp_set1); // set2 is now union of array1 and array2 
	   //System.out.println(set2);
	   // compute the Jaccard index
	   Jaccard_index = (float)set1.size()/(float)set2.size();
	   //System.out.println(Jaccard_index);
	   return Jaccard_index;
   }

   /**
    * This private method takes in a filename corresponding to a Transactions JSON,
    * and processes it appropriately. It will fill up the transactions matrix D 
    * with the appropriate star-ratings, using the two HashMap's deals_map and members_map
    * to locate appropriate rows and columns.
    * 
    *  @param filename (String)
    *  @return void
    */   
   private static void ProcessTransactions(String filename){
	   //System.out.println("\nThis is the ProcessTransactions method.");
	   //System.out.printf("Processing the following JSON: %s\n", filename);
       
	   JSONObject curr_object;	// this will store the current transaction information	
	   int row;					// which row to enter data
	   int column;				// which column to enter data
	   float curr_rating;			// will store the current transaction's rating value; THIS goes into matrix! 
	   
	   try{
	       // this is JSON processing
		   JSONParser parser = new JSONParser();
		   Object object = parser.parse(new FileReader(filename));
		   JSONObject transJSON = (JSONObject) object;
		   JSONArray trans_array = (JSONArray)transJSON.get("items");
		   
		   int num_trans = trans_array.size();

		   // loop thorugh all transactions in the JSON, fill up matrix D
		   for(int i = 0; i < num_trans; i++){
		     //obtain the column via the members_map HashMap
			 curr_object = (JSONObject)trans_array.get(i);
		     //System.out.println(curr_object);
		     column = (int)members_map.get(curr_object.get("user_id").toString());
		     //System.out.println(column);
		     
		     //obtain the row via the deals_map HashMap
		     row = (int)deals_map.get(curr_object.get("deal_id"));
		     //System.out.println(row);
		     
		     // obtain the rating of this current deal; must convert it to float!
		     curr_rating = Float.parseFloat(curr_object.get("stars").toString());
		     
		     // fill entry (row, column) of D with float value curr_rating
		     D.addToEntry(row,column, curr_rating);
		   } //closing for loop	   
	   } // closing try
       catch (FileNotFoundException e) {
    	   e.printStackTrace();
	   }
	   catch (IOException e) {
		   e.printStackTrace();
	   } 
	   catch (ParseException e) {
		   e.printStackTrace();
	   }
   }
   
   
   /**
    * This private method will compute the Correlation similarity matrix,
    * which is based on user transaction history. 
    */   
   private static void FillCorrelation(){
	   //System.out.println("\nThis is the Correlation method.");  
	   PearsonsCorrelation Pearson = new PearsonsCorrelation();
	   // compute Pearson correlation matrix for transaction matrix D
	   C = Pearson.computeCorrelationMatrix(D);
   }

} // closing of the LoadData class