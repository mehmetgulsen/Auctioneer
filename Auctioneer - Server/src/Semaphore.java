//Mehmet Gülþen
//2013400075
//mehmetgulsen95@hotmail.com
//CMPE436-Term

/*
 * Binary Semaphore from the slides on Piazza.
 */
public class Semaphore {

    private int value;

    public Semaphore(int value) {
        this.value = value;
    }

    public synchronized void P() {
        if (value == 0) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
        value = 0;
    }

    public synchronized void V() {
    	value = 1;
        notifyAll();   
    }

}