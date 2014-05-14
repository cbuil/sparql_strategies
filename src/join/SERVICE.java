package join;

import java.net.URI;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MyBindingComparator;
import utils.SplitQuery;
import bench.BenchmarkResult;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementService;

public class SERVICE implements JoinOperator{
	private final static Logger log = LoggerFactory.getLogger(SERVICE.class);


	
	public BenchmarkResult executeHTTP(SplitQuery sq) {
		return executeHTTP(sq,false);
	}

	@Override
	public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug) {
		BenchmarkResult res = new BenchmarkResult();
		res.setBatchSize(sq.getBatch());
		Set<Binding> results = new TreeSet<Binding>(new MyBindingComparator());
		long start = System.currentTimeMillis();
		long end = 0;
		try{
			//prepare query
			//Prepare the queries

			//P = P1 AND SERVICE c P2
			Query p = QueryFactory.create();

			ElementGroup body = new ElementGroup();
			body.addElement(sq.getP1());

			ElementService se  = new ElementService(sq.getP2Endpoint(), sq.getP2());
			body.addElement(se);

			p.setQuerySelectType();
			p.setQueryPattern(body);
			p.setQueryResultStar(true);
//			p.setDistinct(true);

			if(debug){
				System.out.println("P="+p);
			}
			//init execution of left triple pattern
			QueryExecution qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP1Endpoint().toString(), p);
			
			ResultSet l_results = qexec.execSelect();
			int c=0;
			while (l_results.hasNext()) {
				c++;
				final QuerySolution soln = l_results.nextSolution();
				Binding bind = Algebra.merge(((ResultBinding)soln).getBinding(), ((ResultBinding)soln).getBinding());
				if(!results.contains(bind))
					results.add(bind);
			}
			res.setInterimResults("0", c);
			res.setInterimResults("1", 0);
			qexec.close();
					res.setResults(results);
		}catch(Exception e){
			res.setException(e);
			e.printStackTrace(System.out);
		}finally{
			end = System.currentTimeMillis()-start;
			log.info("[JOIN] SERVICE "+res);
			res.setTotalTime(end);
		}
		return res;
	}

}
