package newt_metatypes;

public class NewtInstance {

	private NewtClass newtClass;

	NewtInstance(NewtClass newtClass) {
		this.newtClass = newtClass;
	}

	@Override
	public String toString() {
		return newtClass.name + " instance";
	}
}
