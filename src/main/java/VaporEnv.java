import java.util.*;

public class VaporEnv{
	private List<ClassType> classList;
	static int indent;
	private int reg_avail;
	//HashMap<ClassType, Integer> active_class;
	//List<String> regs; 
	
	
	VaporEnv(List<ClassType> classList){
		indent = 0;
		this.classList = classList; 
		reg_avail = 0; 
	}
	
	public static void indent() {
		for (int i = 0; i < indent; i++) {
	      System.out.printf("  ");
	    }		
	}
	
	public String getVar(String var_name, ClassType curr_class, Method curr_method) {
		int var_location = Helper.whereObject(var_name, curr_method, curr_class);
		
		//obj in Method args
		switch(var_location) {
			case -4: Helper.exit("Not Found variable");
			case -3:
				break; 
			case -2:
				return var_name; 
			default: 
				return "this + " + var_location * 4 ;
		}
		return " ";
	}
	
	
	public boolean isAssigned (ClassType curr_class) {
		return (active_class.containsKey(curr_class));
	}
	
	public void assignedClass(ClassType curr_class) {

	}
}
