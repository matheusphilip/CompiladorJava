import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

// Classe principal: executa todas as etapas disponíveis e apresenta
// diagnósticos sem interromper o processamento após erros sintáticos recuperáveis.
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

            // 1. Lexer: gera tokens e registra ocorrências de identificadores.
            Lexer lexer = new Lexer(codigo);
            List<Token> tokens = lexer.scanTokens();
            TabelaDeSimbolos tabela = lexer.getTabelaDeSimbolos();

            System.out.println("\n--- TOKENS ENCONTRADOS ---");
            for (Token token : tokens) {
                System.out.println(token);
            }

            // 2. Parser: constrói a AST, marca declarações válidas na tabela
            // e acumula erros sintáticos, tentando recuperar o restante do arquivo.
            Parser parser = new Parser(tokens, tabela);
            Ast.Programa programa = parser.parse();

            System.out.println("\n--- ANÁLISE SINTÁTICA ---");
            if (parser.getErros().isEmpty()) {
                System.out.println("Análise concluída sem erros sintáticos.");
            } else {
                System.out.println(
                        "Foram encontrados " + parser.getErros().size()
                                + " erro(s) sintático(s). O parser recuperou "
                                + "o que foi possível e continuará com as próximas etapas."
                );

                // Use stdout para manter a ordem visual no console do IntelliJ.
                // Misturar stdout e stderr pode deslocar os diagnósticos para
                // depois de outras etapas, dando a impressão de que pertencem
                // à tabela de símbolos.
                for (String erro : parser.getErros()) {
                    System.out.println(erro);
                }
            }

            // A AST pode ser parcial: contém os comandos aceitos após recuperação.
            System.out.println("\n--- ÁRVORE SINTÁTICA ABSTRATA RECUPERADA ---");
            AstPrinter printer = new AstPrinter();
            printer.imprimir(programa);

            // A tabela contém ocorrências lexicais e declarações reconhecidas
            // pelo Parser. Identificadores sem declaração válida aparecem sem tipo.
            System.out.println("\n--- TABELA DE SÍMBOLOS ---");
            tabela.imprimir();

            // 3. Semântico: valida os comandos que sobreviveram à recuperação.
            AnalisadorSemantico semantico = new AnalisadorSemantico(tabela);
            semantico.analisar(programa);

            System.out.println("\n--- ANÁLISE SEMÂNTICA ---");
            if (semantico.temErros()) {
                for (String erro : semantico.getErros()) {
                    System.out.println(erro);
                }
                System.out.println(
                        "Análise semântica concluída com "
                                + semantico.getErros().size() + " erro(s)."
                );
            } else {
                System.out.println("Nenhum erro semântico encontrado nos comandos recuperados.");
            }

            // 4. Exporta a AST recuperada para JSON e DOT.
            System.out.println("\n--- AST EM JSON ---");
            System.out.println(programa.paraJson());

            Files.writeString(Path.of("ast.dot"), programa.paraDot());
            Files.writeString(Path.of("ast.json"), programa.paraJson());
            System.out.println("\nArquivos 'ast.dot' e 'ast.json' gerados com sucesso.");

            // Resultado geral: erros não impedem as etapas restantes, mas
            // o programa analisado não deve ser considerado livre de erros.
            if (!parser.getErros().isEmpty() || semantico.temErros()) {
                System.out.println(
                        "\nRESULTADO: análise concluída com erros. "
                                + "Consulte os diagnósticos acima."
                );
            } else {
                System.out.println("\nRESULTADO: análise concluída sem erros.");
            }

        } catch (ParseError e) {
            // Reservado a erros não recuperáveis ou incompatibilidades estruturais.
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
