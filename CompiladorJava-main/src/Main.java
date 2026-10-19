import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    // Ponto de entrada do programa.
    public static void main(String[] args) {

        // Usa o arquivo informado nos argumentos ou o arquivo padrão.
        String arquivo = args.length > 0
                ? args[0]
                : "exemplos/entrada.java";

        try {
            // Obtém o caminho do arquivo.
            Path caminho = Paths.get(arquivo);

            // Lê o código-fonte usando a codificação UTF-8.
            String codigo = new String(
                    Files.readAllBytes(caminho),
                    StandardCharsets.UTF_8
            );

            System.out.println("--- ARQUIVO ---");
            System.out.println(caminho.toAbsolutePath());

            System.out.println("\n--- CÓDIGO FONTE ---");
            System.out.println(codigo);

            // Realiza a análise léxica e identifica os tokens.
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.scanTokens();

            System.out.println("\n--- TOKENS ENCONTRADOS ---");

            for (Token token : tokens) {
                System.out.println(token);
            }

            // Realiza a análise sintática e constrói a AST.
            Parser parser = new Parser(tokens);
            Ast.Programa programa = parser.parse();

            System.out.println("\n--- ANÁLISE SINTÁTICA ---");
            System.out.println("Análise concluída sem erros sintáticos.");

            // Imprime a Árvore Sintática Abstrata.
            System.out.println("\n--- ÁRVORE SINTÁTICA ABSTRATA ---");

            AstPrinter printer = new AstPrinter();
            printer.imprimir(programa);

        } catch (ParseError e) {
            // Exibe erros encontrados durante a análise sintática.
            System.err.println(e.getMessage());

        } catch (IOException e) {
            // Trata erros ao tentar ler o arquivo.
            System.err.println(
                    "[ERRO DE LEITURA] Não foi possível ler o arquivo: "
                            + arquivo
            );

            System.err.println(e.getMessage());

        } catch (InvalidPathException e) {
            // Trata caminhos de arquivo inválidos.
            System.err.println(
                    "[ERRO DE CAMINHO] Caminho de arquivo inválido: "
                            + arquivo
            );
        }
    }
}