// IR9A.java CS5154/6054 2019 Cheng
// Read in two inverted indexes with Tfs
// randomly select training set for each run
// compute dictionary and idfs
// follow TrainRocchio and use the training set to
// compute the two centers m0 and m1 and then w = m0 - m1
// make classification for the test docs and report accuracy
// Usage: java IR9A 

import java.io.*;
import java.util.*;

public class IR9A{

  static final int numberOfClasses = 2;
  static final int numberOfPrecomputedLogTfs = 100;
  int[] numberOfDocs = new int[numberOfClasses];
  int[] numberOfTerms = new int[numberOfClasses];
  int[] Nc = new int[numberOfClasses];
  double[] precomputedLogTfs = new double[numberOfPrecomputedLogTfs];
  int N = 0;
  ArrayList<HashMap<String, HashMap<Integer, Integer>>> indexes = 
     new ArrayList<HashMap<String, HashMap<Integer, Integer>>>(numberOfClasses);
  boolean[] training0 = null;  boolean[] training1 = null;
  double[] docLen0 = null; double[] docLen1 = null;
  ArrayList<HashMap<String, Double>> centroids = 
	new ArrayList<HashMap<String, Double>>(numberOfClasses);
  int nct = 0;  double centroidLength = 0;
  double b = 0;
  HashMap<String, Double> idfs = new HashMap<String, Double>();
  HashMap<String, Double> r = new HashMap<String, Double>();
  double[] scores0 = null;  double[] scores1 = null;
  int[] classifiedTo0 = null;  int[] classifiedTo1 = null;

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
	System.out.println(Nc[0] + " " + Nc[1] + " " + N);
	System.out.println(numberOfDocs[0] + " " + numberOfDocs[1]);
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
	

  void trainRocchio(int c, boolean[] training, double[] docLen){
	HashMap<String, Double> centroid = new HashMap<String, Double>();
	idfs.forEach((x, idf) -> {
		centroid.put(x, 0.0);
		if (indexes.get(c).containsKey(x))
			indexes.get(c).get(x).forEach((docID, tf) -> {
				if (training[docID]){
				double w = tf < numberOfPrecomputedLogTfs ?
					precomputedLogTfs[tf] :
					Math.log10((double)tf) + 1.0;
				centroid.put(x, centroid.get(x) + w / docLen[docID]);
				}
			});
		centroid.put(x, centroid.get(x) * idf);
	});
	centroid.forEach((x, y) -> centroid.put(x, centroid.get(x) / Nc[c]));
	centroids.add(centroid);
  }

  void computeRandB(){
	idfs.forEach((x, y) -> 
		r.put(x, centroids.get(0).get(x) - centroids.get(1).get(x)));
	centroids.get(1).forEach((x, y) -> b += y * y);
	centroids.get(0).forEach((x, y) -> b -= y * y);
	b *= 0.5;
  }

  void applyLinearClassifier(){
	scores0 = new double[numberOfDocs[0]];
	for (int i = 0; i < numberOfDocs[0]; i++) scores0[i] = 0;
	scores1 = new double[numberOfDocs[1]];
	for (int i = 0; i < numberOfDocs[1]; i++) scores1[i] = 0;
	r.forEach((x, y) -> {
		double idf = idfs.get(x);
		if (indexes.get(0).containsKey(x))
			indexes.get(0).get(x).forEach((docID, tf) -> {
				double w = tf < numberOfPrecomputedLogTfs ?
					precomputedLogTfs[tf] :
					Math.log10((double)tf) + 1.0;
				scores0[docID] += w * idf * y; });
		if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach((docID, tf) -> {
				double w = tf < numberOfPrecomputedLogTfs ?
					precomputedLogTfs[tf] :
					Math.log10((double)tf) + 1.0;
				scores1[docID] += w * idf * y; });
	});
  }

  void applyRocchio(){
	double[][] distances0 = new double[numberOfDocs[0]][numberOfClasses];
	double[][] distances1 = new double[numberOfDocs[1]][numberOfClasses];
	for (int i = 0; i < numberOfDocs[0]; i++) 
		distances0[i][0] = distances0[i][1] = 0;
	for (int i = 0; i < numberOfDocs[1]; i++) 
		distances1[i][0] = distances1[i][1] = 0;
	idfs.forEach((x, idf) -> {
		if (indexes.get(0).containsKey(x))
			indexes.get(0).get(x).forEach((docID, tf) -> {
				double w = tf < numberOfPrecomputedLogTfs ?
					precomputedLogTfs[tf] :
					Math.log10((double)tf) + 1.0;
				w *= idf;
				double d = w - centroids.get(0).get(x);
				distances0[docID][0] += d * d;
				d = w - centroids.get(1).get(x);
				distances0[docID][1] += d * d;
			});
		if (indexes.get(1).containsKey(x))
			indexes.get(1).get(x).forEach((docID, tf) -> {
				double w = tf < numberOfPrecomputedLogTfs ?
					precomputedLogTfs[tf] :
					Math.log10((double)tf) + 1.0;
				w *= idf;
				double d = w - centroids.get(0).get(x);
				distances1[docID][0] += d * d;
				d = w - centroids.get(1).get(x);
				distances1[docID][1] += d * d;
			});
	});
	classifiedTo0 = new int[numberOfDocs[0]];
	for (int i = 0; i < numberOfDocs[0]; i++)
		classifiedTo0[i] = distances0[i][0] <= distances0[i][1] ? 0 : 1;
	classifiedTo1 = new int[numberOfDocs[1]];
	for (int i = 0; i < numberOfDocs[1]; i++)
		classifiedTo1[i] = distances1[i][0] <= distances1[i][1] ? 0 : 1;
  }

  void accuracy(){  // based on the linear classifier
	int trainingTP = 0;  int testTP = 0;
	int trainingFP = 0;  int testFP = 0;
	int trainingFN = 0;  int testFN = 0;
	int trainingTN = 0;  int testTN = 0;
	for (int i = 0; i < numberOfDocs[0]; i++) if (scores0[i] + b >= 0)
			if (training0[i]) trainingTP++; else testTP++;
		else if (training0[i]) trainingFN++; else testFN++;
	for (int i = 0; i < numberOfDocs[1]; i++) if (scores1[i] + b >= 0)
			if (training1[i]) trainingFP++; else testFP++;
		else if (training1[i]) trainingTN++; else testTN++;
	System.out.println("Linear Classifier");
	//Sytem.out.println("Training Data");
	System.out.println("Training Data : Tp-" + trainingTP + " " + "Fp-" + trainingFP + " "
		+"Fn-" + trainingFN + " " + "Tn-" + trainingTN);
	System.out.println("Test Data : Tp-" + testTP + " " + "Fp-" + testFP + " "
		+"Fn-" + testFN + " " + "Tn-" + testTN);
  }

  void accuracy2(){  // Now we use the result of ApplyRocchio
	int trainingTP = 0;  int testTP = 0;
	int trainingFP = 0;  int testFP = 0;
	int trainingFN = 0;  int testFN = 0;
	int trainingTN = 0;  int testTN = 0;
	for (int i = 0; i < numberOfDocs[0]; i++) if (classifiedTo0[i] == 0 )
			if (training0[i]) trainingTP++; else testTP++;
		else if (training0[i]) trainingFN++; else testFN++;
	for (int i = 0; i < numberOfDocs[1]; i++) if (classifiedTo1[i] == 0 )
			if (training1[i]) trainingFP++; else testFP++;
		else if (training1[i]) trainingTN++; else testTN++;
	System.out.println("Rocchio Classification");
	System.out.println("Training Data : Tp-" + trainingTP + " " + "Fp-" + trainingFP + " "
		+"Fn-" + trainingFN + " " + "Tn-" + trainingTN);
	System.out.println("Test Data : Tp-" + testTP + " " + "Fp-" + testFP + " "
		+"Fn-" + testFN + " " + "Tn-" + testTN);
  }


 public static void main(String[] args){
   IR9A ir9 = new IR9A();
   ir9.readInvertedIndexWithTfs("adInvertedTf.txt", 0);
   ir9.readInvertedIndexWithTfs("isrInvertedTf.txt", 1);
   ir9.selectTrainingSets(0.6);
   ir9.idf();
   ir9.documentLengths();
   ir9.trainRocchio(0, ir9.training0, ir9.docLen0);
   ir9.trainRocchio(1, ir9.training1, ir9.docLen1);
   ir9.computeRandB();
   ir9.applyLinearClassifier();
   ir9.accuracy();
   ir9.applyRocchio();
   ir9.accuracy2();
 }
}

      