/*parser grammar SysYParser;
options {
    tokenVocab = SysYLexer;//注意使用该语句指定词法分析器；请不要修改词法分析器或语法分析器的文件名，否则Makefile可能无法正常工作，影响评测结果
}
program
   : compUnit
   ;

compUnit
   : (funcDef | decl)+ EOF
   ;

decl
    :constDecl
    | varDecl
;

constDecl
    :
    CONST bType constDef ( COMMA constDef )* SEMICOLON
    ;

bType
    :
    INT;

constDef
    :
    IDENT (L_BRACKT constExp R_BRACKT)* ASSIGN constInitVal
;

constInitVal
    :
    constExp
    | L_BRACE ( constInitVal( COMMA constInitVal ) * )? R_BRACE;

varDecl
    :
    bType varDef ( COMMA varDef)* SEMICOLON;

varDef
    :
    IDENT ( L_BRACKT constExp R_BRACKT )*
    | IDENT ( L_BRACKT constExp R_BRACKT )* ASSIGN initVal
    ;

initVal
    :
    exp | L_BRACE ( initVal ( COMMA initVal )* )? R_BRACE
    ;
funcDef
    : funcType IDENT L_PAREN (funcFParams)? R_PAREN block
    ;
funcType
    :
    VOID | INT
    ;
funcFParams
    :
    funcFParam ( COMMA funcFParam )*
    ;
funcFParam
    :
    bType IDENT (L_BRACKT R_BRACKT ( L_BRACKT exp R_BRACKT )*)?
    ;
block
    :
    L_BRACE ( blockItem )* R_BRACE
    ;

blockItem
    :decl
    | stmt
    ;
stmt
    : lVal ASSIGN exp SEMICOLON //  赋值语句
    | (exp)? SEMICOLON //  空语句，如单独的a;
    | block // {}这样所形成的代码块,stmt(语句)可以是一堆代码块
    | IF L_PAREN cond R_PAREN stmt ( ELSE stmt ) // if语句
    | WHILE L_PAREN cond R_PAREN stmt // while语句
    | BREAK SEMICOLON // break语句
    | CONTINUE SEMICOLON // continue语句
    | RETURN (exp)? SEMICOLON // return语句
    ;


// 可以代表一个单独的值的
exp
   : L_PAREN exp R_PAREN
   | lVal
   | number
   | IDENT L_PAREN funcRParams? R_PAREN
   | unaryOp exp
   | exp (MUL | DIV | MOD) exp
   | exp (PLUS | MINUS) exp
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
*/

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
    : funcFParam (COMMA funcFParams)*
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
    | RETURN (exp)? SEMICOLON
    ;

exp
   : L_PAREN exp R_PAREN
   | lVal
   | number
   | funcName L_PAREN funcRParams? R_PAREN
   | unaryOp exp
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