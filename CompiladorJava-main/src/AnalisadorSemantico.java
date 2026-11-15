import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Percorre a AST e realiza a validação semântica do programa.
// Verifica declarações duplicadas, variáveis não declaradas,
// compatibilidade de tipos e operandos numéricos.
public class AnalisadorSemantico {

    // Tabela que guarda as variáveis declaradas pelo programa.
    private final TabelaDeSimbolos tabela = new TabelaDeSimbolos();

    // Lista que acumula todos os erros semânticos encontrados.
    private final List<String> erros = new ArrayList<>();

    // Percorre todos os comandos da AST e analisa cada um deles.
    public void analisar(Ast.Programa programa) {
        for (Ast.Comando comando : programa.comandos) {
            analisarComando(comando);
        }
    }

    // Permite que o Main acesse a tabela de símbolos preenchida.
    public TabelaDeSimbolos getTabela() {
        return tabela;
    }

    // Retorna a lista de erros semânticos sem permitir alterações externas.
    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }

    // Informa se foi encontrado pelo menos um erro semântico.
    public boolean temErros() {
        return !erros.isEmpty();
    }

    // Identifica o tipo do comando e chama a análise correspondente.
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
        throw new IllegalArgumentException(
                "Comando não suportado pelo analisador semântico."
        );
    }

    // Analisa declarações como: int idade = 20;
    private void analisarDeclaracao(Ast.Declaracao declaracao) {
        String nome = declaracao.nome.lexeme;
        Tipo tipoDeclarado = converterTipo(declaracao.tipo);

        // Impede a declaração duplicada da mesma variável.
        if (tabela.existe(nome)) {
            Simbolo anterior = tabela.buscar(nome);

            erro(
                    declaracao.nome,
                    "A variável '" + nome + "' já foi declarada na linha "
                            + anterior.getLinhaDeclaracao() + "."
            );

            // Ainda analisa o inicializador para encontrar outros erros.
            if (declaracao.inicializador != null) {
                inferirTipo(declaracao.inicializador);
            }

            return;
        }

        // Se houver inicializador, verifica se o tipo é compatível.
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

        // Registra a variável na tabela somente após as verificações.
        tabela.adicionar(
                new Simbolo(
                        nome,
                        tipoDeclarado,
                        "variável",
                        declaracao.nome.line
                )
        );
    }

    // Analisa atribuições como: idade = 20;
    private void analisarAtribuicao(Ast.Atribuicao atribuicao) {
        String nome = atribuicao.nome.lexeme;
        Simbolo destino = tabela.buscar(nome);
        Tipo tipoValor = inferirTipo(atribuicao.valor);

        // Verifica se a variável foi declarada antes de ser usada.
        if (destino == null) {
            erro(
                    atribuicao.nome,
                    "A variável '" + nome + "' não foi declarada."
            );

            return;
        }

        // Verifica se o tipo do valor é compatível com o tipo da variável.
        if (tipoValor != null
                && !atribuicaoCompativel(destino.getTipo(), tipoValor)) {
            erro(
                    atribuicao.nome,
                    "Não é possível atribuir um valor do tipo '"
                            + tipoValor.getNome() + "' à variável '" + nome
                            + "' do tipo '" + destino.getTipo().getNome() + "'."
            );
        }
    }

    // Descobre recursivamente o tipo de cada expressão da AST.
    // Retorna null quando já foi encontrado um erro na expressão.
    private Tipo inferirTipo(Ast.Expressao expressao) {
        // Números inteiros e decimais possuem tipos diferentes.
        if (expressao instanceof Ast.Numero) {
            Ast.Numero numero = (Ast.Numero) expressao;

            if (numero.token.type == TokenType.INT_LITERAL) {
                return Tipo.INT;
            }

            if (numero.token.type == TokenType.FLOAT_LITERAL) {
                return Tipo.FLOAT;
            }
        }

        // Reconhece strings, caracteres e valores booleanos.
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
                    erro(
                            literal.token,
                            "Literal não suportado pelo analisador semântico."
                    );

                    return null;
            }
        }

        // Consulta a tabela de símbolos para descobrir o tipo da variável.
        if (expressao instanceof Ast.Identificador) {
            Ast.Identificador identificador = (Ast.Identificador) expressao;
            Simbolo simbolo = tabela.buscar(identificador.nome.lexeme);

            if (simbolo == null) {
                erro(
                        identificador.nome,
                        "A variável '" + identificador.nome.lexeme
                                + "' não foi declarada."
                );

                return null;
            }

            return simbolo.getTipo();
        }

        // Analisa operações unárias, como -10 e +5.
        if (expressao instanceof Ast.Unaria) {
            Ast.Unaria unaria = (Ast.Unaria) expressao;
            Tipo tipoOperando = inferirTipo(unaria.direita);

            if (tipoOperando == null) {
                return null;
            }

            // O operador unário só aceita valores numéricos.
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

        // Analisa operações binárias, como 10 + 5 e 10 % 3.
        if (expressao instanceof Ast.Binaria) {
            Ast.Binaria binaria = (Ast.Binaria) expressao;
            Tipo tipoEsquerda = inferirTipo(binaria.esquerda);
            Tipo tipoDireita = inferirTipo(binaria.direita);

            // Evita gerar erros em cascata quando um operando já é inválido.
            if (tipoEsquerda == null || tipoDireita == null) {
                return null;
            }

            // Operações aritméticas exigem dois operandos numéricos.
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

            // O resultado recebe o tipo numérico de maior nível.
            return promover(tipoEsquerda, tipoDireita);
        }

        throw new IllegalArgumentException(
                "Expressão não suportada pelo analisador semântico."
        );
    }

    // Converte o token de tipo declarado para o enum Tipo.
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
                        "Tipo de declaração desconhecido: " + token.lexeme
                );
        }
    }

    // Verifica se é possível atribuir o tipo de origem ao tipo de destino.
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

    // Retorna o tipo numérico de maior nível entre os dois operandos.
    private Tipo promover(Tipo esquerda, Tipo direita) {
        return esquerda.getNivelNumerico() >= direita.getNivelNumerico()
                ? esquerda
                : direita;
    }

    // Cria uma mensagem de erro semântico com linha e coluna.
    private void erro(Token token, String mensagem) {
        erros.add(
                "[ERRO SEMÂNTICO] Linha " + token.line
                        + ", coluna " + token.column
                        + ": " + mensagem
        );
    }
}