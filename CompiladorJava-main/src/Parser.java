import java.util.ArrayList;
import java.util.List;

// Analisa os tokens e constrói a Árvore Sintática Abstrata (AST).
public class Parser {

    private final List<Token> tokens;
    private int atual = 0; // Posição do token atual.

    public Parser(List<Token> tokens) {
        // Verifica se a lista termina com EOF.
        if (tokens == null
                || tokens.isEmpty()
                || tokens.get(tokens.size() - 1).type != TokenType.EOF) {
            throw new IllegalArgumentException(
                    "A lista de tokens deve terminar com EOF."
            );
        }

        this.tokens = tokens;
    }

    // Analisa todos os comandos e monta o programa.
    public Ast.Programa parse() {
        List<Ast.Comando> comandos = new ArrayList<>();

        while (!verificar(TokenType.EOF)) {
            comandos.add(comando());
        }

        return new Ast.Programa(comandos);
    }

    // Identifica se o próximo comando é declaração ou atribuição.
    private Ast.Comando comando() {
        if (corresponder(TokenType.KW_INT, TokenType.KW_STRING)) {
            return declaracao(anterior());
        }

        if (verificar(TokenType.IDENTIFIER)) {
            return atribuicao();
        }

        throw erro(
                espiar(),
                "Esperado uma declaração ou uma atribuição."
        );
    }

    // Analisa uma declaração, como: int idade = 20;
    private Ast.Declaracao declaracao(Token tipo) {
        Token nome = consumir(
                TokenType.IDENTIFIER,
                "Esperado nome da variável após 'int'."
        );

        Ast.Expressao inicializador = null;

        // Verifica se existe um valor inicial para a variável.
        if (corresponder(TokenType.ASSIGN)) {
            inicializador = expressao();
        }

        consumir(
                TokenType.SEMICOLON,
                "Esperado ';' após declaração."
        );

        return new Ast.Declaracao(tipo, nome, inicializador);
    }

    // Analisa uma atribuição, como: idade = 25;
    private Ast.Atribuicao atribuicao() {
        Token nome = consumir(
                TokenType.IDENTIFIER,
                "Esperado nome da variável."
        );

        consumir(
                TokenType.ASSIGN,
                "Esperado '=' após o nome da variável."
        );

        Ast.Expressao valor = expressao();

        consumir(
                TokenType.SEMICOLON,
                "Esperado ';' após atribuição."
        );

        return new Ast.Atribuicao(nome, valor);
    }

    // Analisa somas e subtrações, como: 10 + 5 - 2.
    private Ast.Expressao expressao() {
        Ast.Expressao esquerda = termo();

        while (corresponder(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = anterior();
            Ast.Expressao direita = termo();

            // Cria um nó da AST para a operação.
            esquerda = new Ast.Binaria(esquerda, operador, direita);
        }

        return esquerda;
    }

    // Analisa números, variáveis e expressões entre parênteses.
    private Ast.Expressao termo() {
        if (corresponder(TokenType.INT_LITERAL)) {
            return new Ast.Numero(anterior());
        }

        if (corresponder(TokenType.IDENTIFIER)) {
            return new Ast.Identificador(anterior());
        }

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
                "Esperado um número, identificador ou '('."
        );
    }

    // Verifica se o token atual corresponde a algum dos tipos informados.
    private boolean corresponder(TokenType... tipos) {
        for (TokenType tipo : tipos) {
            if (verificar(tipo)) {
                avancar();
                return true;
            }
        }

        return false;
    }

    // Consome o token esperado ou lança um erro sintático.
    private Token consumir(TokenType tipo, String mensagem) {
        if (verificar(tipo)) {
            return avancar();
        }

        throw erro(espiar(), mensagem);
    }

    // Verifica o tipo do token atual sem avançar.
    private boolean verificar(TokenType tipo) {
        return espiar().type == tipo;
    }

    // Retorna o token atual e avança para o próximo.
    private Token avancar() {
        Token token = espiar();

        if (token.type != TokenType.EOF) {
            atual++;
        }

        return token;
    }

    // Retorna o token atual sem consumi-lo.
    private Token espiar() {
        return tokens.get(atual);
    }

    // Retorna o último token consumido.
    private Token anterior() {
        return tokens.get(atual - 1);
    }

    // Cria uma mensagem de erro com a linha e o token encontrado.
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