import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Percorre a AST e valida declarações, usos de identificadores e tipos.
// A tabela já foi criada pelo Lexer e complementada pelo Parser.
public class AnalisadorSemantico {

    private final TabelaDeSimbolos tabela;
    private final List<String> erros = new ArrayList<>();

    // Controla as declarações já encontradas na passagem semântica.
    // Isso preserva a regra de uso somente depois da declaração.
    private final Set<String> declaracoesEncontradas = new HashSet<>();
    private final Set<String> declaracoesConcluidas = new HashSet<>();

    public AnalisadorSemantico(TabelaDeSimbolos tabela) {
        if (tabela == null) {
            throw new IllegalArgumentException("A tabela de símbolos não pode ser nula.");
        }
        this.tabela = tabela;
    }

    // Percorre todos os comandos da AST em ordem de ocorrência no programa.
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

        if (comando instanceof Ast.ChamadaMetodo) {
            analisarChamadaMetodo((Ast.ChamadaMetodo) comando);
            return;
        }

        throw new IllegalArgumentException(
                "Comando não suportado pelo analisador semântico."
        );
    }

    // Analisa declarações como: int idade = 20;
    private void analisarDeclaracao(Ast.Declaracao declaracao) {
        String nome = declaracao.nome.lexeme;
        Tipo tipoDeclarado = Tipo.deToken(declaracao.tipo.type);

        // A tabela foi pré-preenchida, portanto a duplicidade é controlada
        // pela ordem dos comandos percorridos nesta passagem.
        if (!declaracoesEncontradas.add(nome)) {
            Simbolo simbolo = tabela.buscar(nome);
            int linhaAnterior = simbolo != null
                    ? simbolo.getLinhaDeclaracao()
                    : -1;

            erro(
                    declaracao.nome,
                    "A variável '" + nome + "' já foi declarada"
                            + (linhaAnterior > 0
                                ? " na linha " + linhaAnterior + "."
                                : " anteriormente.")
            );

            // Percorre o inicializador mesmo em uma declaração duplicada
            // para encontrar outros usos de identificadores inválidos.
            if (declaracao.inicializador != null) {
                inferirTipo(declaracao.inicializador);
            }
            return;
        }

        // A própria variável ainda não está em declaracoesConcluidas.
        // Assim, uma referência no seu próprio inicializador não é aceita.
        if (declaracao.inicializador != null) {
            Tipo tipoInicializador = inferirTipo(declaracao.inicializador);

            if (tipoInicializador != null
                    && !atribuicaoCompativel(tipoDeclarado, tipoInicializador)) {
                erro(
                        declaracao.nome,
                        "Não é possível inicializar '" + nome + "' do tipo '"
                                + tipoDeclarado.getNome()
                                + "' com um valor do tipo '"
                                + tipoInicializador.getNome() + "'."
                );
            }
        }

        // A variável passa a estar disponível para os comandos seguintes,
        // mesmo se o valor inicial atribuído tiver tipo incompatível.
        declaracoesConcluidas.add(nome);
    }

    // Por enquanto, o subconjunto implementado reconhece as duas saídas
    // padrão de Java: System.out.print(...) e System.out.println(...).
    private void analisarChamadaMetodo(Ast.ChamadaMetodo chamada) {
        String nomeMetodo = chamada.nomeCompleto();
        boolean print = "System.out.print".equals(nomeMetodo);
        boolean println = "System.out.println".equals(nomeMetodo);

        if (!print && !println) {
            erro(
                    chamada.caminho.get(chamada.caminho.size() - 1),
                    "A chamada de método '" + nomeMetodo
                            + "' ainda não é suportada pelo compilador."
            );
        } else if (print && chamada.argumentos.size() != 1) {
            erro(
                    chamada.caminho.get(chamada.caminho.size() - 1),
                    "System.out.print(...) exige exatamente um argumento."
            );
        } else if (println && chamada.argumentos.size() > 1) {
            erro(
                    chamada.caminho.get(chamada.caminho.size() - 1),
                    "System.out.println(...) aceita no máximo um argumento nesta versão."
            );
        }

        // Mesmo se a chamada ainda não for suportada, analisa os argumentos
        // para não ocultar erros de uso de variáveis ou de tipos.
        for (Ast.Expressao argumento : chamada.argumentos) {
            inferirTipo(argumento);
        }
    }

    // Analisa atribuições como: idade = 20;
    private void analisarAtribuicao(Ast.Atribuicao atribuicao) {
        String nome = atribuicao.nome.lexeme;
        Tipo tipoValor = inferirTipo(atribuicao.valor);

        // A existência lexical do nome, sozinha, não significa declaração.
        if (!declaracoesConcluidas.contains(nome)) {
            erro(
                    atribuicao.nome,
                    "A variável '" + nome + "' não foi declarada antes deste uso."
            );
            return;
        }

        Simbolo simbolo = tabela.buscar(nome);
        if (simbolo == null || simbolo.getTipo() == null) {
            erro(atribuicao.nome, "Não foi possível recuperar o tipo da variável '" + nome + "'.");
            return;
        }

        if (tipoValor != null
                && !atribuicaoCompativel(simbolo.getTipo(), tipoValor)) {
            erro(
                    atribuicao.nome,
                    "Não é possível atribuir um valor do tipo '"
                            + tipoValor.getNome() + "' à variável '" + nome
                            + "' do tipo '" + simbolo.getTipo().getNome() + "'."
            );
        }
    }

    // Descobre recursivamente o tipo de cada expressão da AST.
    // Retorna null se a expressão contiver um erro semântico.
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
            String nome = identificador.nome.lexeme;

            // O símbolo pode existir na tabela porque o Lexer viu o lexema.
            // Só é válido usá-lo se a declaração já tiver sido processada.
            if (!declaracoesConcluidas.contains(nome)) {
                erro(
                        identificador.nome,
                        "A variável '" + nome + "' não foi declarada antes deste uso."
                );
                return null;
            }

            Simbolo simbolo = tabela.buscar(nome);
            if (simbolo == null || simbolo.getTipo() == null) {
                erro(
                        identificador.nome,
                        "Não foi possível recuperar o tipo da variável '" + nome + "'."
                );
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
                erro(
                        unaria.operador,
                        "O operador unário '" + unaria.operador.lexeme
                                + "' exige um operando numérico, mas recebeu '"
                                + tipoOperando.getNome() + "'."
                );
                return null;
            }

            return tipoOperando;
        }

        if (expressao instanceof Ast.Binaria) {
            Ast.Binaria binaria = (Ast.Binaria) expressao;
            Tipo tipoEsquerda = inferirTipo(binaria.esquerda);
            Tipo tipoDireita = inferirTipo(binaria.direita);

            if (tipoEsquerda == null || tipoDireita == null) {
                return null;
            }

            if (!tipoEsquerda.ehNumerico() || !tipoDireita.ehNumerico()) {
                erro(
                        binaria.operador,
                        "A operação '" + binaria.operador.lexeme
                                + "' exige operandos numéricos. Foram recebidos '"
                                + tipoEsquerda.getNome() + "' e '"
                                + tipoDireita.getNome() + "'."
                );
                return null;
            }

            return promover(tipoEsquerda, tipoDireita);
        }

        throw new IllegalArgumentException(
                "Expressão não suportada pelo analisador semântico."
        );
    }

    // Conversões numéricas crescentes: int -> float -> double.
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
        erros.add(
                "[ERRO SEMÂNTICO] Linha " + token.line
                        + ", coluna " + token.column + ": " + mensagem
        );
    }
}
