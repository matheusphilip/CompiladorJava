import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int atual = 0;

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

    public Ast.Programa parse() {
        List<Ast.Comando> comandos = new ArrayList<>();

        while (!verificar(TokenType.EOF)) {
            comandos.add(comando());
        }

        return new Ast.Programa(comandos);
    }

    private Ast.Comando comando() {
        if (corresponder(TokenType.KW_INT)) {
            return declaracao(anterior());
        }

        if (verificar(TokenType.IDENTIFIER)) {
            return atribuicao();
        }

        throw erro(
                espiar(),
                "Esperado uma declaração 'int' ou uma atribuição."
        );
    }

    private Ast.Declaracao declaracao(Token tipo) {
        Token nome = consumir(
                TokenType.IDENTIFIER,
                "Esperado nome da variável após 'int'."
        );

        Ast.Expressao inicializador = null;

        if (corresponder(TokenType.ASSIGN)) {
            inicializador = expressao();
        }

        consumir(
                TokenType.SEMICOLON,
                "Esperado ';' após declaração."
        );

        return new Ast.Declaracao(tipo, nome, inicializador);
    }

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

    private Ast.Expressao expressao() {
        Ast.Expressao esquerda = termo();

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

    private boolean corresponder(TokenType... tipos) {
        for (TokenType tipo : tipos) {
            if (verificar(tipo)) {
                avancar();
                return true;
            }
        }

        return false;
    }

    private Token consumir(TokenType tipo, String mensagem) {
        if (verificar(tipo)) {
            return avancar();
        }

        throw erro(espiar(), mensagem);
    }

    private boolean verificar(TokenType tipo) {
        return espiar().type == tipo;
    }

    private Token avancar() {
        Token token = espiar();

        if (token.type != TokenType.EOF) {
            atual++;
        }

        return token;
    }

    private Token espiar() {
        return tokens.get(atual);
    }

    private Token anterior() {
        return tokens.get(atual - 1);
    }

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