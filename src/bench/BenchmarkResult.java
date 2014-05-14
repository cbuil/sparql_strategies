package bench;

import java.util.HashMap;
import java.util.Set;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class BenchmarkResult  extends HashMap<String, Object>{

	public static final String RESULTS = "Results";
	public static final String RESULTSIZE = "Resultsize";
	public static final String TOTALTIME = "TotalTime";
	public static final String CALLS = "Calls";
	public static final String EPCALL = "epCall";
	private static final String EXCEPTION = "exception";
	private static final String INTERIM = "interim";
	private static final String BATCH_SIZE = "batch";
	
	public void setResults(Set<Binding> results) {
		this.put(RESULTS, results);
		this.put(RESULTSIZE, results.size());
	}

	public int getResultSize(){
		return this.containsKey(RESULTSIZE)? (int) this.get(RESULTSIZE):-1;
	}

	public void setTotalTime(long l) {
		this.put(TOTALTIME, l);
	}
	
	/**
	 * [RESULTS] [TIME] [EXCEPTION]
	 */
	public String toString() {
		return "[RESULTS] "+getResultSize()+" [TIME] "+getTotalTime()+" [EXCEPTION] "+getException();
		
	}
	
	private String getException() {
		return this.containsKey(EXCEPTION)?  (String) this.get(EXCEPTION):"";
	}

	public long getTotalTime() {
		return this.containsKey(TOTALTIME)? (long) this.get(TOTALTIME):-1;
	}

	public void epCall(int i) {
		String key = EPCALL+i;
		int v =  this.containsKey(key)? (int) this.get(key):0;
		this.put(key, v+1);
	}
	
	public int getEpCall(int i){
		String key = EPCALL+i;
		return  this.containsKey(key)? (int) this.get(key):-1;
	}
	
	public String getShortString(String delim){
		StringBuilder sb = new StringBuilder();
		sb.append(getResultSize())
		.append(delim).append(getTotalTime())
		.append(delim).append(getInterimResults("0"))
		.append(delim).append(getInterimResults("1"))
		.append(delim).append(getEpCall(0))
		.append(delim).append(getEpCall(1))
		.append(delim).append(getBatchSize())
		.append(delim).append(getException());
		
		
		return sb.toString();
	}

	public void setBatchSize(int size) {
		this.put(BATCH_SIZE, size);
	}
	
	public int getBatchSize() {

		return this.containsKey(BATCH_SIZE)? (int) this.get(BATCH_SIZE):-1;
	}

	public Set<Binding> getResults() {
		return (Set<Binding>) this.get(RESULTS);
		
	}

	public void setException(Exception e) {
		this.put(EXCEPTION, e.getClass().getSimpleName());
//		+" "+e.getMessage());
		
	}

	public void setInterimResults(String string, int c) {
		this.put(INTERIM+"#"+string, c);
	}
	public int getInterimResults(String string) {
		String key = INTERIM+"#"+string;
		return  this.containsKey(key)? (int) this.get(key):-1;
	}

}