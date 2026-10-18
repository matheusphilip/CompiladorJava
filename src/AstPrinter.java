public class AstPrinter {

    public void imprimir(Ast.Programa programa) {
        System.out.println("Programa");

        for (int i = 0; i < programa.comandos.size(); i++) {
            boolean ultimo = i == programa.comandos.size() - 1;

            imprimirComando(
                    programa.comandos.get(i),
                    "",
                    ultimo
            );
        }
    }

    private void imprimirComando(
            Ast.Comando comando,
            String prefixo,
            boolean ultimo
    ) {
        if (comando instanceof Ast.Declaracao) {
            Ast.Declaracao declaracao = (Ast.Declaracao) comando;

            linha(prefixo, ultimo, "Declaracao");

            String filhos = prefixoFilhos(prefixo, ultimo);

            linha(
                    filhos,
                    false,
                    "TIPO (" + declaracao.tipo.lexeme + ")"
            );

            linha(
                    filhos,
                    declaracao.inicializador == null,
                    "ID (" + declaracao.nome.lexeme + ")"
            );

            if (declaracao.inicializador != null) {
                linha(filhos, false, "OP_ATRIB (=)");
                imprimirExpressao(
                        declaracao.inicializador,
                        filhos,
                        true
                );
            }

            return;
        }

        if (comando instanceof Ast.Atribuicao) {
            Ast.Atribuicao atribuicao = (Ast.Atribuicao) comando;

            linha(prefixo, ultimo, "Atribuicao");

            String filhos = prefixoFilhos(prefixo, ultimo);

            linha(
                    filhos,
                    false,
                    "ID (" + atribuicao.nome.lexeme + ")"
            );

            linha(filhos, false, "OP_ATRIB (=)");

            imprimirExpressao(
                    atribuicao.valor,
                    filhos,
                    true
            );

            return;
        }

        throw new IllegalArgumentException(
                "Tipo de comando desconhecido."
        );
    }

    private void imprimirExpressao(
            Ast.Expressao expressao,
            String prefixo,
            boolean ultimo
    ) {
        if (expressao instanceof Ast.Numero) {
            Ast.Numero numero = (Ast.Numero) expressao;

            linha(
                    prefixo,
                    ultimo,
                    "NUM (" + numero.token.lexeme + ")"
            );

            return;
        }

        if (expressao instanceof Ast.Identificador) {
            Ast.Identificador identificador =
                    (Ast.Identificador) expressao;

            linha(
                    prefixo,
                    ultimo,
                    "ID (" + identificador.nome.lexeme + ")"
            );

            return;
        }

        if (expressao instanceof Ast.Binaria) {
            Ast.Binaria binaria = (Ast.Binaria) expressao;

            linha(
                    prefixo,
                    ultimo,
                    "Expressao (" + binaria.operador.lexeme + ")"
            );

            String filhos = prefixoFilhos(prefixo, ultimo);

            imprimirExpressao(
                    binaria.esquerda,
                    filhos,
                    false
            );

            imprimirExpressao(
                    binaria.direita,
                    filhos,
                    true
            );

            return;
        }

        throw new IllegalArgumentException(
                "Tipo de expressão desconhecido."
        );
    }

    private void linha(
            String prefixo,
            boolean ultimo,
            String texto
    ) {
        System.out.println(
                prefixo + (ultimo ? "└── " : "├── ") + texto
        );
    }

    private String prefixoFilhos(
            String prefixo,
            boolean ultimo
    ) {
        return prefixo + (ultimo ? "    " : "│   ");
    }
}