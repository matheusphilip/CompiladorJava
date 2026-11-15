public enum Tipo {
    INT("int", 1),
    FLOAT("float", 2),
    DOUBLE("double", 3),
    STRING("String", 0),
    CHAR("char", 0),
    BOOLEAN("boolean", 0);

    private final String nome;
    private final int nivelNumerico;

    Tipo(String nome, int nivelNumerico) {
        this.nome = nome;
        this.nivelNumerico = nivelNumerico;
    }

    public String getNome() {
        return nome;
    }

    public boolean ehNumerico() {
        return nivelNumerico > 0;
    }

    public int getNivelNumerico() {
        return nivelNumerico;
    }
}