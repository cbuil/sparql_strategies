package bench;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

public class CONSTANTS {
	private final static Model model = ModelFactory.createDefaultModel();
	public final static String NS = "http://e.com/v#";
	
	public static final Property L_PRED= model.createProperty(CONSTANTS.NS+"pred_left");
	public static final Property R_PRED= model.createProperty(CONSTANTS.NS+"pred_right");
	
	
	public static final String L_EP = "http://localhost:3030/ds/query";
	public static final String R_EP = "http://localhost:3031/ds/query";
	
	private static final String L_QUERY_STRING="SELECT DISTINCT * WHERE { ?join <"+L_PRED+"> ?ol . }";
	public static final Query L_QUERY = QueryFactory.create(L_QUERY_STRING);
	
	
	private static final String R_SubSub_QUERY_STRING="SELECT DISTINCT * WHERE { ?join <"+R_PRED+"> ?or . }";
	public static final Query R_SubSub_QUERY = QueryFactory.create(R_SubSub_QUERY_STRING);
	
	private static final String R_SubObj_QUERY_STRING="SELECT DISTINCT * WHERE { ?sr <"+R_PRED+"> ?join . }";
	public static final Query R_SubObj_QUERY = QueryFactory.create(R_SubObj_QUERY_STRING);
	
	
	
	public static final String L_DATA="data_left.nt";
	public static final String R_SubSub_DATA="data_subsub_right.nt";
	public static final String R_SubObj_DATA="data_subobj_right.nt";
	
}
