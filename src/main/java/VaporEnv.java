import visitor.*;
import syntaxtree.*;
import java.util.*;

public class VaporEnv {
    
  List<ClassType> classList; 
  ClassType curr_class;
  
  //Used only in second pass
  int indentation_level;
  int[] label_num = new int[5]; // 0 - if_else, 1 - while, 2 - null, 3 - bounds, 4 - other labels
  int tmp_num;
  int var_num;
  HashMap<Integer, VaporValue> variable_map;
  HashMap<String, Integer> identifier_map;
  Vector<Integer> call_parameters;
  Stack<Vector<Integer>> call_list;
  
  public VaporEnv(List<ClassType> classList) {
    this.classList = classList;

    indentation_level = 0;
    label_num[0] = 0;
    label_num[1] = 0;
    label_num[2] = 0;
    label_num[3] = 0;
    label_num[4] = 0;
    tmp_num = 0;
    var_num = 0;
    variable_map = null;
    identifier_map = null;

    call_list = new Stack<Vector<Integer>>();

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

    for (int i = 0; i < curr_class.fields.size(); i++) {
    	String obj_name = curr_class.fields_name.get(i);
    	ticket = getIdentifier(obj_name);
    	variable_map.get(ticket).class_name = Helper.getObject(obj_name, curr_class).toString();
    }
  }

  void endParseMethod() {
	  System.out.println("Variable Map");
	  for (Integer ticket: variable_map.keySet()){
          String Vapor = variable_map.get(ticket).identifier;  
          System.out.println(ticket + " " + Vapor);  
      } 
	  System.out.println("Identifier Map");
	  for ( String idf: identifier_map.keySet()){
          Integer ticket = identifier_map.get(idf);  
          System.out.println(idf + " " + ticket.toString() );  
      } 
    variable_map = null;
    identifier_map = null;
    var_num = 0;
    tmp_num = 0;
  }


  //Methods to support environment variable operations

  int obtainVarNumber() {
	  var_num += 1;
    return var_num - 1;
  }

  int obtainTempNumber() {
	  tmp_num += 1;
    return tmp_num - 1;
  }

  int addLabel (String type) {
	  
	  if (type.equals("if_else")) {
		  label_num[0] +=1;
		  return label_num[0] - 1;
	  }else if (type.equals("while")) {
		  label_num[1] +=1;
		  return label_num[1] - 1;
	  }else if (type.equals("null")) {
		  label_num[2] +=1;
		  return label_num[2] - 1;
	  }else if (type.equals("bounds")) {
		  label_num[3] +=1;
		  return label_num[3] - 1;
	  }else {
		  label_num[4] +=1;
		  return label_num[4] - 1;
	  }
  }
  

  //Add new identifier to registers
  int getIdentifier(String identifier) {

    Integer out = identifier_map.get(identifier);
    int _ret = -1;
    int ticket = 0;

    if (out == null) {
      ticket = obtainVarNumber(); 
      VaporValue v = new VaporValue(identifier);
      variable_map.put(ticket, v);
      identifier_map.put(identifier, ticket);
      _ret = ticket;
    } else {
      _ret = out;
    }
    return _ret;
  }
  
  int getTemporary() {
    int ticket = obtainVarNumber();
    int tmp = obtainTempNumber();

    VaporValue v = new VaporValue("t." + tmp); 
    variable_map.put(ticket, v);
    return ticket;
  }


  int getLabel(String type) {
    int ticket = obtainVarNumber();
    int tmp = addLabel(type);
    
    VaporValue v ;
    if (type.equals("if_else")) {
    	v = new VaporValue("if" + tmp + "_end"); 
    }else if (type.equals("while")){
    	v = new VaporValue("while" + tmp + "_end"); 
    }else if (type.equals("null")) {
    	v = new VaporValue("null" + tmp); 
    }else if (type.equals("bounds")){
    	v = new VaporValue("bounds" + tmp); 
    }else {
    	v = new VaporValue("label" + tmp);
    }
    
    variable_map.put(ticket, v);
    return ticket;
  }
  

  String findVariableEnv(int ticket) {
    String s = variable_map.get(ticket).identifier;
    String t;
    int offset = 0;
    
    if (curr_class.fields_name.contains(s)) {
    	offset = curr_class.fields_name.indexOf(s)+4;
    	t = "[this+" + offset*4 + "]";
    	ticket = getTemporary();
    	s = findVariableEnv(ticket);
    	System.out.println(s + " = " + t);
        for (int i = 0; i < indentation_level; i++) {
          System.out.printf("  ");
        }
    }
    return s;
  }

  String findVariableEnvStrict(int ticket) {
    String s = variable_map.get(ticket).identifier;
    int offset = 0;
    
    if (curr_class.fields_name.contains(s)) {
    	offset = curr_class.fields_name.indexOf(s)+4;
    	 s = "[this+" + offset + "]";
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

