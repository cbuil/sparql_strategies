package join;

import java.net.URI;
import java.util.Set;

import utils.SplitQuery;
import bench.BenchmarkResult;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public enum JoinOps {
		NESTED(new NestedLoop()),
		SYMHASH(new SymmetricalHashJoin()),
		VALUES( new VALUES()),
		UNION( new UNION()),
		UNIONFILTER( new UNIONFilter()),
		FILTER( new FILTER()),
		SERVICE( new SERVICE()),
		SYMHASHP(new SymmetricalHashPageJoin());
		
		private JoinOperator delegate;
		private JoinOps(JoinOperator delegate) { this.delegate=delegate; }
		
//		public Set<Binding> executeInMemory(Model model_left, Model model_right,boolean samePosJoin) {
//			return delegate.executeInMemory(model_left,model_right, samePosJoin);
//		}

		public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug) {
			 return delegate.executeHTTP(sq, debug);
		}
}
