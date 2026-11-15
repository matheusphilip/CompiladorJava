// Exceção usada para representar erros encontrados na análise sintática.
public class ParseError extends RuntimeException {

    public ParseError(String mensagem) {
        super(mensagem);
    }
}