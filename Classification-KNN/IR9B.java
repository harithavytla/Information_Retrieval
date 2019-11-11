// IR9B.java CS5154/6054 2019 Cheng
// kNN classification
// Usage: java IR9B k

import java.io.*;
import java.util.*;

public class IR9B{

  static final int numberOfClasses = 2;
  static final int numberOfPrecomputedLogTfs = 100;
  int[] numberOfDocs = new int[numberOfClasses];
  int[] numberOfTerms = new int[numberOfClasses];
  int[] Nc = new int[numberOfClasses];
  double[] precomputedLogTfs = new double[numberOfPrecomputedLogTfs];
  int N = 0;  int M = 0;  // number of training and number of testing
  ArrayList<HashMap<String, HashMap<Integer, Integer>>> indexes = 
     new ArrayList<HashMap<String, HashMap<Integer, Integer>>>(numberOfClasses);
  boolean[] training0 = null;  boolean[] training1 = null;
  double[] docLen0 = null; double[] docLen1 = null;
  int nct = 0; 
  HashMap<String, Double> idfs = new HashMap<String, Double>();
  HashMap<Integer, Double> training = new HashMap<Integer, Double>();
  int[] classifiedTo0 = null;  int[] classifiedTo1 = null;
  double[][] sim00 = null;  double[][] sim01 = null;
  double[][] sim10 = null;  double[][] sim11 = null;
	// simxy[i][j] is the cosine similarity between test sample i in class x
	// and training sample j in class y
	// will be used for ranking all training docs against i 
	// and thus i does not have to be normalized

  void readInvertedIndexWithTfs(String filename, int c){
 	Scanner in = null;  
    	try {
      		in = new Scanner(new File(filename));
    	} catch (FileNotFoundException e){
      		System.err.println("not found");
      		System.exit(1);
    	}
    	String[] tokens = in.nextLine().split(" ");
    	numberOfTerms[c] = Integer.parseInt(tokens[0]);
    	numberOfDocs[c] = Integer.parseInt(tokens[1]);
	HashMap<String, HashMap<Integer, Integer>> index = new
		HashMap<String, HashMap<Integer, Integer>>(numberOfTerms[c]);
    	for (int i = 0; i < numberOfTerms[c]; i++){
		String[] terms = in.nextLine().split(" ");
		int df = terms.length / 2;  // document frequency
		HashMap<Integer, Integer> postings = new HashMap<Integer, Integer>(df);
		for (int j = 0; j < df; j++) 
			postings.put(Integer.parseInt(terms[2 * j + 1]), 
				Integer.parseInt(terms[2 * j + 2]));
		index.put(terms[0], postings);
	}
    	in.close();
	indexes.add(index);
  }

  void selectTrainingSets(double proportion){
	training0 = new boolean[numberOfDocs[0]];
	training1 = new boolean[numberOfDocs[1]];
	Random random = new Random();
	for (int i = 0; i < numberOfDocs[0]; i++)
		training0[i] = random.nextDouble() < proportion;
	for (int i = 0; i < numberOfDocs[1]; i++)
		training1[i] = random.nextDouble() < proportion;
	Nc[0] = 0;  Nc[1] = 0;  
	for (int i = 0; i < numberOfDocs[0]; i++) if (training0[i]) Nc[0]++; 
	for (int i = 0; i < numberOfDocs[1]; i++) if (training1[i]) Nc[1]++; 
	N = Nc[0] + Nc[1];
	M = numberOfDocs[0] + numberOfDocs[1] - N;
	System.out.println(Nc[0] + " " + Nc[1] + " " + N);
	System.out.println(numberOfDocs[0] + " " + numberOfDocs[1] + " " + M);
  }

  void idf(){
	indexes.get(0).forEach((x, y) -> {
		nct = 0;
		y.forEach((u, v) -> { if (training0[u]) nct++; });
		if (nct > 0) if (idfs.containsKey(x)) idfs.put(x, idfs.get(x) + nct);
			else idfs.put(x, (double)nct); });
	indexes.get(1).forEach((x, y) -> {
		nct = 0;
		y.forEach((u, v) -> { if (training1[u]) nct++; });
		if (nct > 0) if (idfs.containsKey(x)) idfs.put(x, idfs.get(x) + nct);
			else idfs.put(x, (double)nct); });
	double logN = Math.log10((double)N);
	idfs.forEach((x, y) -> idfs.put(x, logN - Math.log10(y)));
	System.out.println(idfs.size());
  }

  void documentLengths(){
	docLen0 = new double[numberOfDocs[0]];
	docLen1 = new double[numberOfDocs[1]];
    	for (int i = 1; i < numberOfPrecomputedLogTfs; i++)
      		precomputedLogTfs[i] = 1.0 + Math.log10((double)i);
	for (int i = 0; i < numberOfDocs[0]; i++) docLen0[i] = 0;
	for (int i = 0; i < numberOfDocs[1]; i++) docLen1[i] = 0;
	idfs.forEach((x, idf) -> {
		if (indexes.get(0).containsKey(x))
			indexes.get(0).get(x).forEach((docID, tf) -> {
				double w = tf < numberOfPrecomputedLogTfs ?
					precomputedLogTfs[tf] :
					Math.log10((double)tf) + 1.0;
				w *= idf;
				docLen0[docID] += w * w;
			});
		if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach((docID, tf) -> {
				double w = tf < numberOfPrecomputedLogTfs ?
					precomputedLogTfs[tf] :
					Math.log10((double)tf) + 1.0;
				w *= idf;
				docLen1[docID] += w * w;
			});
	});
	for (int i = 0; i < numberOfDocs[0]; i++) docLen0[i] = Math.sqrt(docLen0[i]);
	for (int i = 0; i < numberOfDocs[1]; i++) docLen1[i] = Math.sqrt(docLen1[i]);
  }
	
  void cosineScores(){  // You need to do the same to sim01 and sim11.
	sim00 = new double[numberOfDocs[0]][numberOfDocs[0]];
	for (int i = 0; i < numberOfDocs[0]; i++)
		for (int j = 0; j < numberOfDocs[0]; j++) sim00[i][j] = 0;
	sim10 = new double[numberOfDocs[1]][numberOfDocs[0]];
	for (int i = 0; i < numberOfDocs[1]; i++)
		for (int j = 0; j < numberOfDocs[0]; j++) sim10[i][j] = 0;
	idfs.forEach((x, idf) -> {
		if (indexes.get(0).containsKey(x)){
			training.clear();
			indexes.get(0).get(x).forEach((u, tf) -> {
				if (training0[u]){
					double w = tf < numberOfPrecomputedLogTfs ?
						precomputedLogTfs[tf] :
						Math.log10((double)tf) + 1.0;
					training.put(u, w * idf);}});
			indexes.get(0).get(x).forEach((u, tf) -> {
				if (!training0[u]){
					double w = tf < numberOfPrecomputedLogTfs ?
						precomputedLogTfs[tf] :
						Math.log10((double)tf) + 1.0;
					training.forEach((u2, w2) ->
						sim00[u][u2] += w * idf * w2);
				}
			});
			if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach((u, tf) -> {
				if (!training1[u]){
					double w = tf < numberOfPrecomputedLogTfs ?
						precomputedLogTfs[tf] :
						Math.log10((double)tf) + 1.0;
					training.forEach((u2, w2) ->
						sim10[u][u2] += w * idf * w2);
				}
			});
		}
	});
	for (int i = 0; i < numberOfDocs[0]; i++) if (!training0[i])
		for (int j = 0; j < numberOfDocs[0]; j++) if (training0[j])
			sim00[i][j] /= docLen0[j];
	for (int i = 0; i < numberOfDocs[1]; i++) if (!training1[i])
		for (int j = 0; j < numberOfDocs[0]; j++) if (training0[j])
			sim10[i][j] /= docLen0[j];

  // copy and paste the above part of cosineScores here
  // and modify it for sim10 and sim11
	
	sim01 = new double[numberOfDocs[0]][numberOfDocs[1]];
	for (int i = 0; i < numberOfDocs[0]; i++)
		for (int j = 0; j < numberOfDocs[1]; j++) sim01[i][j] = 0;
	sim11 = new double[numberOfDocs[1]][numberOfDocs[1]];
	for (int i = 0; i < numberOfDocs[1]; i++)
		for (int j = 0; j < numberOfDocs[1]; j++) sim11[i][j] = 0;
	idfs.forEach((x, idf) -> {
		if (indexes.get(0).containsKey(x)){
			training.clear();
			indexes.get(0).get(x).forEach((u, tf) -> {
				if (training0[u]){
					double w = tf < numberOfPrecomputedLogTfs ?
						precomputedLogTfs[tf] :
						Math.log10((double)tf) + 1.0;
					training.put(u, w * idf);}});
			indexes.get(0).get(x).forEach((u, tf) -> {
				if (!training0[u]){
					double w = tf < numberOfPrecomputedLogTfs ?
						precomputedLogTfs[tf] :
						Math.log10((double)tf) + 1.0;
					training.forEach((u2, w2) ->
						sim01[u][u2] += w * idf * w2);
				}
			});
			if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach((u, tf) -> {
				if (!training1[u]){
					double w = tf < numberOfPrecomputedLogTfs ?
						precomputedLogTfs[tf] :
						Math.log10((double)tf) + 1.0;
					training.forEach((u2, w2) ->
						sim11[u][u2] += w * idf * w2);
				}
			});
		}
	});
	for (int i = 0; i < numberOfDocs[0]; i++) if (!training0[i])
		for (int j = 0; j < numberOfDocs[1]; j++) if (training1[j])
			sim01[i][j] /= docLen1[j];
	for (int i = 0; i < numberOfDocs[1]; i++) if (!training1[i])
		for (int j = 0; j < numberOfDocs[1]; j++) if (training1[j])
			sim11[i][j] /= docLen1[j];

  }

  void ApplykNN(int k){  // You need to add code for classifiedTo1
	TreeMap<Double, Integer> sorted = new TreeMap<Double, Integer>();
	classifiedTo0 = new int[numberOfDocs[0]];
	for (int i = 0; i < numberOfDocs[0]; i++) if (!training0[i]){
		sorted.clear();
		for (int j = 0; j < numberOfDocs[0]; j++) if (training0[j])
			sorted.put(sim00[i][j] + j * 0.00001, 0);
		for (int j = 0; j < numberOfDocs[1]; j++) if (training1[j])
			sorted.put(sim01[i][j] + (j + numberOfDocs[0]) * 0.00001, 1);
		int c0 = 0; int c1 = 0;
		for (int j = 0; j < k; j++)
			if (sorted.pollLastEntry().getValue() == 0) c0++;
			else c1++;
		classifiedTo0[i] = c0 < c1 ? 1 : 0;
	}
   // copy and paste the above code for ApplykNN and modify it for classifiedTo1
	TreeMap<Double, Integer> sorted1 = new TreeMap<Double, Integer>();
	classifiedTo1 = new int[numberOfDocs[1]];
	for (int i = 0; i < numberOfDocs[1]; i++) if (!training1[i]){
		sorted1.clear();
		for (int j = 0; j < numberOfDocs[0]; j++) if (training0[j])
			sorted1.put(sim10[i][j] + j * 0.00001, 0);
		for (int j = 0; j < numberOfDocs[1]; j++) if (training1[j])
			sorted1.put(sim11[i][j] + (j + numberOfDocs[0]) * 0.00001, 1);
		int c0 = 0; int c1 = 0;
		for (int j = 0; j < k; j++)
			if (sorted1.pollLastEntry().getValue() == 0) c0++;
			else c1++;
		classifiedTo1[i] = c0 < c1 ? 1 : 0;
	}
  }
		 
  void accuracy(){  // Now we use the result of ApplykNN
	// You need to change ??? to some code for this to work
	// You need to output a single accuracy for the test data
	int TP = 0; int FP = 0; int FN = 0; int TN = 0;
	for (int i = 0; i < numberOfDocs[0]; i++) if (!training0[i])
		if (classifiedTo0[i] == 0) TP++; else FN++;
	for (int i = 0; i < numberOfDocs[1]; i++) if (!training1[i])
		if (classifiedTo1[i] ==0) FP++; else TN++;
	System.out.println(TP + " " + FP + " " + FN + " " + TN);
	
	double accuracy= (double)(TP + TN)/(TP + TN + FP + FN);
	System.out.println("Accuracy of the Given Training Data is" +" " +accuracy);
  }

public static void main(String[] args){
   if (args.length < 1){
	System.err.println("Usage: java IR9B k");
	System.exit(1);
   }
   IR9B ir9 = new IR9B();
   ir9.readInvertedIndexWithTfs("adInvertedTf.txt", 0);
   ir9.readInvertedIndexWithTfs("isrInvertedTf.txt", 1);
   ir9.selectTrainingSets(0.6);
   ir9.idf();
   ir9.documentLengths();
   ir9.cosineScores();
   ir9.ApplykNN(Integer.parseInt(args[0]));
   ir9.accuracy();
 }
}

