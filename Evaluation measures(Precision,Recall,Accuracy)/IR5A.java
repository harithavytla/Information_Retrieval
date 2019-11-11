// IR5A.java CS5154/6054 2019 Cheng
// Read in an inverted index with Tfs
// randomly generate a query of a certain number (7, which can be changed) number of words
// rank tops docs based on tf-idf cosine similarity and also Jaccard coefficient
// then evaluate the Jaccard coefficient information retrieval against tf-idf
// Usage: java IR5A adInvertedTf.txt
//        java IR5A isrInvertedTf.txt

import java.io.*;
import java.util.*;
import java.text.*;

public class IR5A{

  static final int numberOfPrecomputedLogTfs = 100;
  static final int tops = 20;
  int numberOfDocs = 0;
  int numberOfTerms = 0;
  HashMap<String, HashMap<Integer, Integer>> index = 
	new HashMap<String, HashMap<Integer, Integer>>();
  String[] termArray = null;
  String[] titles = null; // read in
  double[] precomputedLogTfs = new double[numberOfPrecomputedLogTfs];
  double[] docLengths = null; // precomputed
  HashSet<Integer> query = new HashSet<Integer>();
  double[] scores = null;
  double logN = 0;
  int[] Y = null; // precomputed
  int[] XnY = null;
  HashSet<Integer> relevant = new HashSet<Integer>();
  HashSet<Integer> retrieved = new HashSet<Integer>();  
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


 void precompute(){
    	logN = Math.log10((double)numberOfDocs);
    	for (int i = 1; i < numberOfPrecomputedLogTfs; i++)
      		precomputedLogTfs[i] = 1.0 + Math.log10((double)i);
    	docLengths = new double[numberOfDocs];
    	for (int d = 0; d < numberOfDocs; d++) docLengths[d] = 0;
    	Y = new int[numberOfDocs];
    	for (int d = 0; d < numberOfDocs; d++) Y[d] = 0;
	index.forEach((t, y) -> y.forEach((d, tf) -> {  // tf is tf_td and y.size() is df_t
        	double w = tf < numberOfPrecomputedLogTfs ?
          		precomputedLogTfs[tf] : 1.0 + Math.log10((double)tf);
		double weight = w * (logN - Math.log10((double)(y.size())));
        	docLengths[d] += weight * weight; 
		Y[d]++;
	}));
    	for (int d = 0; d < numberOfDocs; d++) docLengths[d] = Math.sqrt(docLengths[d]);
 }


  void generateQuery(int queryLen){
	Random random = new Random();
	for (int i = 0; i < queryLen; i++) query.add(random.nextInt(numberOfTerms));
  	
  }

 void cosineScore(){  
	scores = new double[numberOfDocs];  // scores are not normalized until rankDocs()
   	for (int i = 0; i < numberOfDocs; i++) scores[i] = 0;
	query.forEach(t -> {
		HashMap<Integer, Integer> y = index.get(termArray[t]);
		double idf = logN - Math.log10((double)(y.size()));
		System.out.println("in cosine "+termArray[t] + " " + decimalf.format(idf));
		y.forEach((d, tf) -> {
        		double w = (tf < numberOfPrecomputedLogTfs) ?
          			precomputedLogTfs[tf] : 1.0 + Math.log10((double)(tf));
			double weight = w * idf;
			scores[d] += weight * weight; });
        	});
 }

 void intersections(){  
	XnY = new int[numberOfDocs];
   	for (int d = 0; d < numberOfDocs; d++) XnY[d] = 0;
	query.forEach(x -> {
		HashMap<Integer, Integer> m = index.get(termArray[x]);
		System.out.println("in intersections"+termArray[x]);
		m.forEach((d, v) -> XnY[d]++);
        	});
 }

  void rankDocs(){
	TreeMap<Double, Integer> sorted = new TreeMap<Double, Integer>();
	for (int i = 0; i < numberOfDocs; i++) if (scores[i] > 0)
		sorted.put(scores[i] / docLengths[i] + (i * 0.0000001), i);  // normalizing doc vectors but not query
	for (int j = 0; j < tops; j++) relevant.add(sorted.pollLastEntry().getValue());
	sorted.clear();
	for (int i = 0; i < numberOfDocs; i++) if (XnY[i] > 0){
		double JC = XnY[i]/ (Y[i] + query.size() - XnY[i]);// Your code for Jaccard coefficient  
		sorted.put(JC + (i * 0.0000001), i);
	}
	for (int j = 0; j < tops; j++) retrieved.add(sorted.pollLastEntry().getValue());
	System.out.println(retrieved.size()+"    "+retrieved);
	System.out.println(relevant.size()+"	"+relevant);
  }

  void evaluate(){
		int retSize=retrieved.size();
	HashSet<Integer> tempRetrieved = retrieved;  
	tempRetrieved.retainAll(relevant);
	System.out.println(tempRetrieved);
	int tp = tempRetrieved.size();
	
	System.out.println("tp="+tp);

	System.out.println(retSize);
	double fp =retSize -tp;
	double fn = relevant.size()-tp;
	System.out.println("fp is"+fp+" fn is"+fn);
	System.out.println("Precision = " + tp /(tp+fp));
	System.out.println("Recall = " + tp / (tp+fn));
  }	 

 public static void main(String[] args){
   if (args.length < 1)
   {
	System.err.println("Usage: java IR5A invertedTf");
	System.exit(1);
   }
   IR5A ir5 = new IR5A();
   ir5.readInvertedIndexWithTfs(args[0]);
   ir5.precompute();
   ir5.generateQuery(7);
   ir5.cosineScore();
   ir5.intersections();
   ir5.rankDocs();
   ir5.evaluate();
 }
}

      