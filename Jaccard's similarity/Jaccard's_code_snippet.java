void jaccard(String query){  
	XnY.forEach((j, xny) -> {
		int Y = setOfGrams(dictionary[j]).size();
		double Z=(X+Y) - XnY.get(j); //Compute XuY
	        double score = XnY.get(j)/Z ;//Jaccard's coefficient
		if (score > 0.2) System.out.println(dictionary[j] + "  " + score);
     	});
 }
