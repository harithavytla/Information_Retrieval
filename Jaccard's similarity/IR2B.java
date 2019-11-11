// IR2B.java CS5154/6054 2018 Cheng
// generate an inverted index of 3-grams for a dictionary
// type (misspelled) words until you enter an empty line
// words in dictionary with Jaccard coeff > 0.2 displayed for each
// Usage: java IR2A isaInverted.txt 
// Usage: java IR2A adInverted.txt 

// Design:  First build the inverted index for the gram/term matrix
// Given a query, find its set of grams, and its size is |X|
// for each gram in query, use the inverted index to generate |XnY| for a set of terms.
// This should be a HashMap<Integer, Integer> for accumulating counts.
// for each of these terms, find its |Y| and then |XuY| = |X| + |Y| -|XnY|
// Jaccard coefficient is |XnY|/|XuY|, be careful not to use integer division


import java.io.*;
import java.util.*;

public class IR2B{

  static final int gramLength = 3;
  int dictionarySize = 0;
  String[] dictionary = null;
  HashMap<String, HashSet<Integer>> index = new HashMap<String, HashSet<Integer>>();
  HashMap<Integer, Integer> XnY = new HashMap<Integer, Integer>(); 
  int X = 0; // query size

  void readDictionary(String filename){  // first column as dictionary
	Scanner in = null;
    	try {
      		in = new Scanner(new File(filename));
    	} catch (FileNotFoundException e){
      		System.err.println("not found");
      		System.exit(1);
    	}
	
	dictionarySize = Integer.parseInt(in.nextLine().split(" ")[0]);  
	dictionary = new String[dictionarySize];
	for (int n = 0; n < dictionarySize; n++){
		String line = in.nextLine();
		int pos = line.indexOf(' ');
		dictionary[n] = line.substring(0, pos);
		int numberOfGrams = dictionary[n].length() - gramLength + 1;
		if (numberOfGrams > 0)
			for (int i = 0; i < numberOfGrams; i++){
				String gram = dictionary[n].substring(i, i + gramLength);
				if (index.containsKey(gram))
					index.get(gram).add(n);
				else{
					HashSet<Integer> hset = new HashSet<Integer>();
					hset.add(n);
					index.put(gram, hset);
				}
			}
	}
	in.close();
  }

  HashSet<String> setOfGrams(String word){
	int numberOfGrams = word.length() - gramLength + 1;
	HashSet<String> hset = new HashSet<String>();
 	for (int j = 0; j < numberOfGrams; j++) 
		hset.add(word.substring(j, j + gramLength));
 	return hset;
  }	


 void correctSpellings(){  
   	Scanner in = new Scanner(System.in);
   	System.out.println("Enter a word for spelling correction.");
   	while (in.hasNextLine()){
     		String query = in.nextLine();
     		if (query.length() < gramLength) break;
     		computeXnY(query);
     		jaccard(query);
     		System.out.println("Enter a word for spelling correction or empty line for end.");
   	}
 }

 void computeXnY(String query){ 
	XnY.clear();
	HashSet<String> qgrams = setOfGrams(query);
	X = qgrams.size();
	qgrams.forEach(gram -> { if (index.containsKey(gram))
			index.get(gram).forEach(k -> {
				if (XnY.containsKey(k)) XnY.put(k, XnY.get(k) + 1);
				else XnY.put(k, 1); }); });
 }


 void jaccard(String query){  
	XnY.forEach((j, xny) -> {
		int Y = setOfGrams(dictionary[j]).size();
		double Z=(X+Y) - XnY.get(j); //Compute XuY
	        double score = XnY.get(j)/Z ;//Jaccard's coefficient
		if (score > 0.2) System.out.println(dictionary[j] + "  " + score);
     	});
 }

 public static void main(String[] args){
   IR2B ir2 = new IR2B();
   ir2.readDictionary(args[0]);
   ir2.correctSpellings();
 }
}
   