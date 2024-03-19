lexer grammar SysYLexer;


CONST : 'const';

INT : 'int';

VOID : 'void';

IF : 'if';

ELSE : 'else';

WHILE : 'while';

BREAK : 'break';

CONTINUE : 'continue';

RETURN : 'return';

PLUS : '+';

MINUS : '-';

MUL : '*';

DIV : '/';

MOD : '%';

ASSIGN : '=';

EQ : '==';

NEQ : '!=';

LT : '<';

GT : '>';

LE : '<=';

GE : '>=';

NOT : '!';

AND : '&&';

OR : '||';

L_PAREN : '(';

R_PAREN : ')';

L_BRACE : '{';

R_BRACE : '}';

L_BRACKT : '[';

R_BRACKT : ']';

COMMA : ',';

SEMICOLON : ';';

IDENT : (LETTER) ( LETTER | DIGIT)*
   ;

fragment LETTER : [_a-zA-Z];

fragment DIGIT : [0-9];

INTEGER_CONST : ([1-9]+)[0-9]*
              | '0'[0-7]*
              | ('0x' | '0X')[0-9A-Fa-f]+                                    //数字常量，包含十进制数，0开头的八进制数，0x或0X开头的十六进制数
              ;

WS
   : [ \r\n\t]+ -> skip
   ;

LINE_COMMENT
   : '//' .*? '\n' -> skip
   ;

MULTILINE_COMMENT
   : '/*' .*? '*/' -> skip
   ;