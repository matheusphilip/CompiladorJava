import java.util.List;

// Representa a Árvore Sintática Abstrata (AST) do programa.
// Cada classe interna representa um nó diferente da árvore.
public class Ast {

    // Nó raiz da AST. Guarda todos os comandos analisados.
    public static class Programa {
        public final List<Comando> comandos;

        public Programa(List<Comando> comandos) {
            this.comandos = comandos;
        }

        // Converte a AST completa para o formato JSON.
        public String paraJson() {
            StringBuilder json = new StringBuilder();
            json.append("{\n  \"programa\": [\n");

            for (int i = 0; i < comandos.size(); i++) {
                json.append(comandos.get(i).paraJson(4));

                // Adiciona vírgula entre os comandos do JSON.
                if (i < comandos.size() - 1) {
                    json.append(",");
                }

                json.append("\n");
            }

            json.append("  ]\n}");
            return json.toString();
        }

        // Converte a AST completa para o formato DOT.
        // O DOT pode ser usado para gerar uma imagem do grafo.
        public String paraDot() {
            StringBuilder dot = new StringBuilder();
            dot.append("digraph AST {\n");
            dot.append("  node [shape=box, fontname=\"Arial\"];\n");
            dot.append("  raiz [label=\"Programa\"];\n");

            int contador = 1;

            // Gera um nó DOT para cada comando do programa.
            for (Comando comando : comandos) {
                contador = comando.paraDot(dot, "raiz", contador);
            }

            dot.append("}\n");
            return dot.toString();
        }
    }

    // Classe base para declarações e atribuições.
    public static abstract class Comando {
        public abstract String paraJson(int indentacao);
        public abstract int paraDot(StringBuilder dot, String pai, int contador);
    }

    // Representa uma declaração de variável.
    // Exemplos: int idade; e int idade = 20;
    public static class Declaracao extends Comando {
        public final Token tipo;
        public final Token nome;
        public final Expressao inicializador;

        public Declaracao(Token tipo, Token nome, Expressao inicializador) {
            this.tipo = tipo;
            this.nome = nome;
            this.inicializador = inicializador;
        }

        // Gera o JSON da declaração.
        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            StringBuilder json = new StringBuilder();

            json.append(espacos).append("{\n");
            json.append(espacos).append("  \"tipo\": \"Declaracao\",\n");
            json.append(espacos).append("  \"tipoVariavel\": \"")
                    .append(tipo.lexeme).append("\",\n");
            json.append(espacos).append("  \"nome\": \"")
                    .append(nome.lexeme).append("\",\n");
            json.append(espacos).append("  \"linha\": ").append(nome.line).append(",\n");
            json.append(espacos).append("  \"inicializador\": ");

            // Uma declaração pode não ter valor inicial.
            if (inicializador == null) {
                json.append("null");
            } else {
                json.append(inicializador.paraJson(indentacao + 2));
            }

            json.append("\n").append(espacos).append("}");
            return json.toString();
        }

        // Gera o nó DOT da declaração.
        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Declaração\\n")
                    .append(tipo.lexeme).append(" ").append(nome.lexeme)
                    .append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            // Se existir inicializador, ele se torna filho da declaração.
            if (inicializador != null) {
                contador = inicializador.paraDot(dot, no, contador);
            }

            return contador;
        }
    }

    // Representa uma atribuição de valor a uma variável.
    // Exemplo: idade = 20;
    public static class Atribuicao extends Comando {
        public final Token nome;
        public final Expressao valor;

        public Atribuicao(Token nome, Expressao valor) {
            this.nome = nome;
            this.valor = valor;
        }

        // Gera o JSON da atribuição.
        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            StringBuilder json = new StringBuilder();

            json.append(espacos).append("{\n");
            json.append(espacos).append("  \"tipo\": \"Atribuicao\",\n");
            json.append(espacos).append("  \"variavel\": \"")
                    .append(nome.lexeme).append("\",\n");
            json.append(espacos).append("  \"linha\": ").append(nome.line).append(",\n");
            json.append(espacos).append("  \"valor\": ");
            json.append(valor.paraJson(indentacao + 2));
            json.append("\n").append(espacos).append("}");

            return json.toString();
        }

        // Gera o nó DOT da atribuição.
        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Atribuição\\n")
                    .append(nome.lexeme).append(" =\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            // A expressão atribuída se torna filha da atribuição.
            return valor.paraDot(dot, no, contador);
        }
    }

    // Classe base para todas as expressões.
    public static abstract class Expressao {
        public abstract String paraJson(int indentacao);
        public abstract int paraDot(StringBuilder dot, String pai, int contador);
    }

    // Representa operações com dois operandos.
    // Exemplos: 10 + 5, 10 * 2 e 10 % 3.
    public static class Binaria extends Expressao {
        public final Expressao esquerda;
        public final Token operador;
        public final Expressao direita;

        public Binaria(Expressao esquerda, Token operador, Expressao direita) {
            this.esquerda = esquerda;
            this.operador = operador;
            this.direita = direita;
        }

        // Gera o JSON da operação binária.
        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            StringBuilder json = new StringBuilder();

            json.append("{\n");
            json.append(espacos).append("  \"tipo\": \"Binaria\",\n");
            json.append(espacos).append("  \"operador\": \"")
                    .append(operador.lexeme).append("\",\n");
            json.append(espacos).append("  \"esquerda\": ");
            json.append(esquerda.paraJson(indentacao + 2));
            json.append(",\n");
            json.append(espacos).append("  \"direita\": ");
            json.append(direita.paraJson(indentacao + 2));
            json.append("\n").append(espacos).append("}");

            return json.toString();
        }

        // Gera o nó DOT da operação binária.
        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Operador\\n")
                    .append(operador.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            // Os dois operandos se tornam filhos do operador.
            contador = esquerda.paraDot(dot, no, contador);
            contador = direita.paraDot(dot, no, contador);

            return contador;
        }
    }

    // Representa operações com apenas um operando.
    // Exemplos: -10 e +5.
    public static class Unaria extends Expressao {
        public final Token operador;
        public final Expressao direita;

        public Unaria(Token operador, Expressao direita) {
            this.operador = operador;
            this.direita = direita;
        }

        // Gera o JSON da operação unária.
        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            StringBuilder json = new StringBuilder();

            json.append("{\n");
            json.append(espacos).append("  \"tipo\": \"Unaria\",\n");
            json.append(espacos).append("  \"operador\": \"")
                    .append(operador.lexeme).append("\",\n");
            json.append(espacos).append("  \"operando\": ");
            json.append(direita.paraJson(indentacao + 2));
            json.append("\n").append(espacos).append("}");

            return json.toString();
        }

        // Gera o nó DOT da operação unária.
        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Operador unário\\n")
                    .append(operador.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            // O operando se torna filho do operador unário.
            return direita.paraDot(dot, no, contador);
        }
    }

    // Representa números inteiros e decimais.
    public static class Numero extends Expressao {
        public final Token token;

        public Numero(Token token) {
            this.token = token;
        }

        // Gera o JSON do número.
        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);

            return "{\n"
                    + espacos + "  \"tipo\": \"Numero\",\n"
                    + espacos + "  \"valor\": \"" + token.lexeme + "\"\n"
                    + espacos + "}";
        }

        // Gera o nó DOT do número.
        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Número\\n")
                    .append(token.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            return contador;
        }
    }

    // Representa strings, caracteres e valores booleanos.
    public static class Literal extends Expressao {
        public final Token token;

        public Literal(Token token) {
            this.token = token;
        }

        // Gera o JSON do literal.
        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);

            return "{\n"
                    + espacos + "  \"tipo\": \"Literal\",\n"
                    + espacos + "  \"valor\": \"" + token.lexeme + "\"\n"
                    + espacos + "}";
        }

        // Gera o nó DOT do literal.
        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Literal\\n")
                    .append(token.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            return contador;
        }
    }

    // Representa o uso de uma variável dentro de uma expressão.
    public static class Identificador extends Expressao {
        public final Token nome;

        public Identificador(Token nome) {
            this.nome = nome;
        }

        // Gera o JSON do identificador.
        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);

            return "{\n"
                    + espacos + "  \"tipo\": \"Identificador\",\n"
                    + espacos + "  \"nome\": \"" + nome.lexeme + "\"\n"
                    + espacos + "}";
        }

        // Gera o nó DOT do identificador.
        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Identificador\\n")
                    .append(nome.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            return contador;
        }
    }
}