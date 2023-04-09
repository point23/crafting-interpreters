package com.interpreter.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

// debug-build-args: "A:\code\interpreters\jlox\data\test.jlox"
/* @incomplete:
	- [1] Comma expression 						# int x = 1, y = 2, z = 3;
	- [2] Conditional operator ("ternary")		# int x = condition ? 1 : 2;
	- [3] Error Production: binary operator that appearing at the beginning of an expr  # *2 == 2
	- [4] Divide by zero
	- [5] If left operand is a string, we shall take forms like # "ruby" + 3 ==> "ruby3"
	- [6] Compare other types
	- [7] Redeclaration of a value: var a = 1; var a;
 */

public class Lox {
	private static final Interpreter interpreter = new Interpreter();

	static boolean debugShowAST;
	static boolean debugShowTokens;

	static boolean hadError;
	static boolean hadRuntimeError;

    public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
    }

	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		if (hadError) System.exit(65);
		if (hadRuntimeError) System.exit(70);
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (;;) {
			System.out.print("jlox> ");
			String line = reader.readLine();
			if (line == null) break;
			run(line);

			// @note If user made a mistake, we won't kill the entire session.
			hadError = false;
		}
	}

	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();

//		if (debugShowTokens) {
//			System.out.println("TOKENS: \n========================");
//			for (Token token : tokens) {
//				System.out.println(token.toString());
//			}
//			System.out.println("========================");
//		}

		Parser parser = new Parser(tokens);
		List<Stmt> statements = parser.parse();

		if (hadError) return;

		interpreter.interpret(statements);
	}

	// @todo A Error-Reporter is needed.
	static void error(int line, String message) {
		report(line, "", message);
	}

	static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
			report(token.line, " at end", message);
		} else {
			report(token.line, " at '" + token.lexeme + "' ", message);
		}
	}

	static void runtimeError(RuntimeError e) {
		System.out.println("\nRUNTIME ERROR: " + e.getMessage() + " [line " + e.token.line + "]");
	}

	private static void report(int line, String where, String message) {
		System.err.println("[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}
}