package newt_metatypes;

public class NewtReturn extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Object value;
	
	public NewtReturn(Object value) {
		this.value = value;
	}
}
