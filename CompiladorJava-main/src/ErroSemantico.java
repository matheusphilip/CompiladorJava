// Exceção usada para representar erros encontrados na análise semântica.
public class ErroSemantico extends RuntimeException {

    public ErroSemantico(String mensagem) {
        super(mensagem);
    }
}