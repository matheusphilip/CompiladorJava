// Representa uma variável registrada na tabela de símbolos.
public class Simbolo {

    // Nome da variável declarada.
    private final String nome;

    // Tipo da variável, como int, String ou boolean.
    private final Tipo tipo;

    // Categoria do símbolo. Neste projeto, normalmente é "variável".
    private final String categoria;

    // Linha do código em que a variável foi declarada.
    private final int linhaDeclaracao;

    public Simbolo(
            String nome,
            Tipo tipo,
            String categoria,
            int linhaDeclaracao
    ) {
        this.nome = nome;
        this.tipo = tipo;
        this.categoria = categoria;
        this.linhaDeclaracao = linhaDeclaracao;
    }

    // Retorna o nome da variável.
    public String getNome() {
        return nome;
    }

    // Retorna o tipo da variável.
    public Tipo getTipo() {
        return tipo;
    }

    // Retorna a categoria do símbolo.
    public String getCategoria() {
        return categoria;
    }

    // Retorna a linha em que a variável foi declarada.
    public int getLinhaDeclaracao() {
        return linhaDeclaracao;
    }
}