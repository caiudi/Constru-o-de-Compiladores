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

    // Verifica se ainda existem caracteres para leitura
    public boolean hasNext() {
        return cursor < codigoFonte.length();
    }

    // Retorna o caractere atual sem consumi-lo
    public char peek() {
        if (!hasNext()) {
            return '\0';
        }

        return codigoFonte.charAt(cursor);
    }

    // Retorna o próximo caractere sem consumi-lo
    private char peekNext() {
        if (cursor + 1 >= codigoFonte.length()) {
            return '\0';
        }

        return codigoFonte.charAt(cursor + 1);
    }

    // Consome o caractere atual
    public char advance() {

        if (!hasNext()) {
            return '\0';
        }

        char atual = codigoFonte.charAt(cursor);
        cursor++;

        if (atual == '\n') {
            linha++;
            coluna = 1;
        } else {
            coluna++;
        }

        return atual;
    }

    // Retorna o próximo token encontrado
    public Token nextToken() {

        // Ignora espaços e comentários.
        // Caso exista erro em comentário, retorna o token de erro.
        Token erroComentario = ignorarEspacosEComentarios();

        if (erroComentario != null) {
            return erroComentario;
        }

        // Fim do arquivo
        if (!hasNext()) {
            return new Token(
                    TokenType.EOF,
                    "",
                    linha,
                    coluna
            );
        }

        int linhaInicial = linha;
        int colunaInicial = coluna;

        char atual = peek();

        // Identificador ou palavra reservada
        if (Character.isLetter(atual)) {
            return reconhecerIdentificador(
                    linhaInicial,
                    colunaInicial
            );
        }

        // Literal numérico
        if (Character.isDigit(atual)) {
            return reconhecerNumero(
                    linhaInicial,
                    colunaInicial
            );
        }

        // String
        if (atual == '"') {
            return reconhecerString(
                    linhaInicial,
                    colunaInicial
            );
        }

        // Operadores e delimitadores
        switch (atual) {

            case '+':
                advance();

                return new Token(
                        TokenType.MAIS,
                        "+",
                        linhaInicial,
                        colunaInicial
                );

            case '-':
                advance();

                return new Token(
                        TokenType.MENOS,
                        "-",
                        linhaInicial,
                        colunaInicial
                );

            case '*':
                advance();

                return new Token(
                        TokenType.MULTIPLICACAO,
                        "*",
                        linhaInicial,
                        colunaInicial
                );

            case '/':
                advance();

                return new Token(
                        TokenType.DIVISAO,
                        "/",
                        linhaInicial,
                        colunaInicial
                );

            case '=':
                advance();

                // Maximal munch: tenta reconhecer ==
                if (peek() == '=') {
                    advance();

                    return new Token(
                            TokenType.IGUAL,
                            "==",
                            linhaInicial,
                            colunaInicial
                    );
                }

                return new Token(
                        TokenType.ATRIBUICAO,
                        "=",
                        linhaInicial,
                        colunaInicial
                );

            case '!':
                advance();

                // Maximal munch: tenta reconhecer !=
                if (peek() == '=') {
                    advance();

                    return new Token(
                            TokenType.DIFERENTE,
                            "!=",
                            linhaInicial,
                            colunaInicial
                    );
                }

                return new Token(
                        TokenType.NEGACAO,
                        "!",
                        linhaInicial,
                        colunaInicial
                );

            case '<':
                advance();

                // Maximal munch: tenta reconhecer <=
                if (peek() == '=') {
                    advance();

                    return new Token(
                            TokenType.MENOR_IGUAL,
                            "<=",
                            linhaInicial,
                            colunaInicial
                    );
                }

                return new Token(
                        TokenType.MENOR,
                        "<",
                        linhaInicial,
                        colunaInicial
                );

            case '>':
                advance();

                // Maximal munch: tenta reconhecer >=
                if (peek() == '=') {
                    advance();

                    return new Token(
                            TokenType.MAIOR_IGUAL,
                            ">=",
                            linhaInicial,
                            colunaInicial
                    );
                }

                return new Token(
                        TokenType.MAIOR,
                        ">",
                        linhaInicial,
                        colunaInicial
                );

            case '&':
                advance();

                // && é válido, & sozinho não é
                if (peek() == '&') {
                    advance();

                    return new Token(
                            TokenType.E_LOGICO,
                            "&&",
                            linhaInicial,
                            colunaInicial
                    );
                }

                return erro(
                        "&",
                        linhaInicial,
                        colunaInicial,
                        "operador invalido"
                );

            case '|':
                advance();

                // || é válido, | sozinho não é
                if (peek() == '|') {
                    advance();

                    return new Token(
                            TokenType.OU_LOGICO,
                            "||",
                            linhaInicial,
                            colunaInicial
                    );
                }

                return erro(
                        "|",
                        linhaInicial,
                        colunaInicial,
                        "operador invalido"
                );

            case '(':
                advance();

                return new Token(
                        TokenType.ABRE_PARENTESES,
                        "(",
                        linhaInicial,
                        colunaInicial
                );

            case ')':
                advance();

                return new Token(
                        TokenType.FECHA_PARENTESES,
                        ")",
                        linhaInicial,
                        colunaInicial
                );

            case '{':
                advance();

                return new Token(
                        TokenType.ABRE_CHAVES,
                        "{",
                        linhaInicial,
                        colunaInicial
                );

            case '}':
                advance();

                return new Token(
                        TokenType.FECHA_CHAVES,
                        "}",
                        linhaInicial,
                        colunaInicial
                );

            case ',':
                advance();

                return new Token(
                        TokenType.VIRGULA,
                        ",",
                        linhaInicial,
                        colunaInicial
                );

            case ';':
                advance();

                return new Token(
                        TokenType.PONTO_VIRGULA,
                        ";",
                        linhaInicial,
                        colunaInicial
                );

            default:

                // Caractere que não pertence ao alfabeto
                String caractereInvalido =
                        String.valueOf(advance());

                return erro(
                        caractereInvalido,
                        linhaInicial,
                        colunaInicial,
                        "caractere invalido"
                );
        }
    }

    /*
     * AFD DE IDENTIFICADOR
     *
     * q0 -- letra --> q1
     * q1 -- letra/digito/_ --> q1
     *
     * q1 = estado de aceitacao
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

            if (Character.isLetterOrDigit(atual)
                    || atual == '_') {

                lexema.append(advance());

            } else {
                break;
            }
        }

        String texto = lexema.toString();

        // Limite definido na especificação
        if (texto.length() > 64) {

            return erro(
                    texto,
                    linhaInicial,
                    colunaInicial,
                    "identificador possui mais de 64 caracteres"
            );
        }

        // Verifica se o identificador é uma palavra reservada
        TokenType tipo =
                PALAVRAS_RESERVADAS.get(texto);

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
     * AFD DE NUMERO
     *
     * q0 -- digito --> q1
     * q1 -- digito --> q1
     * q1 -- . --> q2
     * q2 -- digito --> q3
     * q3 -- digito --> q3
     *
     * q1 e q3 = estados de aceitacao
     */
    private Token reconhecerNumero(
            int linhaInicial,
            int colunaInicial) {

        StringBuilder lexema = new StringBuilder();

        // Estado q1: parte inteira
        while (hasNext()
                && Character.isDigit(peek())) {

            lexema.append(advance());
        }

        // Verifica se existe uma parte decimal
        if (peek() == '.'
                && Character.isDigit(peekNext())) {

            // q1 -> q2
            lexema.append(advance());

            // q2 -> q3
            while (hasNext()
                    && Character.isDigit(peek())) {

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
     * AFD DE STRING
     *
     * q0 -- " --> q1
     * q1 -- caractere valido --> q1
     * q1 -- \ --> qEscape
     * qEscape -- escape valido --> q1
     * q1 -- " --> q2
     *
     * q2 = estado de aceitacao
     */
    private Token reconhecerString(
            int linhaInicial,
            int colunaInicial) {

        StringBuilder lexema =
                new StringBuilder();

        // q0 -> q1
        // Consome as aspas de abertura
        lexema.append(advance());

        while (hasNext()) {

            char atual = peek();

            // q1 -> q2
            // Encontrou as aspas de fechamento
            if (atual == '"') {

                lexema.append(advance());

                return new Token(
                        TokenType.STRING_LITERAL,
                        lexema.toString(),
                        linhaInicial,
                        colunaInicial
                );
            }

            // Strings não podem ocupar mais de uma linha
            if (atual == '\n') {

                return erro(
                        lexema.toString(),
                        linhaInicial,
                        colunaInicial,
                        "string nao fechada antes do fim da linha"
                );
            }

            // Estado de escape
            if (atual == '\\') {

                lexema.append(advance());

                // EOF logo após a barra invertida
                if (!hasNext()) {

                    return erro(
                            lexema.toString(),
                            linhaInicial,
                            colunaInicial,
                            "string nao fechada antes do EOF"
                    );
                }

                char escape = peek();

                // Escapes permitidos pela especificação
                if (escape == '"'
                        || escape == '\\'
                        || escape == 'n'
                        || escape == 't') {

                    lexema.append(advance());

                } else {

                    lexema.append(advance());

                    return erro(
                            lexema.toString(),
                            linhaInicial,
                            colunaInicial,
                            "sequencia de escape invalida"
                    );
                }

            } else {

                // Caractere comum da string
                lexema.append(advance());
            }
        }

        // EOF antes de encontrar as aspas finais
        return erro(
                lexema.toString(),
                linhaInicial,
                colunaInicial,
                "string nao fechada antes do EOF"
        );
    }

    /*
     * Ignora:
     *
     * - espaços
     * - tabulações
     * - quebras de linha
     * - comentários //
     * - comentários de bloco
     *
     * Caso um comentário de bloco não seja fechado,
     * retorna um Token de erro.
     */
    private Token ignorarEspacosEComentarios() {

        boolean continuar = true;

        while (continuar && hasNext()) {

            continuar = false;

            // Espaços em branco
            while (hasNext()
                    && Character.isWhitespace(peek())) {

                advance();
            }

            // Comentário de uma linha
            if (peek() == '/'
                    && peekNext() == '/') {

                // Consome //
                advance();
                advance();

                while (hasNext()
                        && peek() != '\n') {

                    advance();
                }

                continuar = true;
            }

            // Comentário de bloco
            else if (peek() == '/'
                    && peekNext() == '*') {

                int linhaInicial = linha;
                int colunaInicial = coluna;

                // Consome /*
                advance();
                advance();

                boolean fechado = false;

                while (hasNext()) {

                    if (peek() == '*'
                            && peekNext() == '/') {

                        // Consome */
                        advance();
                        advance();

                        fechado = true;
                        continuar = true;

                        break;
                    }

                    advance();
                }

                // EOF antes do fechamento */
                if (!fechado) {

                    return erro(
                            "/*",
                            linhaInicial,
                            colunaInicial,
                            "comentario de bloco nao fechado antes do EOF"
                    );
                }
            }
        }

        return null;
    }

    /*
     * Tratamento padrão dos erros léxicos.
     */
    private Token erro(
            String lexema,
            int linhaErro,
            int colunaErro,
            String mensagem) {

        System.err.println(
                "Erro lexico na linha "
                        + linhaErro
                        + ", coluna "
                        + colunaErro
                        + ": "
                        + mensagem
                        + " ["
                        + lexema
                        + "]"
        );

        return new Token(
                TokenType.ERRO,
                lexema,
                linhaErro,
                colunaErro
        );
    }
}
