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

    //getParentTypes(curr_class, variable_map);
  }

  /*
  void getParentTypes(J2VClassLayout j, HashMap<Integer, VaporValue> h) {
    int ticket;
    for (String id : j.member_offsets.keySet()) {
      ticket = getIdentifier(id);
      variable_map.get(ticket).class_name = j.member_types.get(id);
    }

    if (j.parent != null) {
      getParentTypes(layout.get(j.parent), h);
    }
  }
  */
  
  void endParseMethod() {
    variable_map = null;
    identifier_map = null;
    counter_var = 0;
    counter_temp = 0;
  }


  //Methods to support environment variable operations
  ///////////////////////

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

  //Methods to handle the adding of new identifiers
  ///////////////////
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

  /*
  int findMemberOffset(String class_name, String member_name) {
//    J2VClassLayout j = layout.get(class_name);
    return layout.get(class_name).member_offsets.get(member_name);
  }*/


  //////////////////////
}

/*
class J2VClassLayout {
  String id;
  String parent;
  int size;

  Vector<String> function_list;
  Vector<String> member_list;
  HashMap<String, Integer> virtual_table;
  HashMap<String, Integer> member_offsets;
  HashMap<String, String> member_types;
  HashMap<String, String> method_types;
}
*/

class VaporValue {
  String identifier;
  String class_name;
  VaporValue(String input) {
    identifier = input;
    class_name = null;
  }
}

