public enum TokenType {
    // Keywords
    KW_CLASS, KW_PUBLIC, KW_PRIVATE, KW_STATIC, KW_NEW, KW_INT, KW_FLOAT, KW_DOUBLE, KW_STRING, KW_CHAR, KW_BOOLEAN, KW_VOID, KW_IF, KW_ELSE, KW_WHILE, KW_FOR, KW_RETURN, KW_TRUE, KW_FALSE,

    // Simbolos de 1 caractere
    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }
    LBRACKET, // [
    RBRACKET, // ]
    SEMICOLON, // ;
    COMMA, // ,
    DOT, // .
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
