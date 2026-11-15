import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Percorre a AST e verifica declarações, identificadores e tipos.
public class AnalisadorSemantico {
    private final TabelaDeSimbolos tabela = new TabelaDeSimbolos();
    private final List<String> erros = new ArrayList<>();

    public void analisar(Ast.Programa programa) {
        for (Ast.Comando comando : programa.comandos) {
            analisarComando(comando);
        }
    }

    public TabelaDeSimbolos getTabela() {
        return tabela;
    }

    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }

    public boolean temErros() {
        return !erros.isEmpty();
    }

    private void analisarComando(Ast.Comando comando) {
        if (comando instanceof Ast.Declaracao) {
            analisarDeclaracao((Ast.Declaracao) comando);
            return;
        }

        if (comando instanceof Ast.Atribuicao) {
            analisarAtribuicao((Ast.Atribuicao) comando);
            return;
        }

        // Proteção para caso a AST receba um novo tipo de comando no futuro.
        throw new IllegalArgumentException("Comando não suportado pelo analisador semântico.");
    }

    private void analisarDeclaracao(Ast.Declaracao declaracao) {
        String nome = declaracao.nome.lexeme;
        Tipo tipoDeclarado = converterTipo(declaracao.tipo);

        // A declaração duplicada não substitui o símbolo anterior.
        if (tabela.existe(nome)) {
            Simbolo anterior = tabela.buscar(nome);
            erro(declaracao.nome,
                    "A variável '" + nome + "' já foi declarada na linha "
                            + anterior.getLinhaDeclaracao() + ".");

            // Ainda percorre o inicializador para encontrar outros erros.
            if (declaracao.inicializador != null) {
                inferirTipo(declaracao.inicializador);
            }
            return;
        }

        if (declaracao.inicializador != null) {
            Tipo tipoInicializador = inferirTipo(declaracao.inicializador);

            if (tipoInicializador != null
                    && !atribuicaoCompativel(tipoDeclarado, tipoInicializador)) {
                erro(declaracao.nome,
                        "Não é possível inicializar '" + nome + "' do tipo '"
                                + tipoDeclarado.getNome() + "' com um valor do tipo '"
                                + tipoInicializador.getNome() + "'.");
            }
        }

        // Registra a variável após verificar seu inicializador.
        tabela.adicionar(new Simbolo(nome, tipoDeclarado, declaracao.nome.line));
    }

    private void analisarAtribuicao(Ast.Atribuicao atribuicao) {
        String nome = atribuicao.nome.lexeme;
        Simbolo destino = tabela.buscar(nome);
        Tipo tipoValor = inferirTipo(atribuicao.valor);

        if (destino == null) {
            erro(atribuicao.nome,
                    "A variável '" + nome + "' não foi declarada.");
            return;
        }

        if (tipoValor != null
                && !atribuicaoCompativel(destino.getTipo(), tipoValor)) {
            erro(atribuicao.nome,
                    "Não é possível atribuir um valor do tipo '"
                            + tipoValor.getNome() + "' à variável '" + nome
                            + "' do tipo '" + destino.getTipo().getNome() + "'.");
        }
    }

    // Descobre o tipo de cada expressão recursivamente.
    // null significa que já houve um erro nessa expressão.
    private Tipo inferirTipo(Ast.Expressao expressao) {
        if (expressao instanceof Ast.Numero) {
            Ast.Numero numero = (Ast.Numero) expressao;
            if (numero.token.type == TokenType.INT_LITERAL) {
                return Tipo.INT;
            }
            if (numero.token.type == TokenType.FLOAT_LITERAL) {
                return Tipo.FLOAT;
            }
        }

        if (expressao instanceof Ast.Literal) {
            Ast.Literal literal = (Ast.Literal) expressao;
            switch (literal.token.type) {
                case STRING_LITERAL:
                    return Tipo.STRING;
                case CHAR_LITERAL:
                    return Tipo.CHAR;
                case KW_TRUE:
                case KW_FALSE:
                    return Tipo.BOOLEAN;
                default:
                    erro(literal.token, "Literal não suportado pelo analisador semântico.");
                    return null;
            }
        }

        if (expressao instanceof Ast.Identificador) {
            Ast.Identificador identificador = (Ast.Identificador) expressao;
            Simbolo simbolo = tabela.buscar(identificador.nome.lexeme);

            if (simbolo == null) {
                erro(identificador.nome,
                        "A variável '" + identificador.nome.lexeme + "' não foi declarada.");
                return null;
            }

            return simbolo.getTipo();
        }

        if (expressao instanceof Ast.Unaria) {
            Ast.Unaria unaria = (Ast.Unaria) expressao;
            Tipo tipoOperando = inferirTipo(unaria.direita);

            if (tipoOperando == null) {
                return null;
            }

            if (!tipoOperando.ehNumerico()) {
                erro(unaria.operador,
                        "O operador unário '" + unaria.operador.lexeme
                                + "' exige um operando numérico, mas recebeu '"
                                + tipoOperando.getNome() + "'.");
                return null;
            }

            return tipoOperando;
        }

        if (expressao instanceof Ast.Binaria) {
            Ast.Binaria binaria = (Ast.Binaria) expressao;
            Tipo tipoEsquerda = inferirTipo(binaria.esquerda);
            Tipo tipoDireita = inferirTipo(binaria.direita);

            // Evita gerar erros em cascata quando um operando já é inválido.
            if (tipoEsquerda == null || tipoDireita == null) {
                return null;
            }

            if (!tipoEsquerda.ehNumerico() || !tipoDireita.ehNumerico()) {
                erro(binaria.operador,
                        "A operação '" + binaria.operador.lexeme
                                + "' exige operandos numéricos. Foram recebidos '"
                                + tipoEsquerda.getNome() + "' e '"
                                + tipoDireita.getNome() + "'.");
                return null;
            }

            // O resultado segue o tipo numérico de maior nível.
            return promover(tipoEsquerda, tipoDireita);
        }

        throw new IllegalArgumentException("Expressão não suportada pelo analisador semântico.");
    }

    private Tipo converterTipo(Token token) {
        switch (token.type) {
            case KW_INT:
                return Tipo.INT;
            case KW_FLOAT:
                return Tipo.FLOAT;
            case KW_DOUBLE:
                return Tipo.DOUBLE;
            case KW_STRING:
                return Tipo.STRING;
            case KW_CHAR:
                return Tipo.CHAR;
            case KW_BOOLEAN:
                return Tipo.BOOLEAN;
            default:
                throw new IllegalArgumentException(
                        "Tipo de declaração desconhecido: " + token.lexeme);
        }
    }

    // Conversões numéricas crescentes: int -> float -> double.
    // Os demais tipos só são atribuíveis ao mesmo tipo.
    private boolean atribuicaoCompativel(Tipo destino, Tipo origem) {
        if (destino == origem) {
            return true;
        }

        if (!destino.ehNumerico() || !origem.ehNumerico()) {
            return false;
        }

        return destino.getNivelNumerico() >= origem.getNivelNumerico();
    }

    private Tipo promover(Tipo esquerda, Tipo direita) {
        return esquerda.getNivelNumerico() >= direita.getNivelNumerico()
                ? esquerda
                : direita;
    }

    private void erro(Token token, String mensagem) {
        erros.add("[ERRO SEMÂNTICO] Linha " + token.line + ": " + mensagem);
    }
}
