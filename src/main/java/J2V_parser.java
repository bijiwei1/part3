import syntaxtree.*;
import visitor.*;
import java.util.*;

class J2V_parser extends GJVoidDepthFirst<List<ClassType>>{
	List<ClassType> classList;
	
	J2V_parser(List<ClassType> classList){
		this.classList = classList;
	}
	 
	 /**
	   * f0 -> MainClass()
	   * f1 -> ( TypeDeclaration() )*
	   * f2 -> <EOF>
	   */
	  public Integer visit(Goal n) {
	    Integer _ret=null;
	    n.f0.accept(this, classList);
	    n.f1.accept(this, classList);
	    n.f2.accept(this, classList);
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
	  public void visit(MainClass n, List<ClassType> classList) {
		  return ;
	  }
	  
	  
	  
	  
	
}