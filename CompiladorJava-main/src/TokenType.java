// Define todos os tipos de tokens reconhecidos pelo Lexer.
public enum TokenType {

    // Palavras reservadas da linguagem.
    KW_CLASS, KW_PUBLIC, KW_PRIVATE, KW_STATIC, KW_NEW,
    KW_INT, KW_FLOAT, KW_DOUBLE, KW_STRING, KW_CHAR, KW_BOOLEAN,
    KW_VOID, KW_IF, KW_ELSE, KW_WHILE, KW_FOR, KW_RETURN,
    KW_TRUE, KW_FALSE,

    // Símbolos de um caractere.
    LPAREN,        // (
    RPAREN,        // )
    LBRACE,        // {
    RBRACE,        // }
    LBRACKET,      // [
    RBRACKET,      // ]
    SEMICOLON,     // ;
    COMMA,         // ,
    DOT,           // .
    PLUS,          // +
    MINUS,         // -
    STAR,          // *
    SLASH,         // /
    PERCENT,       // %

    // Operadores de um ou dois caracteres.
    ASSIGN,          // =
    EQ,              // ==
    EXCLAMATION,     // !
    EXCLAMATION_EQ,  // !=
    LESS,            // <
    LESS_EQ,         // <=
    GREATER,         // >
    GREATER_EQ,      // >=
    AND,             // &&
    OR,              // ||

    // Tokens com valores dinâmicos.
    IDENTIFIER,      // Nomes de variáveis e métodos.
    INT_LITERAL,     // Números inteiros.
    FLOAT_LITERAL,   // Números decimais.
    STRING_LITERAL,  // Textos entre aspas duplas.
    CHAR_LITERAL,    // Caracteres entre aspas simples.

    // Indica o fim do arquivo.
    EOF
}