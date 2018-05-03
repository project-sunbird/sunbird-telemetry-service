package telemetry.dispatcher;

import java.util.List;

/**
 * 
 * @author Mahesh Kumar Gangula
 *
 */

public interface  IDispatcher {

	public void dispatch(List<String> events) throws Exception;
}
