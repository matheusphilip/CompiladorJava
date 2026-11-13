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

    // Representa uma declaração de variável.
    public static final class Declaracao implements Comando {
        public final Token tipo;
        public final Token nome;
        public final Expressao inicializador;

        public Declaracao(
                Token tipo,
                Token nome,
                Expressao inicializador
        ) {
            this.tipo = tipo;
            this.nome = nome;
            this.inicializador = inicializador;
        }
    }

    // Representa uma atribuição.
    public static final class Atribuicao implements Comando {
        public final Token nome;
        public final Expressao valor;

        public Atribuicao(Token nome, Expressao valor) {
            this.nome = nome;
            this.valor = valor;
        }
    }

    // Representa uma operação com dois operandos.
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

    // Representa uma operação com um operando, como -10.
    public static final class Unaria implements Expressao {
        public final Token operador;
        public final Expressao direita;

        public Unaria(Token operador, Expressao direita) {
            this.operador = operador;
            this.direita = direita;
        }
    }

    // Representa o uso de uma variável em uma expressão.
    public static final class Identificador implements Expressao {
        public final Token nome;

        public Identificador(Token nome) {
            this.nome = nome;
        }
    }

    // Representa números inteiros ou decimais.
    public static final class Numero implements Expressao {
        public final Token token;

        public Numero(Token token) {
            this.token = token;
        }
    }

    // Representa strings, caracteres e valores booleanos.
    // O token preserva o tipo para a futura análise semântica.
    public static final class Literal implements Expressao {
        public final Token token;

        public Literal(Token token) {
            this.token = token;
        }
    }
}