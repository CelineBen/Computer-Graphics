package comp557.a1;

public class Timer {

	private double elapsed = 0;
	
	private boolean running = false;
	
	private long startTime;
	
	public Timer() {
		start();
	}
	
	public void start() {
		if ( !running ) {
			running = true;
			startTime = System.nanoTime();
		}
	}
	
	public void stop() {
		if ( running ) {
			elapsed += 1e-9*(System.nanoTime() - startTime );		
			running = false;
		} 
	}
	
	public void reset() {
		elapsed = 0;
		if ( running ) {
			startTime = System.nanoTime();
		}
	}
	
	public void resetAndStart() {
		reset();
		start();
	}
	
	public double getElapsed() {
		double total = elapsed;
		if (running) {
	        total += 1e-9*( System.nanoTime() - startTime );
		}
		return total;
	}
	
}
