package bench;


import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import join.JoinOps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.MyResultSet;
import utils.SetOperations;





import utils.SplitQuery;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.util.FileManager;

public class QueryExecution {
	private final static Logger log = LoggerFactory.getLogger(QueryExecution.class);
		
//	public void executeOverInMemory(File dataDir, File outputDir, boolean samePosJoin, JoinOps ... jops){
//		if(!dataDir.exists())dataDir.mkdirs();
//		if(!outputDir.exists())outputDir.mkdirs();
//		
//		
//		Model model_left = ModelFactory.createDefaultModel();
//		Model model_right = ModelFactory.createDefaultModel();
//		
//		
//		InputStream in_left = FileManager.get().open( new File(dataDir,CONSTANTS.L_DATA).getAbsolutePath() );
//		long start = System.currentTimeMillis();
//		model_left.read(in_left,null, "N-TRIPLES");
//		log.info("[LOAD] Left-In-Mem [STMTS] "+model_left.size()+" [TIME] "+(System.currentTimeMillis()-start)+" ms");
//		
//		InputStream in_right;
//		if(samePosJoin)
//			in_right = FileManager.get().open( new File(dataDir,CONSTANTS.R_SubSub_DATA).getAbsolutePath() );
//		else 
//			in_right = FileManager.get().open( new File(dataDir,CONSTANTS.R_SubObj_DATA).getAbsolutePath() );
//		
//		start = System.currentTimeMillis();
//		model_right.read(in_right,null, "N-TRIPLES");
//		log.info("[LOAD] Right-In-Mem [STMTS] "+model_right.size()+" [TIME] "+(System.currentTimeMillis()-start)+" ms");
//		
//		
//		
//		
//		List<Set<Binding>> res = new ArrayList<Set<Binding>>();
//		for(JoinOps jop: jops){
//			try{
//				long t_start = System.currentTimeMillis();
//				Set<Binding> results = jop.executeInMemory(model_left, model_right,samePosJoin);
//				long t_end = System.currentTimeMillis();
//				MyResultSet resSet = new MyResultSet(results);
//				res.add(results);
//				
//				log.info("[JOIN] "+jop.toString()+" [RESULTS] "+results.size()+" [TIME] "+(t_end-t_start)+" ms");
//				FileOutputStream fos = new FileOutputStream(new File(outputDir,jop.name()+"_results.spq"));
//				ResultSetFormatter.outputAsRDF(fos,"RDF/XML-ABBREV", resSet);
//				fos.close();
//				 
//				 FileWriter pw = new FileWriter(new File(outputDir, "results.tsv"),true);
//				 pw.write(jop.name()+"\t"+results.size()+"\t"+(t_end-t_start)+"\n");
//				 pw.close();
//			}catch(Exception e){
//				log.warn("[JOIN] "+jop.toString()+" [ERROR] "+e.getClass().getSimpleName());
//			}
//		}
//		
//		
//		for( int i =0; i < res.size(); i++){
//			System.out.println("["+i+"]>"+res.get(i).size());
//			for( int a = i+1; a < res.size(); a++){
//				if(SetOperations.difference(res.get(i), res.get(a)).size()!=0){
////					System.out.println("  "+i+" and "+a +" are different by "+SetOperations.difference(res.get(i), res.get(a)).size());
////					
//					for(Binding b: res.get(i)){
//						System.out.println();
//						if(!res.get(a).contains(b)){
//							System.out.println("missign binding "+b);
//						}
//						System.out.println(res.get(i).size()+" "+res.get(a).size());
//					}
//				}
//			}	
//		}		
//	}

	
	public Map<String, BenchmarkResult> executeOverHTTP( SplitQuery sq,  boolean debug, JoinOps ... joinOps) {
		Map<String, BenchmarkResult> results = new HashMap<String, BenchmarkResult>();
		for(JoinOps jop: joinOps){
			try{
				BenchmarkResult res = jop.executeHTTP(sq, debug);
				results.put(jop.name(), res);
			}catch(Exception e){
				log.warn("[JOIN] "+jop.toString()+" [ERROR] "+e.getClass().getSimpleName());
				e.printStackTrace();
			}
		}
		return results;
	}
}
