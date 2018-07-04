package compiler.synan;

import compiler.*;
import compiler.abstr.tree.*;
import compiler.lexan.*;
import java.util.*;

/**
 * Sintaksni analizator.
 * 
 * @author sliva
 */
public class SynAn {

	/** Leksikalni analizator. */
	private final LexAn lexAn;

	/** Ali se izpisujejo vmesni rezultati. */
	private final boolean dump;
        int dsa = 0;
        private Symbol simbol;
	/**
	 * Ustvari nov sintaksni analizator.
	 * 
	 * @param lexAn
	 *            Leksikalni analizator.
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
	public SynAn(LexAn lexAn, boolean dump) {
		this.lexAn = lexAn;
		this.dump = dump;
		// TODO
	}
        
        private void nextThingy(){
            simbol = lexAn.lexAn();
        }
        
	/**
	 * Opravi sintaksno analizo.
         * @return 
	 */
	public AbsTree parse() {
            nextThingy();
            return source();
	}
        private AbsTree source(){
            dump("Source -> definitions");
            return definitions();
        }
        private AbsDefs definitions(){
            dump("definitions -> definition definitions1");
            Position ps = simbol.position;
            ArrayList<AbsDef> a = new ArrayList<>();
            a.add(definition());
            if(simbol.token == Token.SEMIC )
                a.addAll(definitions1());
            return new AbsDefs(new Position(ps,simbol.position),a);
        }
        private ArrayList<AbsDef> definitions1(){
            Position ps = simbol.position;
            ArrayList<AbsDef> a = new ArrayList<>();
            if(simbol.token == Token.SEMIC ) {
                dump("definitions1 -> ; definition definitions1");
                nextThingy();
                a.add(definition());
                if(simbol.token == Token.SEMIC )
                    a.addAll(definitions1());
            }
            if(simbol.token != Token.EOF && dsa == 0){
                Report.error(new Position(ps,simbol.position),"Missing ;!");
            }
            //end
            return a;
        }
        private AbsDef definition(){
            Position ps = simbol.position;
            switch (simbol.token) {
                case Token.KW_TYP:
                    dump("definition -> type_definition");
                    nextThingy();
                    return type_definition(ps);
                case Token.KW_FUN:
                    dump("definition -> function_definition");
                    nextThingy();
                    return function_definition(ps);
                case Token.KW_VAR:
                    dump("definition -> variable_definition");
                    nextThingy();
                    return variable_definition(ps);
                default:
                    Report.error(new Position(ps,simbol.position),"Expecting typ var or fun!");
                    break;
            }
            return null;
            
        }
        private AbsTypeDef type_definition(Position ps){
            dump("type_definition -> typ identifier : type");
            String a = "";
            if (simbol.token == Token.IDENTIFIER){
                a = simbol.lexeme;
                nextThingy();
            }
            else
                Report.error(simbol.position,"Expecting identifier!");
            if (simbol.token != Token.COLON)
                Report.error(simbol.position,"Missing :!");
            nextThingy();
            return new AbsTypeDef(new Position(ps,simbol.position),a,type());
        }
        private AbsType type(){
            Position ps = simbol.position;
            String ime = simbol.lexeme;
            switch (simbol.token) {
                case Token.KW_ARR:
                    dump("type -> arr [ int_const ] type ");
                    nextThingy();
                    if (simbol.token != Token.LBRACKET)
                        Report.error(simbol.position,"Expecting left bracket!");
                    nextThingy();
                    if (simbol.token != Token.INT_CONST)
                        Report.error(simbol.position,"Expecting number!");
                    int integer = Integer.parseInt(simbol.lexeme);
                    nextThingy();
                    if (simbol.token != Token.RBRACKET)
                        Report.error(simbol.position,"Expecting right bracket!");
                    nextThingy();
                    AbsType t = type();
                    return new AbsArrType(new Position(ps,simbol.position),integer,t);
                case Token.IDENTIFIER:
                    dump("type -> identifier");
                    nextThingy();
                    return new AbsTypeName(ps,ime);
                case Token.LOGICAL:
                    dump("type -> logical");
                    nextThingy();
                    return new AbsAtomType(ps,AbsAtomType.LOG);
                case Token.INTEGER:
                    dump("type -> integer");
                    nextThingy();
                    return new AbsAtomType(ps,AbsAtomType.INT);
                case Token.STRING:
                    dump("type -> string");
                    nextThingy();
                    return new AbsAtomType(ps,AbsAtomType.STR);
                default:
                    Report.error(simbol.position,"Missing type definition!");
                    break;
            }
            return null;
        }
        private AbsFunDef function_definition(Position ps){
            dump("fun identifier ( parameters ) : type = expression");
            if (simbol.token != Token.IDENTIFIER)
                Report.error(simbol.position,"Expecting identifier!");
            String ime = simbol.lexeme;
            nextThingy();
            if (simbol.token != Token.LPARENT)
                Report.error(simbol.position,"Expecting left parent!");
            nextThingy();
            ArrayList<AbsPar> par = parameters();
            if (simbol.token != Token.RPARENT)
                Report.error(simbol.position,"Expecting right parent!");
            nextThingy();
            if (simbol.token != Token.COLON)
                Report.error(simbol.position,"Expecting :!");
            nextThingy();
            AbsType type = type();
            if (simbol.token != Token.ASSIGN)
                Report.error(simbol.position,"Expecting =!");
            nextThingy();
            AbsExpr exp = expression();
            return new AbsFunDef(new Position(ps,simbol.position),ime,par,type,exp);
        }
        private ArrayList<AbsPar> parameters(){
            dump("parameters -> parameter parameters1");
            ArrayList<AbsPar> a = new ArrayList<>();
            a.add(parameter());
            a.addAll(parameters1());
            return a;
        }
        private ArrayList<AbsPar> parameters1(){
            ArrayList<AbsPar> a = new ArrayList<>();
            if(simbol.token == Token.COMMA){
                dump("parameters1 ->parameter parameters1");
                nextThingy();
                a.add(parameter());
                a.addAll(parameters1());
            }
            return a;
        }
        private AbsPar parameter(){
            dump("parameter -> identifier : type");
            Position ps = simbol.position;
            if (simbol.token != Token.IDENTIFIER)
                Report.error(simbol.position,"Expecting identifier!");
            String ime = simbol.lexeme;
            nextThingy();
            if (simbol.token != Token.COLON)
                Report.error(simbol.position,"Expecting :!");
            nextThingy();
            AbsType type = type();
            return new AbsPar(new Position(ps,simbol.position),ime,type);
        }
        private AbsExpr expression(){
            dump("expression -> logical_ior_expression expression1");
            Position ps = simbol.position;
            AbsExpr a =(logical_ior_expression());
            if(simbol.token == Token.LBRACE){
                dump("{ WHERE definitions }");
                nextThingy();
                if(simbol.token != Token.KW_WHERE)
                   Report.error(simbol.position,"Expecting Where!");
                nextThingy();
                dsa++;
                AbsDefs b = definitions();
                dsa--;
                if(simbol.token != Token.RBRACE)
                   Report.error(simbol.position,"Expecting right brace!");
                nextThingy();
               return new AbsWhere(new Position(ps,simbol.position),a,b);
            }
            
            return a;
        }
        private AbsExpr logical_ior_expression(){
            dump("logical_ior_expression -> logical_and_expression logical_ior_expression1");
            AbsExpr a;
            a = (logical_and_expression());
            if(simbol.token == Token.IOR)
                a=(logical_ior_expression1(a));
            return a;
        }
        private AbsExpr logical_ior_expression1(AbsExpr arg){
            dump("logical_ior_expression1 -> logical_and_expression logical_ior_expression1");
            Position ps = simbol.position;
            AbsExpr a;
            {
                nextThingy();
                AbsExpr b=logical_and_expression();
                a=new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.IOR,arg,b);
                if(simbol.token == Token.IOR)
                    a = logical_ior_expression1(a);
            }
            return a;
        }
        private AbsExpr logical_and_expression(){
            dump("logical_and_expression -> compare_expression logical_and_expression1 ");
            AbsExpr a;
            a = (compare_expression());
            if(simbol.token == Token.AND)
                a = (logical_and_expression1(a));
            return a;
        }
        private AbsExpr logical_and_expression1(AbsExpr arg){
            dump("logical_and_expression1 -> & compare_expression logical_and_expression1 ");
            Position ps = simbol.position;
            AbsExpr a;
            {
                nextThingy();
                AbsExpr b =(compare_expression());
                a=new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.AND,arg,b);
                if(simbol.token == Token.AND)
                    a = (logical_and_expression1(a));
            }
            return a;
        }
        private AbsExpr compare_expression(){
            dump("compare_expression -> additive_expression compare_expression1 ");
            AbsExpr a;
            a = (additive_expression());
            if (simbol.token == Token.EQU ||
                simbol.token == Token.NEQ ||
                simbol.token == Token.GEQ ||
                simbol.token == Token.LEQ ||
                simbol.token == Token.LTH ||
                simbol.token == Token.GTH)
                a = (compare_expression1(a));
            return a;
        }
        private AbsExpr compare_expression1(AbsExpr arg){
            Position ps = simbol.position;
            AbsExpr a=null;
            switch (simbol.token) {
                case Token.EQU:
                    dump("compare_expression1 -> == additive_expression ");
                    nextThingy();
                    a = additive_expression();
                    return new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.EQU,arg,a);
                case Token.NEQ:
                    dump("compare_expression1 -> != additive_expression ");
                    nextThingy();
                    a = additive_expression();
                    return new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.NEQ,arg,a);
                case Token.GEQ:
                    dump("compare_expression1 -> >= additive_expression ");
                    nextThingy();
                    a = additive_expression();
                    return new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.GEQ,arg,a);
                case Token.LEQ:
                    dump("compare_expression1 -> <= additive_expression ");
                    nextThingy();
                    a = additive_expression();
                    return new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.LEQ,arg,a);
                case Token.LTH:
                    dump("compare_expression1 -> < additive_expression ");
                    nextThingy();
                    a = additive_expression();
                    return new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.LTH,arg,a);
                case Token.GTH:
                    dump("compare_expression1 -> > additive_expression ");
                    nextThingy();
                    a = additive_expression();
                    return new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.GTH,arg,a);
                default:
                    break;
            }
            return a;
        }
        private AbsExpr additive_expression(){
            dump("additive_expression -> multiplicative_expression additive_expression1 ");
            AbsExpr a;
            a = (multiplicative_expression());
            if (simbol.token == Token.ADD ||
                simbol.token == Token.SUB)
                a = (additive_expression1(a));
            return a;
        }
        private AbsExpr additive_expression1(AbsExpr arg){
            Position ps = simbol.position;
            AbsExpr a = null;
            if (simbol.token == Token.ADD){
                dump("additive_expression1 -> + multiplicative_expression additive_expression1 ");
                nextThingy();
                    a = (multiplicative_expression());
                    a = new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.ADD,arg,a);
                    if (simbol.token == Token.ADD ||
                        simbol.token == Token.SUB)
                        return (additive_expression1(a));
                    return a;
            }
            if (simbol.token == Token.SUB){
                dump("additive_expression1 -> - multiplicative_expression additive_expression1 ");
                    nextThingy();
                    a = (multiplicative_expression());
                    a = new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.SUB,arg,a);
                    if (simbol.token == Token.ADD ||
                        simbol.token == Token.SUB)
                        return (additive_expression1(a));
                    return a;
            }
            System.out.println("Unexprected error at SynAn additive_expression1");
            return a;
        }
        private AbsExpr multiplicative_expression(){
            dump("multiplicative_expression -> prefix_expression multiplicative_expression1");
            AbsExpr a;
            a = (prefix_expression());
            if (simbol.token == Token.MUL ||
                simbol.token == Token.DIV ||
                simbol.token == Token.MOD)
                a = (multiplicative_expression1(a));
            return a;
        }
        private AbsExpr multiplicative_expression1(AbsExpr arg){
            Position ps = simbol.position;
            AbsExpr a = null;
            if (simbol.token == Token.MUL){
            dump("multiplicative_expression1 -> * prefix_expression multiplicative_expression1");
                nextThingy();
                a = (prefix_expression());
                a = new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.MUL,arg,a);
                if (simbol.token == Token.MUL ||
                simbol.token == Token.DIV ||
                simbol.token == Token.MOD)
                    return (multiplicative_expression1(a));
                return a;
            }
            if (simbol.token == Token.DIV){
            dump("multiplicative_expression1 -> / prefix_expression multiplicative_expression1");
                nextThingy();
                a = (prefix_expression());
                a = new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.DIV,arg,a);
                if (simbol.token == Token.MUL ||
                simbol.token == Token.DIV ||
                simbol.token == Token.MOD)
                    return (multiplicative_expression1(a));
                return a;
            }
            if (simbol.token == Token.MOD){
            dump("multiplicative_expression1 -> % prefix_expression multiplicative_expression1");
                nextThingy();
                a = (prefix_expression());
                a = new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.MOD,arg,a);
                if (simbol.token == Token.MUL ||
                simbol.token == Token.DIV ||
                simbol.token == Token.MOD)
                    return (multiplicative_expression1(a));
                return a;
            }
            System.out.println("Unexpected error at multiplicative_expression1");
            return a;
        }
        private AbsExpr prefix_expression(){
            Position ps = simbol.position;
            if (simbol.token == Token.ADD ){
                dump("prefix_expression -> + prefix_expression ");
                nextThingy();
                return new AbsUnExpr(new Position(ps,simbol.position),AbsUnExpr.ADD,prefix_expression());
            }
            if (simbol.token == Token.NOT ){
                dump("prefix_expression -> ! prefix_expression ");
                nextThingy();
                return new AbsUnExpr(new Position(ps,simbol.position),AbsUnExpr.NOT,prefix_expression());
            }
            if (simbol.token == Token.SUB){
                dump("prefix_expression -> - prefix_expression ");
                nextThingy();
                return new AbsUnExpr(new Position(ps,simbol.position),AbsUnExpr.SUB,prefix_expression());
            }
            else{
                dump("prefix_expression -> postfix_expression");
                
                return (postfix_expression());
            }
        }
        private AbsExpr postfix_expression(){
            dump("postfix_expression -> atom_expression postfix_expression1");
            Position ps = simbol.position;
            AbsExpr a;
            a = (atom_expression());
            if (simbol.token == Token.LBRACKET)
                a = (postfix_expression1(a,ps));
            return a;
        }
        private AbsExpr postfix_expression1(AbsExpr arg, Position ps){
            dump("postfix_expression1 -> [ expression ] postfix_expression1");
            AbsExpr a;
            {
                nextThingy();
                a=(expression()); //]
                a = new AbsBinExpr(new Position(ps,simbol.position),AbsBinExpr.ARR,arg,a);
                if (simbol.token != Token.RBRACKET)
                    Report.error(simbol.position,"Expecting right bracket!");
                nextThingy();
                if (simbol.token == Token.LBRACKET)
                    return (postfix_expression1(a,ps));
                return a;
            }
        }
        private AbsExpr atom_expression(){
           // log_constant int_constant str_constant
           // identifier
           Position ps = simbol.position;
            switch (simbol.token) {
                case Token.IDENTIFIER:
                    String ime = simbol.lexeme;
                    nextThingy();
                    if (simbol.token == Token.LPARENT){
                        dump("atom_expression -> identifier ( expressions )");
                        nextThingy();
                        AbsExprs a=expressions();
                        if (simbol.token != Token.RPARENT)
                            Report.error(simbol.position,"Expecting right parent!");
                        nextThingy();
                        ArrayList<AbsExpr> b = new ArrayList<>();
                        for (int i = 0; i < a.numExprs(); i++) {
                            b.add(a.expr(i));
                        }
                        
                        return new AbsFunCall(new Position(ps,simbol.position),ime,b);
                    }
                    dump("atom_expression -> identifier");
                    return new AbsVarName(new Position(ps,simbol.position),ime);
                case Token.LBRACE:
                    nextThingy();
                    if (simbol.token == Token.KW_IF ||
                            simbol.token == Token.KW_WHILE ||
                            simbol.token == Token.KW_FOR ||
                            simbol.token == Token.IDENTIFIER )
                        return atom_expression3();
                    break;
                case Token.LPARENT:
                    nextThingy();
                    AbsExpr a = expressions();
                    
                    if (simbol.token != Token.RPARENT)
                        Report.error(simbol.position,"Expecting right parent!");
                    nextThingy();
                    return a;
                case Token.LOG_CONST:
                {
                    dump("atom_expression -> log_constant");
                    String retIme = simbol.lexeme;
                    nextThingy();
                    return new AbsAtomConst(new Position(ps,simbol.position),AbsAtomConst.LOG,retIme);
                }    case Token.INT_CONST:
                {
                    dump("atom_expression -> int_constant");
                    String retIme = simbol.lexeme;
                    nextThingy();
                    return new AbsAtomConst(new Position(ps,simbol.position),AbsAtomConst.INT,retIme);
                }    case Token.STR_CONST:
                {
                    dump("atom_expression -> str_constant");
                    String retIme = simbol.lexeme;
                    nextThingy();
                    return new AbsAtomConst(new Position(ps,simbol.position),AbsAtomConst.STR,retIme);
                }    default:
                    Report.error(simbol.position,"unknown error at atom expr");
                    break;
            }
           return null;
        }
        private AbsExpr atom_expression3(){
            Position ps = simbol.position;
            switch (simbol.token) {
                case Token.KW_IF:
                {
                    nextThingy();
                    AbsExpr a = expression();
                    //ae2
                    if (simbol.token != Token.KW_THEN)
                        Report.error(simbol.position,"Expecting then!");
                    nextThingy();
                    AbsExpr b = expression();
                    //ae4
                    if (simbol.token == Token.KW_ELSE){
                        dump("atom_expression -> { if expression then expression else expression }");
                        nextThingy();
                        AbsExpr c= expression();//}
                        if (simbol.token != Token.RBRACE)
                            Report.error(simbol.position,"Expecting right brace!");
                        nextThingy();
                        return new AbsIfThenElse(new Position(ps,simbol.position),a,b,c);
                    }
                    //}
                    else if (simbol.token != Token.RBRACE)
                        Report.error(simbol.position,"Expecting right brace!");
                    dump("atom_expression -> { if expression then expression }");
                    nextThingy();
                    return new AbsIfThen(new Position(ps,simbol.position),a,b);
                }
                case Token.KW_WHILE:
                {
                    dump("atom_expression -> { while expression : expression }");
                    nextThingy();
                    AbsExpr a=expression();// :
                    if (simbol.token != Token.COLON)
                        Report.error(simbol.position,"Expecting :!");
                    nextThingy();
                    AbsExpr b=expression();// }
                    if (simbol.token != Token.RBRACE)
                        Report.error(simbol.position,"Expecting right brace!");
                    nextThingy();
                    return new AbsWhile(new Position(ps,simbol.position),a,b);
                }
                case Token.KW_FOR:
                {
                    dump("atom_expression -> { for identifier = expression , expression , expression : expression }.");
                    nextThingy();
                    if (simbol.token != Token.IDENTIFIER)
                        Report.error(simbol.position,"Expecting identifier!");
                    String ime = simbol.lexeme;
                    nextThingy();
                    
                    if (simbol.token != Token.ASSIGN)
                        Report.error(simbol.position,"Expecting =!");
                    nextThingy();
                    AbsExpr a = expression();//,
                    
                    if (simbol.token != Token.COMMA)
                        Report.error(simbol.position,"Expecting ,!");
                    nextThingy();
                    AbsExpr b = expression();// ,
                    
                    if (simbol.token != Token.COMMA)
                        Report.error(simbol.position,"Expecting ,!");
                    nextThingy();
                    AbsExpr c = expression();// :
                    if (simbol.token != Token.COLON)
                        Report.error(simbol.position,"Expecting :!");
                    nextThingy();
                    AbsExpr d = expression();// }
                    if (simbol.token != Token.RBRACE)
                        Report.error(simbol.position,"Expecting right brace!");
                    nextThingy();
                    return new AbsFor(new Position(ps,simbol.position),new AbsVarName(new Position(ps,simbol.position),ime),a,b,c,d);
                }
                case Token.IDENTIFIER:
                {
                    dump("atom_expression -> { expression = expression }");
                    AbsExpr a = expression();
                    if (simbol.token != Token.ASSIGN)
                        Report.error(simbol.position,"Expecting =!");
                    nextThingy();
                    AbsExpr b = expression();//}
                    if (simbol.token != Token.RBRACE)
                        Report.error(simbol.position,"Expecting right brace!");
                    nextThingy();
                    return new AbsBinExpr(new Position(ps,simbol.position), AbsBinExpr.ASSIGN, a, b);
                }
                default:
                    Report.error(simbol.position,"Unknown error at atom expression3!");
                    break;
            }
            return null;
        }
        private AbsExprs expressions(){
            dump("expressions -> expression expressions1 ");
            Position ps = simbol.position;
            ArrayList<AbsExpr> a = new ArrayList<>();
            a.add(expression());
            if(simbol.token == Token.COMMA)
                a.addAll(expressions1());
            return new AbsExprs(new Position(ps,simbol.position),a);
        }
        private ArrayList<AbsExpr> expressions1(){
            dump("expressions1 -> , expression expressions1 ");
            ArrayList<AbsExpr> a = new ArrayList<>();
            nextThingy();
            a.add(expression());
            if(simbol.token == Token.COMMA)
                a.addAll(expressions1());
            return a;
        }
        private AbsVarDef variable_definition(Position ps){
            dump("variable_definition -> var identifer : type ");
            if (simbol.token != Token.IDENTIFIER)
                Report.error(simbol.position,"Expecting identifier!");
            String ime = simbol.lexeme;
            nextThingy();
                
            if (simbol.token != Token.COLON)
                Report.error(simbol.position,"Missing :!");
            nextThingy();
            return new AbsVarDef(new Position(ps,simbol.position),ime,type());
        }

	/**
	 * Izpise produkcijo v datoteko z vmesnimi rezultati.
	 * 
	 * @param production
	 *            Produkcija, ki naj bo izpisana.
	 */
	private void dump(String production) {
		if (!dump)
			return;
		if (Report.dumpFile() == null)
			return;
		Report.dumpFile().println(production);
	}

}
