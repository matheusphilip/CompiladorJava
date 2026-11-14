import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Monta a tabela de símbolos a partir da árvore sintática e faz as
 * verificações semânticas básicas (redeclaração, uso de variável não
 * declarada e uso antes da inicialização).
 */
public class TabelaSimbolos {

    public static final class Simbolo {
        public final String nome;
        public final String tipo;
        public final int linhaDeclaracao;
        public boolean inicializado;
        public final List<Integer> linhasUso = new ArrayList<>();

        Simbolo(String nome, String tipo, int linhaDeclaracao) {
            this.nome = nome;
            this.tipo = tipo;
            this.linhaDeclaracao = linhaDeclaracao;
        }
    }

    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    public void analisar(Ast.Programa programa) {
        for (Ast.Comando comando : programa.comandos) {
            processar(comando);
        }
    }

    private void processar(Ast.Comando comando) {
        if (comando instanceof Ast.Declaracao) {
            declarar((Ast.Declaracao) comando);
        } else if (comando instanceof Ast.Atribuicao) {
            atribuir((Ast.Atribuicao) comando);
        } else if (comando instanceof Ast.Metodo) {
            metodo((Ast.Metodo) comando);
        } else {
            throw new SemanticError(
                    "[ERRO SEMÂNTICO] Comando desconhecido na tabela de símbolos."
            );
        }
    }

    public List<Simbolo> getSimbolos() {
        return new ArrayList<>(simbolos.values());
    }

    public Simbolo buscar(String nome) {
        return simbolos.get(nome);
    }

    private void declarar(Ast.Declaracao declaracao) {
        String nome = declaracao.nome.lexeme;
        Simbolo existente = simbolos.get(nome);

        if (existente != null) {
            throw new SemanticError(
                    "[ERRO SEMÂNTICO] Linha " + declaracao.nome.line
                            + ": variável '" + nome + "' já declarada na linha "
                            + existente.linhaDeclaracao + "."
            );
        }

        Simbolo simbolo = new Simbolo(
                nome,
                declaracao.tipo.lexeme,
                declaracao.nome.line
        );

        simbolos.put(nome, simbolo);

        if (declaracao.inicializador != null) {
            resolver(declaracao.inicializador);
            simbolo.inicializado = true;
        }
    }

    private void metodo(Ast.Metodo metodo) {
        String nome = metodo.nome.lexeme;
        Simbolo existente = simbolos.get(nome);

        if (existente != null) {
            throw new SemanticError(
                    "[ERRO SEMÂNTICO] Linha " + metodo.nome.line
                            + ": símbolo '" + nome + "' já declarado na linha "
                            + existente.linhaDeclaracao + "."
            );
        }

        Simbolo simbolo = new Simbolo(
                nome,
                metodo.tipo.lexeme,
                metodo.nome.line
        );
        simbolo.inicializado = true;
        simbolos.put(nome, simbolo);

        // Escopo plano: os comandos do corpo do método são analisados na
        // mesma tabela (suficiente para a linguagem atual).
        for (Ast.Comando comando : metodo.corpo) {
            processar(comando);
        }
    }

    private void atribuir(Ast.Atribuicao atribuicao) {
        String nome = atribuicao.nome.lexeme;

        resolver(atribuicao.valor);

        Simbolo simbolo = simbolos.get(nome);

        if (simbolo == null) {
            throw new SemanticError(
                    "[ERRO SEMÂNTICO] Linha " + atribuicao.nome.line
                            + ": variável '" + nome + "' não declarada."
            );
        }

        simbolo.inicializado = true;
        simbolo.linhasUso.add(atribuicao.nome.line);
    }

    private void resolver(Ast.Expressao expressao) {
        if (expressao instanceof Ast.Numero) {
            return;
        }

        if (expressao instanceof Ast.Identificador) {
            Token nome = ((Ast.Identificador) expressao).nome;
            Simbolo simbolo = simbolos.get(nome.lexeme);

            if (simbolo == null) {
                throw new SemanticError(
                        "[ERRO SEMÂNTICO] Linha " + nome.line
                                + ": variável '" + nome.lexeme
                                + "' não declarada."
                );
            }

            if (!simbolo.inicializado) {
                throw new SemanticError(
                        "[ERRO SEMÂNTICO] Linha " + nome.line
                                + ": variável '" + nome.lexeme
                                + "' usada antes de ser inicializada."
                );
            }

            simbolo.linhasUso.add(nome.line);
            return;
        }

        if (expressao instanceof Ast.Binaria) {
            Ast.Binaria binaria = (Ast.Binaria) expressao;

            resolver(binaria.esquerda);
            resolver(binaria.direita);
            return;
        }

        throw new SemanticError(
                "[ERRO SEMÂNTICO] Expressão desconhecida na tabela de símbolos."
        );
    }

    public void imprimir() {
        System.out.println("--- TABELA DE SÍMBOLOS ---");

        if (simbolos.isEmpty()) {
            System.out.println("Nenhum símbolo declarado.");
            return;
        }

        System.out.printf(
                "%-12s %-8s %-12s %-10s %-8s %s%n",
                "NOME", "TIPO", "LINHA", "INICIALIZ.", "USOS", "LINHAS DE USO"
        );

        for (Simbolo simbolo : simbolos.values()) {
            System.out.printf(
                    "%-12s %-8s %-12d %-10s %-8d %s%n",
                    simbolo.nome,
                    simbolo.tipo,
                    simbolo.linhaDeclaracao,
                    simbolo.inicializado ? "sim" : "não",
                    simbolo.linhasUso.size(),
                    formatarLinhasUso(simbolo.linhasUso)
            );
        }

        System.out.println();
        System.out.println("Total de símbolos: " + simbolos.size());
    }

    private String formatarLinhasUso(List<Integer> linhas) {
        if (linhas.isEmpty()) {
            return "-";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < linhas.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(linhas.get(i));
        }

        return sb.toString();
    }
}
