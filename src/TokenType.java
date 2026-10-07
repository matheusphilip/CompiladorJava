public enum TokenType {
    // Keywords
    KW_CLASS, KW_INT, KW_BOOLEAN, KW_IF, KW_ELSE, KW_WHILE, KW_RETURN,

    // Simbolos de 1 caractere
    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }
    SEMICOLON, // ;
    PLUS, // +
    MINUS, // -

    // Operadores de 1 ou 2 caracteres
    ASSIGN, // =
    EQ, // ==

    // Tokens com valores dinâmicos
    IDENTIFIER, // Nomes de variáveis/métodos
    INT_LITERAL, // Números

    // Fim do arquivo
    EOF
}
