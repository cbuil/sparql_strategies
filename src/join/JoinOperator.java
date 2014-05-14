package join;

import utils.SplitQuery;
import bench.BenchmarkResult;

public interface JoinOperator {

	BenchmarkResult executeHTTP(SplitQuery sq, boolean debug);
}
