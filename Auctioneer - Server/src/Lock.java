//Mehmet Gülþen
//2013400075
//mehmetgulsen95@hotmail.com
//CMPE436-Term

/*
 * Reader-Writer solution 2 from lecture notes
 */
public class Lock {
	int numReaders = 0;
	Semaphore mutex = new Semaphore(1);
	Semaphore wlock = new Semaphore(1);
	
	public void startRead(){
		mutex.P();
		numReaders++;
		if(numReaders == 1)
			wlock.P();
		mutex.V();
	}
	
	public void endRead(){
		mutex.P();
		numReaders--;
		if(numReaders==0){
			wlock.V();
		}
		mutex.V();
	}
	
	public void startWrite(){
		wlock.P();
	}

	public void endWrite(){
		wlock.V();
	}
}
