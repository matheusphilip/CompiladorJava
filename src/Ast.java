import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Ast {

    private Ast() {
    }

    public static final class Programa {
        public final List<Comando> comandos;

        public Programa(List<Comando> comandos) {
            this.comandos = Collections.unmodifiableList(
                    new ArrayList<>(comandos)
            );
        }
    }

    public interface Comando {
    }

    public interface Expressao {
    }

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

    public static final class Atribuicao implements Comando {
        public final Token nome;
        public final Expressao valor;

        public Atribuicao(Token nome, Expressao valor) {
            this.nome = nome;
            this.valor = valor;
        }
    }

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

    public static final class Identificador implements Expressao {
        public final Token nome;

        public Identificador(Token nome) {
            this.nome = nome;
        }
    }

    public static final class Numero implements Expressao {
        public final Token token;

        public Numero(Token token) {
            this.token = token;
        }
    }
}