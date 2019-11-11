// IR6B.java CS5154/6054 2019 Cheng
// BIM ranking with pseudo relevance feedback
// Read in an inverted index
// randomly generate a query of a certain number (4, which can be changed) number of words
// implementing 11.3.4 with (11.26) and (11.27)
// Usage: java IR4B adInvertedTf.txt ad14.txt
//        java IR4B isrInvertedTf.txt isr4.txt

import java.io.*;
import java.util.*;
import java.text.*;

public class IR6B{

  static final int tops = 5;
  static final int queryLength = 4;
  int numberOfDocs = 0; // N in (11.27)
  int numberOfTerms = 0;
  HashMap<String, HashSet<Integer>> index = new HashMap<String, HashSet<Integer>>();
  HashSet<Integer> query = new HashSet<Integer>();
  String[] titles = null; // read in
  String[] termArray = null;
  double[] RSVs = null;
  double logN = 0;
  double[] pt = null;
  double[] ut = null;
  int[] dfs = null;   // used in initial ut and update formula for ut (11.27)
  boolean changes = false;
  int V = 0;  // number of top docs or |V| in (11.26) and (11.27)
  HashSet<Integer> topDocs = null;
  DecimalFormat decimalf = new DecimalFormat("#0.000");

 void readInvertedIndex(String filename){ // Tfs not used
 	Scanner in = null;  
    	try {
      		in = new Scanner(new File(filename));
    	} catch (FileNotFoundException e){
      		System.err.println("not found");
      		System.exit(1);
    	}
    	String[] tokens = in.nextLine().split(" ");
    	numberOfTerms = Integer.parseInt(tokens[0]);
    	numberOfDocs = Integer.parseInt(tokens[1]);
    	logN = Math.log10((double)numberOfDocs);
 	termArray = new String[numberOfTerms];
	dfs = new int[numberOfTerms];
   	for (int i = 0; i < numberOfTerms; i++){
		String[] terms = in.nextLine().split(" ");
		dfs[i] = terms.length / 2;  // document frequency
		HashSet<Integer> postings = new HashSet<Integer>(dfs[i]);
		for (int j = 0; j < dfs[i]; j++) 
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

  void initializePU(){
    	pt = new double[numberOfTerms];
    	ut = new double[numberOfTerms];
    	for (int i = 0; i < numberOfTerms; i++){
       		pt[i] = 0.5;
       		ut[i] = (double)(dfs[i]) / numberOfDocs;
    	}
	topDocs = new HashSet<Integer>();
    	changes = true;
  }

  void updatePU(){
	for (int t = 0; t < numberOfTerms; t++){
      		int vt = 0;  // |Vt| in (11.26) and (11.27) 
		HashSet<Integer> m = index.get(termArray[t]);
		for (int d: m) if (topDocs.contains(d)) vt++;
	      pt[t] = (Math.abs(vt)+0.5)/(Math.abs(V)+1); //(11.26)
	      ut[t] = (dfs[t]-Math.abs(vt)+0.5)/(numberOfDocs-Math.abs(V)+1);//(11.27)
    	}
  }

 void BIMRSVs(){
   	for (int i = 0; i < numberOfDocs; i++) RSVs[i] = 0;
	query.forEach(t -> {
   		System.out.println(termArray[t] + " " + 
      			decimalf.format(pt[t]) + " " + 
      			decimalf.format(ut[t]));
     		double ct = Math.log(pt[t]) - Math.log(1.0 - pt[t])
           		+ Math.log(1.0 - ut[t]) - Math.log(ut[t]);  // (11.18)
		for (int d: index.get(termArray[t])) RSVs[d] += ct;
        	});
  }


  void rankDocs(){
	TreeMap<Double, Integer> sorted = new TreeMap<Double, Integer>();
	HashSet<Integer> newTopDocs = new HashSet<Integer>();
	for (int i = 0; i < numberOfDocs; i++) if (RSVs[i] > 0)
		sorted.put(RSVs[i] + (i * 0.0000001), i);
	for (int j = 0; j < tops; j++){ 
		if (sorted.isEmpty()) break;
		int doc = sorted.pollLastEntry().getValue();
		System.out.println(titles[doc] + " " + RSVs[doc]);
		newTopDocs.add(doc);
	}
	changes = !newTopDocs.equals(topDocs);
	if (changes) topDocs = newTopDocs;
	V = topDocs.size();
  }	 

 void pseudoRelevanceFeedback(){
	generateQuery();
	RSVs = new double[numberOfDocs];
     	initializePU();
     	int n = 0;
    	while (changes){
     		System.out.println("\nIteration " + ++n);
     		BIMRSVs();
     		rankDocs();
     		if (changes) updatePU();
    	}
   }

 public static void main(String[] args){
   if (args.length < 2){
	System.err.println("Usage: java IR6B invertedTf collection");
	System.exit(1);
   }
   IR6B ir6 = new IR6B();
   ir6.readInvertedIndex(args[0]);
   ir6.readTitles(args[1]);
   ir6.pseudoRelevanceFeedback();
 }
}

      