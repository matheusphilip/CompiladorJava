import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Representa a Árvore Sintática Abstrata (AST) do programa.
public class Ast {

    public static class Programa {
        public final List<Comando> comandos;

        public Programa(List<Comando> comandos) {
            this.comandos = comandos;
        }

        public String paraJson() {
            StringBuilder json = new StringBuilder();
            json.append("{\n  \"programa\": [\n");

            for (int i = 0; i < comandos.size(); i++) {
                json.append(comandos.get(i).paraJson(4));
                if (i < comandos.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }

            json.append("  ]\n}");
            return json.toString();
        }

        public String paraDot() {
            StringBuilder dot = new StringBuilder();
            dot.append("digraph AST {\n");
            dot.append("  node [shape=box, fontname=\"Arial\"];\n");
            dot.append("  raiz [label=\"Programa\"];\n");

            int contador = 1;
            for (Comando comando : comandos) {
                contador = comando.paraDot(dot, "raiz", contador);
            }

            dot.append("}\n");
            return dot.toString();
        }
    }

    public static abstract class Comando {
        public abstract String paraJson(int indentacao);
        public abstract int paraDot(StringBuilder dot, String pai, int contador);
    }

    public static class Declaracao extends Comando {
        public final Token tipo;
        public final Token nome;
        public final Expressao inicializador;

        public Declaracao(Token tipo, Token nome, Expressao inicializador) {
            this.tipo = tipo;
            this.nome = nome;
            this.inicializador = inicializador;
        }

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

            if (inicializador == null) {
                json.append("null");
            } else {
                json.append(inicializador.paraJson(indentacao + 2));
            }

            json.append("\n").append(espacos).append("}");
            return json.toString();
        }

        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Declaração\\n")
                    .append(tipo.lexeme).append(" ").append(nome.lexeme)
                    .append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            if (inicializador != null) {
                contador = inicializador.paraDot(dot, no, contador);
            }
            return contador;
        }
    }

    public static class Atribuicao extends Comando {
        public final Token nome;
        public final Expressao valor;

        public Atribuicao(Token nome, Expressao valor) {
            this.nome = nome;
            this.valor = valor;
        }

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

        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Atribuição\\n")
                    .append(nome.lexeme).append(" =\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");
            return valor.paraDot(dot, no, contador);
        }
    }

    // Representa uma chamada encadeada, como System.out.println("Olá").
    public static class ChamadaMetodo extends Comando {
        public final List<Token> caminho;
        public final List<Expressao> argumentos;

        public ChamadaMetodo(List<Token> caminho, List<Expressao> argumentos) {
            this.caminho = new ArrayList<>(caminho);
            this.argumentos = new ArrayList<>(argumentos);
        }

        public String nomeCompleto() {
            return caminho.stream()
                    .map(token -> token.lexeme)
                    .collect(Collectors.joining("."));
        }

        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            StringBuilder json = new StringBuilder();
            json.append(espacos).append("{\n");
            json.append(espacos).append("  \"tipo\": \"ChamadaMetodo\",\n");
            json.append(espacos).append("  \"alvo\": \"")
                    .append(nomeCompleto()).append("\",\n");
            json.append(espacos).append("  \"linha\": ")
                    .append(caminho.get(0).line).append(",\n");
            json.append(espacos).append("  \"argumentos\": [");

            if (!argumentos.isEmpty()) {
                json.append("\n");
                for (int i = 0; i < argumentos.size(); i++) {
                    json.append(" ".repeat(indentacao + 4))
                            .append(argumentos.get(i).paraJson(indentacao + 4));
                    if (i < argumentos.size() - 1) {
                        json.append(",");
                    }
                    json.append("\n");
                }
                json.append(espacos).append("  ");
            }

            json.append("]\n").append(espacos).append("}");
            return json.toString();
        }

        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Chamada de método\\n")
                    .append(nomeCompleto()).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");

            for (Expressao argumento : argumentos) {
                contador = argumento.paraDot(dot, no, contador);
            }
            return contador;
        }
    }

    public static abstract class Expressao {
        public abstract String paraJson(int indentacao);
        public abstract int paraDot(StringBuilder dot, String pai, int contador);
    }

    public static class Binaria extends Expressao {
        public final Expressao esquerda;
        public final Token operador;
        public final Expressao direita;

        public Binaria(Expressao esquerda, Token operador, Expressao direita) {
            this.esquerda = esquerda;
            this.operador = operador;
            this.direita = direita;
        }

        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append(espacos).append("  \"tipo\": \"Binaria\",\n");
            json.append(espacos).append("  \"operador\": \"")
                    .append(operador.lexeme).append("\",\n");
            json.append(espacos).append("  \"esquerda\": ")
                    .append(esquerda.paraJson(indentacao + 2)).append(",\n");
            json.append(espacos).append("  \"direita\": ")
                    .append(direita.paraJson(indentacao + 2));
            json.append("\n").append(espacos).append("}");
            return json.toString();
        }

        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Operador\\n")
                    .append(operador.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");
            contador = esquerda.paraDot(dot, no, contador);
            return direita.paraDot(dot, no, contador);
        }
    }

    public static class Unaria extends Expressao {
        public final Token operador;
        public final Expressao direita;

        public Unaria(Token operador, Expressao direita) {
            this.operador = operador;
            this.direita = direita;
        }

        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append(espacos).append("  \"tipo\": \"Unaria\",\n");
            json.append(espacos).append("  \"operador\": \"")
                    .append(operador.lexeme).append("\",\n");
            json.append(espacos).append("  \"operando\": ")
                    .append(direita.paraJson(indentacao + 2));
            json.append("\n").append(espacos).append("}");
            return json.toString();
        }

        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Operador unário\\n")
                    .append(operador.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");
            return direita.paraDot(dot, no, contador);
        }
    }

    public static class Numero extends Expressao {
        public final Token token;
        public Numero(Token token) { this.token = token; }

        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            return "{\n" + espacos + "  \"tipo\": \"Numero\",\n"
                    + espacos + "  \"valor\": \"" + token.lexeme + "\"\n"
                    + espacos + "}";
        }

        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Número\\n")
                    .append(token.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");
            return contador;
        }
    }

    public static class Literal extends Expressao {
        public final Token token;
        public Literal(Token token) { this.token = token; }

        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            return "{\n" + espacos + "  \"tipo\": \"Literal\",\n"
                    + espacos + "  \"valor\": \"" + token.lexeme + "\"\n"
                    + espacos + "}";
        }

        @Override
        public int paraDot(StringBuilder dot, String pai, int contador) {
            String no = "no" + contador++;
            dot.append("  ").append(no).append(" [label=\"Literal\\n")
                    .append(token.lexeme).append("\"];\n");
            dot.append("  ").append(pai).append(" -> ").append(no).append(";\n");
            return contador;
        }
    }

    public static class Identificador extends Expressao {
        public final Token nome;
        public Identificador(Token nome) { this.nome = nome; }

        @Override
        public String paraJson(int indentacao) {
            String espacos = " ".repeat(indentacao);
            return "{\n" + espacos + "  \"tipo\": \"Identificador\",\n"
                    + espacos + "  \"nome\": \"" + nome.lexeme + "\"\n"
                    + espacos + "}";
        }

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
