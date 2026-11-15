# Compilador Java

Analisador léxico, sintático simplificado e semântico para declarações
e atribuições aritméticas.

## Como executar

O programa usa por padrão o arquivo:

```text
exemplos/entrada.java
```

Compile:

```bash
javac -d out src/*.java
```

Execute:

```bash
java -cp out Main
```

Para analisar outro arquivo:

```bash
java -cp out Main exemplos/entrada.java
```

## Saídas geradas

- Lista de tokens com tipo, lexema, linha e coluna.
- Tabela de símbolos com identificador, tipo, categoria e linha.
- Árvore Sintática Abstrata (AST) em texto indentado.
- AST exportada em `ast.json`.
- AST exportada em `ast.dot`.
- Erros léxicos, sintáticos e semânticos com linha e coluna.

## Gramática BNF

```bnf
<programa> ::= <comando>*

<comando> ::= <declaracao> | <atribuicao>

<declaracao> ::= <tipo> IDENTIFICADOR [ "=" <expressao> ] ";"

<atribuicao> ::= IDENTIFICADOR "=" <expressao> ";"

<tipo> ::= "int" | "float" | "double" | "String" | "char" | "boolean"

<expressao> ::= <termo> ( ("+" | "-") <termo> )*

<termo> ::= <unaria> ( ("*" | "/" | "%") <unaria> )*

<unaria> ::= ("+" | "-") <unaria> | <primaria>

<primaria> ::= NUMERO | STRING | CHAR | "true" | "false"
             | IDENTIFICADOR | "(" <expressao> ")"
```

## Exemplo de entrada

```java
int idade = 20;
int total = idade + 5;
```

## Exemplos de erro

```java
int resultado = "texto";
```

```java
total = 10;
```

O primeiro gera erro de incompatibilidade de tipos.
O segundo gera erro de variável não declarada.