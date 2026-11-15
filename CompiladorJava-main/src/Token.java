// Representa uma menor unidade significativa do código-fonte.
// Exemplos: palavra reservada, identificador, número, operador ou símbolo.
public class Token {

    // Tipo do token, definido no enum TokenType.
    public final TokenType type;

    // Texto original encontrado no código-fonte.
    public final String lexeme;

    // Linha em que o token aparece.
    public final int line;

    // Coluna em que o token começa.
    public final int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    // Formata o token para exibição no terminal.
    @Override
    public String toString() {
        return String.format(
                "Token[%-15s | Lexema: '%s' | Linha: %d | Coluna: %d]",
                type,
                lexeme,
                line,
                column
        );
    }
}