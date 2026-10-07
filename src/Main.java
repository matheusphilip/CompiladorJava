import java.util.List;

public class Main {
    public static void main(String[] args) {
        String codigo = "int x = 10;\nif(x == 10) {\nx = x + 1;\n}";

        System.out.println("--- CÓDIGO FONTE ---");
        System.out.println(codigo);
        System.out.println("\n--- TOKENS ENCONTRADOS ---");

        Lexer lexer = new Lexer(codigo);
        List<Token> tokens = lexer.scanTokens();

        for(Token token : tokens) {
            System.out.println(token);
        }
    }
}