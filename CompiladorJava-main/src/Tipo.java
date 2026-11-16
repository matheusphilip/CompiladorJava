// Tipos suportados pela análise semântica simplificada do projeto.
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

    public int getNivelNumerico() {
        return nivelNumerico;
    }

    public boolean ehNumerico() {
        return nivelNumerico > 0;
    }

    // Converte o token de tipo da declaração para o tipo interno do compilador.
    public static Tipo deToken(TokenType token) {
        switch (token) {
            case KW_INT: return INT;
            case KW_FLOAT: return FLOAT;
            case KW_DOUBLE: return DOUBLE;
            case KW_STRING: return STRING;
            case KW_CHAR: return CHAR;
            case KW_BOOLEAN: return BOOLEAN;
            default:
                throw new IllegalArgumentException(
                        "Tipo não suportado pela análise semântica: " + token
                );
        }
    }
}
