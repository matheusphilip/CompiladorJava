import java.util.ArrayList;
import java.util.List;

// Analisa os tokens e constrói a Árvore Sintática Abstrata.
public class Parser {

    private final List<Token> tokens;
    private int atual = 0;

    // Recebe a lista de tokens produzida pelo analisador léxico.
    public Parser(List<Token> tokens) {
        // Verifica se a lista existe e termina com EOF.
        if (tokens == null
                || tokens.isEmpty()
                || tokens.get(tokens.size() - 1).type != TokenType.EOF) {
            throw new IllegalArgumentException(
                    "A lista de tokens deve terminar com EOF."
            );
        }

        this.tokens = tokens;
    }

    // Analisa os comandos e acumula os erros encontrados.
    public Ast.Programa parse() {
        List<Ast.Comando> comandos = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        while (!verificar(TokenType.EOF)) {
            // Guarda o início da tentativa para garantir progresso
            // durante a recuperação de um erro.
            int inicioComando = atual;

            try {
                comandos.add(comando());
            } catch (ParseError e) {
                // Registra a mensagem sem interromper imediatamente
                // a análise dos comandos seguintes.
                erros.add(e.getMessage());

                // Descarta o restante do comando inválido ou retoma
                // em um início reconhecível de próximo comando.
                sincronizar(inicioComando);
            }
        }

        // Não devolve uma AST parcial como se estivesse correta.
        // O Main poderá imprimir todas as mensagens pelo catch existente.
        if (!erros.isEmpty()) {
            throw new ParseError(
                    String.join(System.lineSeparator(), erros)
            );
        }

        return new Ast.Programa(comandos);
    }

    // Identifica se o próximo comando é declaração ou atribuição.
    private Ast.Comando comando() {
        // Reconhece os tipos de declaração suportados.
        if (corresponder(
                TokenType.KW_INT,
                TokenType.KW_FLOAT,
                TokenType.KW_DOUBLE,
                TokenType.KW_STRING,
                TokenType.KW_CHAR,
                TokenType.KW_BOOLEAN
        )) {
            return declaracao(anterior());
        }

        // Um identificador no início do comando indica uma atribuição.
        if (verificar(TokenType.IDENTIFIER)) {
            return atribuicao();
        }

        throw erro(
                espiar(),
                "Esperado uma declaração ou uma atribuição."
        );
    }

    // Analisa declarações com ou sem inicialização.
    // Exemplos: int idade; e int idade = 20;
    private Ast.Declaracao declaracao(Token tipo) {
        // Exige o nome da variável após o tipo.
        Token nome = consumir(
                TokenType.IDENTIFIER,
                "Esperado nome da variável após '"
                        + tipo.lexeme + "'."
        );

        Ast.Expressao inicializador = null;

        // Se houver '=', analisa a expressão inicializadora.
        if (corresponder(TokenType.ASSIGN)) {
            inicializador = expressao();
        } else if (!verificar(TokenType.SEMICOLON)) {
            // Detecta casos como: int negativo -10;
            throw erro(
                    espiar(),
                    "Esperado '=' para inicializar a variável "
                            + "ou ';' para encerrar a declaração."
            );
        }

        // Exige o encerramento da declaração.
        consumir(
                TokenType.SEMICOLON,
                "Esperado ';' após declaração."
        );

        // Compatibilidade de tipos pertence à análise semântica.
        return new Ast.Declaracao(tipo, nome, inicializador);
    }

    // Analisa uma atribuição, como: idade = 25;
    private Ast.Atribuicao atribuicao() {
        // Exige a variável que receberá o valor.
        Token nome = consumir(
                TokenType.IDENTIFIER,
                "Esperado nome da variável."
        );

        // Exige o operador de atribuição.
        consumir(
                TokenType.ASSIGN,
                "Esperado '=' após o nome da variável."
        );

        // Analisa a expressão atribuída.
        Ast.Expressao valor = expressao();

        // Exige o encerramento da atribuição.
        consumir(
                TokenType.SEMICOLON,
                "Esperado ';' após atribuição."
        );

        // A existência da variável será verificada no semântico.
        return new Ast.Atribuicao(nome, valor);
    }

    // Analisa soma e subtração.
    private Ast.Expressao expressao() {
        Ast.Expressao esquerda = termo();

        // Agrupa operações de mesma precedência da esquerda para a direita.
        while (corresponder(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = anterior();
            Ast.Expressao direita = termo();

            esquerda = new Ast.Binaria(
                    esquerda,
                    operador,
                    direita
            );
        }

        return esquerda;
    }

    // Analisa multiplicação, divisão e resto.
    // Esses operadores possuem precedência maior que soma e subtração.
    private Ast.Expressao termo() {
        Ast.Expressao esquerda = unaria();

        while (corresponder(
                TokenType.STAR,
                TokenType.SLASH,
                TokenType.PERCENT
        )) {
            Token operador = anterior();
            Ast.Expressao direita = unaria();

            esquerda = new Ast.Binaria(
                    esquerda,
                    operador,
                    direita
            );
        }

        return esquerda;
    }

    // Analisa operadores com um operando.
    // Exemplos: -10, +5 e -(a + 2).
    private Ast.Expressao unaria() {
        if (corresponder(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = anterior();
            Ast.Expressao direita = unaria();

            return new Ast.Unaria(operador, direita);
        }

        return primaria();
    }

    // Reconhece literais, identificadores e expressões agrupadas.
    private Ast.Expressao primaria() {
        // Reconhece números inteiros e decimais.
        if (corresponder(
                TokenType.INT_LITERAL,
                TokenType.FLOAT_LITERAL
        )) {
            return new Ast.Numero(anterior());
        }

        // Reconhece strings, caracteres e booleanos.
        if (corresponder(
                TokenType.STRING_LITERAL,
                TokenType.CHAR_LITERAL,
                TokenType.KW_TRUE,
                TokenType.KW_FALSE
        )) {
            return new Ast.Literal(anterior());
        }

        // Reconhece uma variável usada em uma expressão.
        if (corresponder(TokenType.IDENTIFIER)) {
            return new Ast.Identificador(anterior());
        }

        // Reconhece uma expressão entre parênteses.
        if (corresponder(TokenType.LPAREN)) {
            Ast.Expressao expressao = expressao();

            consumir(
                    TokenType.RPAREN,
                    "Esperado ')' para fechar a expressão."
            );

            return expressao;
        }

        throw erro(
                espiar(),
                "Esperado um número, string, caractere, "
                        + "booleano, identificador ou '('."
        );
    }

    // Recupera a análise após um comando inválido.
    // Esta recuperação é restrita à gramática de declarações e atribuições.
    private void sincronizar(int inicioComando) {
        // Se o comando falhou sem consumir nenhum token,
        // avança uma posição para evitar um laço infinito.
        if (atual == inicioComando && !verificar(TokenType.EOF)) {
            avancar();
        }

        while (!verificar(TokenType.EOF)) {
            // Um tipo indica um possível início de nova declaração.
            // Não o consome, pois comando() precisará dele.
            if (inicioDeclaracao()) {
                return;
            }

            // IDENTIFIER seguido de '=' indica uma possível atribuição.
            // Também preserva esses tokens para a próxima tentativa.
            if (inicioAtribuicao()) {
                return;
            }

            // O ponto e vírgula encerra o trecho descartado.
            if (corresponder(TokenType.SEMICOLON)) {
                return;
            }

            // Descarta tokens do comando inválido até encontrar
            // um ponto em que seja possível retomar.
            avancar();
        }
    }

    // Verifica se o token atual pode iniciar uma declaração.
    private boolean inicioDeclaracao() {
        return verificar(TokenType.KW_INT)
                || verificar(TokenType.KW_FLOAT)
                || verificar(TokenType.KW_DOUBLE)
                || verificar(TokenType.KW_STRING)
                || verificar(TokenType.KW_CHAR)
                || verificar(TokenType.KW_BOOLEAN);
    }

    // Verifica se os próximos tokens podem iniciar uma atribuição.
    private boolean inicioAtribuicao() {
        return verificar(TokenType.IDENTIFIER)
                && atual + 1 < tokens.size()
                && tokens.get(atual + 1).type == TokenType.ASSIGN;
    }

    // Consome o token atual se corresponder a um dos tipos informados.
    private boolean corresponder(TokenType... tipos) {
        for (TokenType tipo : tipos) {
            if (verificar(tipo)) {
                avancar();
                return true;
            }
        }

        return false;
    }

    // Exige um token específico ou lança um erro sintático.
    private Token consumir(TokenType tipo, String mensagem) {
        if (verificar(tipo)) {
            return avancar();
        }

        throw erro(espiar(), mensagem);
    }

    // Verifica o tipo do token atual sem consumi-lo.
    private boolean verificar(TokenType tipo) {
        return espiar().type == tipo;
    }

    // Retorna o token atual e avança, exceto quando encontra EOF.
    private Token avancar() {
        Token token = espiar();

        if (token.type != TokenType.EOF) {
            atual++;
        }

        return token;
    }

    // Retorna o token atual sem avançar.
    private Token espiar() {
        return tokens.get(atual);
    }

    // Retorna o último token consumido.
    private Token anterior() {
        return tokens.get(atual - 1);
    }

    // Monta uma mensagem de erro de estrutura.
    private ParseError erro(Token token, String mensagem) {
        String encontrado;

        if (token.type == TokenType.EOF) {
            encontrado = "fim do arquivo";
        } else {
            encontrado = "'" + token.lexeme + "'";
        }

        return new ParseError(
                "[ERRO SINTÁTICO] Linha " + token.line
                        + ": " + mensagem
                        + " Encontrado: " + encontrado + "."
        );
    }
}