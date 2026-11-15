import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// Classe principal do analisador.
// Executa a análise léxica, sintática e semântica.
public class Main {

    public static void main(String[] args) {
        // Usa o arquivo padrão caso nenhum caminho seja informado.
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

            // 1. Análise léxica: transforma o código em tokens.
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.scanTokens();

            System.out.println("\n--- TOKENS ENCONTRADOS ---");

            for (Token token : tokens) {
                System.out.println(token);
            }

            // 2. Análise sintática: transforma os tokens em AST.
            Parser parser = new Parser(tokens);
            Ast.Programa programa = parser.parse();

            System.out.println("\n--- ANÁLISE SINTÁTICA ---");
            System.out.println("Análise concluída sem erros sintáticos.");

            System.out.println("\n--- ÁRVORE SINTÁTICA ABSTRATA ---");
            AstPrinter printer = new AstPrinter();
            printer.imprimir(programa);

            // 3. Análise semântica: valida variáveis e tipos.
            AnalisadorSemantico semantico = new AnalisadorSemantico();
            semantico.analisar(programa);

            System.out.println("\n--- ANÁLISE SEMÂNTICA ---");
            semantico.getTabela().imprimir();

            if (semantico.temErros()) {
                for (String erro : semantico.getErros()) {
                    System.err.println(erro);
                }

                System.err.println("Análise semântica concluída com erros.");
            } else {
                System.out.println("Análise semântica concluída sem erros.");
            }

            // 4. Exporta a AST nos formatos JSON e DOT.
            System.out.println("\n--- AST EM JSON ---");
            System.out.println(programa.paraJson());

            Files.writeString(Path.of("ast.dot"), programa.paraDot());
            Files.writeString(Path.of("ast.json"), programa.paraJson());

            System.out.println("\nArquivos 'ast.dot' e 'ast.json' gerados com sucesso.");

        } catch (ParseError e) {
            System.err.println(e.getMessage());

        } catch (IOException e) {
            System.err.println(
                    "[ERRO DE LEITURA] Não foi possível ler o arquivo: " + arquivo
            );
            System.err.println(e.getMessage());

        } catch (InvalidPathException e) {
            System.err.println(
                    "[ERRO DE CAMINHO] Caminho de arquivo inválido: " + arquivo
            );
        }
    }
}