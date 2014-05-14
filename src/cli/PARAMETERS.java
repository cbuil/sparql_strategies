package cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;

public class PARAMETERS {

	/*
	 * Commandline parameters
	 */
	protected static final String PARAM_HELP = "?";
	protected static final String PARAM_HELP1 = "h";
	
	/**
	 * HELP
	 */
	
	static final OptionGroup OPTIONGROUP_HELP = new OptionGroup();
	
	static{
		OPTIONGROUP_HELP.addOption(OptionBuilder.withArgName("help")
				.hasArgs(0)
				.withDescription("print help screen")
				.create(PARAM_HELP));
		OPTIONGROUP_HELP.addOption(OptionBuilder.withArgName("help")
		.hasArgs(0)
		.withDescription("print help screen")
		.create(PARAM_HELP1));
	}
	
	protected static final String PARAM_DATA_DIR = "d";
	public static final Option OPTION_DATA_DIR = OptionBuilder.withArgName("directory")
	.hasArgs(1).withLongOpt("")
	.withDescription("")
	.create(PARAM_DATA_DIR);
	
	protected static final String PARAM_INPUT = "i";
	public static final Option OPTION_INPUT = OptionBuilder.withArgName("RATIO")
	.hasArgs(1).withLongOpt("")
	.withDescription("")
	.create(PARAM_INPUT);
	
	protected static final String PARAM_CARD = "c";
	public static final Option OPTION_CARD = OptionBuilder.withArgName("CARD")
	.hasArgs(1).withLongOpt("")
	.withDescription("")
	.create(PARAM_CARD);
	
	protected static final String PARAM_JOIN = "j";
	public static final Option OPTION_JOIN = OptionBuilder.withArgName("JOIN")
	.hasArgs(1).withLongOpt("")
	.withDescription("")
	.create(PARAM_JOIN);
	
	protected static final String PARAM_OUTPUT_DIR = "o";
	public static final Option OPTION_OUTPUT_DIR = OptionBuilder.withArgName("dir")
	.hasArgs(1).withLongOpt("")
	.withDescription("")
	.create(PARAM_OUTPUT_DIR);
	
	public static final String PARAM_ENDPOINTS = "e";
	public static final Option OPTION_ENDPOINTS = OptionBuilder.withArgName("localEndpoint")
			.hasArgs(1).withLongOpt("")
			.withDescription("local endpoint URI")
			.create(PARAM_ENDPOINTS);
			
	public static final String PARAM_SUBQUERIES = "q";
	public static final Option OPTION_SUBQUERIES =  OptionBuilder.withArgName("query.ini")
			.hasArgs(1).withLongOpt("")
			.withDescription("query config file")
			.create(PARAM_SUBQUERIES);
	public static final String PARAM_DEBUG = "l";
	public static final Option OPTION_DEBUG =  OptionBuilder.withArgName("debug flag")
			.hasArgs(0).withLongOpt("")
			.withDescription("debug flag")
			.create(PARAM_DEBUG);
	public static final String PARAM_BATCHSIZE = "b";
	public static final Option OPTION_BATCHSIZE  =  OptionBuilder.withArgName("batch size")
			.hasArgs(1).withLongOpt("")
			.withDescription("batch size")
			.create(PARAM_BATCHSIZE);
	
}
