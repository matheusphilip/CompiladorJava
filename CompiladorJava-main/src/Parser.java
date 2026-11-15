import java.util.ArrayList;
import java.util.List;

// Analisa a lista de tokens e constrói a Árvore Sintática Abstrata (AST).
// Reconhece declarações, atribuições e expressões aritméticas.
public class Parser {

    // Lista de tokens produzida pelo Lexer.
    private final List<Token> tokens;

    // Índice do token que está sendo analisado no momento.
    private int atual = 0;

    // Recebe a lista de tokens e verifica se ela termina com EOF.
    public Parser(List<Token> tokens) {
        if (tokens == null
                || tokens.isEmpty()
                || tokens.get(tokens.size() - 1).type != TokenType.EOF) {
            throw new IllegalArgumentException(
                    "A lista de tokens deve terminar com EOF."
            );
        }

        this.tokens = tokens;
    }

    // Percorre todos os tokens e monta a lista de comandos do programa.
    // Caso encontre um erro, registra a mensagem e tenta recuperar a análise.
    public Ast.Programa parse() {
        List<Ast.Comando> comandos = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        // Continua enquanto não chegar ao fim do arquivo.
        while (!verificar(TokenType.EOF)) {
            // Guarda a posição inicial para evitar laço infinito na recuperação.
            int inicioComando = atual;

            try {
                comandos.add(comando());
            } catch (ParseError e) {
                // Guarda o erro e descarta o restante do comando inválido.
                erros.add(e.getMessage());
                sincronizar(inicioComando);
            }
        }

        // Se houve algum erro, impede a devolução de uma AST incompleta.
        if (!erros.isEmpty()) {
            throw new ParseError(
                    String.join(System.lineSeparator(), erros)
            );
        }

        return new Ast.Programa(comandos);
    }

    // Identifica se o próximo comando é uma declaração ou uma atribuição.
    private Ast.Comando comando() {
        // Se começar com um tipo, é uma declaração de variável.
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

        // Se começar com um identificador, pode ser uma atribuição.
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

        // Exige o ponto e vírgula que encerra a declaração.
        consumir(
                TokenType.SEMICOLON,
                "Esperado ';' após declaração."
        );

        return new Ast.Declaracao(tipo, nome, inicializador);
    }

    // Analisa uma atribuição, como: idade = 20;
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

        // Analisa a expressão que será atribuída.
        Ast.Expressao valor = expressao();

        // Exige o ponto e vírgula que encerra a atribuição.
        consumir(
                TokenType.SEMICOLON,
                "Esperado ';' após atribuição."
        );

        return new Ast.Atribuicao(nome, valor);
    }

    // Analisa soma e subtração.
    // Esses operadores têm precedência menor que multiplicação e divisão.
    private Ast.Expressao expressao() {
        Ast.Expressao esquerda = termo();

        // Agrupa operações de mesma precedência da esquerda para a direita.
        while (corresponder(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = anterior();
            Ast.Expressao direita = termo();

            esquerda = new Ast.Binaria(esquerda, operador, direita);
        }

        return esquerda;
    }

    // Analisa multiplicação, divisão e resto.
    // Esses operadores têm precedência maior que soma e subtração.
    private Ast.Expressao termo() {
        Ast.Expressao esquerda = unaria();

        while (corresponder(
                TokenType.STAR,
                TokenType.SLASH,
                TokenType.PERCENT
        )) {
            Token operador = anterior();
            Ast.Expressao direita = unaria();

            esquerda = new Ast.Binaria(esquerda, operador, direita);
        }

        return esquerda;
    }

    // Analisa operadores unários.
    // Exemplos: -10, +5 e -(10 + 5).
    private Ast.Expressao unaria() {
        if (corresponder(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = anterior();
            Ast.Expressao direita = unaria();

            return new Ast.Unaria(operador, direita);
        }

        return primaria();
    }

    // Reconhece literais, identificadores e expressões entre parênteses.
    private Ast.Expressao primaria() {
        // Reconhece números inteiros e decimais.
        if (corresponder(
                TokenType.INT_LITERAL,
                TokenType.FLOAT_LITERAL
        )) {
            return new Ast.Numero(anterior());
        }

        // Reconhece strings, caracteres e valores booleanos.
        if (corresponder(
                TokenType.STRING_LITERAL,
                TokenType.CHAR_LITERAL,
                TokenType.KW_TRUE,
                TokenType.KW_FALSE
        )) {
            return new Ast.Literal(anterior());
        }

        // Reconhece uma variável usada dentro de uma expressão.
        if (corresponder(TokenType.IDENTIFIER)) {
            return new Ast.Identificador(anterior());
        }

        // Reconhece expressões agrupadas por parênteses.
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

    // Recupera a análise após encontrar um comando inválido.
    // Descarta tokens até encontrar um possível início de novo comando.
    private void sincronizar(int inicioComando) {
        // Se nenhum token foi consumido, avança para evitar laço infinito.
        if (atual == inicioComando && !verificar(TokenType.EOF)) {
            avancar();
        }

        while (!verificar(TokenType.EOF)) {
            // Um tipo indica o possível início de uma nova declaração.
            if (inicioDeclaracao()) {
                return;
            }

            // Um identificador seguido de '=' indica uma atribuição.
            if (inicioAtribuicao()) {
                return;
            }

            // O ponto e vírgula encerra o trecho inválido.
            if (corresponder(TokenType.SEMICOLON)) {
                return;
            }

            // Descarta tokens que não permitem retomar a análise.
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

    // Consome o token atual caso ele corresponda a um dos tipos informados.
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

    // Monta uma mensagem de erro sintático com linha e coluna.
    private ParseError erro(Token token, String mensagem) {
        String encontrado;

        if (token.type == TokenType.EOF) {
            encontrado = "fim do arquivo";
        } else {
            encontrado = "'" + token.lexeme + "'";
        }

        return new ParseError(
                "[ERRO SINTÁTICO] Linha " + token.line
                        + ", coluna " + token.column
                        + ": " + mensagem
                        + " Encontrado: " + encontrado + "."
        );
    }
}