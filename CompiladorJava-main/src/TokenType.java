// Define todos os tipos de tokens que o Lexer atual pode produzir.
// Nem todos esses tokens fazem parte da gramática aceita pelo Parser ainda.
public enum TokenType {

    // Palavras reservadas: tipos e modificadores.
    KW_CLASS, KW_PUBLIC, KW_PRIVATE, KW_PROTECTED, KW_STATIC, KW_FINAL,
    KW_ABSTRACT, KW_SYNCHRONIZED, KW_VOLATILE, KW_TRANSIENT,
    KW_NATIVE, KW_STRICTFP, KW_NEW,
    KW_INT, KW_FLOAT, KW_DOUBLE, KW_STRING, KW_CHAR, KW_BOOLEAN,
    KW_BYTE, KW_SHORT, KW_LONG, KW_VOID,

    // Estrutura de classes e pacotes.
    KW_INTERFACE, KW_ENUM, KW_EXTENDS, KW_IMPLEMENTS,
    KW_PACKAGE, KW_IMPORT, KW_THIS, KW_SUPER, KW_INSTANCEOF,

    // Controle de fluxo e exceções.
    KW_IF, KW_ELSE, KW_WHILE, KW_FOR, KW_SWITCH, KW_CASE,
    KW_DEFAULT, KW_DO, KW_BREAK, KW_CONTINUE, KW_RETURN,
    KW_TRY, KW_CATCH, KW_FINALLY, KW_THROW, KW_THROWS, KW_ASSERT,

    // Palavras reservadas não utilizadas (reservadas em Java).
    KW_CONST, KW_GOTO,

    // Literais reservados.
    KW_TRUE, KW_FALSE, KW_NULL,

    // Delimitadores e símbolos.
    LPAREN,          // (
    RPAREN,          // )
    LBRACE,          // {
    RBRACE,          // }
    LBRACKET,        // [
    RBRACKET,        // ]
    SEMICOLON,       // ;
    COMMA,           // ,
    DOT,             // .
    COLON,           // :
    DOUBLE_COLON,    // ::
    AT,              // @
    QUESTION,        // ?

    // Operadores aritméticos e incrementais.
    PLUS,            // +
    PLUS_PLUS,       // ++
    PLUS_ASSIGN,     // +=
    MINUS,           // -
    MINUS_MINUS,     // --
    MINUS_ASSIGN,    // -=
    ARROW,           // ->
    STAR,            // *
    STAR_ASSIGN,     // *=
    SLASH,           // /
    SLASH_ASSIGN,    // /=
    PERCENT,         // %
    PERCENT_ASSIGN,  // %=

    // Operadores de atribuição e comparação.
    ASSIGN,          // =
    EQ,              // ==
    EXCLAMATION,     // !
    EXCLAMATION_EQ,  // !=
    LESS,            // <
    LESS_EQ,         // <=
    GREATER,         // >
    GREATER_EQ,      // >=

    // Operadores lógicos e bit a bit.
    AND,             // &&
    OR,              // ||
    BIT_NOT,         // ~
    BIT_AND,         // &
    BIT_AND_ASSIGN,  // &=
    BIT_OR,          // |
    BIT_OR_ASSIGN,   // |=
    BIT_XOR,         // ^
    BIT_XOR_ASSIGN,  // ^=

    // Deslocamento de bits.
    LSHIFT,          // <<
    LSHIFT_ASSIGN,   // <<=
    RSHIFT,          // >>
    RSHIFT_ASSIGN,   // >>=
    URSHIFT,         // >>>
    URSHIFT_ASSIGN,  // >>>=

    // Tokens com valores dinâmicos.
    IDENTIFIER,
    INT_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    CHAR_LITERAL,

    // Fim do arquivo.
    EOF
}
