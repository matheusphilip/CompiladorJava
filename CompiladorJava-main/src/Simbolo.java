public class Simbolo {

    private final String nome;
    private final Tipo tipo;
    private final int linhaDeclaracao;

    public Simbolo(
            String nome,
            Tipo tipo,
            int linhaDeclaracao
    ) {
        this.nome = nome;
        this.tipo = tipo;
        this.linhaDeclaracao = linhaDeclaracao;
    }

    public String getNome() {
        return nome;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public int getLinhaDeclaracao() {
        return linhaDeclaracao;
    }
}