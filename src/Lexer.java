import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0; // Início do token atual
    private int current = 0; // Caractere sendo analisado
    private int line = 1; // Linha atual

    public Lexer(String source) {
        this.source = source;
    }

    // Analisa o código inteiro
    public List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        // Adiciona um token indicando o fim do arquivo (EOF)
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    // Checa se 'current' chegou ao final do texto
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // Passa pro próximo caractere
    private char advance() {
        return source.charAt(current++);
    }

    // Cria um token usando o texto entre 'start' e 'current'
    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line));
    }

    // Olha o caractere atual
    private char peek() {
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // Checa se o próximo caractere é o esperado (útil para diferenciar '=' de '==')
    private boolean match(char expected) {
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private void scanToken() {
        char c = advance();

        switch(c) {
            // Símbolos de 1 caractere
            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;

            // Operador que pode ter 1 ou 2 caracteres
            case '=':
                addToken(match('=') ? TokenType.EQ : TokenType.ASSIGN);
                break;

            // Espaços em branco são ignorados
            case ' ':
            case '\r':
            case '\t':
                break;

            // Quebra de linha
            case '\n':
                line++;
                break;

            default:
                if(isDigit(c)) {
                    number();
                } else if(isAlpha(c)) {
                    identifier();
                } else {
                    System.err.printf("Erro Léxico na linha %d: Caractere inválido '%c'%n", line, c);
                }
                break;
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private void number() {
        while(isDigit(peek())) {
            advance();
        }
        addToken(TokenType.INT_LITERAL);
    }

    private void identifier() {
        while(isAlpha(peek()) || isDigit(peek())) {
            advance();
        }

        String text = source.substring(start, current);

        TokenType type;
        switch(text) {
            case "class": type = TokenType.KW_CLASS; break;
            case "int": type = TokenType.KW_INT; break;
            case "boolean": type = TokenType.KW_BOOLEAN; break;
            case "if": type = TokenType.KW_IF; break;
            case "else": type = TokenType.KW_ELSE; break;
            case "while": type = TokenType.KW_WHILE; break;
            case "return": type = TokenType.KW_RETURN; break;
            default: type = TokenType.IDENTIFIER; break; // Se não for keyword, é um nome de variável/métodos
        }

        addToken(type);
    }
}
