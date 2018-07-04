package compiler.frames;

import compiler.abstr.*;
import compiler.abstr.tree.AbsArrType;
import compiler.abstr.tree.AbsAtomConst;
import compiler.abstr.tree.AbsAtomType;
import compiler.abstr.tree.AbsBinExpr;
import compiler.abstr.tree.AbsDefs;
import compiler.abstr.tree.AbsExprs;
import compiler.abstr.tree.AbsFor;
import compiler.abstr.tree.AbsFunCall;
import compiler.abstr.tree.AbsFunDef;
import compiler.abstr.tree.AbsIfThen;
import compiler.abstr.tree.AbsIfThenElse;
import compiler.abstr.tree.AbsPar;
import compiler.abstr.tree.AbsTypeDef;
import compiler.abstr.tree.AbsTypeName;
import compiler.abstr.tree.AbsUnExpr;
import compiler.abstr.tree.AbsVarDef;
import compiler.abstr.tree.AbsVarName;
import compiler.abstr.tree.AbsWhere;
import compiler.abstr.tree.AbsWhile;
import compiler.seman.SymbDesc;
import compiler.seman.SymbTable;
import java.util.Stack;

public class FrmEvaluator implements Visitor {
    int funMyLvl = 1;
    Stack<FrmFrame> d = new Stack<>();
    @Override
    public void visit(AbsArrType acceptor) {
        //acceptor.type.accept(this);
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
    public void visit(AbsDefs acceptor) {
        for (int i = 0; i < acceptor.numDefs(); i++) {
                acceptor.def(i).accept(this);
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
        
    }

    @Override
    public void visit(AbsFunCall acceptor) {
        FrmFrame sd = d.lastElement();
        FrmFrame da = FrmDesc.getFrame(SymbDesc.getNameDef(acceptor));
        if (da != null)
        sd.sizeArgs = da.sizePars;
    }

    @Override
    public void visit(AbsFunDef acceptor) {
        FrmFrame a = new FrmFrame(acceptor,funMyLvl);
        d.push(a);
        funMyLvl++;
        for (int i = 0; i < acceptor.numPars(); i++) {
            FrmDesc.setAccess(acceptor.par(i), new FrmParAccess(acceptor.par(i),a));
        }
        acceptor.expr.accept(this);
        d.pop();
        FrmDesc.setFrame(acceptor, a);
        funMyLvl--;
    }

    @Override
    public void visit(AbsIfThen accpetor) {
        
    }

    @Override
    public void visit(AbsIfThenElse accpetor) {
        
    }

    @Override
    public void visit(AbsPar acceptor) {
    }

    @Override
    public void visit(AbsTypeDef acceptor) {
    }

    @Override
    public void visit(AbsTypeName acceptor) {
    }

    @Override
    public void visit(AbsUnExpr acceptor) {
        
    }

    @Override
    public void visit(AbsVarDef acceptor) {
        FrmDesc.setAccess(acceptor, new FrmVarAccess(acceptor));
    }

    @Override
    public void visit(AbsVarName acceptor) {
    }

    @Override
    public void visit(AbsWhere acceptor) {
        SymbTable.newScope();
        FrmFrame sd = d.lastElement();
        for (int i = 0; i < acceptor.defs.numDefs(); i++) {
            if(acceptor.defs.def(i) instanceof AbsVarDef)
                FrmDesc.setAccess(acceptor.defs.def(i), new FrmLocAccess((AbsVarDef)acceptor.defs.def(i),sd));
            else
                acceptor.defs.def(i).accept(this);
        }
        acceptor.expr.accept(this);
        SymbTable.oldScope();
    }

    @Override
    public void visit(AbsWhile acceptor) {
    }
	
	// TODO
	
}
