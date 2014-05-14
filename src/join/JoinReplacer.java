package join;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;

public class JoinReplacer extends ElementVisitorBase{
	
	
		
	private Node _n;

	public JoinReplacer(Node asNode) {
		_n = asNode;
	}
	
	@Override
	public void visit(ElementPathBlock el) {
		java.util.ListIterator<TriplePath> it = el.getPattern().iterator();
		final Var join = Var.alloc( "join" );
		while ( it.hasNext() ) {
			final TriplePath tp = it.next();
			if ( tp.getSubject().equals( join ) || tp.getPredicate().equals( join )|| tp.getObject().equals( join )) {
				it.remove();
				Node s = tp.getSubject().equals( join )? _n: tp.getSubject();
				Node p = tp.getPredicate().equals( join )? _n: tp.getPredicate();
				Node o = tp.getObject().equals( join )? _n: tp.getObject();
				it.add(new TriplePath( new Triple(s, p, o)));
				
			}
		}
	}
}
        

