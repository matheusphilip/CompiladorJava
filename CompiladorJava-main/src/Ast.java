import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Representa a Árvore Sintática Abstrata (AST).
public final class Ast {

    private Ast() {
    }

    // Representa o programa, composto por vários comandos.
    public static final class Programa {
        public final List<Comando> comandos;

        public Programa(List<Comando> comandos) {
            // Cria uma cópia da lista e impede modificações.
            this.comandos = Collections.unmodifiableList(
                    new ArrayList<>(comandos)
            );
        }
    }

    // Interface para todos os tipos de comandos.
    public interface Comando {
    }

    // Interface para todos os tipos de expressões.
    public interface Expressao {
    }

    // Representa a declaração de uma variável.
    public static final class Declaracao implements Comando {
        public final Token tipo;
        public final Token nome;
        public final Expressao inicializador;

        public Declaracao(Token tipo, Token nome, Expressao inicializador) {
            this.tipo = tipo;
            this.nome = nome;
            this.inicializador = inicializador;
        }
    }

    // Representa a atribuição de um valor a uma variável.
    public static final class Atribuicao implements Comando {
        public final Token nome;
        public final Expressao valor;

        public Atribuicao(Token nome, Expressao valor) {
            this.nome = nome;
            this.valor = valor;
        }
    }

    // Representa uma operação com dois operandos, como 10 + 20.
    public static final class Binaria implements Expressao {
        public final Expressao esquerda;
        public final Token operador;
        public final Expressao direita;

        public Binaria(
                Expressao esquerda,
                Token operador,
                Expressao direita
        ) {
            this.esquerda = esquerda;
            this.operador = operador;
            this.direita = direita;
        }
    }

    // Representa o nome de uma variável usada em uma expressão.
    public static final class Identificador implements Expressao {
        public final Token nome;

        public Identificador(Token nome) {
            this.nome = nome;
        }
    }

    // Representa um valor numérico literal, como 10 ou 25.
    public static final class Numero implements Expressao {
        public final Token token;

        public Numero(Token token) {
            this.token = token;
        }
    }
}