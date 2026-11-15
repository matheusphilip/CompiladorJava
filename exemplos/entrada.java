package teste.compilador;

import java.util.*;

// Comentário de linha simples para testar quebra de linha e coluna
/* Comentário em bloco
   ocupando múltiplas linhas */

@Deprecated
public abstract class TesteCompleto extends Object implements Runnable {

    private static final int CONSTANTE = 42;
    protected volatile double decimal = 3.14;
    transient float flutuante = 2.5;
    byte b = 1;
    short s = 2;
    long l = 1000;
    boolean flag = true;
    String texto = "Teste com \"aspas escapadas\" e \\barra\\";
    char letra = 'A';
    char quebra = '\n';
    char aspaSimples = '\'';
    Object nulo = null;
    int _variavel$Valida = 0;

    public synchronized strictfp void executar(String[] args) throws Exception {
        int x = 10;
        x++;
        x--;
        x += 5;
        x -= 2;
        x *= 3;
        x /= 2;
        x %= 4;

        // Operadores relacionais, lógicos e ternário
        boolean condicao = (x == 10 || x != 0) && !(x < 5 || x <= 3 || x > 20 || x >= 15);
        int ternario = condicao ? 1 : 0;

        // Operadores bitwise e shift
        int bits = (5 & 3) | (5 ^ 1) + (~2);
        bits &= 7;
        bits |= 1;
        bits ^= 2;
        bits = (bits << 2) >> 1;
        bits = bits >>> 1;
        bits <<= 1;
        bits >>= 1;
        bits >>>= 1;

        // Estruturas de controle
        if (this instanceof TesteCompleto) {
            for (int i = 0; i < 10; i++) {
                if (i == 3) continue;
                if (i == 8) break;
            }
        } else {
            while (flag) {
                do {
                    flag = false;
                } while (false);
            }
        }

        switch (x) {
            case 1:
                break;
            default:
                return;
        }

        // Exceções, asserções e referências Java 8+
        try {
            assert x >= 0;
            Runnable r = () -> System.out.println("Lambda");
            Runnable ref = super::toString;
            if (nulo == null) {
                throw new RuntimeException("Erro");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Reservadas não usadas do Java
            // const goto
        }
    }

    native void metodoNativo();
}

interface MinhaInterface {}
enum MeuEnum { OPCAO_A, OPCAO_B }