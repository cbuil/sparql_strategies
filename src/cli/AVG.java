package cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import join.JoinOps;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import utils.QueryParser;
import utils.SplitQuery;
import bench.BenchmarkResult;
import bench.QueryExecution;

/**
 * 
 * @author UmbrichJ
 * 
 */
public class AVG extends CLIObject {

	private final static Logger log = LoggerFactory.getLogger(AVG.class);
	private final static String descr = "Benchmark the execution of a SPARQL query against an endpoint URI";

	@Override
	public String getDescription() {
		return descr;
	}

	@Override
	protected void addOptions(Options opts) {
		opts.addOption(PARAMETERS.OPTION_OUTPUT_DIR);
		opts.addOption(PARAMETERS.OPTION_INPUT);
	}

	@Override
	protected void execute(CommandLine cmd) {
		try {
			File in = new File(cmd.getOptionValue(PARAMETERS.PARAM_INPUT));
			File out = new File(cmd.getOptionValue(PARAMETERS.PARAM_OUTPUT_DIR));
			PrintWriter pw = new PrintWriter(out);
			
			Scanner s = new Scanner(in);
			String q= null, j= null, b= null;
			
			int [] sum= new int[7];
			int count = 0;
			while (s.hasNextLine()){
				String [] v = s.nextLine().trim().split("\t");
				if(v.length<9) {
//					System.out.println(Arrays.toString(v));
					continue;
				}
//				System.out.println(Arrays.toString(v));
				if ( q == null){
					q = v[0];
					j = v[1];
					b= v[8];
					
					for ( int i =2; i < 9;i++){
						sum[i-2] += Integer.valueOf(v[i]);
					}
					count++;
				}else if ( (q.equals(v[0]) && j.equals(v[1]) && b.equals(v[8]))){
					q = v[0];
					j = v[1];
					b= v[8];
					
					for ( int i =2; i < 9;i++){
						int a =Integer.valueOf(v[i]);
						sum[i-2] += a;
					}
					count++;
				}
				else{
					StringBuilder sb = new StringBuilder();
					sb.append(q).append("\t")
					.append(j).append("\t");
					System.out.println(q+" "+j+" "+count);
					for( int i: sum){
						sb.append(i/(double) count).append("\t");	
					}
					pw.println(sb.toString());
					
					//reset
					Arrays.fill(sum, 0);
					count =0;
					
					q = v[0];
					j = v[1];
					b= v[8];
					for ( int i =2; i < 9;i++){
						sum[i-2] += Integer.valueOf(v[i]);
					}
					count++;
				}
			}
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void runQuery(File q, String[] joins, String localEndpoint, File outDir, boolean debug, int batch) {
		try {
			
			SplitQuery sq = QueryParser.splitQuery(q.toString(), localEndpoint);
			sq.setBatch(batch);
			
			QueryExecution qe = new QueryExecution();
			
			PrintWriter pwAll = new PrintWriter(new File(outDir, q.getName()+"_results.tsv"));
			for(String join: joins){
				System.out.println("RUNNING "+join +" for "+q);
				Map<String, BenchmarkResult> results = qe.executeOverHTTP(sq, debug, JoinOps.valueOf(join));
				PrintWriter pw = new PrintWriter(new File(outDir, q.getName()+"_"+join+".txt"));
					
				for(Entry<String, BenchmarkResult> ent: results.entrySet()){
					pw.println("#JOIN results time ep0_calls ep1_calls");
					pw.println(ent.getValue().getShortString(","));
					pw.println("#Bindings");
					System.out.println(q.getName()+","+ent.getKey()+","+ent.getValue().getShortString(","));
					if(ent.getValue()!= null && ent.getValue().getResults()!=null){
						for(Binding b: ent.getValue().getResults()){
							pw.println(b);
						}
					}
					System.err.println(q.getName()+"\t"+ent.getKey()+"\t"+ent.getValue().getShortString("\t"));
					pwAll.println(q.getName()+"\t"+ent.getKey()+"\t"+ent.getValue().getShortString("\t"));
				}
				pw.close();
			}
			pwAll.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private String executingInfo(String[][] subqueries, URI[] endpoints) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n#--------------").append("\n");
		for (int a = 0; a < 2; a++) {
			sb.append("#==== Query").append(" ====<\n");
			for (int i = 0; i < subqueries[a].length; i++) {
				sb.append("# ").append(subqueries[a][i]).append("\n");
			}
			sb.append("#== Endpoint").append(a).append(": ")
			.append(endpoints[a]).append("\n");
		}
		return sb.toString();
	}

	/**
	 * expect the a file of form 
	 ******** 
	 * tp1
	 * 
	 * tp2 
	 ********
	 * with more then one subquery 
	 ******** 
	 * tp11 tp12
	 * 
	 * tp21 tp22
	 ********
	 * @param optionValue
	 * @return
	 * @throws FileNotFoundException
	 */
	private String[][] parseSubqueries(String subqueries)
			throws FileNotFoundException {

		List<String> subq1 = new ArrayList<String>();
		List<String> subq2 = new ArrayList<String>();

		List<String> subq = subq1;
		Scanner s = new Scanner(new File(subqueries));
		int i = 0;
		while (s.hasNextLine()) {
			String line = s.nextLine().trim();
			subq.add(line);
			if (line.length() == 0)
				subq = subq2;
		}
		String[][] subQ = new String[2][];
		subQ[0]= new String[subq1.size()];
		subQ[1]= new String[subq2.size()];
		subQ[0] = (String[]) subq1.toArray(subQ[0]);
		subQ[1] = (String[]) subq2.toArray(subQ[1]);

		return subQ;
	}

	/**
	 * Expect a file of the following format URI1 URI2
	 * 
	 * @param endpointConfig
	 * @return
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 */
	private URI[] parseEndpoint(String endpointConfig)
			throws FileNotFoundException, URISyntaxException {
		URI[] ep = new URI[2];
		Scanner s = new Scanner(new File(endpointConfig));
		int i = 0;
		while (s.hasNextLine()) {
			ep[i] = new URI(s.nextLine());
			i++;
		}
		return ep;
	}
	
	public static void main(String[] args) {
		String[] joins = {
//				"VALUES",
//				"UNION",
//				"SERVICE",
				"FILTER",
//				"NESTED",
//				"SYMHASH",
				};
		System.out.println("Joins "+Arrays.toString(joins));
		File outDir = new File("test.outdir");
		if (!outDir.exists())
			outDir.mkdirs();
		
		String localEndpoint = "http://localhost:3030/mgi/sparql";
		localEndpoint = "http://localhost:3230/linkedmdb/sparql";
//		File subQueries = new File("resources/queries/bio/carlos_join3.ini");
		File subQueries = new File("resources/queries/movies/query6.txt");
//		File subQueries = new File("resources/queries/bio/carlos_join8.txt");
		
		boolean debug = false;
		int batch = -1;
		AVG run = new AVG();
		if(subQueries.isDirectory()){
			for(File q : subQueries.listFiles()){
				if(q.isFile()){
					run.runQuery(q, joins, localEndpoint,outDir,debug, batch);
				}
			}
		}else{
			run.runQuery(subQueries, joins, localEndpoint,outDir,debug, batch);
		}
	}
}