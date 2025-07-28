package bill.zkaifleet.context;


public class CycleDetectedException extends RuntimeException {
    private static final long serialVersionUID = 2433323606170664204L ;

	public CycleDetectedException ( ) { super ( ) ; }
	public CycleDetectedException(String msg) { super(msg); }
	public CycleDetectedException ( String msg, Throwable t ) { super ( msg, t ) ; }
	public CycleDetectedException ( Throwable t ) { super ( t ) ; }
}