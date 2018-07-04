package compiler.interpreter;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;

public final class Interpreter {

	public static boolean debug = true;
        

	/*--- staticni del navideznega stroja ---*/
	
	/** Pomnilnik navideznega stroja. */
	public static HashMap<Integer, Object> mems = new HashMap<Integer, Object>();
	public static HashMap<FrmLabel, Object> global = new HashMap<FrmLabel, Object>();
	public static HashMap<Integer, String> globalVarName = new HashMap<Integer, String>();
	
	public static void stM(Integer address, Object value) {
		if (debug) System.out.println(" [" + address + "] <= " + value);
		mems.put(address, value);
	}

	public static Object ldM(Integer address) {
		Object value = mems.get(address);
		if (debug) System.out.println(" [" + address + "] => " + value);
                if (value == null){
                    if (address < globalVarName.size()*4)
                        System.out.println("WARNING: Using null variable: "+globalVarName.get(address).substring(1)+"!");
                    else
                        System.out.println(" "+address +" in not set ( is null )");
                }
		return value;
	}
	
        boolean rtrn=false;
	/** Kazalec na vrh klicnega zapisa. */
	private static int fp = 1000;

	/** Kazalec na dno klicnega zapisa. */
	private static int sp = 1000;
	
	/*--- dinamicni del navideznega stroja ---*/
	
	/** Zacasne spremenljivke (`registri') navideznega stroja. */
	public HashMap<FrmTemp, Object> temps = new HashMap<>();
		
	public void stT(FrmTemp temp, Object value) {
		if (debug) System.out.println(" " + temp.name() + " <= " + value);
		temps.put(temp, value);
	}

	public Object ldT(FrmTemp temp) {
		Object value = temps.get(temp);
		if (debug) System.out.println(" " + temp.name() + " => " + value);
		return value;
	}
	
	/*--- Izvajanje navideznega stroja. ---*/
	
	public Interpreter(FrmFrame frame, ImcSEQ code) {
		if (debug) {
			System.out.println("[START OF " + frame.label.name() + "]");
		}
                if(frame.fun.name.toLowerCase().equals("main")){
                    stM(fp +4,0);
                    int pts = 0;
                    for (ImcChunk chunk : Main.imcodegen.chunks) {
                        if (chunk instanceof ImcDataChunk){
                            global.put(((ImcDataChunk)chunk).label, pts);
                            globalVarName.put(pts, ((ImcDataChunk) chunk).label.name());
                            pts+=4;
                        }
                    }
                }
		stM(sp , fp);
                stT(frame.FP,sp);
                stT(frame.RV,sp-frame.size());
		fp = sp;
		sp = sp - frame.size();
		if (debug) {
			System.out.println("[FP=" + fp + "]");
			System.out.println("[SP=" + sp + "]");
		}

		int pc = 0;
		Object result = null;
		while (pc < code.stmts.size()) {
                        if (rtrn) break;
			if (debug) System.out.println("pc=" + pc);
			ImcCode instruction = code.stmts.get(pc);
			result = execute(instruction);
			if (result instanceof FrmLabel) {
				for (pc = 0; pc < code.stmts.size(); pc++) {
					instruction = code.stmts.get(pc);
					if ((instruction instanceof ImcLABEL) && (((ImcLABEL) instruction).label.name().equals(((FrmLabel) result).name())))
						break;
				}
			}
			else
				pc++;
		}
		rtrn=false;
		fp = (Integer) ldM(fp);
		sp = sp + frame.size();
		if (debug) {
			System.out.println("[FP=" + fp + "]");
			System.out.println("[SP=" + sp + "]");
		}
		
		stM(sp, result);
		if (debug) {
			System.out.println("[RV=" + result + "]");
		}

		if (debug) {
			System.out.println("[END OF " + frame.label.name() + "]");
		}
	}
	
	public Object execute(ImcCode instruction) {
		if (instruction instanceof ImcBINOP) {
			ImcBINOP instr = (ImcBINOP) instruction;
			Object fstSubValue = execute(instr.limc);
			Object sndSubValue = execute(instr.rimc);
			switch (instr.op) {
			case ImcBINOP.OR:
				return ((((Integer) fstSubValue) != 0) || (((Integer) sndSubValue) != 0) ? 1 : 0);
			case ImcBINOP.AND:
				return ((((Integer) fstSubValue) != 0) && (((Integer) sndSubValue) != 0) ? 1 : 0);
			case ImcBINOP.EQU:
				return (((Integer) fstSubValue).intValue() == ((Integer) sndSubValue).intValue() ? 1 : 0);
			case ImcBINOP.NEQ:
				return (((Integer) fstSubValue).intValue() != ((Integer) sndSubValue).intValue() ? 1 : 0);
			case ImcBINOP.LTH:
				return (((Integer) fstSubValue) < ((Integer) sndSubValue) ? 1 : 0);
			case ImcBINOP.GTH:
				return (((Integer) fstSubValue) > ((Integer) sndSubValue) ? 1 : 0);
			case ImcBINOP.LEQ:
				return (((Integer) fstSubValue) <= ((Integer) sndSubValue) ? 1 : 0);
			case ImcBINOP.GEQ:
				return (((Integer) fstSubValue) >= ((Integer) sndSubValue) ? 1 : 0);
			case ImcBINOP.ADD:
				return (((Integer) fstSubValue) + ((Integer) sndSubValue));
			case ImcBINOP.SUB:
				return (((Integer) fstSubValue) - ((Integer) sndSubValue));
			case ImcBINOP.MUL:
				return (((Integer) fstSubValue) * ((Integer) sndSubValue));
			case ImcBINOP.DIV:
				return (((Integer) fstSubValue) / ((Integer) sndSubValue));
			}
			Report.error("Internal error.");
			return null;
		}
		
		if (instruction instanceof ImcCALL) {
			ImcCALL instr = (ImcCALL) instruction;
			int offset = 0;
                        
			for (ImcCode arg : instr.args) {
				stM(sp + offset, execute(arg));
				offset += 4;
			}
                        if (instr.label.name().equals("_main")){
                                System.out.println("Main class can not be called and is skipped");
                                return null;
                        }
                        if (instr.label.name().equals("_returnStr")){
                                rtrn=true;
                                return (String) ldM(sp + 4);
                        }
                        if (instr.label.name().equals("_returnLog")){
                                rtrn=true;
                                return (boolean) ldM(sp + 4);
                        }
                        if (instr.label.name().equals("_returnInt")){
                                rtrn=true;
                                return (Integer) ldM(sp + 4);
                        }
			if (instr.label.name().equals("_putInt")) {
				System.out.println((Integer) ldM(sp + 4));
				return (Integer) ldM(sp + 4);
			}
			if (instr.label.name().equals("_putIntN")) {
				System.out.print((Integer) ldM(sp + 4));
				return (Integer) ldM(sp + 4);
			}
			if (instr.label.name().equals("_getInt")) {
				Scanner scanner = new Scanner(System.in);
				return scanner.nextInt();
			}
			if (instr.label.name().equals("_putString")) {
				System.out.println((String) ldM(sp + 4));
				return (String) ldM(sp + 4);
			}if (instr.label.name().equals("_putStringN")) {
				System.out.print((String) ldM(sp + 4));
				return (String) ldM(sp + 4);
			}
			if (instr.label.name().equals("_getString")) {
				Scanner scanner = new Scanner(System.in);
				return scanner.next();
			}
                        for (ImcChunk chunk : Main.imcodegen.chunks) {
                            if (chunk instanceof ImcCodeChunk){
                                ImcCodeChunk a = (ImcCodeChunk)chunk;
                                if (a.frame.label.name().toLowerCase().equals(instr.label.name().toLowerCase())){
                                    Interpreter interpreter = new Interpreter(a.frame,a.lincode.linear());
                                    return ldM((Integer)interpreter.temps.get(a.frame.FP));
                                }
                            }
                        }
			return null;
		}
		
		if (instruction instanceof ImcCJUMP) {
			ImcCJUMP instr = (ImcCJUMP) instruction;
			Object cond = execute(instr.cond);
			if (cond instanceof Integer) {
				if (((Integer) cond) == 0)
                                    return instr.falseLabel;
				else
                                    return instr.trueLabel;
			}
			else Report.error("CJUMP: illegal condition type.");
		}
		
		if (instruction instanceof ImcCONST) {
			ImcCONST instr = (ImcCONST) instruction;
			return instr.value;
		}
		
		if (instruction instanceof ImcJUMP) {
			ImcJUMP instr = (ImcJUMP) instruction;
			return instr.label;
		}
		
		if (instruction instanceof ImcLABEL) {
			return "ImcLABEL incorrect code";
		}
		
		if (instruction instanceof ImcMEM) {
			ImcMEM instr = (ImcMEM) instruction;
                        Object a = execute(instr.expr);
			return ldM((Integer) execute(instr.expr));
		}
		
		if (instruction instanceof ImcMOVE) {
			ImcMOVE instr = (ImcMOVE) instruction;
			if (instr.dst instanceof ImcTEMP) {
				FrmTemp temp = ((ImcTEMP) instr.dst).temp;
				Object srcValue = execute(instr.src);
				stT(temp, srcValue);
				return srcValue;
			}
			if (instr.dst instanceof ImcMEM) {
				Object dstValue = execute(((ImcMEM) instr.dst).expr);
				Object srcValue = execute(instr.src);
				stM((Integer) dstValue, srcValue);
				return srcValue;
			}
		}
		
		if (instruction instanceof ImcNAME) {
			ImcNAME instr = (ImcNAME) instruction;
                        Object a = global.get(instr.label);
                        if (a == null)
                            return instr.label.name().substring(1);
                        return a;
		}
		
		if (instruction instanceof ImcTEMP) {
			ImcTEMP instr = (ImcTEMP) instruction;
			return ldT(instr.temp);
		}
		
		return null;
	}
	
}
