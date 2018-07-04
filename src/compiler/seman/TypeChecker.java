package compiler.seman;

import java.util.*;

import compiler.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.type.*;

/**
 * Preverjanje tipov.
 * 
 * @author sliva
 */
public class TypeChecker implements Visitor {

    @Override
    public void visit(AbsArrType acceptor) {
        acceptor.type.accept(this);
        SymbDesc.setType(acceptor, new SemArrType(acceptor.length,SymbDesc.getType(acceptor.type)));
        
    }

    @Override
    public void visit(AbsAtomConst acceptor) {
        if(acceptor.type==AbsAtomConst.INT)
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.INT));
        if(acceptor.type==AbsAtomConst.STR)
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.STR));
        if(acceptor.type==AbsAtomConst.LOG)
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.LOG));
    }

    @Override
    public void visit(AbsAtomType acceptor) {
        if(acceptor.type==AbsAtomType.INT)
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.INT));
        if(acceptor.type==AbsAtomType.LOG)
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.LOG));
        if(acceptor.type==AbsAtomType.STR)
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.STR));
    }

    @Override
    public void visit(AbsBinExpr acceptor) {
        acceptor.expr1.accept(this);
        acceptor.expr2.accept(this);
        
        if (acceptor.oper != AbsBinExpr.ARR) if (acceptor.oper == AbsBinExpr.ASSIGN){
            if (SymbDesc.getType(acceptor.expr1).sameStructureAs(SymbDesc.getType(acceptor.expr2)))
                SymbDesc.setType(acceptor,  SymbDesc.getType(acceptor.expr1)); 
        }
            
        else if (SymbDesc.getType(acceptor.expr1).sameStructureAs(new SemAtomType(SemAtomType.LOG)) &&
                SymbDesc.getType(acceptor.expr2).sameStructureAs(new SemAtomType(SemAtomType.LOG))){
            if (!(acceptor.oper == AbsBinExpr.AND || acceptor.oper == AbsBinExpr.EQU ||
                    acceptor.oper == AbsBinExpr.GEQ || acceptor.oper == AbsBinExpr.GTH ||
                    acceptor.oper == AbsBinExpr.IOR || acceptor.oper == AbsBinExpr.LEQ ||
                    acceptor.oper == AbsBinExpr.LTH || acceptor.oper == AbsBinExpr.NEQ))
                Report.error("Wrong operator 1");
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.LOG));
        }
        else if (SymbDesc.getType(acceptor.expr1).sameStructureAs(new SemAtomType(SemAtomType.INT)) &&
                SymbDesc.getType(acceptor.expr2).sameStructureAs(new SemAtomType(SemAtomType.INT))){
            
            switch (acceptor.oper) {
                case AbsBinExpr.ADD:
                case AbsBinExpr.SUB:
                case AbsBinExpr.MUL:
                case AbsBinExpr.DIV:
                case AbsBinExpr.MOD:
                    SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.INT));
                    break;
                case AbsBinExpr.EQU:
                case AbsBinExpr.GEQ:
                case AbsBinExpr.GTH:
                case AbsBinExpr.LEQ:
                case AbsBinExpr.LTH:
                case AbsBinExpr.NEQ:
                    SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.LOG));
                    break;
                default:
                    Report.error("Wrong operator 2 "+acceptor.position);
                    break;
            }
        }
        
        else if ((SymbDesc.getType(acceptor.expr1).sameStructureAs(new SemAtomType(SemAtomType.INT)) &&
                SymbDesc.getType(acceptor.expr2).sameStructureAs(new SemAtomType(SemAtomType.LOG)))
                || 
                (SymbDesc.getType(acceptor.expr1).sameStructureAs(new SemAtomType(SemAtomType.LOG)) &&
                SymbDesc.getType(acceptor.expr2).sameStructureAs(new SemAtomType(SemAtomType.INT)))){
            if(!( acceptor.oper == AbsBinExpr.EQU ||acceptor.oper == AbsBinExpr.GEQ ||
                    acceptor.oper == AbsBinExpr.GTH ||acceptor.oper == AbsBinExpr.LEQ ||
                    acceptor.oper == AbsBinExpr.LTH ||acceptor.oper == AbsBinExpr.NEQ))
                Report.error("Wrong operator 3 "+acceptor.position);
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.LOG));
        }     
        else{
            Report.error("Failed at typechecker binExpr");
        } else {
            if (!SymbDesc.getType(acceptor.expr2).sameStructureAs(new SemAtomType(SemAtomType.INT)))
                Report.error("error at typechecker arr");
            SemType st = SymbDesc.getType(acceptor.expr1);
            if ( st instanceof SemArrType)
                SymbDesc.setType(acceptor, ((SemArrType) st).type);
        }
    }

    @Override
    public void visit(AbsDefs acceptor) {
        for (int i = 0; i < acceptor.numDefs(); i++){
            if( acceptor.def(i) instanceof AbsTypeDef )
               acceptor.def(i).accept(this); 
        }
        for (int i = 0; i < acceptor.numDefs(); i++){
            if( acceptor.def(i) instanceof AbsVarDef )
               acceptor.def(i).accept(this); 
        }
        for (int i = 0; i < acceptor.numDefs(); i++){
            if(acceptor.def(i) instanceof AbsFunDef )
                acceptor.def(i).accept(this);
            
        }
        for (int i = 0; i < acceptor.numDefs(); i++){
            if(acceptor.def(i) instanceof AbsFunDef ){
                ((AbsFunDef)acceptor.def(i)).expr.accept(this);
                
                if (!((SemFunType) SymbDesc.getType(((AbsFunDef)acceptor.def(i)))).resultType.sameStructureAs(SymbDesc.getType(((AbsFunDef)acceptor.def(i)).expr))){
                    Report.error("Resulting type of function "+((AbsFunDef)acceptor.def(i)).name+" must be the same "+ ((SemFunType) SymbDesc.getType(((AbsFunDef)acceptor.def(i)))).resultType.actualType());
                }
                
            }
        }
    }

    @Override
    public void visit(AbsExprs acceptor) {
        for (int i = 0; i < acceptor.numExprs(); i++) {
            acceptor.expr(i).accept(this);
        }
        SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr(acceptor.numExprs() -1)));
    }

    @Override
    public void visit(AbsFor acceptor) {
        acceptor.count.accept(this);
        acceptor.lo.accept(this);
        acceptor.hi.accept(this);
        acceptor.step.accept(this);
        acceptor.body.accept(this);
        if(!SymbDesc.getType(acceptor.lo).sameStructureAs(new SemAtomType(SemAtomType.INT))||
           !SymbDesc.getType(acceptor.hi).sameStructureAs(new SemAtomType(SemAtomType.INT))||
           !SymbDesc.getType(acceptor.step).sameStructureAs(new SemAtomType(SemAtomType.INT)))
            Report.error("For loop requiers bottom border, upper border and step to be INTEGERS");
        SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
    }

    @Override
    public void visit(AbsFunCall acceptor) {
        AbsFunDef ds =(AbsFunDef) SymbDesc.getNameDef(acceptor);
        for (int i = 0; i < acceptor.numArgs(); i++) {
            acceptor.arg(i).accept(this);
            if (!SymbDesc.getType(ds.par(i)).sameStructureAs(SymbDesc.getType(acceptor.arg(i)))){
                Report.error("Argument on position " + (i+1) + " is not " + SymbDesc.getType(ds.par(i)));}
        }
        SymbDesc.setType(acceptor, SymbDesc.getType(ds.type));
    }

    @Override
    public void visit(AbsFunDef acceptor) {
        ArrayList<SemType> a = new ArrayList<>();
        for (int i = 0; i < acceptor.numPars(); i++) {
            acceptor.par(i).accept(this);
            a.add(SymbDesc.getType(acceptor.par(i)));
        }
        SemTypeName b = new SemTypeName(acceptor.name);
        acceptor.type.accept(this);
        b.setType(SymbDesc.getType(acceptor.type));
        SymbDesc.setType(acceptor, new SemFunType(a,b));
    }

    @Override
    public void visit(AbsIfThen accpetor) {
        accpetor.cond.accept(this);
        accpetor.thenBody.accept(this);
        if (SymbDesc.getType(accpetor.cond).sameStructureAs(new SemAtomType(SemAtomType.LOG))) {
            SymbDesc.setType(accpetor, new SemAtomType(SemAtomType.VOID));
        }
        else
            Report.error("Condition must be logical");
    }

    @Override
    public void visit(AbsIfThenElse accpetor) {
        accpetor.cond.accept(this);
        accpetor.thenBody.accept(this);
        accpetor.elseBody.accept(this);
        if (SymbDesc.getType(accpetor.cond).sameStructureAs(new SemAtomType(SemAtomType.LOG))) {
            SymbDesc.setType(accpetor, new SemAtomType(SemAtomType.VOID));
        }
        else
            Report.error("Condition must be logical");
    }

    @Override
    public void visit(AbsPar acceptor) {
        SemTypeName a = new SemTypeName(acceptor.name);
        acceptor.type.accept(this);
        if(acceptor.type instanceof AbsArrType)
            a.setType(SymbDesc.getType(((AbsArrType)acceptor.type).type));
        else
            a.setType(SymbDesc.getType(acceptor.type));
        SymbDesc.setType(acceptor, a);
    }

    @Override
    public void visit(AbsTypeDef acceptor) {
        SemTypeName a = new SemTypeName(acceptor.name);
        acceptor.type.accept(this);
        a.setType(SymbDesc.getType(acceptor.type));
        SymbDesc.setType(acceptor, a);
    }

    @Override
    public void visit(AbsTypeName acceptor) {
        SymbDesc.setType(acceptor, SymbDesc.getType(SymbTable.fnd(acceptor.name)));
    }

    @Override
    public void visit(AbsUnExpr acceptor) {
        acceptor.expr.accept(this);
        SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr));
    }

    @Override
    public void visit(AbsVarDef acceptor) {
        acceptor.type.accept(this);
        SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
    }

    @Override
    public void visit(AbsVarName acceptor) {
        SymbDesc.setType(acceptor, SymbDesc.getType(SymbDesc.getNameDef(acceptor)));
    }

    @Override
    public void visit(AbsWhere acceptor) {
        acceptor.defs.accept(this);
        acceptor.expr.accept(this);
        SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr));
    }

    @Override
    public void visit(AbsWhile acceptor) {
        acceptor.cond.accept(this);
        acceptor.body.accept(this);
        if (SymbDesc.getType(acceptor.cond).sameStructureAs(new SemAtomType(SemAtomType.LOG))) {
            SymbDesc.setType(acceptor, new SemAtomType(SemAtomType.VOID));
        }
        else
            Report.error("Condition must be logical");
    }

	// TODO

}

