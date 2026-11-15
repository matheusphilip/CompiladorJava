import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Analisa o código-fonte caractere por caractere e gera a lista de tokens.
// Também controla linha e coluna para melhorar as mensagens de erro.
public class Lexer {

    private final String source;

    // Lista que recebe todos os tokens encontrados.
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;       // Início do token atual.
    private int current = 0;     // Posição do caractere sendo analisado.
    private int line = 1;        // Linha atual.
    private int column = 1;      // Coluna atual.
    private int tokenColumn = 1; // Coluna onde o token atual começou.

    // Mapa com todas as palavras reservadas da linguagem.
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        // Tipos primitivos e String
        KEYWORDS.put("int", TokenType.KW_INT);
        KEYWORDS.put("float", TokenType.KW_FLOAT);
        KEYWORDS.put("double", TokenType.KW_DOUBLE);
        KEYWORDS.put("String", TokenType.KW_STRING);
        KEYWORDS.put("char", TokenType.KW_CHAR);
        KEYWORDS.put("boolean", TokenType.KW_BOOLEAN);
        KEYWORDS.put("void", TokenType.KW_VOID);
        KEYWORDS.put("byte", TokenType.KW_BYTE);
        KEYWORDS.put("short", TokenType.KW_SHORT);
        KEYWORDS.put("long", TokenType.KW_LONG);

        // Modificadores de acesso e comportamento
        KEYWORDS.put("public", TokenType.KW_PUBLIC);
        KEYWORDS.put("private", TokenType.KW_PRIVATE);
        KEYWORDS.put("protected", TokenType.KW_PROTECTED);
        KEYWORDS.put("static", TokenType.KW_STATIC);
        KEYWORDS.put("final", TokenType.KW_FINAL);
        KEYWORDS.put("abstract", TokenType.KW_ABSTRACT);
        KEYWORDS.put("synchronized", TokenType.KW_SYNCHRONIZED);
        KEYWORDS.put("volatile", TokenType.KW_VOLATILE);
        KEYWORDS.put("transient", TokenType.KW_TRANSIENT);
        KEYWORDS.put("native", TokenType.KW_NATIVE);
        KEYWORDS.put("strictfp", TokenType.KW_STRICTFP);

        // Estrutura de classes, interfaces e pacotes
        KEYWORDS.put("class", TokenType.KW_CLASS);
        KEYWORDS.put("interface", TokenType.KW_INTERFACE);
        KEYWORDS.put("enum", TokenType.KW_ENUM);
        KEYWORDS.put("extends", TokenType.KW_EXTENDS);
        KEYWORDS.put("implements", TokenType.KW_IMPLEMENTS);
        KEYWORDS.put("package", TokenType.KW_PACKAGE);
        KEYWORDS.put("import", TokenType.KW_IMPORT);
        KEYWORDS.put("new", TokenType.KW_NEW);
        KEYWORDS.put("this", TokenType.KW_THIS);
        KEYWORDS.put("super", TokenType.KW_SUPER);
        KEYWORDS.put("instanceof", TokenType.KW_INSTANCEOF);

        // Controle de fluxo
        KEYWORDS.put("return", TokenType.KW_RETURN);
        KEYWORDS.put("if", TokenType.KW_IF);
        KEYWORDS.put("else", TokenType.KW_ELSE);
        KEYWORDS.put("while", TokenType.KW_WHILE);
        KEYWORDS.put("for", TokenType.KW_FOR);
        KEYWORDS.put("switch", TokenType.KW_SWITCH);
        KEYWORDS.put("case", TokenType.KW_CASE);
        KEYWORDS.put("default", TokenType.KW_DEFAULT);
        KEYWORDS.put("do", TokenType.KW_DO);
        KEYWORDS.put("break", TokenType.KW_BREAK);
        KEYWORDS.put("continue", TokenType.KW_CONTINUE);

        // Tratamento de exceções e asserções
        KEYWORDS.put("try", TokenType.KW_TRY);
        KEYWORDS.put("catch", TokenType.KW_CATCH);
        KEYWORDS.put("finally", TokenType.KW_FINALLY);
        KEYWORDS.put("throw", TokenType.KW_THROW);
        KEYWORDS.put("throws", TokenType.KW_THROWS);
        KEYWORDS.put("assert", TokenType.KW_ASSERT);

        // Reservadas não utilizadas
        KEYWORDS.put("const", TokenType.KW_CONST);
        KEYWORDS.put("goto", TokenType.KW_GOTO);

        // Literais reservados
        KEYWORDS.put("true", TokenType.KW_TRUE);
        KEYWORDS.put("false", TokenType.KW_FALSE);
        KEYWORDS.put("null", TokenType.KW_NULL);
    }

    public Lexer(String source) {
        this.source = source;
    }

    // Percorre todo o código e adiciona o token EOF no final.
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            tokenColumn = column;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    // Verifica se o analisador chegou ao final do código.
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Consome o caractere atual e avança a coluna.
    private char advance() {
        char caractere = source.charAt(current++);
        column++;
        return caractere;
    }

    // Cria um token comum, usando o texto entre start e current.
    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line, tokenColumn));
    }

    // Cria um token com valor próprio, como strings e caracteres.
    private void addToken(TokenType type, String value) {
        tokens.add(new Token(type, value, line, tokenColumn));
    }

    // Olha o caractere atual sem consumi-lo.
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    // Olha o próximo caractere sem consumi-lo.
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    // Consome o próximo caractere apenas se ele for o esperado.
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }

        current++;
        column++;
        return true;
    }

    // Processa comentários de bloco delimitados por /* e */.
    private void blockComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance();
                advance();
                return;
            }

            if (peek() == '\n') {
                advance();
                line++;
                column = 1;
            } else {
                advance();
            }
        }

        System.err.printf(
                "Erro léxico na linha %d, coluna %d: comentário em bloco não fechado.%n",
                line,
                tokenColumn
        );
    }

    // Identifica o tipo de cada símbolo ou palavra do código.
    private void scanToken() {
        char c = advance();

        switch (c) {
            // Símbolos de um caractere
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case '[': addToken(TokenType.LBRACKET); break;
            case ']': addToken(TokenType.RBRACKET); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '@': addToken(TokenType.AT); break;
            case '?': addToken(TokenType.QUESTION); break;
            case '~': addToken(TokenType.BIT_NOT); break;

            case ':':
                addToken(match(':') ? TokenType.DOUBLE_COLON : TokenType.COLON);
                break;

            // Aritméticos e atribuições compostas
            case '+':
                if (match('+')) {
                    addToken(TokenType.PLUS_PLUS);
                } else if (match('=')) {
                    addToken(TokenType.PLUS_ASSIGN);
                } else {
                    addToken(TokenType.PLUS);
                }
                break;

            case '-':
                if (match('-')) {
                    addToken(TokenType.MINUS_MINUS);
                } else if (match('=')) {
                    addToken(TokenType.MINUS_ASSIGN);
                } else if (match('>')) {
                    addToken(TokenType.ARROW);
                } else {
                    addToken(TokenType.MINUS);
                }
                break;

            case '*':
                addToken(match('=') ? TokenType.STAR_ASSIGN : TokenType.STAR);
                break;

            case '%':
                addToken(match('=') ? TokenType.PERCENT_ASSIGN : TokenType.PERCENT);
                break;

            // Relacionais e atribuição
            case '=':
                addToken(match('=') ? TokenType.EQ : TokenType.ASSIGN);
                break;

            case '!':
                addToken(match('=') ? TokenType.EXCLAMATION_EQ : TokenType.EXCLAMATION);
                break;

            case '<':
                if (match('<')) {
                    addToken(match('=') ? TokenType.LSHIFT_ASSIGN : TokenType.LSHIFT);
                } else if (match('=')) {
                    addToken(TokenType.LESS_EQ);
                } else {
                    addToken(TokenType.LESS);
                }
                break;

            case '>':
                if (match('>')) {
                    if (match('>')) {
                        addToken(match('=') ? TokenType.URSHIFT_ASSIGN : TokenType.URSHIFT);
                    } else if (match('=')) {
                        addToken(TokenType.RSHIFT_ASSIGN);
                    } else {
                        addToken(TokenType.RSHIFT);
                    }
                } else if (match('=')) {
                    addToken(TokenType.GREATER_EQ);
                } else {
                    addToken(TokenType.GREATER);
                }
                break;

            // Lógicos e Bitwise
            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else if (match('=')) {
                    addToken(TokenType.BIT_AND_ASSIGN);
                } else {
                    addToken(TokenType.BIT_AND);
                }
                break;

            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else if (match('=')) {
                    addToken(TokenType.BIT_OR_ASSIGN);
                } else {
                    addToken(TokenType.BIT_OR);
                }
                break;

            case '^':
                addToken(match('=') ? TokenType.BIT_XOR_ASSIGN : TokenType.BIT_XOR);
                break;

            // Divisão, atribuição de divisão e comentários
            case '/':
                if (match('/')) {
                    // Comentário de linha: ignora até a quebra de linha.
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }

                    if (peek() == '\n') {
                        advance();
                        line++;
                        column = 1;
                    }
                } else if (match('*')) {
                    blockComment();
                } else if (match('=')) {
                    addToken(TokenType.SLASH_ASSIGN);
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case '"':
                string();
                break;

            case '\'':
                charLiteral();
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                column = 1;
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.err.printf(
                            "Erro léxico na linha %d, coluna %d: caractere inválido '%c'.%n",
                            line,
                            tokenColumn,
                            c
                    );
                }
                break;
        }
    }

    // Verifica se o caractere é um dígito.
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Verifica se o caractere pode iniciar um identificador.
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_'
                || c == '$';
    }

    // Reconhece números inteiros e decimais.
    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) {
                advance();
            }

            addToken(TokenType.FLOAT_LITERAL);
        } else {
            addToken(TokenType.INT_LITERAL);
        }
    }

    // Reconhece strings delimitadas por aspas duplas (com suporte a escape \).
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\\' && peekNext() != '\0') {
                advance(); // Consome a barra '\'
                if (peek() == '\n') {
                    advance();
                    line++;
                    column = 1;
                } else {
                    advance(); // Consome o caractere escapado
                }
                continue;
            }

            if (peek() == '\n') {
                advance();
                line++;
                column = 1;
            } else {
                advance();
            }
        }

        if (isAtEnd()) {
            System.err.printf(
                    "Erro léxico na linha %d, coluna %d: string não fechada.%n",
                    line,
                    tokenColumn
            );
            return;
        }

        advance(); // Consome a aspa de fechamento

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING_LITERAL, value);
    }

    // Reconhece caracteres delimitados por aspas simples (com suporte a escape \).
    private void charLiteral() {
        if (isAtEnd() || peek() == '\n' || peek() == '\r') {
            System.err.printf(
                    "Erro léxico na linha %d, coluna %d: literal de caractere não fechado.%n",
                    line,
                    tokenColumn
            );
            return;
        }

        if (peek() == '\'') {
            advance();
            System.err.printf(
                    "Erro léxico na linha %d, coluna %d: literal de caractere vazio.%n",
                    line,
                    tokenColumn
            );
            return;
        }

        if (peek() == '\\') {
            advance(); // Consome '\'
            if (!isAtEnd() && peek() != '\n' && peek() != '\r') {
                advance(); // Consome o caractere escapado (ex: 'n', 't', '\'', '\\')
            }
        } else {
            advance(); // Consome o caractere normal
        }

        if (peek() == '\'') {
            advance(); // Consome a aspa de fechamento

            String value = source.substring(start + 1, current - 1);
            addToken(TokenType.CHAR_LITERAL, value);
        } else {
            System.err.printf(
                    "Erro léxico na linha %d, coluna %d: literal de caractere não fechado.%n",
                    line,
                    tokenColumn
            );
        }
    }

    // Reconhece identificadores e palavras reservadas.
    private void identifier() {
        while (isAlpha(peek()) || isDigit(peek())) {
            advance();
        }

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }
}