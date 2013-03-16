package communication;

import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;
/**
 * A synchronized fifo buffer that is used to pass messages between threads.
 *
 */
public class Buffer {
	
	
	private ArrayDeque<String> deque;
	private Semaphore mutex;
	private Semaphore full;
	private Semaphore empty;
	
	
	public Buffer(){
		deque = new ArrayDeque<String>();
		mutex = new Semaphore(1);
		empty = new Semaphore(0);
		full = new Semaphore(10);
	}
	
	public void write(String message) throws InterruptedException{

			full.acquire();
			mutex.acquire();
			deque.add(message);
			mutex.release();
			empty.release();

		
	}
	
	
	public String read() throws InterruptedException{
		String message = "";

			empty.acquire();
			mutex.acquire();
			message = deque.remove();
			mutex.release();
			full.release();
			
				
		return message;
	}
}
