// IR4A.java CS5154/6054 2019 Cheng
// Generate inverted index for a collection of documents
// The first line: numberOfTerms numberOfDocs
// followed by numberOfTerms lines of term, postings list of alternating
// docIDs and term frequencies (tfs)
// Usage: java IR4A < collection > invertedIndexWithTfs
// Usage: java IR4A < ad14.txt > adInvertedTf.txt
//        java IR4A < isr4.txt > isrInvertedTf.txt

import java.io.*;
import java.util.*;

public class IR4A{

  int numberOfDocs = 0;
  int numberOfTerms = 0;
  HashMap<String, HashMap<Integer, Integer>> index = 
	new HashMap<String, HashMap<Integer, Integer>>();

  void readCollection(){
 	Scanner in = new Scanner(System.in);
    	while (in.hasNextLine()){
      		String[] parts = in.nextLine().split("\t");
     		tokenize(parts[1], numberOfDocs); // title
      		in.nextLine(); // ignore journal
      		tokenize(in.nextLine(), numberOfDocs); // abstract
      		numberOfDocs++;
   	}
	numberOfTerms = index.size();
    	System.out.println(numberOfTerms + " " + numberOfDocs);
  }

  void tokenize(String line, int doc){  
 	String[] tokens = line.toLowerCase().split("[^a-z0-9]");
    	// tokens contains only a-z0-9 but may be empty string or numbers
    	for (String s: tokens) if (s.length() > 0 && s.charAt(0) > '9')
		if (index.containsKey(s)){
			HashMap<Integer, Integer> map = index.get(s);
			if (map.containsKey(doc))
				map.put(doc, map.get(doc) + 1);
			else map.put(doc, 1);
		}else{
			HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
			map.put(doc, 1);
			index.put(s, map);
		}
  }

  void printInvertedIndex(){
	index.forEach((s, map) -> {
		System.out.print(s);
		map.forEach((doc, tf) -> System.out.print(" " + doc + " " + tf));
   		System.out.println(); });
 }

 public static void main(String[] args){
   IR4A ir4 = new IR4A();
   ir4.readCollection();
   ir4.printInvertedIndex();
 }
}

      