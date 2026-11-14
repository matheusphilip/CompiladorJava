import java.util.LinkedHashMap;
import java.util.Map;

public class TabelaDeSimbolos {

    private static class Entrada {
        String tipo;
        int linha;
        int ordemInsercao;

        Entrada(String tipo, int linha, int ordemInsercao) {
            this.tipo = tipo;
            this.linha = linha;
            this.ordemInsercao = ordemInsercao;
        }
    }

    // LinkedHashMap mantém a ordem de inserção, conforme exigido pelo barema
    private Map<String, Entrada> tabela = new LinkedHashMap<>();
    private int contadorOrdem = 1;

    public void adicionar(String nome, String tipo, int linha) {
        if (tabela.containsKey(nome)) {
            System.err.println("[ERRO SEMÂNTICO] Variável '" + nome
                    + "' já foi declarada anteriormente na linha " + linha);
            System.exit(1);
        }
        tabela.put(nome, new Entrada(tipo, linha, contadorOrdem++));
    }

    public boolean existe(String nome) {
        return tabela.containsKey(nome);
    }

    public String getTipo(String nome) {
        Entrada entrada = tabela.get(nome);
        return entrada != null ? entrada.tipo : null;
    }

    public void imprimir() {
        System.out.println("\n========================================");
        System.out.println("          TABELA DE SÍMBOLOS            ");
        System.out.println("========================================");
        System.out.printf("%-6s | %-15s | %-10s | %-6s%n",
                "ORDEM", "IDENTIFICADOR", "TIPO", "LINHA");
        System.out.println("----------------------------------------");
        for (Map.Entry<String, Entrada> entry : tabela.entrySet()) {
            System.out.printf("%-6d | %-15s | %-10s | %-6d%n",
                    entry.getValue().ordemInsercao,
                    entry.getKey(),
                    entry.getValue().tipo,
                    entry.getValue().linha);
        }
        System.out.println("========================================\n");
    }
}