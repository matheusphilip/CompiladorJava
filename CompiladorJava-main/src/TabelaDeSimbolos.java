import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// A tabela nasce durante a análise léxica e é enriquecida pelo Parser.
// Chave: lexema do identificador. Valor: ocorrências e declarações desse nome.
public class TabelaDeSimbolos {

    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    private Simbolo obterOuCriar(String nome) {
        return simbolos.computeIfAbsent(nome, Simbolo::new);
    }

    // Fase léxica: registra cada ocorrência de um token IDENTIFIER.
    public void registrarOcorrencia(Token token) {
        if (token == null || token.type != TokenType.IDENTIFIER) {
            return;
        }

        obterOuCriar(token.lexeme).registrarOcorrencia(token);
    }

    // Fase sintática: informa que uma ocorrência representa uma declaração.
    public void registrarDeclaracao(Token token, Tipo tipo, String categoria) {
        if (token == null || token.type != TokenType.IDENTIFIER) {
            throw new IllegalArgumentException(
                    "O token informado para declaração deve ser IDENTIFIER."
            );
        }

        obterOuCriar(token.lexeme)
                .registrarDeclaracao(token, tipo, categoria);
    }

    // Recupera a entrada lexical de um identificador.
    // Atenção: a existência da entrada não significa que ele foi declarado.
    public Simbolo buscar(String nome) {
        return simbolos.get(nome);
    }

    // Indica se o lexema apareceu no código, declarado ou não.
    public boolean existe(String nome) {
        return simbolos.containsKey(nome);
    }

    public Collection<Simbolo> todos() {
        return Collections.unmodifiableCollection(simbolos.values());
    }

    public int tamanho() {
        return simbolos.size();
    }

    // Exibe identificadores, tipo conhecido, linha de declaração e ocorrências.
    public void imprimir() {
        System.out.println("\n================ TABELA DE SÍMBOLOS ================");
        System.out.printf(
                "%-18s | %-10s | %-12s | %-8s | %-10s%n",
                "IDENTIFICADOR", "TIPO", "CATEGORIA", "LINHA", "OCORRÊNCIAS"
        );
        System.out.println("--------------------------------------------------------------------------");

        for (Simbolo simbolo : simbolos.values()) {
            String tipo = simbolo.foiDeclarado()
                    ? simbolo.getTipo().getNome()
                    : "-";
            String categoria = simbolo.getCategoria();
            String linha = simbolo.foiDeclarado()
                    ? String.valueOf(simbolo.getLinhaDeclaracao())
                    : "-";

            System.out.printf(
                    "%-18s | %-10s | %-12s | %-8s | %-10d%n",
                    simbolo.getNome(), tipo, categoria, linha,
                    simbolo.getQuantidadeOcorrencias()
            );
        }

        if (simbolos.isEmpty()) {
            System.out.println("Nenhum identificador encontrado.");
        }

        System.out.println("====================================================\n");
    }
}
