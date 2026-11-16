import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Analisa os tokens, constrói a AST e tenta recuperar erros sintáticos.
// A tabela é recebida do Lexer e enriquecida apenas quando uma declaração
// é reconhecida por completo.
public class Parser {

    private final List<Token> tokens;
    private final TabelaDeSimbolos tabela;
    private final List<String> erros = new ArrayList<>();
    private int atual = 0;

    // Construtor de compatibilidade: cria a tabela lexical a partir dos tokens.
    public Parser(List<Token> tokens) {
        this(tokens, criarTabelaLexica(tokens));
    }

    // Recebe a mesma tabela criada e alimentada pelo Lexer.
    public Parser(List<Token> tokens, TabelaDeSimbolos tabela) {
        if (tokens == null
                || tokens.isEmpty()
                || tokens.get(tokens.size() - 1).type != TokenType.EOF) {
            throw new IllegalArgumentException(
                    "A lista de tokens deve terminar com EOF."
            );
        }

        if (tabela == null) {
            throw new IllegalArgumentException(
                    "A tabela de símbolos não pode ser nula."
            );
        }

        this.tokens = tokens;
        this.tabela = tabela;
    }

    private static TabelaDeSimbolos criarTabelaLexica(List<Token> tokens) {
        TabelaDeSimbolos tabela = new TabelaDeSimbolos();

        if (tokens != null) {
            for (Token token : tokens) {
                tabela.registrarOcorrencia(token);
            }
        }

        return tabela;
    }

    public TabelaDeSimbolos getTabelaDeSimbolos() {
        return tabela;
    }

    // Retorna os erros recuperáveis encontrados durante o parse.
    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }

    // Analisa o máximo possível do arquivo e devolve a AST dos comandos
    // reconhecidos. Erros sintáticos ficam disponíveis em getErros().
    public Ast.Programa parse() {
        List<Ast.Comando> comandos = new ArrayList<>();

        while (!verificar(TokenType.EOF)) {
            int inicioComando = atual;

            try {
                comandos.add(comando());
            } catch (ParseError e) {
                erros.add(e.getMessage());
                sincronizar(inicioComando);
            }
        }

        // Importante: não lançar ParseError apenas por haver erros recuperáveis.
        // A AST parcial permite que a tabela e a análise semântica prossigam.
        return new Ast.Programa(comandos);
    }

    private Ast.Comando comando() {
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

        if (verificar(TokenType.IDENTIFIER)) {
            return comandoIdentificador();
        }

        throw erro(espiar(), "Esperado uma declaração ou uma atribuição.");
    }

    // Distingue atribuições de chamadas de método iniciadas por identificador.
    // Exemplos: idade = 20; e System.out.println("Olá");
    private Ast.Comando comandoIdentificador() {
        Token primeiro = consumir(
                TokenType.IDENTIFIER,
                "Esperado um identificador."
        );

        List<Token> caminho = new ArrayList<>();
        caminho.add(primeiro);

        // Reconhece acessos encadeados: System.out.println
        while (corresponder(TokenType.DOT)) {
            caminho.add(consumir(
                    TokenType.IDENTIFIER,
                    "Esperado um identificador após '.'."
            ));
        }

        // Uma sequência terminada em '(' representa uma chamada de método.
        if (corresponder(TokenType.LPAREN)) {
            List<Ast.Expressao> argumentos = new ArrayList<>();

            if (!verificar(TokenType.RPAREN)) {
                do {
                    argumentos.add(expressao());
                } while (corresponder(TokenType.COMMA));
            }

            consumir(TokenType.RPAREN, "Esperado ')' após os argumentos do método.");
            consumirTerminador("chamada de método");

            return new Ast.ChamadaMetodo(caminho, argumentos);
        }

        // Atribuições continuam aceitando somente um identificador simples.
        if (caminho.size() == 1 && corresponder(TokenType.ASSIGN)) {
            Ast.Expressao valor = expressao();
            consumirTerminador("atribuição");
            return new Ast.Atribuicao(primeiro, valor);
        }

        if (caminho.size() > 1) {
            throw erro(
                    espiar(),
                    "Esperado '(' para iniciar a chamada do método após '"
                            + caminho.get(caminho.size() - 1).lexeme + "'."
            );
        }

        throw erro(
                espiar(),
                "Esperado '=' para atribuição ou '(' para chamada de método."
        );
    }

    // Reconhece: tipo identificador [= expressão] ;
    private Ast.Declaracao declaracao(Token tipo) {
        Token nome = consumir(
                TokenType.IDENTIFIER,
                "Esperado nome da variável após '" + tipo.lexeme + "'."
        );

        Ast.Expressao inicializador = null;

        if (corresponder(TokenType.ASSIGN)) {
            inicializador = expressao();
        } else if (!verificar(TokenType.SEMICOLON)) {
            throw erro(
                    espiar(),
                    "Esperado '=' para inicializar a variável ou ';' para encerrar a declaração."
            );
        }

        consumirTerminador("declaração");

        // A declaração é registrada somente depois que sua estrutura foi
        // reconhecida ou recuperada por inserção do ';'.
        tabela.registrarDeclaracao(nome, Tipo.deToken(tipo.type), "variável");

        return new Ast.Declaracao(tipo, nome, inicializador);
    }

    // Reconhece: identificador = expressão ;
    private Ast.Atribuicao atribuicao() {
        Token nome = consumir(TokenType.IDENTIFIER, "Esperado nome da variável.");
        consumir(TokenType.ASSIGN, "Esperado '=' após o nome da variável.");

        Ast.Expressao valor = expressao();
        consumirTerminador("atribuição");

        return new Ast.Atribuicao(nome, valor);
    }

    // Se o ';' estiver ausente antes de EOF ou de um novo comando reconhecível,
    // aplica recuperação por inserção: considera que faltou apenas o terminador.
    private void consumirTerminador(String nomeComando) {
        if (corresponder(TokenType.SEMICOLON)) {
            return;
        }

        if (verificar(TokenType.EOF)
                || inicioDeclaracao()
                || inicioAtribuicao()
                || inicioChamadaMetodo()) {
            Token encontrado = espiar();
            String depois = encontrado.type == TokenType.EOF
                    ? "fim do arquivo"
                    : "'" + encontrado.lexeme + "'";

            erros.add(
                    "[ERRO SINTÁTICO] Linha " + encontrado.line
                            + ", coluna " + encontrado.column
                            + ": esperado ';' após " + nomeComando
                            + ". Recuperação: inserido ';' antes de " + depois + "."
            );
            return;
        }

        throw erro(espiar(), "Esperado ';' após " + nomeComando + ".");
    }

    private Ast.Expressao expressao() {
        Ast.Expressao esquerda = termo();

        while (corresponder(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = anterior();
            Ast.Expressao direita = termo();
            esquerda = new Ast.Binaria(esquerda, operador, direita);
        }

        return esquerda;
    }

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

    private Ast.Expressao unaria() {
        if (corresponder(TokenType.PLUS, TokenType.MINUS)) {
            Token operador = anterior();
            Ast.Expressao direita = unaria();
            return new Ast.Unaria(operador, direita);
        }

        return primaria();
    }

    private Ast.Expressao primaria() {
        if (corresponder(TokenType.INT_LITERAL, TokenType.FLOAT_LITERAL)) {
            return new Ast.Numero(anterior());
        }

        if (corresponder(
                TokenType.STRING_LITERAL,
                TokenType.CHAR_LITERAL,
                TokenType.KW_TRUE,
                TokenType.KW_FALSE
        )) {
            return new Ast.Literal(anterior());
        }

        if (corresponder(TokenType.IDENTIFIER)) {
            return new Ast.Identificador(anterior());
        }

        if (corresponder(TokenType.LPAREN)) {
            Ast.Expressao interna = expressao();
            consumir(TokenType.RPAREN, "Esperado ')' para fechar a expressão.");
            return interna;
        }

        throw erro(
                espiar(),
                "Esperado um número, string, caractere, booleano, identificador ou '('."
        );
    }

    // Recuperação em modo pânico: descarta o comando inválido até encontrar
    // um ponto e vírgula ou o início provável do próximo comando.
    private void sincronizar(int inicioComando) {
        if (atual == inicioComando && !verificar(TokenType.EOF)) {
            avancar();
        }

        while (!verificar(TokenType.EOF)) {
            if (inicioDeclaracao() || inicioAtribuicao() || inicioChamadaMetodo()) {
                return;
            }

            if (corresponder(TokenType.SEMICOLON)) {
                return;
            }

            avancar();
        }
    }

    private boolean inicioDeclaracao() {
        return verificar(TokenType.KW_INT)
                || verificar(TokenType.KW_FLOAT)
                || verificar(TokenType.KW_DOUBLE)
                || verificar(TokenType.KW_STRING)
                || verificar(TokenType.KW_CHAR)
                || verificar(TokenType.KW_BOOLEAN);
    }

    private boolean inicioAtribuicao() {
        return verificar(TokenType.IDENTIFIER)
                && atual + 1 < tokens.size()
                && tokens.get(atual + 1).type == TokenType.ASSIGN;
    }

    // Verifica por antecipação se há uma cadeia de membros seguida de '('.
    // Não altera a posição atual do Parser.
    private boolean inicioChamadaMetodo() {
        int indice = atual;

        if (indice >= tokens.size()
                || tokens.get(indice).type != TokenType.IDENTIFIER) {
            return false;
        }
        indice++;

        while (indice < tokens.size()
                && tokens.get(indice).type == TokenType.DOT) {
            indice++;
            if (indice >= tokens.size()
                    || tokens.get(indice).type != TokenType.IDENTIFIER) {
                return false;
            }
            indice++;
        }

        return indice < tokens.size()
                && tokens.get(indice).type == TokenType.LPAREN;
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
        String encontrado = token.type == TokenType.EOF
                ? "fim do arquivo"
                : "'" + token.lexeme + "'";

        return new ParseError(
                "[ERRO SINTÁTICO] Linha " + token.line
                        + ", coluna " + token.column + ": " + mensagem
                        + " Encontrado: " + encontrado + "."
        );
    }
}
