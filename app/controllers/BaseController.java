package controllers;

import java.util.concurrent.ConcurrentHashMap;

import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Histogram;

public abstract class BaseController extends Controller {

	protected static ThreadLocal<Long> startTimeMillis = new ThreadLocal<Long>();
	protected static ConcurrentHashMap<String, Histogram> histograms;
	static {
		Metrics.defaultRegistry();
		histograms = new ConcurrentHashMap<String, Histogram>();
	}
	
	@Before
	public static void startTimer() {
		startTimeMillis.set(System.currentTimeMillis());
	}
	
	@After
	public static void markRequestDuration() {
		String key = request.controller + "." + request.actionMethod;
		Histogram histo = histograms.get(key);
		if(histo == null) {
			histo = Metrics.newHistogram(request.controllerClass, request.actionMethod, "requests");
			//note: this is lazy. I don't really need to worry about multiple Histogram
			//objects being created thanks to the underlying way that Metrics.newHistogram does
			//its getOrAdd under the covers. I would need to do those same checks here
			//if that were not the case, this is not an example of a threadsafe way
			//to add stateful objects to a concurrent hash map
			histograms.putIfAbsent(key, histo);
		}
		histo.update( System.currentTimeMillis() - startTimeMillis.get());
		
	}
}
