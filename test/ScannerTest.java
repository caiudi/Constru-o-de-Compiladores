public class ScannerTest {

    public static void main(String[] args) {

        testarTokensValidos();
        testarStringNaoFechadaEOF();
        testarStringNaoFechadaFimLinha();
        testarCaractereInvalido();
        testarCodigoRealista();

        System.out.println("\nTodos os testes foram executados com sucesso!");
    }
  
    // Teste 1
    // Casos validos das cinco categorias de token

    private static void testarTokensValidos() {

        System.out.println("Teste 1 - Tokens validos");

        String codigo =
                "idade int \"Marcelo\" >= 18";

        Scanner scanner = new Scanner(codigo);

        verificar(scanner.nextToken(),
                TokenType.IDENTIFICADOR,
                "idade");

        verificar(scanner.nextToken(),
                TokenType.INT,
                "int");

        verificar(scanner.nextToken(),
                TokenType.STRING_LITERAL,
                "\"Marcelo\"");

        verificar(scanner.nextToken(),
                TokenType.MAIOR_IGUAL,
                ">=");

        verificar(scanner.nextToken(),
                TokenType.NUMERO,
                "18");

        verificar(scanner.nextToken(),
                TokenType.EOF,
                "");

        System.out.println("OK\n");
    }

    // Teste 2
    // String nao fechada ate EOF
  
    private static void testarStringNaoFechadaEOF() {

        System.out.println(
                "Teste 2 - String nao fechada ate EOF"
        );

        Scanner scanner =
                new Scanner("\"texto sem fechar");

        Token token = scanner.nextToken();

        assert token.getTipo() == TokenType.ERRO :
                "Era esperado um erro lexico.";

        System.out.println("OK\n");
    }

    // Teste 3
    // String nao fechada ate o fim da linha
  
    private static void testarStringNaoFechadaFimLinha() {

        System.out.println(
                "Teste 3 - String nao fechada no fim da linha"
        );

        Scanner scanner =
                new Scanner("\"texto sem fechar\nint idade;");

        Token token = scanner.nextToken();

        assert token.getTipo() == TokenType.ERRO :
                "Era esperado um erro lexico.";

        System.out.println("OK\n");
    }

    // Teste 4
    // Caractere fora do alfabeto da linguagem
  
    private static void testarCaractereInvalido() {

        System.out.println(
                "Teste 4 - Caractere invalido"
        );

        Scanner scanner =
                new Scanner("@");

        Token token = scanner.nextToken();

        assert token.getTipo() == TokenType.ERRO :
                "Era esperado erro para o caractere @.";

        assert token.getLinha() == 1 :
                "Linha incorreta.";

        assert token.getColuna() == 1 :
                "Coluna incorreta.";

        System.out.println("OK\n");
    }

    // Teste 5
    // Codigo realista com espacos e comentarios

    private static void testarCodigoRealista() {

        System.out.println(
                "Teste 5 - Codigo realista"
        );

        String codigo =
                "int idade = 18; // idade da pessoa\n"
              + "double altura = 1.75; "
              + "/* verificacao */ "
              + "if (idade >= 18) { return true; }";

        Scanner scanner = new Scanner(codigo);

        TokenType[] esperados = {

                TokenType.INT,
                TokenType.IDENTIFICADOR,
                TokenType.ATRIBUICAO,
                TokenType.NUMERO,
                TokenType.PONTO_VIRGULA,

                TokenType.DOUBLE,
                TokenType.IDENTIFICADOR,
                TokenType.ATRIBUICAO,
                TokenType.NUMERO,
                TokenType.PONTO_VIRGULA,

                TokenType.IF,
                TokenType.ABRE_PARENTESES,
                TokenType.IDENTIFICADOR,
                TokenType.MAIOR_IGUAL,
                TokenType.NUMERO,
                TokenType.FECHA_PARENTESES,
                TokenType.ABRE_CHAVES,

                TokenType.RETURN,
                TokenType.TRUE,
                TokenType.PONTO_VIRGULA,

                TokenType.FECHA_CHAVES,
                TokenType.EOF
        };

        for (TokenType esperado : esperados) {

            Token token = scanner.nextToken();

            assert token.getTipo() == esperado :
                    "Esperado "
                    + esperado
                    + ", mas encontrado "
                    + token.getTipo()
                    + " no lexema "
                    + token.getLexema();
        }

        System.out.println("OK\n");
    }
  
    // Metodo auxiliar
 
    private static void verificar(
            Token token,
            TokenType tipoEsperado,
            String lexemaEsperado) {

        assert token.getTipo() == tipoEsperado :
                "Tipo esperado: "
                + tipoEsperado
                + " | encontrado: "
                + token.getTipo();

        assert token.getLexema().equals(lexemaEsperado) :
                "Lexema esperado: "
                + lexemaEsperado
                + " | encontrado: "
                + token.getLexema();
    }
}
