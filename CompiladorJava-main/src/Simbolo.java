import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Uma entrada representa um lexema identificador único.
// O Lexer adiciona ocorrências; o Parser registra quais ocorrências são declarações.
public class Simbolo {

    // Informações de uma ocorrência em que o Parser reconheceu uma declaração.
    public static class Declaracao {
        private final Tipo tipo;
        private final String categoria;
        private final Token token;

        public Declaracao(Tipo tipo, String categoria, Token token) {
            this.tipo = tipo;
            this.categoria = categoria;
            this.token = token;
        }

        public Tipo getTipo() {
            return tipo;
        }

        public String getCategoria() {
            return categoria;
        }

        public Token getToken() {
            return token;
        }
    }

    private final String nome;
    private final List<Token> ocorrencias = new ArrayList<>();
    private final List<Declaracao> declaracoes = new ArrayList<>();

    public Simbolo(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    // Chamado pelo Lexer para cada ocorrência do identificador.
    public void registrarOcorrencia(Token token) {
        ocorrencias.add(token);
    }

    // Chamado pelo Parser quando confirma uma declaração na gramática.
    public void registrarDeclaracao(Token token, Tipo tipo, String categoria) {
        declaracoes.add(new Declaracao(tipo, categoria, token));
    }

    public List<Token> getOcorrencias() {
        return Collections.unmodifiableList(ocorrencias);
    }

    public List<Declaracao> getDeclaracoes() {
        return Collections.unmodifiableList(declaracoes);
    }

    public int getQuantidadeOcorrencias() {
        return ocorrencias.size();
    }

    public int getQuantidadeDeclaracoes() {
        return declaracoes.size();
    }

    public boolean foiDeclarado() {
        return !declaracoes.isEmpty();
    }

    // Mantém compatibilidade com a forma anterior de consultar o tipo:
    // retorna o tipo da primeira declaração, ou null se nunca foi declarado.
    public Tipo getTipo() {
        return declaracoes.isEmpty() ? null : declaracoes.get(0).getTipo();
    }

    public String getCategoria() {
        return declaracoes.isEmpty()
                ? "identificador"
                : declaracoes.get(0).getCategoria();
    }

    public int getLinhaDeclaracao() {
        return declaracoes.isEmpty()
                ? -1
                : declaracoes.get(0).getToken().line;
    }

    public Declaracao getPrimeiraDeclaracao() {
        return declaracoes.isEmpty() ? null : declaracoes.get(0);
    }
}
