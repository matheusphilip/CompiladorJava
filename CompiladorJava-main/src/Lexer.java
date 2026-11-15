import java.util.ArrayList;
import java.util.List;

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
                line++;
                column = 1;
            }

            advance();
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
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case '[': addToken(TokenType.LBRACKET); break;
            case ']': addToken(TokenType.RBRACKET); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '%': addToken(TokenType.PERCENT); break;

            case '=':
                addToken(match('=') ? TokenType.EQ : TokenType.ASSIGN);
                break;

            case '!':
                addToken(match('=') ? TokenType.EXCLAMATION_EQ : TokenType.EXCLAMATION);
                break;

            case '<':
                addToken(match('=') ? TokenType.LESS_EQ : TokenType.LESS);
                break;

            case '>':
                addToken(match('=') ? TokenType.GREATER_EQ : TokenType.GREATER);
                break;

            case '&':
                if (match('&')) {
                    addToken(TokenType.AND);
                } else {
                    System.err.printf(
                            "Erro léxico na linha %d, coluna %d: '&' isolado não permitido.%n",
                            line,
                            tokenColumn
                    );
                }
                break;

            case '|':
                if (match('|')) {
                    addToken(TokenType.OR);
                } else {
                    System.err.printf(
                            "Erro léxico na linha %d, coluna %d: '|' isolado não permitido.%n",
                            line,
                            tokenColumn
                    );
                }
                break;

            case '/':
                if (match('/')) {
                    // Comentário de linha: ignora até a quebra de linha.
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();
                    }

                    if (peek() == '\n') {
                        line++;
                        column = 1;
                        advance();
                    }
                } else if (match('*')) {
                    blockComment();
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
                || c == '_';
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

    // Reconhece strings delimitadas por aspas duplas.
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                column = 1;
            }

            advance();
        }

        if (isAtEnd()) {
            System.err.printf(
                    "Erro léxico na linha %d, coluna %d: string não fechada.%n",
                    line,
                    tokenColumn
            );
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING_LITERAL, value);
    }

    // Reconhece caracteres delimitados por aspas simples.
    private void charLiteral() {
        advance();

        if (peek() == '\'') {
            advance();

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
        TokenType type;

        switch (text) {
            case "class": type = TokenType.KW_CLASS; break;
            case "public": type = TokenType.KW_PUBLIC; break;
            case "private": type = TokenType.KW_PRIVATE; break;
            case "static": type = TokenType.KW_STATIC; break;
            case "new": type = TokenType.KW_NEW; break;
            case "int": type = TokenType.KW_INT; break;
            case "float": type = TokenType.KW_FLOAT; break;
            case "double": type = TokenType.KW_DOUBLE; break;
            case "String": type = TokenType.KW_STRING; break;
            case "char": type = TokenType.KW_CHAR; break;
            case "boolean": type = TokenType.KW_BOOLEAN; break;
            case "void": type = TokenType.KW_VOID; break;
            case "if": type = TokenType.KW_IF; break;
            case "else": type = TokenType.KW_ELSE; break;
            case "while": type = TokenType.KW_WHILE; break;
            case "for": type = TokenType.KW_FOR; break;
            case "return": type = TokenType.KW_RETURN; break;
            case "true": type = TokenType.KW_TRUE; break;
            case "false": type = TokenType.KW_FALSE; break;
            default: type = TokenType.IDENTIFIER; break;
        }

        addToken(type);
    }
}