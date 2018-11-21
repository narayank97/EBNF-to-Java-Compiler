/* *** This file is given as part of the programming assignment. *** */
//import java.util.Stack;

/* SCOPING STRATEGY:
--->Everytime I see a declared var and its assignment push to Stack
----> Once I hit a different scope keep track of a counter to see what scope im in.

*/
import java.util.*;
import java.lang.Object;

public class Parser {

    static List<List<String>> blockLists= new ArrayList<>(); // list of scopes
    static List<String> blockVars = new ArrayList<>(); // variables in scopes
    static List<String> copyblockVars = new ArrayList<>();//needed to push to blocklists
    int myScopingCounter = -1; // checks what scope i am in
    private Token tok; // the current token
    private void scan() {
	     tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
	  this.scanner = scanner;
	  scan();
	  program();
	  if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }

    private void program() {
	     block();
    }

    private void block(){
      List<String> blockVars = new ArrayList<>();//every new block create fresh scope
      List<String> copyblockVars = new ArrayList<>();
	    declaration_list();
      statement_list();
    }

    private void declaration_list() {
	   while( is(TK.DECLARE)){
       declaration();
     }
     //I copy what was in blockVars
     List<String> copyblockVars = new ArrayList<String>(blockVars);
     //push copy to blocklists
     blockLists.add(copyblockVars);
     //I empty the block vars to create a fresh scope
     blockVars.clear();
    }

    private void declaration() {
	    mustbe(TK.DECLARE);
      //if the token is in the blockVars then its been redeclared
      if(blockVars.contains(tok.string))
      {
        System.out.println("redeclaration of variable "+tok.string);
      }
      //push the token if it has not been redeclared
      blockVars.add(tok.string);
      mustbe(TK.ID);
      while( is(TK.COMMA) ) {
	       scan();
         //do the same check for the variables sepreated by commas
         if(blockVars.contains(tok.string))
         {
           System.out.println("redeclaration of variable "+tok.string);
         }
         blockVars.add(tok.string);
	       mustbe(TK.ID);
	    }
    }

    private void statement_list() {
      //I coomented on what this does in part 2
      while(is(TK.ID)|| is(TK.TILDE) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)) {

        if(is(TK.ID)||is(TK.TILDE))
        {
            assignment();
        }
        else if( is(TK.PRINT))
        {
          printFunc();
        }
        else if(is(TK.DO))
        {
          mustbe(TK.DO);
          doFunc();
          mustbe(TK.ENDDO);
        }
        else if(is(TK.IF))
        {
          mustbe(TK.IF);
          ifFunc();
          mustbe(TK.ENDIF);
          blockLists.remove(blockLists.size()-1); // remove scope 
        }

      }
    }

    private void assignment(){
      if(is(TK.ID))
      {
        refID();
        mustbe(TK.ASSIGN);
        expr();
      }
      else if(is(TK.TILDE))
      {
        refID();
        mustbe(TK.ASSIGN);
        expr();

      }

    }

    private void printFunc(){
      mustbe(TK.PRINT);
      expr();
    }
    private void refID()
    {
      StringBuilder varName = new StringBuilder(); // make the varname for errors
      StringBuilder incomingToken = new StringBuilder();//create the var that is coming in
      String checkString = new String();
      int intNumRef = 0; // gets number of desired scope in ref id
      int traverseCounter; // counter used to traverse scopes
      boolean numRef = false; // bool that tells me if there is a num in ref id

      if(is(TK.TILDE))
      {
        varName.append(tok.string);
        mustbe(TK.TILDE);
        if(is(TK.NUM))
        {
          numRef = true; // set to true since there is a num in the refid
          varName.append(tok.string);//append incoming token to varName
          int x = 0;
          for(int i = 0; i<blockLists.size(); i++)
          {//loop used to skip empty lists
            if(blockLists.get(i).size() == 0)
            {
              x++;
            }
          }
          //get int part of refid and change to int
          intNumRef = Integer.parseInt(tok.string) + x;
          mustbe(TK.NUM);
        }
        if(is(TK.ID))
        {
          if(varName.length() == 1)
          {//that means varName is just ~
            //checks for global scope
            if(blockLists.get(0).contains(tok.string))
            {
              //do nothing
            }
            else
            {// if not in global scope it returns an error
              varName.append(tok.string);
              System.out.println("no such variable "+ varName +" on line "+ tok.lineNumber);
              System.exit(1);
            }
          }
        }
      }
      varName.append(tok.string);
      if(is(TK.ID))
      {
        if(blockLists.size() == 0){ // checks to see no variables declared
          System.out.println(varName+" is an undeclared variable on line "+ tok.lineNumber);
          System.exit(1);

        }
        if(blockLists.get(0).size() == 0)
        { // checks to see if global scope is empty
          System.out.println(varName+" is an undeclared variable on line "+ tok.lineNumber);
          System.exit(1);

        }
        checkString = tok.string;
        incomingToken.append(tok.string);
      }
      //prepare to traverse to entire list of scopes
      traverseCounter = blockLists.size()-1;
      if(numRef == true)
      {
        //change the tarverseal iterator based on the num in refid
        traverseCounter = (blockLists.size()-1) - intNumRef;
        if(intNumRef >= blockLists.size())
        {// if the num of refid is greater then size of scope list returns error
          System.out.println("no such variable "+varName+" on line "+(tok.lineNumber));
          System.exit(1);
        }
      }
      numRef = false; // change back numref back to false
      while(traverseCounter != -1)//while we go through entire scope
      {
        if(blockLists.get(traverseCounter).contains(checkString))
        {//if the id is in the desire scope break
          break;
        }
        else
        {//keep traversing

          traverseCounter--;
        }
      }
      if(traverseCounter == -1)
      {//we have gone through entire scope list, means its undeclared
        System.out.println(checkString+" is an undeclared variable on line "+(tok.lineNumber));
        System.exit(1);
      }
      mustbe(TK.ID);
    }
    private void doFunc(){
      guarded_command();
    }
    private void ifFunc(){
      guarded_command();
      if(is(TK.ELSEIF)){
        while( is(TK.ELSEIF)) {
          mustbe(TK.ELSEIF);
          guarded_command();
        }
      }
      if(is(TK.ELSE))
      {
        mustbe(TK.ELSE);
        block();
      }
    }
    private void expr(){
      term();
      while(is(TK.PLUS) || is(TK.MINUS)) {
	       scan();
	       term();
	    }
    }
    private void guarded_command(){
      expr();
      mustbe(TK.THEN);
      block();
    }
    private void term(){
      factor();
      while(is(TK.TIMES) || is(TK.DIVIDE)) {
	       scan();
	       factor();
	    }

    }
    private void factor(){
        if(is(TK.LPAREN))
        {
          scan();
          expr();
          //scan();
          mustbe(TK.RPAREN);
        }
        else if(is(TK.ID))
        {
          refID();
          //mustbe(TK.ID);
        }
        else if(is(TK.TILDE))
        {
          refID();
        }
        else if(is(TK.NUM))
        {
          mustbe(TK.NUM);
        }
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " + tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line " + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
