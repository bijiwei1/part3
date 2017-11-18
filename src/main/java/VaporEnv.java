import java.util.*;

public class VaporEnv {

	List<ClassType> classList;
	ClassType curr_class;
	String method_name;
	
	// Used only in second pass
	int indentation_level;
	int[] label_num = new int[5]; // 0 - if_else, 1 - while, 2 - null, 3 - bounds, 4 - other labels
	int tmp_num;
	int var_num;
	//int arg_num;
	HashMap<Integer, VaporValue> variable_map;
	HashMap<String, Integer> identifier_map;
	List<Integer> call_parameters_ticket;
	List<String> call_parameters_const;
	String const_num;
	

	public VaporEnv(List<ClassType> classList) {
		this.classList = classList;

		indentation_level = 0;
		label_num[0] = 1;
		label_num[1] = 1;
		label_num[2] = 1;
		label_num[3] = 1;
		label_num[4] = 1;
		tmp_num = 0;
		var_num = 0;
		variable_map = null;
		identifier_map = null;
		call_parameters_ticket = new ArrayList<Integer>();
		call_parameters_const = new ArrayList<String>();
		// call_list = new Stack<List<Integer>>();
		const_num = "";

	}

	void startParseClass(String class_name) {
		curr_class = Helper.getClass(class_name, classList);
	}

	void endParseClass() {
		curr_class = null;
	}

	void startParseMethod() {
		
		variable_map = new HashMap<Integer, VaporValue>();
		identifier_map = new HashMap<String, Integer>();
		var_num = 0;
		tmp_num = 0;
		int ticket;
		ticket = getIdentifier("this");
		
		variable_map.get(ticket).class_name = curr_class.class_name;

		//load class field
		for (int i = 0; i < curr_class.fields.size(); i++) {
			String obj_name = curr_class.fields_name.get(i);
			ticket = getIdentifier(obj_name);
			variable_map.get(ticket).class_name = Helper.getObject(obj_name, curr_class).toString();
		}
		
		//add method field to var_map and identifier_map
		if (!method_name.equals("main")) {
			Method curr_method = Helper.getMethod(method_name, curr_class);
			for (int i = 0; i < curr_method.vars.size(); i++) {
				String method_var = curr_method.vars_name.get(i);
				ticket = 1000 + i;
				VaporValue v = new VaporValue(method_var);
				variable_map.put(ticket, v);
				identifier_map.put(method_var, ticket);
				System.out.println("Add fields to variable map" + method_var);
				variable_map.get(ticket).class_name = curr_method.args.get(i).toString();
			}
		}
	}

	void endParseMethod() {
		variable_map = null;
		identifier_map = null;
		var_num = 0;
		tmp_num = 0;
	}

	void clearCallParam() {
		call_parameters_ticket.clear();
		call_parameters_const.clear();
	}

	// Methods to support environment variable operations

	int addVarNum() {
		var_num += 1;
		return var_num - 1;
	}

	int addTmpNum() {
		tmp_num += 1;
		return tmp_num - 1;
	}

	int addLabel(String type) {

		if (type.equals("if_else")) {
			label_num[0] += 1;
			return label_num[0] - 1;
		} else if (type.equals("while")) {
			label_num[1] += 1;
			return label_num[1] - 1;
		} else if (type.equals("null")) {
			label_num[2] += 1;
			return label_num[2] - 1;
		} else if (type.equals("bounds")) {
			label_num[3] += 1;
			return label_num[3] - 1;
		} else {
			label_num[4] += 1;
			return label_num[4] - 1;
		}
	}

	int getIdentifier(String identifier) {

		Integer out = identifier_map.get(identifier);
		int _ret;
		int ticket = 0;
		
		if (out == null) {
			ticket = addVarNum();
			VaporValue v = new VaporValue(identifier);
			variable_map.put(ticket, v);
			identifier_map.put(identifier, ticket);
			_ret = ticket;
		}else {
			_ret = out;
		}
		return _ret;
	}

	int checkVar(String identifier) {
		Integer out = identifier_map.get(identifier);
		if (out != null)
			return out; 
		return -1;
	}
	
	int getTemporary() {
		
		int ticket = addVarNum();
		int tmp = addTmpNum();

		VaporValue v = new VaporValue("t." + tmp);
		variable_map.put(ticket, v);
		return ticket;
	}

	int getLabel(String type) {
		int ticket = addVarNum();
		int tmp = addLabel(type);

		VaporValue v;
		if (type.equals("if_else")) {
			v = new VaporValue("if" + tmp + "_end");
		} else if (type.equals("while")) {
			v = new VaporValue("while" + tmp + "_end");
		} else if (type.equals("null")) {
			v = new VaporValue("null" + tmp);
		} else if (type.equals("bounds")) {
			v = new VaporValue("bounds" + tmp);
		} else {
			v = new VaporValue("label" + tmp);
		}

		variable_map.put(ticket, v);
		return ticket;
	}

	String findVariableEnv(int ticket) {
		if (ticket == -1) {
			return const_num;
		}
		
		if (ticket > 1000) {
			String s = variable_map.get(ticket).identifier;
			return s;
		}
		
		String s = variable_map.get(ticket).identifier;
		String t;
		int offset = 0;

		// Var is in class field
		if (curr_class.fields_name.contains(s)) {
			for (int i = 0; i < indentation_level; i++) {
				System.out.printf("  ");
			}
			offset = curr_class.fields_name.indexOf(s);
			t = "[this+" + (offset + 1) * 4 + "]";
			ticket = getTemporary();
			s = findVariableEnv(ticket);
			System.out.println(s + " = " + t);
		}

		return s;
	}

}

class VaporValue {
	String identifier;
	String class_name;

	VaporValue(String input) {
		identifier = input;
		class_name = null;
	}
}
