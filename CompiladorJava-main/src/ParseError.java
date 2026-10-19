// Representa erros encontrados durante a análise sintática.
public class ParseError extends RuntimeException {

    // Recebe a mensagem de erro e a envia para a classe pai.
    public ParseError(String message) {
        super(message);
    }
}