public enum TokenType {
    // Keywords
    KW_CLASS, KW_INT, KW_FLOAT, KW_STRING, KW_BOOLEAN, KW_VOID, KW_IF, KW_ELSE, KW_WHILE, KW_RETURN, KW_TRUE, KW_FALSE,

    // Simbolos de 1 caractere
    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }
    SEMICOLON, // ;
    COMMA, // ,
    PLUS, // +
    MINUS, // -
    STAR, // *
    SLASH, // /
    PERCENT, // %

    // Operadores de 1 ou 2 caracteres
    ASSIGN, EQ, // = e ==
    EXCLAMATION, EXCLAMATION_EQ, // ! e !=
    LESS, LESS_EQ, // < e <=
    GREATER, GREATER_EQ, // > e >=
    AND, OR, // && e ||

    // Tokens com valores dinâmicos
    IDENTIFIER, // Nomes de variáveis/métodos
    INT_LITERAL, // Números
    FLOAT_LITERAL,
    STRING_LITERAL,
    CHAR_LITERAL,

    // Fim do arquivo
    EOF
}
