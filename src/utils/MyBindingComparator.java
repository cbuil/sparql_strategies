package utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class MyBindingComparator implements Comparator<Binding> {

	@Override
	public int compare(Binding o1, Binding o2) {
		TreeSet<Var> orderedvars = new TreeSet<Var>(new Comparator<Var>() {
			@Override
			public int compare(Var o1, Var o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		Iterator<Var> vars = o1.vars();
		while(vars.hasNext()){
			orderedvars.add(vars.next());
		}
		
		int res = o1.size() - o2.size(); 
		if(res == 0) {
			for(Var var: orderedvars){
				if(o2.contains(var)){
					res=o1.get(var).toString().compareTo(o2.get(var).toString()); 
					if(res != 0 ) break;
				}
			}
		}
//		System.out.println("------\n"+o1+"\n"+o2+"\n => "+res);
		return res;
	}
}