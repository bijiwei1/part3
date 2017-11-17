import syntaxtree.*;
import visitor.*;
import java.util.*;

public class J2V_visitor extends GJNoArguDepthFirst<Integer> {
  VaporEnv env;
  List<ClassType> classList; 
  
  public J2V_visitor(VaporEnv env) {
    this.env = env;
    this.classList = env.classList;
  }

  /**
   * f0 -> MainClass()
   * f1 -> ( TypeDeclaration() )*
   * f2 -> <EOF>
   */
  public Integer visit(Goal n) {
    Integer _ret=null;
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    return _ret;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> "public"
   * f4 -> "static"
   * f5 -> "void"
   * f6 -> "main"
   * f7 -> "("
   * f8 -> "String"
   * f9 -> "["
   * f10 -> "]"
   * f11 -> Identifier()
   * f12 -> ")"
   * f13 -> "{"
   * f14 -> ( VarDeclaration() )*
   * f15 -> ( Statement() )*
   * f16 -> "}"
   * f17 -> "}"
   */
  public Integer visit(MainClass n) {
    Integer return_value=null;

    String curr_class = n.f1.f0.toString();
    env.startParseClass(curr_class);
    env.startParseMethod();

    stmtMethodParam(curr_class, "main");

    pushIndentation();

    n.f14.accept(this);
    n.f15.accept(this);

    indentVapor();
    System.out.println("ret");

    popIndentation();
    env.endParseMethod();
    env.endParseClass();


    return return_value;
  }

  /**
   * f0 -> ClassDeclaration()
   *       | ClassExtendsDeclaration()
   */
  public Integer visit(TypeDeclaration n) {
    Integer _ret=null;
    n.f0.accept(this);
    return _ret;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "{"
   * f3 -> ( VarDeclaration() )*
   * f4 -> ( MethodDeclaration() )*
   * f5 -> "}"
   */
  public Integer visit(ClassDeclaration n) {
    Integer _ret=null;

    String class_name = n.f1.f0.toString();
    env.startParseClass(class_name);

    n.f4.accept(this);

    env.endParseClass();
    return _ret;
  }

  /**
   * f0 -> "class"
   * f1 -> Identifier()
   * f2 -> "extends"
   * f3 -> Identifier()
   * f4 -> "{"
   * f5 -> ( VarDeclaration() )*
   * f6 -> ( MethodDeclaration() )*
   * f7 -> "}"
   */
  public Integer visit(ClassExtendsDeclaration n) {
    Integer _ret=null;

    String class_name = n.f1.f0.toString();
    env.startParseClass(class_name);

    n.f6.accept(this);

    env.endParseClass();

    return _ret;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   * f2 -> ";"
   */
  public Integer visit(VarDeclaration n) {
    Integer _ret=null;
    
    //String type = n.f0.accept(new J2VHack());
    GType obj = GType.getType(n.f0, classList);
    String type = obj.toString();
    int ticket = n.f1.accept(this);
    env.variable_map.get(ticket).class_name = type;
    
    return _ret;
  }

  /**
   * f0 -> "public"
   * f1 -> Type()
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( FormalParameterList() )?
   * f5 -> ")"
   * f6 -> "{"
   * f7 -> ( VarDeclaration() )*
   * f8 -> ( Statement() )*
   * f9 -> "return"
   * f10 -> Expression()
   * f11 -> ";"
   * f12 -> "}"
   */
  public Integer visit(MethodDeclaration n) {
    Integer _ret=null;

    System.out.println("");
    String method_name = n.f2.f0.toString();
    String class_name = env.curr_class.class_name;

    env.startParseMethod();
    stmtMethodParam(class_name, method_name);
    n.f4.accept(this);


    pushIndentation();
    n.f7.accept(this);
    n.f8.accept(this);
    int a = n.f10.accept(this);

    
    indentVapor();
    
    System.out.println("ret " + env.findVariableEnv(a));
    

    popIndentation();
    env.endParseMethod();

    return _ret;
  }

  /**
   * f0 -> FormalParameter()
   * f1 -> ( FormalParameterRest() )*
   */
  public Integer visit(FormalParameterList n) {
    Integer _ret=null;
    n.f0.accept(this);
    n.f1.accept(this);
    return _ret;
  }

  /**
   * f0 -> Type()
   * f1 -> Identifier()
   */
  public Integer visit(FormalParameter n) {
    Integer _ret=null;
    
    GType obj = GType.getType(n.f0, classList);
    String type = obj.toString();
    
    String arg = n.f1.f0.toString();
    int ticket = env.getIdentifier(arg);
    env.variable_map.get(ticket).class_name = type; 

    return _ret;
  }

  /**
   * f0 -> ","
   * f1 -> FormalParameter()
   */
  public Integer visit(FormalParameterRest n) {
    Integer _ret=null;
    n.f0.accept(this);
    n.f1.accept(this);
    return _ret;
  }

  /**
   * f0 -> ArrayType()
   *       | BooleanType()
   *       | IntegerType()
   *       | Identifier()
   */
  public Integer visit(Type n) {
    Integer _ret=null;
    n.f0.accept(this);
    return _ret;
  }

  /**
   * f0 -> "int"
   * f1 -> "["
   * f2 -> "]"
   */
  /*
  public Integer visit(ArrayType n) {
    Integer _ret=null;
    n.f0.accept(this);
    n.f1.accept(this);
    n.f2.accept(this);
    return _ret;
  }
  */

  /**
   * f0 -> "boolean"
   */
  public Integer visit(BooleanType n) {
    Integer _ret=null;
    n.f0.accept(this);
    return _ret;
  }

  /**
   * f0 -> "int"
   */
  public Integer visit(IntegerType n) {
    Integer _ret=null;
    n.f0.accept(this);
    return _ret;
  }

  /**
   * f0 -> Block()
   *       | AssignmentStatement()
   *       | ArrayAssignmentStatement()
   *       | IfStatement()
   *       | WhileStatement()
   *       | PrintStatement()
   */
  public Integer visit(Statement n) {
    Integer _ret=null;
    n.f0.accept(this);
    return _ret;
  }

  /**
   * f0 -> "{"
   * f1 -> ( Statement() )*
   * f2 -> "}"
   */
  public Integer visit(Block n) {
    Integer _ret=null;
    n.f1.accept(this);
    return _ret;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "="
   * f2 -> Expression()
   * f3 -> ";"
   */
  public Integer visit(AssignmentStatement n) {
    Integer _ret=null;
    String identifier = n.f0.f0.toString();
    Integer a = n.f2.accept(this);
    int ticket = env.getIdentifier(identifier);

    VaporValue v1 = env.variable_map.get(ticket);
    if (a!= -1) {
    	VaporValue v2 = env.variable_map.get(a);
    	if (v1.class_name != null ) {
    		v1.class_name = v2.class_name;
    	}
    }else {
    	if (v1.class_name != null ) {
    		v1.class_name = "Int";
    	}
    }
    
    stmtAssignment(ticket, env.findVariableEnv(a)); 
    //stmtPrint(env.findVariableEnvStrict(ticket) + " = " + env.findVariableEnv(a));
    
    _ret = ticket;
    return _ret;
  }

  /**
   * f0 -> Identifier()
   * f1 -> "["
   * f2 -> Expression()
   * f3 -> "]"
   * f4 -> "="
   * f5 -> Expression()
   * f6 -> ";"
   */
  public Integer visit(ArrayAssignmentStatement n) {
    Integer _ret=null;
    int a = n.f0.accept(this);
    int b = n.f2.accept(this);
    int c = n.f5.accept(this);
    int ticket1 = env.getTemporary();
    int ticket2 = env.getTemporary();
    int ticket3 = env.getTemporary();
    int ticket4 = env.getTemporary();
    int ticket5 = env.getTemporary();
    int bound1 = env.getLabel("bounds"); 
    
    stmtMemoryAccess(ticket1, env.findVariableEnv(a));
    stmtAssignment(ticket2, "Lt(" + env.findVariableEnv(b) + " " + env.findVariableEnv(ticket1) + ")");
    stmtIfGoto(ticket2, bound1);
    pushIndentation();
    indentVapor();
    System.out.println("Error(\"array index out of bounds\")");
    popIndentation();
    stmtLabel(bound1);
    stmtAssignment(ticket3, "MulS(" + env.findVariableEnv(b) + " 4)");
    stmtAssignment(ticket4, "Add(" + env.findVariableEnv(a) + " " + env.findVariableEnv(ticket3) + ")");
    stmtAssignment(ticket5, "Add(" + env.findVariableEnv(ticket4) + " 4)");
    stmtMemoryAssignment(ticket5, env.findVariableEnv(c));

    _ret = c;
    return _ret;
  }

  /**
   * f0 -> "if"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   * f5 -> "else"
   * f6 -> Statement()
   */
  public Integer visit(IfStatement n) {
    Integer _ret=null;


    int a = n.f2.accept(this);
    int label1 = env.getLabel("if_else");
    int label2 = env.getLabel("if_else");
    int ticket1 = env.getTemporary();

    //ticket1 = a < 1
    //if ticket1 goto label1:
    //  blah
    //  goto label2:
    //label1:
    //  blah
    //label2:

    stmtAssignment(ticket1, "LtS(" + env.findVariableEnv(a) + " 1)" );
    stmtIfGoto(ticket1, label1);

    pushIndentation();
    n.f4.accept(this);
    stmtGoto(label2);
    popIndentation();

    stmtLabel(label1);
    pushIndentation();
    n.f6.accept(this);
    popIndentation();

    stmtLabel(label2);

    return _ret;
  }

  /**
   * f0 -> "while"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> Statement()
   */
  public Integer visit(WhileStatement n) {
    Integer _ret=null;

    int label1 = env.getLabel("while");
    int label2 = env.getLabel("while");

    //Initial conditional tag
    stmtLabel(label1);
    //Jump to end
    int a = n.f2.accept(this);
    stmtIf0Goto(a, label2);
    //Main Loop code
    n.f4.accept(this);
    //Jump to conditional
    stmtGoto(label1);

    stmtLabel(label2);

    return _ret;
  }

  /**
   * f0 -> "System.out.println"
   * f1 -> "("
   * f2 -> Expression()
   * f3 -> ")"
   * f4 -> ";"
   */
  public Integer visit(PrintStatement n) {
    Integer _ret=null;
    int a = n.f2.accept(this);
    indentVapor();
    System.out.println("PrintIntS(" + env.findVariableEnv(a) + ")");
    return _ret;
  }

  /**
   * f0 -> AndExpression()
   *       | CompareExpression()
   *       | PlusExpression()
   *       | MinusExpression()
   *       | TimesExpression()
   *       | ArrayLookup()
   *       | ArrayLength()
   *       | MessageSend()
   *       | PrimaryExpression()
   */
  public Integer visit(Expression n) {
    Integer _ret=null;
    _ret = n.f0.accept(this);
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "&&"
   * f2 -> PrimaryExpression()
   */
  public Integer visit(AndExpression n) {
    Integer _ret=null;
    //Pseudocode
    //ticket1 = LtS(a 1)
    //ticket2 = LtS(b 1)
    //if ticket1 goto :label1
    //  if ticket2 goto :label2
    //    ticket3 = 1
    //goto: control2
    //control1: 
    //  ticket3 = 0
    //control2:
    int a = n.f0.accept(this);
    int b = n.f2.accept(this);
    int ticket1 = env.getTemporary();
    int ticket2 = env.getTemporary();
    int ticket3 = env.getTemporary();
    int label1 = env.getLabel("label");
    int label2 = env.getLabel("label");
    stmtAssignment(ticket1, "LtS(" + env.findVariableEnv(a) + " 1)");
    stmtAssignment(ticket2, "LtS(" + env.findVariableEnv(b) + " 1)");

    stmtIfGoto(ticket1, label1);
    pushIndentation();
    stmtIfGoto(ticket2, label2);
    pushIndentation();
    stmtAssignment(ticket3, "1");
    popIndentation();
    popIndentation();
    stmtGoto(label2);
    stmtLabel(label1);
    pushIndentation();
    stmtAssignment(ticket3, "0");
    popIndentation();
    stmtLabel(label2);
    
    _ret = ticket3;
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "<"
   * f2 -> PrimaryExpression()
   */
  public Integer visit(CompareExpression n) {
    Integer _ret=null;
    int a = n.f0.accept(this);
    /*
    String PE1 = "";
    String PE2 = "";
    //If this is constant 
    if (a == -1) {
    	PE1 = const_num;
    }*/
    int b = n.f2.accept(this);
    /*
    if (b == -1) {
    	PE2 = const_num;
    }*/
    int ticket = env.getTemporary();
    /*
    if (a == -1 && b == -1) {
    	 stmtAssignment(ticket, "LtS(" + PE1 + " " + PE2 + ")");
    }else if (a == -1) {
    	stmtAssignment(ticket, "LtS(" + PE1 + " " + env.findVariableEnv(b) + ")");
    }else if (b == -1) {
    	stmtAssignment(ticket, "LtS(" + env.findVariableEnv(a) + " " + PE2 + ")");
    }else {
    	stmtAssignment(ticket, "LtS(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
    }*/
    stmtAssignment(ticket, "LtS(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
    _ret = ticket;
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "+"
   * f2 -> PrimaryExpression()
   */
  public Integer visit(PlusExpression n) {
    Integer _ret=null;
   
    
    int a = n.f0.accept(this);
    int b = n.f2.accept(this);
    
    int ticket = env.getTemporary();
    /*
    if (a == -1 && b == -1) {
   	 stmtAssignment(ticket, "Add(" + PE1 + " " + PE2 + ")");
   }else if (a == -1) {
   	stmtAssignment(ticket, "Add(" + PE1 + " " + env.findVariableEnv(b) + ")");
   }else if (b == -1) {
   	stmtAssignment(ticket, "Add(" + env.findVariableEnv(a) + " " + PE2 + ")");
   }else {
   	stmtAssignment(ticket, "Add(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
   }*/
    
    stmtAssignment(ticket, "Add(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
    _ret = ticket;
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "-"
   * f2 -> PrimaryExpression()
   */
  public Integer visit(MinusExpression n) {
    Integer _ret=null;
    
    int a = n.f0.accept(this);
    int b = n.f2.accept(this);
    
    int ticket = env.getTemporary();
    /*
    if (a == -1 && b == -1) {
      	 stmtAssignment(ticket, "Sub(" + PE1 + " " + PE2 + ")");
      }else if (a == -1) {
      	stmtAssignment(ticket, "Sub(" + PE1 + " " + env.findVariableEnv(b) + ")");
      }else if (b == -1) {
      	stmtAssignment(ticket, "Sub(" + env.findVariableEnv(a) + " " + PE2 + ")");
      }else {
      	stmtAssignment(ticket, "Sub(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
      }*/
    stmtAssignment(ticket, "Sub(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
    _ret = ticket;
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "*"
   * f2 -> PrimaryExpression()
   */
  public Integer visit(TimesExpression n) {
    Integer _ret=null;
    
    int a = n.f0.accept(this); 
    int b = n.f2.accept(this);
    
    int ticket = env.getTemporary();

    /*
    if (a == -1 && b == -1) {
      	 stmtAssignment(ticket, "MulS(" + PE1 + " " + PE2 + ")");
      }else if (a == -1) {
      	stmtAssignment(ticket, "MulS(" + PE1 + " " + env.findVariableEnv(b) + ")");
      }else if (b == -1) {
      	stmtAssignment(ticket, "MulS" + env.findVariableEnv(a) + " " + PE2 + ")");
      }else {
      	stmtAssignment(ticket, "MulS(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
      }
      */

    stmtAssignment(ticket, "MulS(" + env.findVariableEnv(a) + " " + env.findVariableEnv(b) + ")");
    
    _ret = ticket;
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "["
   * f2 -> PrimaryExpression()
   * f3 -> "]"
   */
  public Integer visit(ArrayLookup n) {
    Integer _ret=null;
    int a = n.f0.accept(this);
    int b = n.f2.accept(this);
  
    int ticket1 = env.getTemporary();
    int ticket2 = env.getTemporary();
    int ticket3 = env.getTemporary();
    int ticket4 = env.getTemporary();
    int ticket5 = env.getTemporary();
    int bound1 = env.getLabel("bounds");
       

    //pseudocode
    //ticket1 = [a]
    //ticket2 = LtS(b ticket1) // b < ticket1
    //if ticket2 goto: bounds1
    //  Error("Array out of bounds")
    //control1:
    //ticket3 = b * 4
    //ticket4 = a + ticket3
    //ticket5 = [ticket4+4]
    
    stmtMemoryAccess(ticket1, env.findVariableEnv(a));
    /*
    if (b == -1) {
    	stmtAssignment(ticket2, "Lt(" + index + " " + env.findVariableEnv(ticket1) + ")");
    }else {
    	stmtAssignment(ticket2, "Lt(" + env.findVariableEnv(b) + " " + env.findVariableEnv(ticket1) + ")");
    }*/
    stmtAssignment(ticket2, "Lt(" + env.findVariableEnv(b) + " " + env.findVariableEnv(ticket1) + ")");
    stmtIfGoto(ticket2, bound1);
    pushIndentation();
    stmtPrint("Error(\"array index out of bounds\")");
    popIndentation();
    stmtLabel(bound1);
    /*
    if (b == -1) {
    	stmtAssignment(ticket3, "MulS(" + index + " 4)");
    }else {
    	stmtAssignment(ticket3, "MulS(" + env.findVariableEnv(b) + " 4)");
    }*/
    stmtAssignment(ticket3, "MulS(" + env.findVariableEnv(b) + " 4)");
    stmtAssignment(ticket4, "Add(" + env.findVariableEnv(a) + " " + env.findVariableEnv(ticket3) + ")");
    stmtMemoryAccess(ticket5, env.findVariableEnv(ticket4) + "+4");

    _ret = ticket5;
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> "length"
   */
  public Integer visit(ArrayLength n) {
    Integer _ret=null;

    int a = n.f0.accept(this);
    int ticket = env.getTemporary();
    stmtMemoryAccess(ticket, env.findVariableEnv(a));

    n.f1.accept(this);
    n.f2.accept(this);
    return _ret;
  }

  /**
   * f0 -> PrimaryExpression()
   * f1 -> "."
   * f2 -> Identifier()
   * f3 -> "("
   * f4 -> ( ExpressionList() )?
   * f5 -> ")"
   */
  public Integer visit(MessageSend n) {
    Integer _ret=null;

    int a = n.f0.accept(this);
    
    //Error check the null pointer (except for this):
    //if a goto null1
    //  Error("null pointer")
    //null1:
    
    if (a != 0) {
    	int null1 = env.getLabel("null");
    	stmtIfGoto(a, null1);
    	pushIndentation();
    	stmtPrint("Error(\"null pointer\")");
    	popIndentation();
    	stmtLabel(null1);
    }

    int ticket1 = env.getTemporary();
    int ticket2 = env.getTemporary();

    String method_name = n.f2.f0.toString();
    String class_name;
    
    //to get the correct classname of the function
    if (a == 0) {
      class_name = env.curr_class.class_name; 
    } else {
      class_name = env.variable_map.get(a).class_name;
    }

    //J2VClassLayout class_layout = env.layout.get(class_name);
    ClassType curr_class = Helper.getClass(class_name, classList);
    int offset = Helper.getMethodIdx(method_name, curr_class);
    
    String method_type = curr_class.methods.get(offset).return_value.toString();

    //to get the function name in ticket1
    stmtMemoryAccess(ticket1, env.findVariableEnv(a));
    stmtMemoryAccess(ticket1, env.findVariableEnv(ticket1) + "+" + offset * 4);
      
    //env.call_list.push(env.call_parameters_ticket);
    //env.call_parameters_ticket = new ArrayList<Integer>();
    env.clearCallParam();
    
    n.f4.accept(this);

    String parameters = "";

    /*
    for (Integer ticket_param : env.call_parameters_ticket) {
      parameters += " ";
      if (ticket_param == -1) {
    	  int idx = env.call_parameters_ticket.indexOf(ticket_param);
    	  parameters += env.call_parameters_const.get(idx);
      }else {
    	  parameters += env.findVariableEnv(ticket_param);
      }
    }*/
    for (int i = 0; i < env.call_parameters_ticket.size(); i ++) {
    	parameters += " ";
    	Integer ticket = env.call_parameters_ticket.get(i);
    	if (ticket == -1) {
    		parameters += env.call_parameters_const.get(i);
    	}else {
    		parameters += env.findVariableEnv(ticket);
    	}
    }

    stmtAssignment(ticket2, "call " + env.findVariableEnv(ticket1) + "(" + env.findVariableEnv(a) + parameters + ")");

    //env.call_parameters_ticket = env.call_list.pop();

    VaporValue v = env.variable_map.get(ticket2);
    v.class_name = method_type;

    _ret = ticket2;
    return _ret;
  }

  /**
   * f0 -> Expression()
   * f1 -> ( ExpressionRest() )*
   */
  public Integer visit(ExpressionList n) {
    Integer _ret=null;
    Integer a = n.f0.accept(this);
    
    env.call_parameters_ticket.add(a);
    if (a == -1) {
    	env.call_parameters_const.add(env.const_num);
    }else {
    	env.call_parameters_const.add("NULL");
    }
    n.f1.accept(this);
    return _ret;
  }

  /**
   * f0 -> ","
   * f1 -> Expression()
   */
  public Integer visit(ExpressionRest n) {
    Integer _ret=null;
    Integer a = n.f1.accept(this);
    env.call_parameters_ticket.add(a);
    if (a == -1) {
    	env.call_parameters_const.add(env.const_num);
    }else {
    	env.call_parameters_const.add("NULL");
    }
    return _ret;
  }

  /**
   * f0 -> IntegerLiteral()
   *       | TrueLiteral()
   *       | FalseLiteral()
   *       | Identifier()
   *       | ThisExpression()
   *       | ArrayAllocationExpression()
   *       | AllocationExpression()
   *       | NotExpression()
   *       | BracketExpression()
   */
  public Integer visit(PrimaryExpression n) {
    Integer _ret=null;
    _ret = n.f0.accept(this);
    return _ret;
  }

  /**
   * f0 -> <INTEGER_LITERAL>
   */
  public Integer visit(IntegerLiteral n) {
    Integer _ret=null;
    //int ticket = env.getTemporary(); 
    //stmtAssignment(ticket, n.f0.toString());
    _ret = -1;
    env.const_num = n.f0.toString();
    System.out.println("Add constant number" + env.const_num);
    return _ret;
  }

  /**
   * f0 -> "true"
   */
  public Integer visit(TrueLiteral n) {
    Integer _ret=null;

    int ticket = env.getTemporary(); 
    stmtAssignment(ticket, "1");
    _ret = ticket;
    //_ret = -2;
    return _ret;
  }

  /**
   * f0 -> "false"
   */
  public Integer visit(FalseLiteral n) {
    Integer _ret=null;

    int ticket = env.getTemporary(); 
    stmtAssignment(ticket, "0");
    _ret = ticket;
    //_ret = -3; 
    return _ret;
  }

  /**
   * f0 -> <IDENTIFIER>
   */
  public Integer visit(Identifier n) {
    Integer _ret = env.getIdentifier(n.f0.toString());
    return _ret;
  }

  /**
   * f0 -> "this"
   */
  public Integer visit(ThisExpression n) {
    Integer _ret=null;
    _ret = 0;
    return _ret;
  }

  /**
   * f0 -> "new"
   * f1 -> "int"
   * f2 -> "["
   * f3 -> Expression()
   * f4 -> "]"
   */
  public Integer visit(ArrayAllocationExpression n) {
    Integer _ret=null;
    int a = n.f3.accept(this);

    int ticket1 = env.getTemporary();
    int ticket2 = env.getTemporary();
    int ticket3 = env.getTemporary();

    //Assigning the size in position 1 as the size of only the data values, and not metadata
    stmtAssignment(ticket1, "MulS(" + env.findVariableEnv(a) + " 4)");
    stmtAssignment(ticket2, "Add(" + env.findVariableEnv(ticket1) + " 4)");
    stmtAssignment(ticket3, "HeapAllocZ(" + env.findVariableEnv(ticket2) + ")");
    stmtMemoryAssignment(ticket3, env.findVariableEnv(a));
     
    _ret = ticket3;

    return _ret;
  }

  /**
   * f0 -> "new"
   * f1 -> Identifier()
   * f2 -> "("
   * f3 -> ")"
   */
  public Integer visit(AllocationExpression n) {
    Integer _ret=null;
    int ticket = 0;
    String class_name = n.f1.f0.toString();

    ClassType curr_class = Helper.getClass(class_name, classList);

    ticket = env.getTemporary(); 

    VaporValue v = env.variable_map.get(ticket);
    v.class_name = class_name;

    int offset = 1;
    if (curr_class.methods.size() > 1) {
    	offset = curr_class.methods.size() - 1 ;
    }
    
    stmtAssignment(ticket, "HeapAllocZ(" + offset * 4 + ")");
    stmtMemoryAssignment(ticket, ":vmt_" + curr_class.toString());

    _ret = ticket;
    return _ret;
  }

  /**
   * f0 -> "!"
   * f1 -> Expression()
   */
  public Integer visit(NotExpression n) {
    Integer _ret=null;
    //TODO check this?
    int a = n.f1.accept(this);
    int ticket = env.getTemporary();
    stmtAssignment(ticket, "LtS(" + env.findVariableEnv(a) + " 1)");
    _ret = ticket;
    return _ret;
  }

  /**
   * f0 -> "("
   * f1 -> Expression()
   * f2 -> ")"
   */
  public Integer visit(BracketExpression n) {
    Integer _ret=null;
    _ret = n.f1.accept(this);
    return _ret;
  }

  //////////////////////////
  void stmtMethodParam(String class_name, String method) {
    if (method.equals("main")) {
      System.out.printf("func Main(");
    } else {
      System.out.printf("func " + class_name + "." + method + "(this");
      ClassType curr_class = Helper.getClass(class_name, classList);
      Method curr_method = Helper.getMethod(method, curr_class);
      for (String arg: curr_method.args_name) {
    	  System.out.printf(" " + arg);
      }
    }
    System.out.printf(")\n");
  }
  

  //////////////////////////////
  void pushIndentation() {
    env.indentation_level += 1;
  }

  void popIndentation() {
    env.indentation_level -= 1;
  }

  void stmtAssignment(int lhs, String rhs) {
    if (rhs == null) {
      Helper.exit("Null rhs given to stmtAssignment function");
    }
    indentVapor();
    System.out.println(env.findVariableEnv(lhs) +  " = " + rhs);
  }

  void stmtMemoryAssignment(int lhs, String rhs) {
    if (rhs == null) {
    	Helper.exit("Null rhs given to stmtMemoryAssignment function");
    }
    indentVapor();
    System.out.println("[" + env.findVariableEnv(lhs) + "] = " + rhs);
  }

  void stmtMemoryAccess(int lhs, String rhs) {
    if (rhs == null) {
    	Helper.exit("Null rhs given to stmtMemoryAccess function");
    }
    indentVapor();
    System.out.println(env.findVariableEnv(lhs) + " = [" + rhs + "]");
  }

  void stmtLabel(int label) {
    indentVapor();
    System.out.println(env.findVariableEnv(label) + ":");
  }

  void stmtIf0Goto(int ticket, int label) {
    indentVapor();
    System.out.println("if0 " + env.findVariableEnv(ticket) + " goto :" + env.findVariableEnv(label));
  }

  void stmtIfGoto(int ticket, int label) {
    indentVapor();
    System.out.println("if " + env.findVariableEnv(ticket) + " goto :" + env.findVariableEnv(label));
  }

  void stmtGoto(int label) {
    indentVapor();
    System.out.println("goto :" + env.findVariableEnv(label));
  }

  void stmtPrint(String input) {
    indentVapor();
    System.out.println(input);
  }

  void indentVapor() {
    for (int i = 0; i < env.indentation_level; i++) {
      System.out.printf("  ");
    }
  }
}
