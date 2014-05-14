package utils;

import java.util.Set;
import java.util.TreeSet;



import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class SetOperations {
	/**
	 * 
	 * @param setA
	 * @param setB
	 * @return - union of A and B 
	 */
	public static <T> Set<T> union(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>(setA);
		tmp.addAll(setB);
		return tmp;
	}

	
	/**
	 * 
	 * @param setA
	 * @param setB
	 * @return - intersection of A and B 
	 */
	public static Set<Binding> intersection(Set<Binding> setA, Set<Binding> setB) {
		Set<Binding> tmp = new TreeSet<Binding>(new MyBindingComparator());
		tmp.addAll(setB); 
		for (Binding x : setA)
			if (setB.contains(x))
				tmp.add(x);
		return tmp;
	}

	/**
	 * 
	 * @param setA
	 * @param setB
	 * @return - returns only the statements in A ( A\B)
	 */
	public static Set<Binding> difference(Set<Binding> setA, Set<Binding> setB) {
		Set<Binding> tmp = new TreeSet<Binding>(new MyBindingComparator());
		tmp.addAll(setA);
		tmp.removeAll(setB);
		return tmp;
	}

	
//	public static <T> Set<T> symDifference(Set<T> setA, Set<T> setB) {
//		Set<T> tmpA;
//		Set<T> tmpB;
//
//		tmpA = union(setA, setB);
//		tmpB = intersection(setA, setB);
//		return difference(tmpA, tmpB);
//	}

	public static <T> boolean isSubset(Set<T> setA, Set<T> setB) {
		return setB.containsAll(setA);
	}

	public static <T> boolean isSuperset(Set<T> setA, Set<T> setB) {
		return setA.containsAll(setB);
	}
}