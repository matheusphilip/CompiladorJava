import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// Armazena as variáveis declaradas pelo programa.
// O LinkedHashMap preserva a ordem em que os símbolos foram inseridos.
public class TabelaDeSimbolos {

    // A chave é o nome da variável e o valor é o objeto Simbolo.
    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    // Adiciona uma variável à tabela.
    // Retorna false caso a variável já tenha sido declarada.
    public boolean adicionar(Simbolo simbolo) {
        if (simbolos.containsKey(simbolo.getNome())) {
            return false;
        }

        simbolos.put(simbolo.getNome(), simbolo);
        return true;
    }

    // Procura uma variável pelo nome.
    // Retorna null caso a variável não exista.
    public Simbolo buscar(String nome) {
        return simbolos.get(nome);
    }

    // Verifica se uma variável já foi declarada.
    public boolean existe(String nome) {
        return simbolos.containsKey(nome);
    }

    // Retorna todos os símbolos cadastrados, na ordem de inserção.
    public Collection<Simbolo> todos() {
        return simbolos.values();
    }

    // Imprime a tabela de símbolos formatada no terminal.
    public void imprimir() {
        System.out.println("\n========== TABELA DE SÍMBOLOS ==========");

        // Cabeçalho da tabela.
        System.out.printf(
                "%-18s | %-10s | %-12s | %-8s%n",
                "IDENTIFICADOR",
                "TIPO",
                "CATEGORIA",
                "LINHA"
        );

        System.out.println("------------------------------------------------------");

        // Imprime cada variável declarada.
        for (Simbolo simbolo : simbolos.values()) {
            System.out.printf(
                    "%-18s | %-10s | %-12s | %-8d%n",
                    simbolo.getNome(),
                    simbolo.getTipo().getNome(),
                    simbolo.getCategoria(),
                    simbolo.getLinhaDeclaracao()
            );
        }

        // Mensagem exibida quando nenhuma variável foi declarada.
        if (simbolos.isEmpty()) {
            System.out.println("Nenhum símbolo declarado.");
        }

        System.out.println("========================================\n");
    }
}