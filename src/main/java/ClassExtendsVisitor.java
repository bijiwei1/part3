import syntaxtree.*;
import visitor.*;
import java.util.*;

public class ClassExtendsVisitor extends GJVoidDepthFirst<List<ClassType>> {

	public void visit(ClassExtendsDeclaration n, List<ClassType> classList) {
		String cname = n.f1.f0.toString();
		String super_cname = n.f3.f0.toString();
		ClassType super_class = Helper.getClass(super_cname, classList);
		ClassType curr_class = Helper.getClass(cname, classList);

		//check whether super class exists
		if (super_class == null || curr_class == null){
			Helper.exit("Type error");
		}
		curr_class.super_class = super_class;
	}
}
