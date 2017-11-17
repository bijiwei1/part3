import visitor.*;
import syntaxtree.*;
import java.util.*;

public class VaporEnv {
    
  List<ClassType> classList; 
  ClassType curr_class;
  
  //Used only in second pass
  int indentation_level;
  int counter_label;
  int counter_temp;
  int counter_var;
  HashMap<Integer, VaporValue> variable_map;
  HashMap<String, Integer> identifier_map;
  Vector<Integer> call_parameters;
  Stack<Vector<Integer>> call_list;
  
  public VaporEnv(List<ClassType> classList) {
    this.classList = classList;

    indentation_level = 0;
    counter_label = 0; //Only one instance of a label allowed in the entire program woooot
    counter_temp = 0;
    counter_var = 0;
    variable_map = null;
    identifier_map = null;

    call_parameters = new Vector<Integer>();
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
    counter_var = 0;
    counter_temp = 0;
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
    counter_var = 0;
    counter_temp = 0;
  }


  //Methods to support environment variable operations

  int obtainVarNumber() {
    counter_var += 1;
    return counter_var - 1;
  }

  int obtainTempNumber() {
    counter_temp += 1;
    return counter_temp - 1;
  }

  int obtainLabelNumber() {
    counter_label += 1;
    return counter_label - 1;
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
    int temp = obtainTempNumber();

    VaporValue v = new VaporValue("t." + String.valueOf(temp)); 
    variable_map.put(ticket, v);
    return ticket;
  }


  int getLabel() {
    int ticket = obtainVarNumber();
    int temp = obtainLabelNumber();

    VaporValue v = new VaporValue("control" + String.valueOf(temp));  
    variable_map.put(ticket, v);
    return ticket;
  }
  

  String findVariableEnv(int ticket) {
    String s = variable_map.get(ticket).identifier;
    String t;
    int offset = 0;
    
    if (curr_class.fields_name.contains(s)) {
    	offset = curr_class.fields_name.indexOf(s)+4;
    	t = "[this+" + String.valueOf(offset) + "]";
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
    	 s = "[this+" + String.valueOf(offset) + "]";
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

