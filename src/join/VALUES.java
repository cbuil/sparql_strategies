package join;

import java.net.URI;
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
import bench.CONSTANTS;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.core.ResultBinding;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class VALUES implements JoinOperator{
	private final static Logger log = LoggerFactory.getLogger(VALUES.class);
	Set<Binding> results = new TreeSet<Binding>(new MyBindingComparator());


	//	@Override
	//	public Set<Binding> executeHTTP(boolean samePosJoin) {
	//		long start = System.currentTimeMillis();
	//		
	//		QueryExecution l_qexec = QueryExecutionFactory.sparqlService(CONSTANTS.L_EP,CONSTANTS.L_QUERY);
	//		ResultSet l_results = l_qexec.execSelect();
	//
	//		StringBuilder sb = new StringBuilder();
	//		
	//		
	//		HashMap<RDFNode, Set<Binding>> l_solnMap = new HashMap<RDFNode, Set<Binding>>();
	//		
	//		while (l_results.hasNext()) {
	//		    QuerySolution l_soln = l_results.nextSolution();
	//		    //store binding with join value as key
	//		    Set<Binding> set = l_solnMap.get(l_soln.get("join"));
	//			if(set == null){
	//				set = new TreeSet<Binding>(new MyBindingComparator());
	//				l_solnMap.put(l_soln.get("join"),set);
	//			}
	//			set.add(((ResultBinding)l_soln).getBinding());
	//			
	//		 	sb.append(" <").append(l_soln.get("join")).append("> ");
	//		}
	//		l_qexec.close();
	//		
	//		Query r_q = CONSTANTS.R_SubObj_QUERY;
	//		if(samePosJoin)
	//			r_q = CONSTANTS.R_SubSub_QUERY;
	//		
	//		
	//		String query = r_q.clone().toString();
	//		
	//		query = query.replace("{", "{ VALUES ?join { "+sb.toString()+"} ");
	////		System.out.println(query);
	//		QueryExecution r_qexec = QueryExecutionFactory.sparqlService(CONSTANTS.R_EP,QueryFactory.create(query));
	//		
	//		
	//		Set<Binding> res =  new TreeSet<Binding>(new MyBindingComparator());
	//		ResultSet r_results = r_qexec.execSelect();
	//
	//		while (r_results.hasNext()) {
	//			QuerySolution r_soln = r_results.nextSolution();
	//			if(l_solnMap.containsKey(r_soln.get("join"))){
	//				for(Binding s: l_solnMap.get(r_soln.get("join"))){
	//					Binding b = Algebra.merge(((ResultBinding)r_soln).getBinding(), s);
	//					if(!res.contains(b))
	//						res.add(b);
	//				}
	//			}
	//		}
	//		r_qexec.close();
	//		
	//		long end = System.currentTimeMillis();
	//		
	//		log.info("[JOIN] SPARQL1.1_VALUES [RESULTS] "+res.size()+" [TIME] "+(end-start)+" ms");
	//		
	//		return res;
	//	}

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

			String joinVar = QueryParser.findJoinVar(p1Vars, p2Vars);
			// Create variables and bindings for the VALUES block

			List<Var> variables = new ArrayList<Var>();
			for(String v: p1.getResultVars())
				variables.add( Var.alloc(v));


			if(sq.getBatch()>0){
				runBatch(sq, variables, p1, p2, res, debug);
			}
			else{
				runSingle(sq, variables,  p1, p2, res, debug);
			}
			res.setResults(results);
		}catch(Exception e){
			res.setException(e);
			e.printStackTrace(System.out);
		}finally{
			end = System.currentTimeMillis()-start;
			log.info("[JOIN] VALUES "+res);
			res.setTotalTime(end);
		}
		return res;
	}

	private void runSingle(SplitQuery sq, List<Var> variables, Query p1, Query p2,
			BenchmarkResult res, boolean debug) {

		List<Binding> bindings = new ArrayList<Binding>();
		//init execution of left triple pattern
		QueryExecution qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP1Endpoint().toString(), p1);
		ResultSet l_results = qexec.execSelect();
		res.epCall(0);
		int c=0;
		while (l_results.hasNext()) {
			c++;
			QuerySolution soln = l_results.nextSolution();
			//			
			BindingMap m =BindingFactory.create();
			for ( final Var var : variables ) {
				//System.out.println(soln.get( var.getName()).asNode());
				if(soln.get(var.getName())!=null)
					m.add( var , soln.get( var.getName()).asNode() );
			}
			bindings.add(m);
		}
		res.setInterimResults("0", c);
		if(debug){
			System.out.println();
			//			System.out.println("----\nBindings for query P2 "+bindings);
		}
		qexec.close();

		p2.setValuesDataBlock(variables, bindings);
		if(debug){
			System.out.println("----\nExecuting query P2 "+p2);
		}

		c=0;
		QueryExecution p2qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint().toString(), p2);
		ResultSet p2_results = p2qexec.execSelect();
		res.epCall(1);
		while (p2_results.hasNext()) {
			c++;
			QuerySolution solnp2 = p2_results.nextSolution();
			//			System.out.println(solnp2);
			//			Binding bind = Algebra.merge(((ResultBinding)solnp2).getBinding(), ((ResultBinding)solnp2).getBinding());
			if(!results.contains(((ResultBinding)solnp2).getBinding()))
				results.add(((ResultBinding)solnp2).getBinding());
		}
		res.setInterimResults("1", c);
		if(c==0) results = new TreeSet<Binding>(new MyBindingComparator());
		p2qexec.close();

	}

	private void runBatch(SplitQuery sq, List<Var> variables, Query p1, Query p2,
			BenchmarkResult res, boolean debug) {
		List<Binding> bindings = new ArrayList<Binding>();
		//init execution of left triple pattern
		QueryExecution qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP1Endpoint().toString(), p1);
		ResultSet l_results = qexec.execSelect();
		res.epCall(0);
		int c=0,c1=0;
		while (l_results.hasNext()) {
			c++;
			QuerySolution soln = l_results.nextSolution();
			//			
			BindingMap m =BindingFactory.create();
			for ( final Var var : variables ) {
				//System.out.println(soln.get( var.getName()).asNode());
				if(soln.get(var.getName())!=null)
					m.add( var , soln.get( var.getName()).asNode() );
			}
			bindings.add(m);

			if(c%sq.getBatch()==0){
				System.out.println("Running batch with "+sq.getBatch());
				Query p22 = p2.cloneQuery();
				p22.setValuesDataBlock(variables, bindings);
				if(debug){
					System.out.println("----\nExecuting query P2 "+p22);
				}

				try{
					QueryExecution p2qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint().toString(), p22);
					ResultSet p2_results = p2qexec.execSelect();
					res.epCall(1);
					while (p2_results.hasNext()) {
						c1++;
						QuerySolution solnp2 = p2_results.nextSolution();
						if(!results.contains(((ResultBinding)solnp2).getBinding()))
							results.add(((ResultBinding)solnp2).getBinding());
					}
					p2qexec.close();
					bindings = new ArrayList<Binding>();
				}catch(Exception e){
					e.printStackTrace();
					System.out.println(p22);
				}
			}
		}

		if(c % sq.getBatch()!=0 ){
			System.out.println("Running last batch");
			Query p22 = p2.cloneQuery();
			p22.setValuesDataBlock(variables, bindings);
			if(debug){
				System.out.println("----\nExecuting query P2 "+p22);
			}


			QueryExecution p2qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint().toString(), p22);
			ResultSet p2_results = p2qexec.execSelect();
			res.epCall(1);
			while (p2_results.hasNext()) {
				c1++;
				QuerySolution solnp2 = p2_results.nextSolution();
				if(!results.contains(((ResultBinding)solnp2).getBinding()))
					results.add(((ResultBinding)solnp2).getBinding());
			}

			p2qexec.close();
		}
		res.setInterimResults("1", c1);
		if(c1==0) results = new TreeSet<Binding>(new MyBindingComparator());
		res.setInterimResults("0", c);
		qexec.close();
	}
}
