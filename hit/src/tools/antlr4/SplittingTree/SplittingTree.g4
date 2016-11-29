grammar SplittingTree;


splitting_tree 
	: input   '(' (output '(' splitting_tree ')' ';')* ')'
//	: input   '(' ( branch ';')* ')'   
	| state?
//	| input   '(' (output state ';')+  ')'
	; 
	
	
	
//branch 
//	: output '(' splitting_tree ')' 
//	;	
	
	
	
/*	
splitting_tree 
	: input   '(' (splitting_tree ';')+  ')'  
	| '(' (state ';')*  ')'
	| output '(' splitting_tree ')' 
	| output   '(' state? ')' 
	; 	
	 */
	
state
	: 	ID 	
	| 	NUMBER
	;


input
	:	ID
	|	NUMBER	
	;

output
	:	ID 
	|	NUMBER
	;



/** "a numeral [-]?(.[0-9]+ | [0-9]+(.[0-9]*)? )" */ 
NUMBER
   : '-'? ( '.' DIGIT+ | DIGIT+ ( '.' DIGIT* )? )
   ;


fragment DIGIT
   : ('0'..'9')
   ;

/** "Any string of alphabetic ([a-zA-Z\200-\377]) characters, underscores
 *  ('_') or digits ([0-9])"
 */ 

ID
   : LETTER ( LETTER | DIGIT )*
   | DIGIT ( LETTER | DIGIT )*   
   ;


   
fragment LETTER
   : [a-zA-Z\u0080-\u00FF_]
   ;



COMMENT
   : '/*' .*? '*/' -> skip
   ;


LINE_COMMENT
   : '//' .*? '\r'? '\n' -> skip
   ;


/** "a '#' character is considered a line output from a C preprocessor (e.g.,
 *  # 34 to indicate line 34 ) and discarded"
 */ 
PREPROC
   : '#' .*? '\n' -> skip
   ;


//WS
//   : [\t\n\r]+ -> skip 
//   ;
   
 
