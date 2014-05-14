package utils;

import com.hp.hpl.jena.sparql.syntax.Element;

public class SplitQuery {

	private Element _p1;
	private String _p1EP;
	private Element _p2;
	private String _p2EP;
	private int _batch;

	public SplitQuery(Element p1, String p1EP, Element p2, String p2EP) {
		_p1 = p1;
		_p1EP= p1EP;
		_p2 = p2;
		_p2EP= p2EP;
		
	}

	public Element getP1() {
		return _p1;
	}
	public Element getP2() {
		return _p2;
	}

	public String getP2Endpoint() {
		return _p2EP;
	}

	public String getP1Endpoint() {
		return _p1EP;
	}
	
	@Override
	public String toString() {
		return "SplitQuery:\n___\nP1 (EP: "+_p1EP+")\n"+_p1+"\nP2 (EP: "+_p2EP+"\n"+_p2+")\n___";
	}

	public void setBatch(int batch) {
		_batch = batch;
		
	}
	public int getBatch(){
		return _batch;
	}

}
