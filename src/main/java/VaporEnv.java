import visitor.*;
import syntaxtree.*;
import java.util.*;

public class VaporEnv {
    
  
  //Used only in second pass
  int indentation_level;
  int counter_label;
  int counter_temp;
  int counter_var;
  HashMap<Integer, VaporValue> variable_map;
  HashMap<String, Integer> identifier_map;
  Vector<Integer> call_parameters;
  Stack<Vector<Integer>> call_list;
  
  public J2VEnv() {
    layout = new HashMap<String, J2VClassLayout>();
    list_classes = new Vector<String>();
    cur_class = null;
    main_class = null;

    indentation_level = 0;
    counter_label = 0; //Only one instance of a label allowed in the entire program woooot
    counter_temp = 0;
    counter_var = 0;
    variable_map = null;
    identifier_map = null;

    call_parameters = new Vector<Integer>();
    call_list = new Stack<Vector<Integer>>();

  }

  // Class Layout Stuff
  ///////////////////////////
  void pushClass(String class_name, String parent_name) {
    cur_class = new J2VClassLayout();
    layout.put(class_name, cur_class);

    cur_class.id = class_name;
    cur_class.size = 4;

    cur_class.method_types = new HashMap<String, String>();
    cur_class.member_types = new HashMap<String, String>();
    cur_class.member_offsets = new HashMap<String, Integer>();
    cur_class.virtual_table = new HashMap<String, Integer>();
    cur_class.member_list = new Vector<String>();
    cur_class.function_list = new Vector<String>();

    list_classes.add(class_name);
    cur_class.parent = parent_name;
  }
  
  void popClass() {
    if (cur_class == null) {
      J2VError.throwError("Did not previously initialize class before popping"); 
    }
    cur_class = null;
  }

  void pushMember(String member_name, String member_type) {
    if (cur_class == null) {
      J2VError.throwError("Did not previously initialize class before adding member"); 
    }
    cur_class.member_list.add(member_name);
    cur_class.member_types.put(member_name, member_type);
  }

  void pushMethod(String method_name, String method_type) {
    if (cur_class == null) {
      J2VError.throwError("Did not previously initialize class before adding method"); 
    }
    cur_class.function_list.add(method_name);
    cur_class.method_types.put(method_name, method_type);
  }



  int createVirtualTable(String method_name, HashMap<String, String> function_list, HashMap<String, Integer> virtual_table) {
    J2VClassLayout cur = layout.get(method_name);
    String cur_parent = cur.parent;
    String cur_class = cur.id;
    int count_functions = 0;

    for (String cur_function : cur.function_list) {
      if (!function_list.containsKey(cur_function)) { 
        function_list.put(cur_function, cur_class); 
      }
    }
    if (cur.parent != null) {
      count_functions = createVirtualTable(cur_parent, function_list, virtual_table);
    }

    for (String cur_function : cur.function_list) {
      cur_class = function_list.get(cur_function); 
      virtual_table.put(cur_function, count_functions * 4); 
      count_functions += 1;
      System.out.println("  :" + cur_class + "." + cur_function);
    }

    return count_functions;
  }



  int createLayout(J2VClassLayout j, HashMap<String, Integer> h, HashMap<String, String> t, J2VClassLayout orig) {
    int offset = 4;
    if (j.parent != null) {
      offset = createLayout(layout.get(j.parent), h, t, orig);
    }
    for (String member : j.member_list) {
      if (!h.containsKey(member)) {
        h.put(member, offset); 
        offset += 4;
        orig.size += 4;
      }
    }
    for (String method : j.method_types.keySet()) {
      t.put(method, j.method_types.get(method));
    }
    return offset;
  }


  void createAllVirtualTables() {
    System.out.println("");
    System.out.println("");

    HashMap<String, String> function_list = null;
    HashMap<String, Integer> virtual_table = null;

    for (String cur_class : list_classes) {
      if (!cur_class.equals(main_class)) {
        System.out.println("const vmt_" + cur_class);
        function_list = new HashMap<String, String>();
        J2VClassLayout j = layout.get(cur_class);
        j.size = 4;
        virtual_table = j.virtual_table;
        createVirtualTable(cur_class, function_list, virtual_table);
        createLayout(j, j.member_offsets, j.method_types, j);
        System.out.println("");
      }
    }

    System.out.println("");
  }

  /////////////////////////////
  /////////////////////////////
  //Oh, how I do wish I could use my push pop notation for everything.
  //ALAS!
  //The following will be used in J2VParser

  void startParseClass(String class_name) {
    cur_class = layout.get(class_name);
  }

  void endParseClass() {
    cur_class = null;
  }

  void startParseMethod() {
    variable_map = new HashMap<Integer, VaporValue>();
    identifier_map = new HashMap<String, Integer>();
    counter_var = 0;
    counter_temp = 0;
    int ticket;
    ticket = getIdentifier("this");
    variable_map.get(ticket).class_name = cur_class.id; 

    getParentTypes(cur_class, variable_map);
  }

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
    if (cur_class.member_offsets.containsKey(s)) {
      offset = cur_class.member_offsets.get(s);

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
    if (cur_class.member_offsets.containsKey(s)) {
      offset = cur_class.member_offsets.get(s);
      s = "[this+" + String.valueOf(offset) + "]";
    }
    return s;
  }

  int findMemberOffset(String class_name, String member_name) {
//    J2VClassLayout j = layout.get(class_name);
    return layout.get(class_name).member_offsets.get(member_name);
  }


  //////////////////////
}



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

class VaporValue {
  String identifier;
  String class_name;
  VaporValue(String input) {
    identifier = input;
    class_name = null;
  }
}
