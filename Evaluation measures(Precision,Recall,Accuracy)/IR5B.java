// IR5B.java CS5154/6054 2019 Cheng
// Read in an inverted index with Tfs
// randomly generate a query of a certain number (4, which can be changed) number of words
// then evaluate the Jaccard coefficient ranking against the top 20 tf-idf retrieval as a the relevant set
// Usage: java IR5B adInvertedTf.txt
//        java IR5B isrInvertedTf.txt

import java.io.*;
import java.util.*;
import java.text.*;

public class IR5B{

  static final int numberOfPrecomputedLogTfs = 100;
  static final int tops = 20; // size of relevant set, or top tf-idf retrieved
  static final int tops2 = 40; // Jaccard ranking length
  static final int queryLength = 4;
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
  int[] ranked = new int[tops2];
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


  void generateQuery(){
	Random random = new Random();
	for (int i = 0; i < queryLength; i++) query.add(random.nextInt(numberOfTerms));
  }

 void cosineScore(){  
	scores = new double[numberOfDocs];  // scores are not normalized until rankDocs()
   	for (int i = 0; i < numberOfDocs; i++) scores[i] = 0;
	query.forEach(t -> {
		HashMap<Integer, Integer> y = index.get(termArray[t]);
		double idf = logN - Math.log10((double)(y.size()));
//		System.out.println(termArray[t] + " " + decimalf.format(idf));
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
		m.forEach((d, v) -> XnY[d]++);
        	});
 }

  void rankDocs(){
	TreeMap<Double, Integer> sorted = new TreeMap<Double, Integer>();
	for (int i = 0; i < numberOfDocs; i++) if (scores[i] > 0)
		sorted.put(scores[i] / docLengths[i] + (i * 0.0000001), i);  // normalizing doc vectors but not query
	for (int j = 0; j < tops; j++) relevant.add(sorted.pollLastEntry().getValue());
	sorted.clear();
	for (int d = 0; d < numberOfDocs; d++) if (XnY[d] > 0){
		double XuY = 4+Y[d]-XnY[d];
		double JC = XnY[d]/XuY;// Your code for Jaccard coefficient 
		sorted.put(JC + (d * 0.0000001), d);
	}
	//System.out.println(sorted);
	for (int j = 0; j < tops2; j++) ranked[j] = sorted.pollLastEntry().getValue();
  }

  void evaluate(){
	int tp = 0; 
	double MAP = 0;
	for (int j = 0; j < tops2; j++){
		int q=j+1;
		//System.out.println(ranked);
		if (relevant.contains(ranked[j])){
			tp++;
			MAP += tp / q ; // precision
		}
		//System.out.println(tp);
		System.out.println(decimalf.format(tp/(double)q) + "\t" + decimalf.format(tp/(double)20));
		// The ??? should be tp+fp for precision and tp+fn for recall
		// tp+fn is a constant (the size of the relevant), and tp+fp is the number of retrieved so far
	}
	MAP /= tp;
	System.out.println("MAP = " + decimalf.format(MAP));
  }	 

 public static void main(String[] args){
   if (args.length < 1){
	System.err.println("Usage: java IR5B invertedTf");
	System.exit(1);
   }
   IR5B ir5 = new IR5B();
   ir5.readInvertedIndexWithTfs(args[0]);
   ir5.precompute();
   ir5.generateQuery();
   ir5.cosineScore();
   ir5.intersections();
   ir5.rankDocs();
   ir5.evaluate();
 }
}

      