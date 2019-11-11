// IR2A.java CS5154/6054 2018 Cheng
// generate an inverted index of 3-grams for a dictionary
// Usage: java IR2A < isaInverted.txt > isa3gInverted.txt
// Usage: java IR2A < adInverted.txt > ad3gInverted.txt

import java.io.*;
import java.util.*;

public class IR2A{

  static final int gramLength = 3;
  int dictionarySize = 0;
  String[] dictionary = null;
  HashMap<String, HashSet<Integer>> index = new HashMap<String, HashSet<Integer>>();

  void readDictionary(){  // first column as dictionary
	Scanner in = new Scanner(System.in);
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

 void printInvertedGramIndex(){
	index.forEach((gram, postings) -> { System.out.print(gram);
		postings.forEach(x -> System.out.print(" " + x));
		System.out.println(); });
 }

 public static void main(String[] args){
   IR2A ir2 = new IR2A();
   ir2.readDictionary();
   ir2.printInvertedGramIndex();
 }
}
   