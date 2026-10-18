import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String arquivo = args.length > 0
                ? args[0]
                : "exemplos/entrada.java";

        try {
            Path caminho = Paths.get(arquivo);

            String codigo = new String(
                    Files.readAllBytes(caminho),
                    StandardCharsets.UTF_8
            );

            System.out.println("--- ARQUIVO ---");
            System.out.println(caminho.toAbsolutePath());

            System.out.println("\n--- CÓDIGO FONTE ---");
            System.out.println(codigo);

            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.scanTokens();

            System.out.println("\n--- TOKENS ENCONTRADOS ---");

            for (Token token : tokens) {
                System.out.println(token);
            }

            Parser parser = new Parser(tokens);
            Ast.Programa programa = parser.parse();

            System.out.println("\n--- ANÁLISE SINTÁTICA ---");
            System.out.println("Análise concluída sem erros sintáticos.");

            System.out.println("\n--- ÁRVORE SINTÁTICA ABSTRATA ---");

            AstPrinter printer = new AstPrinter();
            printer.imprimir(programa);

        } catch (ParseError e) {
            System.err.println(e.getMessage());

        } catch (IOException e) {
            System.err.println(
                    "[ERRO DE LEITURA] Não foi possível ler o arquivo: "
                            + arquivo
            );

            System.err.println(e.getMessage());

        } catch (InvalidPathException e) {
            System.err.println(
                    "[ERRO DE CAMINHO] Caminho de arquivo inválido: "
                            + arquivo
            );
        }
    }
}