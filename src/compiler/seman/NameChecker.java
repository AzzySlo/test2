package compiler.seman;

import compiler.Report;
import compiler.abstr.*;
import compiler.abstr.tree.*;

/**
 * Preverjanje in razresevanje imen (razen imen komponent).
 * 
 * @author sliva
 */
public class NameChecker implements Visitor  {

    @Override
    public void visit(AbsArrType acceptor) {
    }

    @Override
    public void visit(AbsAtomConst acceptor) {
    }

    @Override
    public void visit(AbsAtomType acceptor) {
    }

    @Override
    public void visit(AbsBinExpr acceptor) {
        acceptor.expr1.accept(this);
        acceptor.expr2.accept(this);
    }

    @Override
    public void visit(AbsDefs acceptor)  {
        for (int i = 0; i < acceptor.numDefs(); i++){
            if( acceptor.def(i) instanceof AbsTypeDef || acceptor.def(i) instanceof AbsVarDef )
               acceptor.def(i).accept(this); 
        }
        for (int i = 0; i < acceptor.numDefs(); i++) {
            if (acceptor.def(i) instanceof AbsTypeDef)
                ((AbsTypeDef)acceptor.def(i)).type.accept(this);
        }
        for (int i = 0; i < acceptor.numDefs(); i++) {
            if (acceptor.def(i) instanceof AbsVarDef)
                ((AbsVarDef)acceptor.def(i)).type.accept(this);
        }
        for (int i = 0; i < acceptor.numDefs(); i++){
            if(acceptor.def(i) instanceof AbsFunDef )
                acceptor.def(i).accept(this);
        }
        for (int i = 0; i < acceptor.numDefs(); i++){
            if(acceptor.def(i) instanceof AbsFunDef ){
                AbsFunDef a = ((AbsFunDef)acceptor.def(i));
                a.type.accept(this);
                SymbTable.newScope();
                for (int j = 0; j < a.numPars(); j++) {
                    a.par(j).accept(this);
                }
                a.expr.accept(this);
                SymbTable.oldScope();
            }
        }
    }

    @Override
    public void visit(AbsExprs acceptor) {
        for (int i = 0; i < acceptor.numExprs(); i++) {
            acceptor.expr(i).accept(this);
        }
    }

    @Override
    public void visit(AbsFor acceptor) {
        acceptor.count.accept(this);
        acceptor.lo.accept(this);
        acceptor.hi.accept(this);
        acceptor.step.accept(this);
        acceptor.body.accept(this);
    }

    @Override
    public void visit(AbsFunCall acceptor) {
        if (SymbTable.fnd(acceptor.name) == null){
            Report.error("Function name "+acceptor.name+" does not exist."); 
        }
        SymbDesc.setNameDef(acceptor, SymbTable.fnd(acceptor.name));
        for (int i = 0; i < acceptor.numArgs(); i++) {
            acceptor.arg(i).accept(this);
        }
    }

    @Override
    public void visit(AbsFunDef acceptor){
        try { SymbTable.ins(acceptor.name, acceptor);} catch(SemIllegalInsertException e){ Report.error("This function already exists."); }
    }

    @Override
    public void visit(AbsIfThen acceptor) {
        acceptor.cond.accept(this);
        acceptor.thenBody.accept(this);
    }

    @Override
    public void visit(AbsIfThenElse acceptor) {
        acceptor.cond.accept(this);
        acceptor.thenBody.accept(this);
        acceptor.elseBody.accept(this);
    }

    @Override
    public void visit(AbsPar acceptor) {
        try { SymbTable.ins(acceptor.name, acceptor);} catch(SemIllegalInsertException e){ Report.error("This parameter already exists."); }
        acceptor.type.accept(this);
    }

    @Override
    public void visit(AbsTypeDef acceptor){
        try { SymbTable.ins(acceptor.name, acceptor);} catch(SemIllegalInsertException e){ Report.error("This type already exists."); }
     }

    @Override
    public void visit(AbsTypeName acceptor) {
        if (SymbTable.fnd(acceptor.name) == null) {
            Report.error("Type name does not exist.");
        }
        SymbDesc.setNameDef(acceptor, SymbTable.fnd(acceptor.name));
    }

    @Override
    public void visit(AbsUnExpr acceptor) {
        acceptor.expr.accept(this);
    }

    @Override
    public void visit(AbsVarDef acceptor){
        try { SymbTable.ins(acceptor.name, acceptor);} catch(SemIllegalInsertException e){ Report.error("This variable already exists."); }
    }

    @Override
    public void visit(AbsVarName acceptor) {
        if (SymbTable.fnd(acceptor.name) == null) {
            Report.error("Variable Name "+acceptor.name+" does not exist.");
        }
        SymbDesc.setNameDef(acceptor, SymbTable.fnd(acceptor.name));
    }

    @Override
    public void visit(AbsWhere acceptor) {
        SymbTable.newScope();
        acceptor.defs.accept(this);
        acceptor.expr.accept(this);
        SymbTable.oldScope();
    }

    @Override
    public void visit(AbsWhile acceptor) {
        acceptor.cond.accept(this);
        acceptor.body.accept(this);
    }

	// TODO

}
