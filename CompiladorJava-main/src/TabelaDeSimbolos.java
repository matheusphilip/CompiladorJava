import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class TabelaDeSimbolos {

    private final Map<String, Simbolo> simbolos =
            new LinkedHashMap<>();

    // Adiciona uma variável à tabela.
    public boolean adicionar(Simbolo simbolo) {

        if (simbolos.containsKey(simbolo.getNome())) {
            return false;
        }

        simbolos.put(simbolo.getNome(), simbolo);
        return true;
    }

    // Procura uma variável pelo nome.
    public Simbolo buscar(String nome) {
        return simbolos.get(nome);
    }

    // Verifica se a variável existe.
    public boolean existe(String nome) {
        return simbolos.containsKey(nome);
    }

    // Retorna todos os símbolos cadastrados.
    public Collection<Simbolo> todos() {
        return simbolos.values();
    }

    // Imprime a tabela no terminal.
    public void imprimir() {

        System.out.println(
                "\n========== TABELA DE SÍMBOLOS =========="
        );

        System.out.printf(
                "%-18s | %-10s | %-8s%n",
                "IDENTIFICADOR",
                "TIPO",
                "LINHA"
        );

        System.out.println("----------------------------------------");

        for (Simbolo simbolo : simbolos.values()) {

            System.out.printf(
                    "%-18s | %-10s | %-8d%n",
                    simbolo.getNome(),
                    simbolo.getTipo().getNome(),
                    simbolo.getLinhaDeclaracao()
            );
        }

        System.out.println("========================================\n");
    }
}