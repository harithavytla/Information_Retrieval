// IR4B.java CS5154/6054 2019 Cheng
// Read in an inverted index with Tfs
// randomly generate a query of a certain number (7, which can be changed) number of words
// rank tops docs based on tf-idf cosine similarity
// Usage: java IR4B adInvertedTf.txt ad14.txt
//        java IR4B isrInvertedTf.txt isr4.txt

import java.io.*;
import java.util.*;
import java.text.*;

public class IR4B{

  static final int numberOfPrecomputedLogTfs = 100;
  static final int tops = 10;
  int numberOfDocs = 0;
  int numberOfTerms = 0;
  HashMap<String, HashMap<Integer, Integer>> index = 
	new HashMap<String, HashMap<Integer, Integer>>();
  String[] termArray = null;
  String[] titles = null; // read in
  double[] precomputedLogTfs = new double[numberOfPrecomputedLogTfs];
  double[] docLengths = null; // precomputed
  double[] scores = null;
  double logN = 0;
  DecimalFormat decimalf = new DecimalFormat("#0.00");

 void readInvertedIndexWithTfs(String filename){
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
	termArray = new String[numberOfTerms];
    	for (int i = 0; i < numberOfTerms; i++){
		String[] terms = in.nextLine().split(" ");
		int df = terms.length / 2;  // document frequency
		HashMap<Integer, Integer> postings = new HashMap<Integer, Integer>(df);
		for (int j = 0; j < df; j++) 
			postings.put(Integer.parseInt(terms[2 * j + 1]), 
				Integer.parseInt(terms[2 * j + 2]));
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

 void precompute(){
    	logN = Math.log10((double)numberOfDocs);
    	for (int i = 1; i < numberOfPrecomputedLogTfs; i++)
      		precomputedLogTfs[i] = 1.0 + Math.log10((double)i);
    	docLengths = new double[numberOfDocs];
    	for (int i = 0; i < numberOfDocs; i++) docLengths[i] = 0;
	index.forEach((t, y) -> y.forEach((d, tf) -> {  // tf is tf_td and y.size() is df_t
        	double w = tf < numberOfPrecomputedLogTfs ?
          		precomputedLogTfs[tf] : 1.0 + Math.log10((double)tf);
		double weight = w * (logN - Math.log10((double)(y.size())));
        	docLengths[d] += weight * weight; }));
    	for (int i = 0; i < numberOfDocs; i++) docLengths[i] = Math.sqrt(docLengths[i]);
 }


  HashSet<Integer> generateQuery(int queryLen){
	HashSet<Integer> query = new HashSet<Integer>();
	Random random = new Random();
	for (int i = 0; i < queryLen; i++) query.add(random.nextInt(numberOfTerms));
  	return query;
  }

 void cosineScore(HashSet<Integer> query){  
	scores = new double[numberOfDocs];  // scores are not normalized until rankDocs()
   	for (int i = 0; i < numberOfDocs; i++) scores[i] = 0;
	query.forEach(t -> {
		HashMap<Integer, Integer> y = index.get(termArray[t]);
		double idf = logN - Math.log10((double)(y.size()));
		System.out.println(termArray[t] + " " + decimalf.format(idf));
		y.forEach((d, tf) -> {
        		double w = (tf < numberOfPrecomputedLogTfs) ?
          			precomputedLogTfs[tf] : 1.0 + Math.log10((double)(tf));
			double weight = w * idf;
			scores[d] += weight * weight; });
        	});
 }

  void rankDocs(){
	TreeMap<Double, Integer> sorted = new TreeMap<Double, Integer>();
	for (int i = 0; i < numberOfDocs; i++) if (scores[i] > 0)
		sorted.put(scores[i] / docLengths[i] + (i * 0.0000001), i);  // normalizing doc vectors but not query
	for (int j = 0; j < tops; j++){
		int doc = sorted.pollLastEntry().getValue();
		System.out.println(titles[doc] + " " + decimalf.format(scores[doc] / docLengths[doc]));
	}
  }	 

 public static void main(String[] args){
   if (args.length < 2){
	System.err.println("Usage: java IR4B invertedTf collection");
	System.exit(1);
   }
   IR4B ir4 = new IR4B();
   ir4.readInvertedIndexWithTfs(args[0]);
   ir4.precompute();
   ir4.readTitles(args[1]);
   ir4.cosineScore(ir4.generateQuery(7));
   ir4.rankDocs();
 }
}

      