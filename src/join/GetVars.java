package join;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker.Walker;

public class GetVars extends ElementVisitorBase {

	private List<Var> _vars;

	public GetVars(List<Var> vars) {
		_vars = vars;
	}

	@Override
	public void visit(ElementPathBlock el) {
		java.util.ListIterator<TriplePath> it = el.getPattern().iterator();
		while ( it.hasNext() ) {
			final TriplePath tp = it.next();
			storeVar(tp.getSubject());
			storeVar(tp.getPredicate());
			storeVar(tp.getObject());
			
		}
	}

	private void storeVar(Node n) {
		if(n instanceof Var){
			if(!_vars.contains(n)){
				_vars.add((Var) n);
			}
		}
		
	}

}
