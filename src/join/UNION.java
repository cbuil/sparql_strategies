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
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class UNION implements JoinOperator{
	private final static Logger log = LoggerFactory.getLogger(UNION.class);
	final Var join = Var.alloc( "join" );
	private final HashMap<RDFNode, Set<Binding>> l_soln = new HashMap<RDFNode, Set<Binding>>();
	private final HashMap<RDFNode, Set<Binding>> r_soln = new HashMap<RDFNode, Set<Binding>>();
	private  Set<Binding> results = new TreeSet<Binding>(new MyBindingComparator());

	private String _joinVar; 




	
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
			// Create variables and bindings for the VALUES block
			List<Var> variables = new ArrayList<Var>();
			variables.add( Var.alloc(_joinVar));


			
			
			if(sq.getBatch()>0){
				runBatch(sq, p1, p2, res, debug);
			}
			else{
				runSingle(sq, p1, p2, res, debug);
				
			}
			

			res.setResults(results);
		}catch(Exception e){
			res.setException(e);
			e.printStackTrace();
		}finally{
			end = System.currentTimeMillis()-start;
			log.info("[JOIN] UNION "+res);
			res.setTotalTime(end);
		}
		return res;
	}
	private void runBatch(SplitQuery sq, Query p1, Query p2,
			BenchmarkResult res, boolean debug) {

		
		//init execution of left triple pattern
		QueryExecution qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP1Endpoint().toString(), p1);
		res.epCall(0);
		ResultSet l_results = qexec.execSelect();
		int c=0, c1=0;
		System.out.println("P1:"+p1);
		ElementGroup body = new ElementGroup();
		while (l_results.hasNext()) {
			c++;
			final QuerySolution l_soln = l_results.nextSolution();
			
			if(l_soln.contains(_joinVar)){
				addSolution(l_soln,true);
				
				
				ElementGroup g= new ElementGroup();
				g.addElement(p2.getQueryPattern());
			
				Expr e = new E_Equals(new ExprVar(_joinVar), new NodeValueNode(l_soln.get(_joinVar).asNode()));
				ElementFilter filter = new ElementFilter(e);
				g.addElement(filter);
				if(body.isEmpty()){
					body.addElement(g);
				}else{
					ElementUnion union = new ElementUnion(g);
					body.addElement(union);
				}
			}
			
			if(c%sq.getBatch()== 0){
				System.out.println("Running batch");
				Query p22 = p2.cloneQuery();
				p22.setQueryPattern(body);
				if(debug){
					System.out.println("----\nExecuting query P2 "+p22);
				}
				QueryExecution p2qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint().toString(), p22);
				ResultSet p2_results = p2qexec.execSelect();
				res.epCall(1);
				
				while (p2_results.hasNext()) {
					c1++;
					QuerySolution solnp2 = p2_results.nextSolution();
					addSolution(solnp2, false);
					
				}
				p2qexec.close();
//				list = new ArrayList<Element>();
				body = new ElementGroup();
			}
		}
		if(c%sq.getBatch() != 0){
			System.out.println("Running batch");
			Query p22 = p1.cloneQuery();
			p22.setQueryPattern(body);
			if(debug){
				System.out.println("----\nExecuting query P2 "+p22);
			}
			QueryExecution p2qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint().toString(), p22);
			ResultSet p2_results = p2qexec.execSelect();
			res.epCall(1);
			while (p2_results.hasNext()) {
				c1++;
				QuerySolution solnp2 = p2_results.nextSolution();
				addSolution(solnp2, false);
				
			}
			p2qexec.close();
//			list = new ArrayList<Element>();
			body = new ElementGroup();
		}
		qexec.close();
		res.setInterimResults("0", c);
		res.setInterimResults("1", c1);
		if(c1==0)results = new TreeSet<Binding>(new MyBindingComparator());
		
	}

	private void runSingle(SplitQuery sq, Query p1, Query p2,
			BenchmarkResult res, boolean debug) {
		//init execution of left triple pattern
		QueryExecution qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP1Endpoint().toString(), p1);
		ResultSet l_results = qexec.execSelect();
		res.epCall(0);
		int c=0;
		System.out.println("P1:"+p1);
		
		ElementGroup body = new ElementGroup();
		while (l_results.hasNext()) {
			c++;
			QuerySolution l_soln = l_results.nextSolution();
			if(l_soln.contains(_joinVar)){
				
			addSolution(l_soln,true);
			
			ElementGroup g= new ElementGroup();
			g.addElement(p2.getQueryPattern());
			
			Expr e = new E_Equals(new ExprVar(_joinVar), new NodeValueNode(l_soln.get(_joinVar).asNode()));
			ElementFilter filter = new ElementFilter(e);
			g.addElement(filter);
			if(body.isEmpty()){
				body.addElement(g);
			}else{
				ElementUnion union = new ElementUnion(g);
				body.addElement(union);
			}
			}
		}
		qexec.close();
		res.setInterimResults("0", c);
		Query p22 = p1.cloneQuery();
		p22.setQueryPattern(body);
		if(debug){
			System.out.println("----\nExecuting query P2 "+p22);
		}
		QueryExecution p2qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint().toString(), p22);
		ResultSet p2_results = p2qexec.execSelect();
		res.epCall(1);
		c=0;		
		res.epCall(1);
		while (p2_results.hasNext()) {
			c++;
			QuerySolution solnp2 = p2_results.nextSolution();
			addSolution(solnp2, false);
		}
		res.setInterimResults("1", c);
		p2qexec.close();
		if(c==0) results = new TreeSet<Binding>(new MyBindingComparator());
	}
//		
//		
//		while (l_results.hasNext()) {
//			c++;
//			final QuerySolution l_soln = l_results.nextSolution();
//
//			ParameterizedSparqlString p2Str = new ParameterizedSparqlString(p2.toString());
//			if(l_soln.get(_joinVar) instanceof LiteralImpl)
//				p2Str.setLiteral(_joinVar, l_soln.get(_joinVar).asNode().toString());
//			if(l_soln.get(_joinVar) instanceof ResourceImpl)
//				p2Str.setIri(_joinVar, l_soln.get(_joinVar).asNode().toString());
//			list.add(p2Str.asQuery().getQueryPattern());
//			addSolution(l_soln, true);
//			
//			
//			
//		}
//		qexec.close();
//		ElementGroup body = new ElementGroup();
//
//		for(int i =0; i< list.size();i++){
//			if(i==0)
//				body.addElement(list.get(0));
//			else{
//				ElementUnion union = new ElementUnion(list.get(i));
//				body.addElement(union);
//			}
//		}
//		res.setInterimResults("0", c);
//
//		
//		p2.setQueryPattern(body);
//		if(debug){
//			System.out.println("----\nExecuting query P2 "+p2);
//		}
//		QueryExecution p2qexec =(QueryEngineHTTP) QueryExecutionFactory.sparqlService(sq.getP2Endpoint().toString(), p2);
//		ResultSet p2_results = p2qexec.execSelect();
//		c=0;
//		while (p2_results.hasNext()) {
//			c++;
//			QuerySolution solnp2 = p2_results.nextSolution();
//			System.out.println(solnp2);
//			Binding bind = Algebra.merge(((ResultBinding)solnp2).getBinding(), ((ResultBinding)solnp2).getBinding());
//			if(!results.contains(bind))
//				results.add(bind);
//		}
//		p2qexec.close();
//		res.setInterimResults("1", c);
//		
//	}

	synchronized public void addSolution(QuerySolution soln, boolean _left) {
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
		}
		set.add(((ResultBinding)soln).getBinding());

		if(comp.containsKey(soln.get(_joinVar))){
//			System.out.println("Here");
			for(Binding s: comp.get(soln.get(_joinVar))){
				Binding bind = Algebra.merge(s, ((ResultBinding)soln).getBinding());
				if(bind!=null && !results.contains(bind)){
					results.add(bind);
//					System.out.println("RESULT");
				}
			}
		}
	}
}