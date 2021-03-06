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
	// int arg_num;
	HashMap<Integer, VaporValue> variable_map;
	HashMap<String, Integer> identifier_map;
	//List<Integer> call_parameters_ticket;
 	Vector<Integer> call_parameters_ticket;
  	Stack<Vector<Integer>> call_list;

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
		//call_parameters_ticket = new ArrayList<Integer>();
		call_list = new Stack<Vector<Integer>>();
		const_num = "";

	}

	void startParseClass(String class_name) {
		curr_class = Helper.getClass(class_name, classList);
		if (curr_class == null) {
			System.out.println("Can't find class");
		}
	}

	void endParseClass() {
		curr_class = null;
	}

	void startParseMethod() {

		variable_map = new HashMap<Integer, VaporValue>();
		identifier_map = new HashMap<String, Integer>();
		var_num = 0;
		tmp_num = 0;

		// load curr class addr
		int ticket;
		ticket = getIdentifier("this");
		variable_map.get(ticket).class_name = curr_class.class_name;

		// load class field
		for (int i = 0; i < curr_class.fields.size(); i++) {
			String obj_name = curr_class.fields_name.get(i);
			ticket = getIdentifier(obj_name);
			variable_map.get(ticket).class_name = Helper.getObject(obj_name, curr_class).toString();
		}

		// load parent class fields - only used by ClassExtendsDeclaration
		
		ClassType tmp = curr_class.super_class;
		while (tmp != null) {
			for (int i = 0; i < tmp.fields.size(); i++) {
				String obj_name = tmp.fields_name.get(i);
				ticket = getIdentifier(obj_name);
				variable_map.get(ticket).class_name = Helper.getObject(obj_name, tmp).toString();
			}
			tmp = tmp.super_class;
		}
		

		// add method variables to var_map and identifier_map
		if (!method_name.equals("main")) {
			Method curr_method = Helper.getMethod(method_name, curr_class);
			for (int i = 0; i < curr_method.vars.size(); i++) {
				String method_var = curr_method.vars_name.get(i);
				ticket = getIdentifier(method_var);
				VaporValue v = new VaporValue(method_var);
				variable_map.put(ticket, v);
				identifier_map.put(method_var, ticket);
				variable_map.get(ticket).class_name = curr_method.vars.get(i).toString();
			}
		}

		// add method arg to var_map and identifier_map
		if (!method_name.equals("main")) {
			Method curr_method = Helper.getMethod(method_name, curr_class);
			for (int i = 0; i < curr_method.args.size(); i++) {
				String method_arg = curr_method.args_name.get(i);
				ticket = getIdentifier(method_arg);
				VaporValue v = new VaporValue(method_arg);
				variable_map.put(ticket, v);
				identifier_map.put(method_arg, ticket);
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

/*
	void clearCallParam() {
		call_parameters_ticket.clear();
		call_parameters_const.clear();
	}
	*/

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
		} else {
			_ret = out;
		}
		return _ret;
	}

	int addTemp() {
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
			v = new VaporValue("if" + tmp + "_else");
			variable_map.put(ticket, v);
			ticket = addVarNum();
			v = new VaporValue("if" + tmp + "_end");
		} else if (type.equals("while")) {
			v = new VaporValue("while" + tmp + "_top");
			variable_map.put(ticket, v);
			ticket = addVarNum();
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
			ticket = addTemp();
			s = findVariableEnv(ticket);
			System.out.println(s + " = " + t);

			return s;
		}

		//Check if variable is in parent class
		offset = offset + 1 + curr_class.fields.size();

		ClassType tmp = curr_class.super_class;
    	while (tmp != null) {
    		if (tmp.fields_name.contains(s)) {
				for (int i = 0; i < indentation_level; i++) {
					System.out.printf("  ");
				}
				offset = tmp.fields_name.indexOf(s) + offset;
				t = "[this+" + offset * 4 + "]";
				ticket = addTemp();
				s = findVariableEnv(ticket);
				System.out.println(s + " = " + t);
				return s; 
			}
      		offset = offset + tmp.fields.size();
      		tmp = tmp.super_class;
    	}

		return s;
	}

	String findVariableEnv_left(int ticket) {
		if (ticket == -1) {
			return const_num;
		}

		String s = variable_map.get(ticket).identifier;
		String t;
		int offset = 0;

		// Var is in current class field 
		if (curr_class.fields_name.contains(s)) {
			for (int i = 0; i < indentation_level; i++) {
				System.out.printf("  ");
			}
			offset = curr_class.fields_name.indexOf(s);
			s = "[this+" + (offset + 1) * 4 + "]";
			return s; 
		}

		offset = offset + 1 + curr_class.fields.size();

		ClassType tmp = curr_class.super_class;
    	while (tmp != null) {
    		if (tmp.fields_name.contains(s)) {
				for (int i = 0; i < indentation_level; i++) {
					System.out.printf("  ");
				}
				offset = tmp.fields_name.indexOf(s) + offset;
				s = "[this+" + offset * 4 + "]";
				return s; 
			}
      		offset = offset + tmp.fields.size();
      		tmp = tmp.super_class;
    	}

		//Var is in parent class field


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
