// Representa os tipos suportados pelo analisador semântico.
// O nível numérico é usado para permitir conversões automáticas,
// como atribuir um int a uma variável float ou double.
public enum Tipo {

    INT("int", 1),
    FLOAT("float", 2),
    DOUBLE("double", 3),
    STRING("String", 0),
    CHAR("char", 0),
    BOOLEAN("boolean", 0);

    // Nome que será exibido nas mensagens e na tabela de símbolos.
    private final String nome;

    // Nível usado para comparar tipos numéricos.
    private final int nivelNumerico;

    Tipo(String nome, int nivelNumerico) {
        this.nome = nome;
        this.nivelNumerico = nivelNumerico;
    }

    // Retorna o nome do tipo.
    public String getNome() {
        return nome;
    }

    // Retorna o nível numérico do tipo.
    public int getNivelNumerico() {
        return nivelNumerico;
    }

    // Informa se o tipo é numérico.
    public boolean ehNumerico() {
        return this == INT || this == FLOAT || this == DOUBLE;
    }
}