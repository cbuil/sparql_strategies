package cli;



import java.util.Arrays;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.LoggerFactory;

public abstract class CLIObject {
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(CLIObject.class);
	protected Options _opts;

	public abstract String getDescription();

	public String getCommand(){
		return this.getClass().getSimpleName();
	}

	
	
		
	protected void init() {
		_opts = new Options();
		_opts.addOptionGroup(PARAMETERS.OPTIONGROUP_HELP);
		addOptions(_opts);
	}

	/** SPECIFIC OPTIONS **/
	
	
		
	/**
	 * add all Option(Groups) to this object
	 * Note: The help flag is set automatically ("?")
	 * @param opts
	 */
	abstract protected void addOptions(Options opts);

	public void run(String[] args) {
		log.info("=======[START] ("+Arrays.toString(args)+")");
		
		CommandLine cmd = verifyArgs(args);
		
		long start = System.currentTimeMillis();
		execute(cmd);
		
		long end = System.currentTimeMillis();
		log.info("=======[END] ("+(end-start)+" ms)");
	}

	abstract protected void execute(CommandLine cmd);

	protected CommandLine verifyArgs(String[] args) {
		init();

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(_opts, args);
		} catch (org.apache.commons.cli.ParseException e) {
			log.info("ERROR: "+e.getClass().getSimpleName()+" : "+e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getSimpleName(), _opts ,true);
			System.exit(-1);
		}
		if(args.length==0 || cmd.hasOption(PARAMETERS.PARAM_HELP)||cmd.hasOption(PARAMETERS.PARAM_HELP1)){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(this.getClass().getSimpleName(), _opts ,true);
			System.exit(-1);
		}
		return cmd;
	}


}
