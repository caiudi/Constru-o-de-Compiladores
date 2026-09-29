import java.util.HashMap;
import java.util.Map;

public class Scanner {

    private final String codigoFonte;
    private int cursor;
    private int linha;
    private int coluna;

    private static final Map<String, TokenType> PALAVRAS_RESERVADAS =
            new HashMap<>();

    static {
        PALAVRAS_RESERVADAS.put("int", TokenType.INT);
        PALAVRAS_RESERVADAS.put("double", TokenType.DOUBLE);
        PALAVRAS_RESERVADAS.put("bool", TokenType.BOOL);
        PALAVRAS_RESERVADAS.put("char", TokenType.CHAR);
        PALAVRAS_RESERVADAS.put("string", TokenType.STRING);
        PALAVRAS_RESERVADAS.put("void", TokenType.VOID);
        PALAVRAS_RESERVADAS.put("if", TokenType.IF);
        PALAVRAS_RESERVADAS.put("else", TokenType.ELSE);
        PALAVRAS_RESERVADAS.put("while", TokenType.WHILE);
        PALAVRAS_RESERVADAS.put("return", TokenType.RETURN);
        PALAVRAS_RESERVADAS.put("true", TokenType.TRUE);
        PALAVRAS_RESERVADAS.put("false", TokenType.FALSE);
    }

    public Scanner(String codigoFonte) {
        this.codigoFonte = codigoFonte;
        this.cursor = 0;
        this.linha = 1;
        this.coluna = 1;
    }

    // Verifica se ainda existem caracteres para serem lidos.
    public boolean hasNext() {
        return cursor < codigoFonte.length();
    }

    // Retorna o caractere atual sem consumi-lo.
    public char peek() {
        if (!hasNext()) {
            return '\0';
        }

        return codigoFonte.charAt(cursor);
    }

    // Retorna o próximo caractere sem consumi-lo.
    private char peekNext() {
        if (cursor + 1 >= codigoFonte.length()) {
            return '\0';
        }

        return codigoFonte.charAt(cursor + 1);
    }

    // Consome o caractere atual e avança o cursor.
    public char advance() {
        char atual = peek();

        if (!hasNext()) {
            return '\0';
        }

        cursor++;

        if (atual == '\n') {
            linha++;
            coluna = 1;
        } else {
            coluna++;
        }

        return atual;
    }

    public Token nextToken() {

        // Ignora espaços e comentários antes de procurar o próximo token.
        ignorarEspacosEComentarios();

        if (!hasNext()) {
            return new Token(TokenType.EOF, "", linha, coluna);
        }

        int linhaInicial = linha;
        int colunaInicial = coluna;

        char atual = peek();

        // AFD de identificadores e palavras reservadas.
        if (Character.isLetter(atual)) {
            return reconhecerIdentificador(linhaInicial, colunaInicial);
        }

        // AFD de números.
        if (Character.isDigit(atual)) {
            return reconhecerNumero(linhaInicial, colunaInicial);
        }

        // AFD de strings.
        if (atual == '"') {
            return reconhecerString(linhaInicial, colunaInicial);
        }

        // Operadores e delimitadores.
        switch (atual) {

            case '+':
                advance();
                return new Token(
                        TokenType.MAIS, "+",
                        linhaInicial, colunaInicial
                );

            case '-':
                advance();
                return new Token(
                        TokenType.MENOS, "-",
                        linhaInicial, colunaInicial
                );

            case '*':
                advance();
                return new Token(
                        TokenType.MULTIPLICACAO, "*",
                        linhaInicial, colunaInicial
                );

            case '/':
                advance();
                return new Token(
                        TokenType.DIVISAO, "/",
                        linhaInicial, colunaInicial
                );

            case '=':
                advance();

                if (peek() == '=') {
                    advance();
                    return new Token(
                            TokenType.IGUAL, "==",
                            linhaInicial, colunaInicial
                    );
                }

                return new Token(
                        TokenType.ATRIBUICAO, "=",
                        linhaInicial, colunaInicial
                );

            case '!':
                advance();

                if (peek() == '=') {
                    advance();
                    return new Token(
                            TokenType.DIFERENTE, "!=",
                            linhaInicial, colunaInicial
                    );
                }

                return new Token(
                        TokenType.NEGACAO, "!",
                        linhaInicial, colunaInicial
                );

            case '<':
                advance();

                if (peek() == '=') {
                    advance();
                    return new Token(
                            TokenType.MENOR_IGUAL, "<=",
                            linhaInicial, colunaInicial
                    );
                }

                return new Token(
                        TokenType.MENOR, "<",
                        linhaInicial, colunaInicial
                );

            case '>':
                advance();

                if (peek() == '=') {
                    advance();
                    return new Token(
                            TokenType.MAIOR_IGUAL, ">=",
                            linhaInicial, colunaInicial
                    );
                }

                return new Token(
                        TokenType.MAIOR, ">",
                        linhaInicial, colunaInicial
                );

            case '&':
                advance();

                if (peek() == '&') {
                    advance();
                    return new Token(
                            TokenType.E_LOGICO, "&&",
                            linhaInicial, colunaInicial
                    );
                }

                return erro("&", linhaInicial, colunaInicial);

            case '|':
                advance();

                if (peek() == '|') {
                    advance();
                    return new Token(
                            TokenType.OU_LOGICO, "||",
                            linhaInicial, colunaInicial
                    );
                }

                return erro("|", linhaInicial, colunaInicial);

            case '(':
                advance();
                return new Token(
                        TokenType.ABRE_PARENTESES, "(",
                        linhaInicial, colunaInicial
                );

            case ')':
                advance();
                return new Token(
                        TokenType.FECHA_PARENTESES, ")",
                        linhaInicial, colunaInicial
                );

            case '{':
                advance();
                return new Token(
                        TokenType.ABRE_CHAVES, "{",
                        linhaInicial, colunaInicial
                );

            case '}':
                advance();
                return new Token(
                        TokenType.FECHA_CHAVES, "}",
                        linhaInicial, colunaInicial
                );

            case ',':
                advance();
                return new Token(
                        TokenType.VIRGULA, ",",
                        linhaInicial, colunaInicial
                );

            case ';':
                advance();
                return new Token(
                        TokenType.PONTO_VIRGULA, ";",
                        linhaInicial, colunaInicial
                );

            default:
                String caractereInvalido = String.valueOf(advance());
                return erro(
                        caractereInvalido,
                        linhaInicial,
                        colunaInicial
                );
        }
    }

    /*
     * AFD para identificadores:
     *
     * q0 -- letra --> q1
     * q1 -- letra/digito/_ --> q1
     *
     * q1 e estado de aceitacao.
     */
    private Token reconhecerIdentificador(
            int linhaInicial,
            int colunaInicial) {

        StringBuilder lexema = new StringBuilder();

        // q0 -> q1
        lexema.append(advance());

        // Estado q1
        while (hasNext()) {

            char atual = peek();

            if (Character.isLetterOrDigit(atual) || atual == '_') {
                lexema.append(advance());
            } else {
                break;
            }
        }

        String texto = lexema.toString();

        // Verifica o limite definido na especificacao.
        if (texto.length() > 64) {
            return erro(texto, linhaInicial, colunaInicial);
        }

        TokenType tipo = PALAVRAS_RESERVADAS.get(texto);

        if (tipo == null) {
            tipo = TokenType.IDENTIFICADOR;
        }

        return new Token(
                tipo,
                texto,
                linhaInicial,
                colunaInicial
        );
    }

    /*
     * AFD para numeros:
     *
     * q0 -- digito --> q1
     * q1 -- digito --> q1
     * q1 -- . --> q2
     * q2 -- digito --> q3
     * q3 -- digito --> q3
     */
    private Token reconhecerNumero(
            int linhaInicial,
            int colunaInicial) {

        StringBuilder lexema = new StringBuilder();

        // Estado q1: parte inteira.
        while (hasNext() && Character.isDigit(peek())) {
            lexema.append(advance());
        }

        // Verifica se existe parte decimal.
        if (peek() == '.' && Character.isDigit(peekNext())) {

            // q1 -> q2
            lexema.append(advance());

            // q2 -> q3
            while (hasNext() && Character.isDigit(peek())) {
                lexema.append(advance());
            }
        }

        return new Token(
                TokenType.NUMERO,
                lexema.toString(),
                linhaInicial,
                colunaInicial
        );
    }

    /*
     * AFD para strings.
     */
    private Token reconhecerString(
        int linhaInicial,
        int colunaInicial) {

    StringBuilder lexema = new StringBuilder();

    // Estado inicial: consome as aspas de abertura
    lexema.append(advance());

    while (hasNext()) {

        char atual = peek();

        // Estado de aceitação: encontrou as aspas de fechamento
        if (atual == '"') {

            lexema.append(advance());

            return new Token(
                    TokenType.STRING_LITERAL,
                    lexema.toString(),
                    linhaInicial,
                    colunaInicial
            );
        }

        // A linguagem não permite string em mais de uma linha
        if (atual == '\n') {

            System.err.println(
                    "Erro lexico na linha "
                    + linhaInicial
                    + ", coluna "
                    + colunaInicial
                    + ": string nao fechada."
            );

            return new Token(
                    TokenType.ERRO,
                    lexema.toString(),
                    linhaInicial,
                    colunaInicial
            );
        }

        // Tratamento das sequências de escape
        if (atual == '\\') {

            lexema.append(advance());

            if (!hasNext()) {
                break;
            }

            char escape = peek();

            if (escape == '"' ||
                escape == '\\' ||
                escape == 'n' ||
                escape == 't') {

                lexema.append(advance());

            } else {

                lexema.append(advance());

                System.err.println(
                        "Erro lexico na linha "
                        + linhaInicial
                        + ", coluna "
                        + colunaInicial
                        + ": sequencia de escape invalida."
                );

                return new Token(
                        TokenType.ERRO,
                        lexema.toString(),
                        linhaInicial,
                        colunaInicial
                );
            }

        } else {
            lexema.append(advance());
        }
    }

    // EOF antes das aspas de fechamento
    System.err.println(
            "Erro lexico na linha "
            + linhaInicial
            + ", coluna "
            + colunaInicial
            + ": string nao fechada antes do EOF."
    );

    return new Token(
            TokenType.ERRO,
            lexema.toString(),
            linhaInicial,
            colunaInicial
    );
}
        // EOF antes de fechar a string.
        return erro(
                lexema.toString(),
                linhaInicial,
                colunaInicial
        );
    }

    private Token ignorarEspacosEComentarios() {

    boolean continuar = true;

    while (continuar && hasNext()) {

        continuar = false;

        // Ignora espaços, tabs e quebras de linha
        while (hasNext() && Character.isWhitespace(peek())) {
            advance();
        }

        // Comentário de uma linha
        if (peek() == '/' && peekNext() == '/') {

            while (hasNext() && peek() != '\n') {
                advance();
            }

            continuar = true;
        }

        // Comentário de bloco
        else if (peek() == '/' && peekNext() == '*') {

            int linhaInicial = linha;
            int colunaInicial = coluna;

            advance(); // /
            advance(); // *

            boolean fechado = false;

            while (hasNext()) {

                if (peek() == '*' && peekNext() == '/') {

                    advance(); // *
                    advance(); // /

                    fechado = true;
                    continuar = true;
                    break;
                }

                advance();
            }

            // Chegou ao EOF sem encontrar */
            if (!fechado) {

                System.err.println(
                        "Erro lexico na linha "
                        + linhaInicial
                        + ", coluna "
                        + colunaInicial
                        + ": comentario de bloco nao fechado."
                );

                return new Token(
                        TokenType.ERRO,
                        "/*",
                        linhaInicial,
                        colunaInicial
                );
            }
        }
    }

    return null;
}
    private Token erro(
            String lexema,
            int linhaErro,
            int colunaErro) {

        System.err.println(
                "Erro lexico na linha "
                + linhaErro
                + ", coluna "
                + colunaErro
                + ": "
                + lexema
        );

        return new Token(
                TokenType.ERRO,
                lexema,
                linhaErro,
                colunaErro
        );
    }
}
