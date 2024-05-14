
parser grammar SysYParser;

options {
    tokenVocab = SysYLexer;
}

program
    : compUnit
    ;

compUnit
    : (funcDef | decl)+ EOF
    ;

decl
    : constDecl | varDecl
    ;

constDecl
    : CONST INT constDef (COMMA constDef)* SEMICOLON
    ;

constDef
    : IDENT (L_BRACKT constExp R_BRACKT)* ASSIGN constInitVal
    ;

constInitVal
    : constExp
    | L_BRACE (constInitVal (COMMA constInitVal)*)? R_BRACE
    ;

varDecl
    : INT varDef (COMMA varDef)* SEMICOLON
    ;

varDef
    : IDENT (L_BRACKT constExp R_BRACKT)*
    | IDENT (L_BRACKT constExp R_BRACKT)* ASSIGN initVal
    ;

initVal
    : exp
    | L_BRACE (initVal (COMMA initVal)*)? R_BRACE
    ;

funcDef
    : funcType funcName L_PAREN (funcFParams)? R_PAREN block
    ;

funcType
    : VOID
    | INT
    ;

funcFParams
    : funcFParam (COMMA funcFParam)*
    ;

funcFParam
    : INT IDENT (L_BRACKT R_BRACKT (L_BRACKT exp R_BRACKT)*)?
    ;

block
    : L_BRACE (blockItem)* R_BRACE
    ;

blockItem
    : decl
    | stmt
    ;

stmt
    : lVal ASSIGN exp SEMICOLON // 换行
    | (exp)? SEMICOLON // 换行
    | block // 换行？不一定，如果这个block是嵌在下面两行的stmt里面的，就不需要换行。著有
    | IF L_PAREN cond R_PAREN stmt (ELSE stmt)?
    | WHILE L_PAREN cond R_PAREN stmt
    | BREAK SEMICOLON
    | CONTINUE SEMICOLON
    | returnStmt
    ;

exp
   : L_PAREN exp R_PAREN
   | lVal
   | number
   | funcName L_PAREN funcRParams? R_PAREN
   | unaryExp
   | exp op=(MUL | DIV | MOD) exp
   | exp op=(PLUS | MINUS) exp
   ;

cond
   : exp
   | cond (LT | GT | LE | GE) cond
   | cond (EQ | NEQ) cond
   | cond AND cond
   | cond OR cond
   ;

lVal
   : IDENT (L_BRACKT exp R_BRACKT)*
   ;

number
   : INTEGER_CONST
   ;

unaryOp
   : PLUS
   | MINUS
   | NOT
   ;

funcRParams
   : param (COMMA param)*
   ;

param
   : exp
   ;

constExp
   : exp
   ;

funcName
    : IDENT
    ;
returnStmt
    : RETURN (exp)? SEMICOLON
    ;

unaryExp
    : unaryOp exp
    ;

binaryExp
    : exp op=(MUL | DIV | MOD) exp
    | exp op=(PLUS | MINUS) exp
    ;