package join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MyBindingComparator;
import utils.QueryParser;
import utils.SplitQuery;
import bench.BenchmarkResult;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class SymmetricalHashJoin implements JoinOperator{
	private final static Logger log = LoggerFactory.getLogger(SymmetricalHashJoin.class);

	private boolean left_expResults = true;
	private boolean right_expResults = true;
	private final HashMap<RDFNode, Set<Binding>> l_soln = new HashMap<RDFNode, Set<Binding>>();
	private final HashMap<RDFNode, Set<Binding>> r_soln = new HashMap<RDFNode, Set<Binding>>();
	private Set<Binding> results = new TreeSet<Binding>(new MyBindingComparator());

	private String _joinVar; 



	synchronized public void addSolution(QuerySolution soln, boolean _left) {
		if(soln == null){
			if(_left) left_expResults=false;
			else right_expResults=false;
			return;
		}
		HashMap<RDFNode, Set<Binding>> map = r_soln;
		HashMap<RDFNode, Set<Binding>> comp = l_soln;
		if(_left){
			map = l_soln;
			comp= r_soln;
		}

		Set<Binding> set = map.get(soln.get(_joinVar));
		if(set == null){
			set = new TreeSet<Binding>(new MyBindingComparator());
			map.put(soln.get(_joinVar),set);
//			System.out.println("Added (l:"+_left+") "+soln.get(_joinVar));
		}
		set.add(((ResultBinding)soln).getBinding());

		if(comp.containsKey(soln.get(_joinVar))){
//			System.out.println("Here");
			for(Binding s: comp.get(soln.get(_joinVar))){
				
				Binding bind = Algebra.merge(s, ((ResultBinding)soln).getBinding());
				
				if(bind != null && !results.contains(bind) && bind.contains(Var.alloc(_joinVar))){
					results.add(bind);
//					System.out.println("RESULT");
				}
			}
		}
	}



	
	public BenchmarkResult executeHTTP(SplitQuery sq) {
		return executeHTTP(sq,false);
	}

	@Override
	public BenchmarkResult executeHTTP(SplitQuery sq, boolean debug) {
		results = new TreeSet<Binding>(new MyBindingComparator());
		BenchmarkResult res = new BenchmarkResult();
		res.setBatchSize(sq.getBatch());
		long start = System.currentTimeMillis();
		long end = 0;
		try{
			Query p1 = QueryFactory.create();
			p1.setQueryPattern(sq.getP1());
			p1.setQuerySelectType();
			p1.setQueryResultStar(true);
//			p1.setDistinct(true);

			List<Var> p1Vars = new ArrayList<Var>();
			ElementWalker.walk( sq.getP1(), new GetVars(p1Vars));

			Query p2 = QueryFactory.create();
			p2.setQueryPattern(sq.getP2());
			p2.setQuerySelectType();
			p2.setQueryResultStar(true);
//			p2.setDistinct(true);

			List<Var> p2Vars = new ArrayList<Var>();
			ElementWalker.walk( sq.getP2(), new GetVars(p2Vars));
			_joinVar = QueryParser.findJoinVar(p1Vars, p2Vars);

			if(debug){
				System.out.println("Executing query P1 "+p1);
			}
			QueryExecutorThread l_thread = new QueryExecutorThread(this, sq.getP1Endpoint(),p1, true);
			l_thread.start();
			res.epCall(0);

			if(debug){
				System.out.println("Executing query P2 "+p2);
			}
			QueryExecutorThread r_thread = new QueryExecutorThread(this, sq.getP2Endpoint(), p2, false);
			r_thread.start();
			res.epCall(1);

			while(left_expResults || right_expResults){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			res.setResults(results);
			res.setInterimResults("0", l_thread.getResults());
			res.setInterimResults("1", r_thread.getResults());
		}catch(Exception e){
		    e.printStackTrace();
			res.setException(e);
//			e.printStackTrace(System.out);
//			e.printStackTrace(System.out);
		}finally{
			end = System.currentTimeMillis()-start;
			log.info("[JOIN] SYMHASH "+res);
			res.setTotalTime(end);
		}
		return res;
	}

	class QueryExecutorThread extends Thread{

		private SymmetricalHashJoin _join;
		private String _url;
		private Query _query;
		private boolean _left;
		private int c;

		public QueryExecutorThread(SymmetricalHashJoin symmetricalHashJoin,
				String url, Query query, boolean left) {
			super();
			_url = url;
			_join = symmetricalHashJoin;
			_query = query;
			_left = left;
		}

		public int getResults() {
			// TODO Auto-generated method stub
			return c;
		}

		@Override
		public void run() {
			QueryExecution qexec;
			qexec = QueryExecutionFactory.sparqlService(_url,_query);

			ResultSet results = qexec.execSelect();
			c=0;
			while (results.hasNext()) {
				c++;
				QuerySolution soln = results.next();
				_join.addSolution(soln,_left);
				if(_left)
					System.out.print("#");
				else{
					System.out.print(".");
				}
				if(c%80==0)System.out.println();
			}
			qexec.close();
			System.out.println("Done "+_left);
			_join.addSolution(null,_left);
		}
	}
}
