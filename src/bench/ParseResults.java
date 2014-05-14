package bench;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class ParseResults {

	public static void main(String[] args) throws FileNotFoundException {
		String []results = {
				"resources/queries/bio/fuseki/results_3.tsv",
				"resources/queries/bio/sesame/results_sesame_3.tsv",

		};

		
		Map<String , Map<String, Integer[][]>> joinQueryResults = new TreeMap<String , Map<String, Integer[][]>>();
		Integer [] batches = {-1,250,500,750};


		for(Integer b: batches){
			int idx=0;
			System.out.println(">>>"+b);
			for(String f: results){
				Scanner s = new Scanner(new File(f));
				while(s.hasNextLine()){
					String []tt = s.nextLine().trim().split("\t");
					if(tt.length < 9) continue;
					//					System.out.println(Arrays.toString(tt));
					String q = tt[0];
					String j = tt[1];

					Map<String, Integer[][]> joinMap = joinQueryResults.get(j);
					if(joinMap == null){
						joinMap = new TreeMap<String, Integer[][]>();
						joinQueryResults.put(j, joinMap);
					}
					//parseResults
					Integer[] r = new Integer[7];
					for(int a = 2; a<9; a++){
						r[a-2] = Integer.valueOf(tt[a]);
					}
					if( (!r[6].equals(b)) && (!j.equals("SYMHASH") && !j.equals("NESTED"))) {
						System.out.println(b+","+j+"= "+r[6]);
						if(r[6].equals(b)) System.out.println("what");
						continue;
					}

					if( r[0] == -1 ){
						r[0] = parseError(tt[9]);
					}
					Integer[][] i = joinMap.get(q);
					if(i==null){
						i = new Integer[results.length][];
						joinMap.put(q, i);
					}
					i[idx]=r;
				}
				idx++;
			}
			System.out.println("===== "+b+" =====");

			idx=0;
			for(String s: results){
				StringBuilder header = new StringBuilder();
//				StringBuilder h2 = new StringBuilder();
				StringBuilder sb = new StringBuilder();
				StringBuilder sbTime = new StringBuilder();
				boolean hp = false;
				for(Entry<String, Map<String, Integer[][]>> ent : joinQueryResults.entrySet()){
					sb.append(ent.getKey());
					sbTime.append(ent.getKey());
					int a =1;
					for(Entry<String, Integer[][]> e : ent.getValue().entrySet()){
						if( !hp){
							header.append("& B").append(a++).append("");
//							h2.append("&F&S");
						}
//						for(Integer[] i: e.getValue()){
//							if(i!=null)
							System.out.println(b+" "+ent.getKey()+" "+e.getKey()+" "+Arrays.toString(e.getValue()[idx]));
								sb.append("& ").append(e.getValue()[idx][0]);
								sbTime.append("& ").append(e.getValue()[idx][1]);
//						}
					}
					hp = true;
					sb.append("\\\\\n");
					sbTime.append("\\\\\n");

				}
				header.append("\\\\\n");
//				h2.append("\\\\\n");
				
				File out = new File(s).getParentFile();
				PrintWriter pw = new PrintWriter(new File(out, "results_agg_"+b+"-"+new File(s).getName()+".tex"));
				pw.println(header.toString());
//				pw.println(h2.toString());
				pw.println(sb.toString());
				pw.close();
				
				pw = new PrintWriter(new File(out, "time_agg_"+b+"-"+new File(s).getName()+".tex"));
				pw.println(header.toString());
//				pw.println(h2.toString());
				pw.println(sbTime.toString());
				pw.close();
				
				idx++;
			}
//			
//			
//			StringBuilder header = new StringBuilder();
//			StringBuilder h2 = new StringBuilder();
//			StringBuilder sb = new StringBuilder();
//			boolean hp = false;
//			for(Entry<String, Map<String, Integer[][]>> ent : joinQueryResults.entrySet()){
//				sb.append(ent.getKey());
//
//				for(Entry<String, Integer[][]> e : ent.getValue().entrySet()){
//					if( !hp){
//						header.append("&\\multicolumn{2}{c}{").append(e.getKey()).append("}");
//						h2.append("&F&S");
//					}
//					for(Integer[] i: e.getValue()){
//						if(i!=null)
//							sb.append("& ").append(i[0]);
//					}
//				}
//				hp = true;
//				sb.append("\\\\\n");
//
//			}
//			header.append("\\\\\n");
//			h2.append("\\\\\n");
//			pw.println(header.toString());
//			pw.println(h2.toString());
//			pw.println(sb.toString());
//			pw.close();
//
//			pw = new PrintWriter(new File("resources/queries/bio/time_agg_"+b+".tex"));
//			header = new StringBuilder();
//			h2 = new StringBuilder();
//			hp = false;
//			sb = new StringBuilder();
//			for(Entry<String, Map<String, Integer[][]>> ent : joinQueryResults.entrySet()){
//				sb.append(ent.getKey());
//
//				for(Entry<String, Integer[][]> e : ent.getValue().entrySet()){
//					if( !hp){
//						header.append("&").append(e.getKey());
//						h2.append("&F&S");
//					}
//					for(Integer[] i: e.getValue()){
//						if(i!=null)
//							sb.append("& ").append(i[1]);
//					}
//				}
//				hp = true;
//				sb.append("\\\\\n");
//
//				//				pw.println(sb.toString());
//			}
//			header.append("\\\\\n");
//			h2.append("\\\\\n");
//
//			//			header.append("&\\multicolumn{2}{c}{").append(ent.getKey()).append("}")
//			pw.println(header.toString());
//			pw.println(h2.toString());
//			pw.println(sb.toString());
//			pw.close();
//			System.out.println(header.toString());
//			System.out.println(sb.toString());

		}


	}

	private static Integer parseError(String string) {
		if(string.equals("QueryExceptionHTTP")){
			return -1;
		}
		if(string.equals("ResultSetException")){
			return -2;
		}else{
			return -3;
		}
	}
}