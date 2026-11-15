// Imprime a AST no terminal em formato de árvore indentada.
public class AstPrinter {

    // Imprime o programa e percorre todos os seus comandos.
    public void imprimir(Ast.Programa programa) {
        System.out.println("Programa");

        for (int i = 0; i < programa.comandos.size(); i++) {
            boolean ultimo = i == programa.comandos.size() - 1;
            imprimirComando(programa.comandos.get(i), "    ", ultimo);
        }
    }

    // Imprime uma declaração ou uma atribuição.
    private void imprimirComando(Ast.Comando comando, String prefixo, boolean ultimo) {
        // Usa └── no último item e ├── nos anteriores.
        String conector = ultimo ? "└── " : "├── ";

        if (comando instanceof Ast.Declaracao declaracao) {
            System.out.println(prefixo + conector + "Declaração");
            System.out.println(prefixo + "    ├── Tipo: " + declaracao.tipo.lexeme);
            System.out.println(prefixo + "    ├── Nome: " + declaracao.nome.lexeme);

            if (declaracao.inicializador != null) {
                System.out.println(prefixo + "    └── Inicializador");
                imprimirExpressao(declaracao.inicializador, prefixo + "        ", true);
            } else {
                System.out.println(prefixo + "    └── Sem inicializador");
            }
        }

        if (comando instanceof Ast.Atribuicao atribuicao) {
            System.out.println(prefixo + conector + "Atribuição");
            System.out.println(prefixo + "    ├── Variável: " + atribuicao.nome.lexeme);
            System.out.println(prefixo + "    └── Valor");
            imprimirExpressao(atribuicao.valor, prefixo + "        ", true);
        }
    }

    // Imprime expressões de forma recursiva.
    private void imprimirExpressao(Ast.Expressao expressao, String prefixo, boolean ultimo) {
        String conector = ultimo ? "└── " : "├── ";

        if (expressao instanceof Ast.Binaria binaria) {
            System.out.println(prefixo + conector + "Operação binária: "
                    + binaria.operador.lexeme);

            // Imprime primeiro o lado esquerdo e depois o direito.
            imprimirExpressao(binaria.esquerda, prefixo + "    ", false);
            imprimirExpressao(binaria.direita, prefixo + "    ", true);
        } else if (expressao instanceof Ast.Unaria unaria) {
            System.out.println(prefixo + conector + "Operação unária: "
                    + unaria.operador.lexeme);

            imprimirExpressao(unaria.direita, prefixo + "    ", true);
        } else if (expressao instanceof Ast.Numero numero) {
            System.out.println(prefixo + conector + "Número: " + numero.token.lexeme);
        } else if (expressao instanceof Ast.Literal literal) {
            System.out.println(prefixo + conector + "Literal: " + literal.token.lexeme);
        } else if (expressao instanceof Ast.Identificador identificador) {
            System.out.println(prefixo + conector + "Identificador: "
                    + identificador.nome.lexeme);
        }
    }
}