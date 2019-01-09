package interpreter;

public class Placeholder {
	
	public String name;
	public Object value;
	
	public Placeholder(String name, Object value){
		this.name = name;
		this.value = value;
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Placeholder)) {
			return false;
		}
		
		if(((Placeholder)obj).name.equals(name)) {
			return true;
		}
		
		return false;
	}

}
