package compiler.lexan;

import compiler.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Leksikalni analizator.
 * 
 * @author sliva
 */
public class LexAn {
	
	/** Ali se izpisujejo vmesni rezultati. */
	private boolean dump;
        private String koda = "fun getInt(x:integer):integer = x;"+
                              "fun putInt(x:integer):integer = x;" +
                              "fun putString(s:string):string = s;"+
                              "fun getString(s:string):string = s;"+
                              "fun putIntN(x:integer):integer = x;" +
                              "fun putStringN(s:string):string = s;"+
                              "fun returnStr(s:string):string = s;" +
                              "fun returnInt(x:integer):integer = x;" +
                              "fun returnLog(b:logical):logical = b;\n";
        private char thisChar;
        private char nextChar;
        private int pointer = 0;
        private int currRow = 0;
        private int currCol = 0;
	/**
	 * Ustvari nov leksikalni analizator.
	 * 
	 * @param sourceFileName
	 *            Ime izvorne datoteke.
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
        Scanner sc;
	public LexAn(String sourceFileName, boolean dump){		
		// TODO
		this.dump = dump;
                try{
                    sc = new Scanner(new File(sourceFileName));
                }catch(FileNotFoundException e){System.out.println("File error");}
                while(sc.hasNextLine()){
                    koda += sc.nextLine()+"\n";
                }
	}
	private void setCharacter(){
            thisChar = koda.charAt(pointer);
            currCol++;
            pointer++;
        }
	/**
	 * Vrne naslednji simbol iz izvorne datoteke. Preden vrne simbol, ga izpise
	 * na datoteko z vmesnimi rezultati.
	 * 
	 * @return Naslednji simbol iz izvorne datoteke.
	 */
        public Symbol lexAn() {
	    Symbol r = lexAn1();
	    if (dump)
	        dump(r);
            return r;
	}
        
        
	public Symbol lexAn1() {
                Position ps = new Position(currRow, currCol);
		while(true){
                    ps = new Position(currRow, currCol);
                    if (koda.length() <= pointer)
                        return new Symbol(Token.EOF, "-1", new Position(ps,new Position(currRow, currCol)));
                    setCharacter();
                    if (koda.length() > pointer)
                        nextChar = koda.charAt(pointer);
                    else
                        nextChar = ' ';
                    
                    if(thisChar == ' ' || thisChar == '\t'){
                        continue;
                    }
                    if(thisChar == '\n'){
                        currCol = 0;
                        currRow++;
                        continue;
                    }
                    if(thisChar == '='){
                        if(nextChar == '='){
                            pointer++;
                            currCol++;
                            return new Symbol(Token.EQU,"==",new Position(ps,new Position(currRow, currCol)));
                        }
                        return new Symbol(Token.ASSIGN,"=",new Position(ps,new Position(currRow, currCol)));
                    } if(thisChar == '!'){
                        if(nextChar == '='){
                            pointer++;
                            currCol++;
                            return new Symbol(Token.NEQ,"!=",new Position(ps,new Position(currRow, currCol)));
                        }
                        return new Symbol(Token.NOT,"!",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '<'){
                        if(nextChar == '='){
                            pointer++;
                            currCol++;
                            return new Symbol(Token.LEQ,"<=",new Position(ps,new Position(currRow, currCol)));
                        }
                        return new Symbol(Token.LTH,"<",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '>'){
                        if(nextChar == '='){
                            pointer++;
                            currCol++;
                            return new Symbol(Token.GEQ,">=",new Position(ps,new Position(currRow, currCol)));
                        }
                        return new Symbol(Token.GTH,">",new Position(ps,new Position(currRow, currCol)));
                    }
                    // ENOJNI ZNAKI------------------------------------------------
                    
                    
                    
                    if(thisChar == '+'){
                        return new Symbol(Token.ADD,"+",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '-'){
                        return new Symbol(Token.SUB,"-",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '*'){
                        return new Symbol(Token.MUL,"*",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '/'){
                        return new Symbol(Token.DIV,"/",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '&'){
                        return new Symbol(Token.AND,"&",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '|'){
                        return new Symbol(Token.IOR,"|",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == ':'){
                        return new Symbol(Token.COLON,":",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == ';'){
                        return new Symbol(Token.SEMIC,";",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '.'){
                        return new Symbol(Token.DOT,".",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == ','){
                        return new Symbol(Token.COMMA,",",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '%'){
                        return new Symbol(Token.MOD,"%",new Position(ps,new Position(currRow, currCol)));
                    }
                    //OKLEPAJI------------------------------------------------
                    
                    
                    
                    
                    if(thisChar == '('){
                        return new Symbol(Token.LPARENT,"(",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == ')'){
                        return new Symbol(Token.RPARENT,")",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '{'){
                        return new Symbol(Token.LBRACE,"{",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '}'){
                        return new Symbol(Token.RBRACE,"}",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == '['){
                        return new Symbol(Token.LBRACKET,"[",new Position(ps,new Position(currRow, currCol)));
                    }
                    if(thisChar == ']'){
                        return new Symbol(Token.RBRACKET,"]",new Position(ps,new Position(currRow, currCol)));
                    }
                   
                    
                    
                    
                    //STRINGI
                    if(thisChar >= 'A' && thisChar <= 'Z' || thisChar >= 'a' && thisChar <= 'z'){
                        String s = ""+thisChar;
                        while(true){
                            if (koda.length() <= pointer){
                                return new Symbol(Token.EOF, "-1", new Position(ps,new Position(currRow, currCol)));
                            }
                            setCharacter();
                            if (koda.length() > pointer)
                                nextChar = koda.charAt(pointer);
                            else
                                nextChar = ' ';
                            
                            if(thisChar >= 'A' && thisChar <= 'Z' || thisChar >= 'a' && thisChar <= 'z' || thisChar == '_' || thisChar >= '0' && thisChar <= '9'){
                                s+=thisChar;
                            }
                            else
                                break;
                        }
                        pointer--;
                        currCol--;
                        switch (s.toLowerCase()){
                            case "string":
                                return new Symbol(Token.STRING,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "logical":
                                return new Symbol(Token.LOGICAL,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "integer":
                                return new Symbol(Token.INTEGER,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "arr":
                                return new Symbol(Token.KW_ARR,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "else":
                                return new Symbol(Token.KW_ELSE,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "for":
                                return new Symbol(Token.KW_FOR,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "fun":
                                return new Symbol(Token.KW_FUN,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "if":
                                return new Symbol(Token.KW_IF,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "then":
                                return new Symbol(Token.KW_THEN,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "typ":
                                return new Symbol(Token.KW_TYP,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "var":
                                return new Symbol(Token.KW_VAR,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "where":
                                return new Symbol(Token.KW_WHERE,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "while":
                                return new Symbol(Token.KW_WHILE,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "true":
                                return new Symbol(Token.LOG_CONST,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            case "false":
                                return new Symbol(Token.LOG_CONST,s.toLowerCase(),new Position(ps,new Position(currRow, currCol))); 
                            default: break;
                                
                        }
                        return new Symbol(Token.IDENTIFIER,s,new Position(ps,new Position(currRow, currCol))); 
                    }
                    
                    
                    
                    
                    //STEVILA
                    if(thisChar >= '0' && thisChar <= '9'){
                        String s = ""+thisChar;
                        while(true){
                            if (koda.length() <= pointer)
                                return new Symbol(Token.EOF, "-1", new Position(ps,new Position(currRow, currCol)));
                            setCharacter();
                            if(thisChar >= '0' && thisChar <= '9'){
                                s+=thisChar;
                            }
                            else{
                                pointer--;
                                currCol--;
                                break;
                            }
                        }
                        return new Symbol(Token.INT_CONST,s,new Position(ps,new Position(currRow, currCol))); 
                    }
                    
                    
                    
                    
                    
                    
                    //STRING MED NAREKOVAJI
                    if(thisChar == (char)39){
                        String s = "";
                        while(true){
                            if (koda.length() <= pointer){
                                Report.error(new Position(ps,new Position(currRow, currCol)),"String does not end!");
                                return new Symbol(Token.EOF, "-1", new Position(ps,new Position(currRow, currCol)));
                            }
                            setCharacter();
                            if (koda.length() > pointer)
                                nextChar = koda.charAt(pointer);
                            else
                                nextChar = ' ';
                            
                            if(thisChar == '\n'){
                                Report.error(new Position(ps,new Position(currRow, currCol)),"String does not end!");
                                break;
                            }
                            if(thisChar >= (char)32 && thisChar <= (char)126){
                                if(thisChar != (char)39)
                                    s+=thisChar;
                                if (thisChar == (char)39 && nextChar == (char)39){
                                    pointer++;
                                    currCol++;
                                    s+=thisChar;
                                }
                                if (thisChar == (char)39 && nextChar != (char)39)
                                    break;
                            }
                            
                        }
                        return new Symbol(Token.STR_CONST,s,new Position(ps,new Position(currRow, currCol))); 
                    }
                    
                    
                    
                    
                    //KOMENTAR
                    if(thisChar == '#'){
                        while(true){
                            if (koda.length() <= pointer)
                                return new Symbol(Token.EOF, "-1", new Position(ps,new Position(currRow, currCol)));
                            setCharacter();
                            if (koda.length() > pointer)
                                nextChar = koda.charAt(pointer);
                            else
                                nextChar = ' ';
                            
                            if(thisChar == '\n'){
                                currCol = 0;
                                currRow++;
                                break;
                            }
                        }
                        continue;
                    }
                    
                    
                    break;
                }
                Report.error(new Position(ps,new Position(currRow, currCol)),"unknown character: "+thisChar);
                //(new Position(ps,new Position(currRow, currCol)),"unknown");
                return null;
	}

	/**
	 * Izpise simbol v datoteko z vmesnimi rezultati.
	 * 
	 * @param symb
	 *            Simbol, ki naj bo izpisan.
	 */
	private void dump(Symbol symb) {
		if (! dump) return;
		if (Report.dumpFile() == null) return;
		if (symb.token == Token.EOF)
			Report.dumpFile().println(symb.toString());
		else
			Report.dumpFile().println("[" + symb.position.toString() + "] " + symb.toString());
	}

}
