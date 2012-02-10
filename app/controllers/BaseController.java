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
	protected static void startMetrics() {
		Histogram histo = Metrics.newHistogram(request.controllerClass, request.actionMethod, "requests");
		histo = histograms.put(request.controller + "." + request.actionMethod, histo);	
	}
	
	@After
	public static void markRequestDuration() {
		Histogram histo = histograms.get(request.controller + "." + request.actionMethod);
		histo.update( System.currentTimeMillis() - startTimeMillis.get());
		
	}
}
