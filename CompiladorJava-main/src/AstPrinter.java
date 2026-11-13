// Exibe a Árvore Sintática Abstrata (AST) no terminal.
public class AstPrinter {

    // Imprime a raiz do programa e percorre todos os comandos.
    public void imprimir(Ast.Programa programa) {
        System.out.println("Programa");

        for (int i = 0; i < programa.comandos.size(); i++) {
            // Verifica se este é o último comando da lista.
            boolean ultimo = i == programa.comandos.size() - 1;

            imprimirComando(
                    programa.comandos.get(i),
                    "",
                    ultimo
            );
        }
    }

    // Identifica o tipo de comando e imprime seus componentes.
    private void imprimirComando(
            Ast.Comando comando,
            String prefixo,
            boolean ultimo
    ) {
        // Imprime uma declaração de variável.
        if (comando instanceof Ast.Declaracao) {
            Ast.Declaracao declaracao = (Ast.Declaracao) comando;

            linha(prefixo, ultimo, "Declaracao");
            String filhos = prefixoFilhos(prefixo, ultimo);

            // Imprime o tipo declarado da variável.
            linha(
                    filhos,
                    false,
                    "TIPO (" + declaracao.tipo.lexeme + ")"
            );

            // O identificador é o último filho quando não há inicializador.
            linha(
                    filhos,
                    declaracao.inicializador == null,
                    "ID (" + declaracao.nome.lexeme + ")"
            );

            // Imprime a atribuição inicial, caso ela exista.
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

        // Imprime uma atribuição de valor a uma variável.
        if (comando instanceof Ast.Atribuicao) {
            Ast.Atribuicao atribuicao = (Ast.Atribuicao) comando;

            linha(prefixo, ultimo, "Atribuicao");
            String filhos = prefixoFilhos(prefixo, ultimo);

            // Imprime o nome da variável que recebe o valor.
            linha(
                    filhos,
                    false,
                    "ID (" + atribuicao.nome.lexeme + ")"
            );

            // Imprime o operador de atribuição.
            linha(filhos, false, "OP_ATRIB (=)");

            // Imprime a expressão atribuída à variável.
            imprimirExpressao(atribuicao.valor, filhos, true);
            return;
        }

        // Interrompe a impressão se o tipo de comando não for suportado.
        throw new IllegalArgumentException(
                "Tipo de comando desconhecido."
        );
    }

    // Identifica e imprime os nós de uma expressão recursivamente.
    private void imprimirExpressao(
            Ast.Expressao expressao,
            String prefixo,
            boolean ultimo
    ) {
        // Imprime um número inteiro ou decimal.
        if (expressao instanceof Ast.Numero) {
            Ast.Numero numero = (Ast.Numero) expressao;

            linha(
                    prefixo,
                    ultimo,
                    "NUM (" + numero.token.lexeme + ")"
            );

            return;
        }

        // Imprime um literal de string, caractere ou booleano.
        if (expressao instanceof Ast.Literal) {
            Ast.Literal literal = (Ast.Literal) expressao;

            linha(
                    prefixo,
                    ultimo,
                    formatarLiteral(literal.token)
            );

            return;
        }

        // Imprime uma referência a uma variável.
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

        // Imprime uma operação com um único operando, como -10.
        if (expressao instanceof Ast.Unaria) {
            Ast.Unaria unaria = (Ast.Unaria) expressao;

            linha(
                    prefixo,
                    ultimo,
                    "Unaria (" + unaria.operador.lexeme + ")"
            );

            // Define o recuo e imprime o único operando.
            String filhos = prefixoFilhos(prefixo, ultimo);

            imprimirExpressao(unaria.direita, filhos, true);
            return;
        }

        // Imprime uma operação com dois operandos, como a + 5.
        if (expressao instanceof Ast.Binaria) {
            Ast.Binaria binaria = (Ast.Binaria) expressao;

            linha(
                    prefixo,
                    ultimo,
                    "Expressao (" + binaria.operador.lexeme + ")"
            );

            // Define o recuo dos operandos da operação.
            String filhos = prefixoFilhos(prefixo, ultimo);

            // O operando esquerdo é o primeiro filho.
            imprimirExpressao(binaria.esquerda, filhos, false);

            // O operando direito é o último filho.
            imprimirExpressao(binaria.direita, filhos, true);
            return;
        }

        // Interrompe a impressão se o tipo de expressão não for suportado.
        throw new IllegalArgumentException(
                "Tipo de expressão desconhecido."
        );
    }

    // Formata o texto de um literal de acordo com o tipo do token.
    private String formatarLiteral(Token token) {
        switch (token.type) {
            // Adiciona aspas duplas na exibição de strings.
            case STRING_LITERAL:
                return "STRING (\"" + token.lexeme + "\")";

            // Adiciona aspas simples na exibição de caracteres.
            case CHAR_LITERAL:
                return "CHAR ('" + token.lexeme + "')";

            // Exibe os valores booleanos true e false.
            case KW_TRUE:
            case KW_FALSE:
                return "BOOLEAN (" + token.lexeme + ")";

            // Rejeita tokens que não representam os literais esperados.
            default:
                throw new IllegalArgumentException(
                        "Tipo de literal desconhecido: " + token.type
                );
        }
    }

    // Imprime um nó com seu recuo e o símbolo de ligação correspondente.
    private void linha(
            String prefixo,
            boolean ultimo,
            String texto
    ) {
        System.out.println(
                prefixo + (ultimo ? "└── " : "├── ") + texto
        );
    }

    // Define o recuo dos filhos, preservando a linha vertical
    // quando ainda existem outros nós no mesmo nível do pai.
    private String prefixoFilhos(
            String prefixo,
            boolean ultimo
    ) {
        return prefixo + (ultimo ? "    " : "│   ");
    }
}