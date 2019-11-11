// IR6A.java CS5154/6054 2019 Cheng
// Read in an inverted index
// randomly generate a query of a certain number (4, which can be changed) number of words
// rank tops docs based on simple BIM with p_t = 0.5 and u_t = df_t/N
// Usage: java IR4A adInvertedTf.txt ad14.txt
//        java IR4A isrInvertedTf.txt isr4.txt

import java.io.*;
import java.util.*;

public class IR6A{

  static final int tops = 10;
  static final int queryLength = 5;
  int numberOfDocs = 0;
  int numberOfTerms = 0;
  HashMap<String, HashSet<Integer>> index = new HashMap<String, HashSet<Integer>>();
  HashSet<Integer> query = new HashSet<Integer>();
  String[] titles = null; // read in
  String[] termArray = null;
  double[] RSVs = null;
  double logN = 0;

 void readInvertedIndexWithTfs(String filename){
 	Scanner in = null;  
    	try {
      		in = new Scanner(new File(filename));
    	    } 
	catch (FileNotFoundException e){
      		System.err.println("not found");
      		System.exit(1);
    	}
    	String[] tokens = in.nextLine().split(" ");
    	numberOfTerms = Integer.parseInt(tokens[0]);
    	numberOfDocs = Integer.parseInt(tokens[1]);
    	logN = Math.log10((double)numberOfDocs);
 	termArray = new String[numberOfTerms];
   	for (int i = 0; i < numberOfTerms; i++){
		String[] terms = in.nextLine().split(" ");
		int df = terms.length / 2;  // document frequency
		HashSet<Integer> postings = new HashSet<Integer>(df);
		for (int j = 0; j < df; j++) 
			postings.add(Integer.parseInt(terms[2 * j + 1]));
		index.put(terms[0], postings);
		termArray[i] = terms[0];
	}
    	in.close();
  }

 void readTitles(String filename){
    Scanner in = null;  
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
      System.err.println("not found");
      System.exit(1);
    }
    titles = new String[numberOfDocs];
    for (int i = 0; i < numberOfDocs; i++){
      titles[i] = in.nextLine();
      in.nextLine(); in.nextLine();
    }
    in.close();
  }

 void generateQuery(){
	Random random = new Random();
	for (int i = 0; i < queryLength; i++) query.add(random.nextInt(numberOfTerms));
  }

 void BIM(){  
	RSVs = new double[numberOfDocs];
   	for (int i = 0; i < numberOfDocs; i++) RSVs[i] = 0;
	query.forEach(x -> {
		HashSet<Integer> m = index.get(termArray[x]);
		double idf = logN - Math.log10((double)(m.size()));
		System.out.println(termArray[x] + " " + idf);
		m.forEach(u -> RSVs[u] += idf);
        	});
 }

  void rankDocs(){
	TreeMap<Double, Integer> sorted = new TreeMap<Double, Integer>();
	for (int i = 0; i < numberOfDocs; i++) if (RSVs[i] > 0)
		sorted.put(RSVs[i] + (i * 0.0000001), i);
	for (int j = 0; j < tops; j++){
		int doc = sorted.pollLastEntry().getValue();
		System.out.println(titles[doc] + " " + RSVs[doc]);
	}
  }	 

 public static void main(String[] args){
   if (args.length < 2){
	System.err.println("Usage: java IR6A invertedTf collection");
	System.exit(1);
   }
   IR6A ir6 = new IR6A();
   ir6.readInvertedIndexWithTfs(args[0]);
   ir6.readTitles(args[1]);
   ir6.generateQuery();
   ir6.BIM();
   ir6.rankDocs();
 }
}


      