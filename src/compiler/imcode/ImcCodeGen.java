package compiler.imcode;

import compiler.Report;
import java.util.*;

import compiler.abstr.*;
import compiler.abstr.tree.AbsArrType;
import compiler.abstr.tree.AbsAtomConst;
import compiler.abstr.tree.AbsAtomType;
import compiler.abstr.tree.AbsBinExpr;
import compiler.abstr.tree.AbsDefs;
import compiler.abstr.tree.AbsExpr;
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
import compiler.frames.*;
import compiler.seman.SymbDesc;

public class ImcCodeGen implements Visitor {

	public LinkedList<ImcChunk> chunks;
        private final HashMap <AbsExpr,Object> neki1;
	public ImcCodeGen() {
		chunks = new LinkedList<>();
                neki1 = new HashMap<>();
	}
	// TODO

    @Override
    public void visit(AbsArrType acceptor) {
    }

    @Override
    public void visit(AbsAtomConst acceptor) {
        //System.out.println(acceptor.type);
        if (acceptor.type == AbsAtomConst.INT)
            neki1.put(acceptor,new ImcCONST(Integer.parseInt(acceptor.value)));
        if (acceptor.type == AbsAtomConst.STR)
            neki1.put(acceptor, new ImcNAME(FrmLabel.newLabel(acceptor.value)));
        
    }

    @Override
    public void visit(AbsAtomType acceptor) {
        
    }

    @Override
    public void visit(AbsBinExpr acceptor) {
        
        if(acceptor.oper == AbsBinExpr.ARR){
            ImcSEQ sq = new ImcSEQ();
            AbsExpr abe = acceptor.expr1;
            int j = 1;
            ImcTEMP imt = new ImcTEMP(new FrmTemp());
            sq.stmts.add(new ImcMOVE(imt,new ImcCONST(0)));
            while (abe instanceof AbsBinExpr){
                AbsBinExpr temp = (AbsBinExpr)abe;
                temp.expr2.accept(this);
                if (neki1.get(temp.expr2) instanceof ImcMEM){
                    ImcTEMP imt0 = new ImcTEMP(new FrmTemp());
                    ImcTEMP imt1 = new ImcTEMP(new FrmTemp());
                    ImcTEMP imt2 = new ImcTEMP(new FrmTemp());
                    ImcTEMP imt3 = new ImcTEMP(new FrmTemp());
                    sq.stmts.add(new ImcMOVE(imt0,(ImcExpr)neki1.get(temp.expr2)));
                    sq.stmts.add(new ImcMOVE(imt1,new ImcBINOP(ImcBINOP.MUL,imt0,new ImcCONST(4*j))));
                    sq.stmts.add(new ImcMOVE(imt2,imt));
                    sq.stmts.add(new ImcMOVE(imt3,new ImcBINOP((ImcBINOP.ADD),imt1,imt2)));
                    sq.stmts.add(new ImcMOVE(imt,imt3));
                }
                else if (neki1.get(temp.expr2) instanceof ImcCONST){
                    ImcTEMP imt1 = new ImcTEMP(new FrmTemp());
                    ImcTEMP imt2 = new ImcTEMP(new FrmTemp());
                    ImcTEMP imt3 = new ImcTEMP(new FrmTemp());
                    sq.stmts.add(new ImcMOVE(imt1 , new ImcCONST(((ImcCONST)neki1.get(temp.expr2)).value*4*j)));
                    sq.stmts.add(new ImcMOVE(imt2 , imt));
                    sq.stmts.add(new ImcMOVE(imt3 , new ImcBINOP(ImcBINOP.ADD,imt1,imt2)));
                    sq.stmts.add(new ImcMOVE(imt  , imt3));
                }
                j++;
                abe = temp.expr1;
            }
            AbsVarName a = (AbsVarName)abe;
            FrmAccess b = FrmDesc.getAccess(SymbDesc.getNameDef(a));
            
             acceptor.expr2.accept(this);
            ImcExpr c = (ImcExpr) neki1.get(acceptor.expr2);
            if (b instanceof FrmVarAccess) 
                neki1.put(acceptor,  new ImcMEM(new ImcBINOP(ImcBINOP.ADD,(ImcExpr)new ImcNAME(((FrmVarAccess) b).label),new ImcBINOP(ImcBINOP.ADD,(c),new ImcESEQ(sq,imt)))));
            else if (b instanceof FrmParAccess)
                neki1.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD,new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(((FrmParAccess) b).frame.FP), new ImcCONST(((FrmParAccess) b).offset)),new ImcBINOP(ImcBINOP.ADD,(c),new ImcESEQ(sq,imt)))));
            else if (b instanceof FrmLocAccess)
                neki1.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD,new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(((FrmLocAccess) b).frame.FP), new ImcCONST(((FrmLocAccess) b).offset)),new ImcBINOP(ImcBINOP.ADD,(c),new ImcESEQ(sq,imt)))));
            



        }
        else{
            acceptor.expr1.accept(this);
            acceptor.expr2.accept(this);
        
        ImcExpr a = (ImcExpr) neki1.get(acceptor.expr1);
        ImcExpr b = (ImcExpr) neki1.get(acceptor.expr2);
            switch (acceptor.oper) {
                case AbsBinExpr.ASSIGN:
                    neki1.put(acceptor, new ImcMOVE(a,b));
                    break;
                case AbsBinExpr.ADD:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.ADD),a,b));
                    break;
                case AbsBinExpr.SUB:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.SUB),a,b));
                    break;
                case AbsBinExpr.MUL:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.MUL),a,b));
                    break;
                case AbsBinExpr.DIV:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.DIV),a,b));
                    break;
                case AbsBinExpr.MOD:
                    //a - (n * int(a/n))
                    neki1.put(acceptor,new ImcBINOP((ImcBINOP.SUB),a,new ImcBINOP((ImcBINOP.MUL),b,new ImcBINOP((ImcBINOP.DIV),a,b))));
                    break;
                
                    
                case AbsBinExpr.GTH:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.GTH),a,b));
                    break;
                case AbsBinExpr.LTH:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.LTH),a,b));
                    break;
                case AbsBinExpr.EQU:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.EQU),a,b));
                    break;
                case AbsBinExpr.NEQ:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.NEQ),a,b));
                    break;
                case AbsBinExpr.GEQ:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.GEQ),a,b));
                    break;
                case AbsBinExpr.LEQ:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.LEQ),a,b));
                    break;
                    
                case AbsBinExpr.AND:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.AND),a,b));
                    break;
                case AbsBinExpr.IOR:
                    neki1.put(acceptor, new ImcBINOP((ImcBINOP.OR),a,b));
                    break;
                default:
                    Report.error("Imc at BIN");
                    break;
            }
        }
        
        
    }

    @Override
    public void visit(AbsDefs acceptor) {
        for (int i = 0; i < acceptor.numDefs(); i++) {
            acceptor.def(i).accept(this);
        }
        
    }

    @Override
    public void visit(AbsExprs acceptor) {
        ImcSEQ sd = new ImcSEQ();
        for (int i = 0; i < acceptor.numExprs()-1; i++) {
            acceptor.expr(i).accept(this);
            if (neki1.get(acceptor.expr(i)) instanceof ImcStmt)
                sd.stmts.add((ImcStmt)neki1.get(acceptor.expr(i)));
            if (neki1.get(acceptor.expr(i)) instanceof ImcExpr)
                sd.stmts.add(new ImcEXP((ImcExpr)neki1.get(acceptor.expr(i))));
            
            
        }
        acceptor.expr(acceptor.numExprs()-1).accept(this);
        try{
            neki1.put(acceptor, new ImcESEQ(sd,(ImcExpr)neki1.get(acceptor.expr(acceptor.numExprs()-1))));
        }
        catch (Exception e){  
            sd.stmts.add((ImcStmt)neki1.get(acceptor.expr(acceptor.numExprs()-1)));
            neki1.put(acceptor, sd);
        }
    }

    @Override
    public void visit(AbsFor acceptor) {
        
        FrmLabel L1 = FrmLabel.newLabel();
        FrmLabel L2 = FrmLabel.newLabel();
        
        acceptor.count.accept(this);
        acceptor.lo.accept(this);
        acceptor.hi.accept(this);
        ImcStmt step1 = new ImcMOVE((ImcExpr)neki1.get(acceptor.count),(ImcExpr)neki1.get(acceptor.lo));
        ImcExpr step2 = new ImcBINOP(ImcBINOP.GTH,(ImcExpr)neki1.get(acceptor.hi),(ImcExpr)neki1.get(acceptor.count));
        acceptor.body.accept(this);
        acceptor.step.accept(this);
        ImcStmt step3 = new ImcMOVE((ImcExpr)neki1.get(acceptor.count),new ImcBINOP((ImcBINOP.ADD),(ImcExpr)neki1.get(acceptor.count),(ImcExpr)neki1.get(acceptor.step)));
        
        ImcSEQ IS = new ImcSEQ();
        IS.stmts.add(step1);
        IS.stmts.add(new ImcCJUMP(step2,L1,L2));
        IS.stmts.add(new ImcLABEL(L1));
        if (neki1.get(acceptor.body) instanceof ImcExpr)
            IS.stmts.add(new ImcEXP((ImcExpr)neki1.get(acceptor.body)));
        else 
            IS.stmts.add((ImcStmt)neki1.get(acceptor.body));
        IS.stmts.add(step3);
        IS.stmts.add(new ImcCJUMP(step2,L1,L2));
        IS.stmts.add(new ImcLABEL(L2));
        
        neki1.put(acceptor, IS);
    }

    @Override
    public void visit(AbsFunCall acceptor) {
        ImcCALL sad = new ImcCALL(FrmDesc.getFrame(SymbDesc.getNameDef(acceptor)).label);
        int nivo = SymbDesc.getScope(SymbDesc.getNameDef(acceptor));

        if (nivo == 0)
            sad.args.add(new ImcCONST(0));
        else {
            if (nivo == FrmDesc.getFrame(SymbDesc.getNameDef(acceptor)).level){
                sad.args.add(new ImcMEM(new ImcTEMP(FrmDesc.getFrame(SymbDesc.getNameDef(acceptor)).FP)));
            }
            else {
                if (FrmDesc.getFrame(SymbDesc.getNameDef(acceptor)).level-nivo == -1)
                    sad.args.add(new ImcTEMP(FrmDesc.getFrame(SymbDesc.getNameDef(acceptor)).FP));
                else {
                    ImcExpr temp = new ImcTEMP(FrmDesc.getFrame(SymbDesc.getNameDef(acceptor)).FP);

                    for (int i = 0; i <= FrmDesc.getFrame(SymbDesc.getNameDef(acceptor)).level-nivo; i++){
                        temp = new ImcMEM(temp);
                    }
                    sad.args.add(temp);
                }

            }
        }
        for (int i = 0; i < acceptor.numArgs(); i++) {
            acceptor.arg(i).accept(this);
            sad.args.add((ImcExpr)neki1.get(acceptor.arg(i)));
        }
        neki1.put(acceptor,sad);
        
    }

    @Override
    public void visit(AbsFunDef acceptor) {
        String name = acceptor.name;
        switch(name){
            case "getInt":
            case "putInt":
            case "getString":
            case "putString":
            case "putStringN":
            case "putIntN":
                return;
        }
        acceptor.expr.accept(this);
        ImcMOVE m = new ImcMOVE(new ImcTEMP(FrmDesc.getFrame(acceptor).RV),(ImcExpr)neki1.get(acceptor.expr));
        chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor),m));
        
    }

    @Override
    public void visit(AbsIfThen acceptor) {
        FrmLabel L1 = FrmLabel.newLabel();
        FrmLabel L2 = FrmLabel.newLabel();
        
        acceptor.cond.accept(this);
        acceptor.thenBody.accept(this);
        
        ImcSEQ IS = new ImcSEQ();
        IS.stmts.add( new ImcCJUMP((ImcExpr)neki1.get(acceptor.cond),L1,L2));
        IS.stmts.add(new ImcLABEL(L1));
        if (neki1.get(acceptor.thenBody) instanceof ImcExpr)
            IS.stmts.add(new ImcEXP((ImcExpr)neki1.get(acceptor.thenBody)));
        else 
            IS.stmts.add((ImcStmt)neki1.get(acceptor.thenBody));
        IS.stmts.add(new ImcLABEL(L2));
        neki1.put(acceptor, IS);
        
    }

    @Override
    public void visit(AbsIfThenElse acceptor) {
        //System.out.println("ad");
        FrmLabel L1 = FrmLabel.newLabel();
        FrmLabel L2 = FrmLabel.newLabel();
        FrmLabel L3 = FrmLabel.newLabel();
        
        acceptor.cond.accept(this);
        acceptor.thenBody.accept(this);
        acceptor.elseBody.accept(this);
        
        //neki1.put(acceptor, new ImcCJUMP((ImcExpr)neki1.get(acceptor.cond),L1,L2) );
        ImcSEQ IS = new ImcSEQ();
        IS.stmts.add( new ImcCJUMP((ImcExpr)neki1.get(acceptor.cond),L1,L2));
        IS.stmts.add(new ImcLABEL(L1));
        if (neki1.get(acceptor.thenBody) instanceof ImcExpr)
            IS.stmts.add(new ImcEXP((ImcExpr)neki1.get(acceptor.thenBody)));
        else 
            IS.stmts.add((ImcStmt)neki1.get(acceptor.thenBody));
        IS.stmts.add(new ImcJUMP(L3));
        IS.stmts.add(new ImcLABEL(L2));
        if (neki1.get(acceptor.elseBody) instanceof ImcExpr)
            IS.stmts.add(new ImcEXP((ImcExpr)neki1.get(acceptor.elseBody)));
        else 
            IS.stmts.add((ImcStmt)neki1.get(acceptor.elseBody));
        IS.stmts.add(new ImcLABEL(L3));
        neki1.put(acceptor, IS);
    }

    @Override
    public void visit(AbsPar acceptor) {
        
    }

    @Override
    public void visit(AbsTypeDef acceptor) {
    }

    @Override
    public void visit(AbsTypeName acceptor) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visit(AbsUnExpr acceptor) {
        neki1.put(acceptor,new ImcCONST(Integer.parseInt(((AbsAtomConst)acceptor.expr).value)));
        
    }

    @Override
    public void visit(AbsVarDef acceptor) {
        FrmAccess b = FrmDesc.getAccess(acceptor);
        if (b instanceof FrmVarAccess)
            chunks.add(new ImcDataChunk(((FrmVarAccess)b).label,SymbDesc.getType(acceptor).size()));
        else
            Report.error("narobe ?");
        
        
    }

    @Override
    public void visit(AbsVarName acceptor) {
        FrmAccess a = FrmDesc.getAccess(SymbDesc.getNameDef(acceptor));
        if (a instanceof FrmVarAccess) 
            neki1.put(acceptor,  new ImcMEM(new ImcNAME(((FrmVarAccess) a).label)));
        else if (a instanceof FrmParAccess)
            neki1.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(((FrmParAccess) a).frame.FP), new ImcCONST(((FrmParAccess) a).offset))));
        else if (a instanceof FrmLocAccess)
            neki1.put(acceptor, new ImcMEM(new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(((FrmLocAccess) a).frame.FP), new ImcCONST(((FrmLocAccess) a).offset))));
        
    }

    @Override
    public void visit(AbsWhere acceptor) {
//       acceptor.defs.accept(this);
//       ImcSEQ iS = new ImcSEQ();
//       AbsDefs a = (AbsDefs)acceptor.defs;
//        for (int i = 0; i < a.numDefs(); i++) {
//            if(a.def(i) instanceof AbsVarDef){
//                AbsVarDef b = (AbsVarDef)a.def(i);
//                //iS.stmts.add();
//            }
//                
//        }
       acceptor.expr.accept(this);
       neki1.put(acceptor, neki1.get(acceptor.expr));
    }

    @Override
    public void visit(AbsWhile acceptor) {
        FrmLabel L1 = FrmLabel.newLabel();
        FrmLabel L2 = FrmLabel.newLabel();
        FrmLabel L3 = FrmLabel.newLabel();
        
        
        
        acceptor.cond.accept(this);
        acceptor.body.accept(this);
        
        
        ImcSEQ IS = new ImcSEQ();
        IS.stmts.add(new ImcLABEL(L3));
        IS.stmts.add( new ImcCJUMP((ImcExpr)neki1.get(acceptor.cond),L1,L2));
        IS.stmts.add(new ImcLABEL(L1));
        if (neki1.get(acceptor.body) instanceof ImcExpr)
            IS.stmts.add(new ImcEXP((ImcExpr)neki1.get(acceptor.body)));
        else 
            IS.stmts.add((ImcStmt)neki1.get(acceptor.body));
        IS.stmts.add(new ImcJUMP(L3));
        IS.stmts.add(new ImcLABEL(L2));
        neki1.put(acceptor, IS);
    }
	
}
