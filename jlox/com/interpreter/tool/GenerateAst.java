package com.interpreter.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output dir>");
            System.exit(64);
        }
        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
                "Assign(Token name, Expr value)",                   // Assignment -> IDENTIFIER "= assignment | equality.
                "Binary(Expr left, Token operator, Expr right)",    //
                "Grouping(Expr expression)",
                "Literal(Object value)",
                "Logical(Expr left, Token operator, Expr right)",
                "Unary(Token operator, Expr right)",
                "Variable(Token name)"
        ));

        defineAst(outputDir,"Stmt", Arrays.asList(
                "Block(List<Stmt> statements)",                         // Block
                "Expression(Expr expression)",                          // ExprStatement
                "If(Expr condition, Stmt thenBranch, Stmt elseBranch)", // IfStatement
                "Print(Expr expression)",                               // PrintStatement
                "Var(Token name, Expr initializer)",                    // VarDeclaration
                "While(Expr condition, Stmt body)"                      // WhileStatement
        ));
    }

    private static void defineAst(String dir, String base, List<String> constructors)
        throws IOException{

        StringBuilder builder = new StringBuilder();

        // Create package, import dependencies...
        builder.append("package com.interpreter.lox;\n");
        builder.append("import java.util.List;\n\n");

        // Declaration starts here
        builder.append("abstract class ").append(base).append("{\n");

        // Build generic visitor interface
        defineVisitor(builder, base, constructors);

        // Build abstract accept method
        builder.append("\tabstract <R> R accept(Visitor<R> visitor);\n\n");

        // Build each non-terminal classes
        for (String constructor : constructors) {
            String type = constructor.split("\\(")[0];
            String fields = constructor.substring(type.length());
            defineType(builder, base, type, fields);
        }

        // Close declaration of base
        builder.append("}");

        // Write to file
        String path = dir + "/" + base + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        String content = builder.toString();
        writer.print(content);
        writer.close();
    }

    private static void defineType(StringBuilder builder, String base, String type, String fields) {
        builder.append("\tstatic class ").append(type).append(" extends ").append(base).append(" {\n");

        // Drop the parentheses and split it by comma
        String[] slots = fields.substring(1, fields.length() - 1).split(", ");

        // Add fields
        //      case: final Token operator;
        for(String item : slots) {
            builder.append("\t\tfinal ").append(item).append(";\n");
        }

        // Add constructor
        //      case: Unary(Token operator, Expr right) {
        builder.append("\t\t").append(type).append(fields).append(" {\n");
        for(String item : slots) {
            String name = item.split(" ")[1];

            //      case: this.operator = operator;
            builder.append("\t\t\tthis.").append(name).append(" = ").append(name).append(";\n");
        }
        builder.append("\t\t}\n");

        // Override accept method.
        builder.append("\n\t\t@Override\n");
        builder.append("\t\t<R> R accept(Visitor<R> visitor) {\n");
        //      case: return visitor.visitUnaryExpr(this);
        builder.append("\t\t\treturn visitor.visit").append(type).append(base).append("(this);\n");
        builder.append("\t\t}\n");

        // Close declaration of derived class
        builder.append("\t}\n\n");
    }

    private static void defineVisitor(StringBuilder builder, String base, List<String> constructors) {
        builder.append("\tinterface Visitor<R> {\n");

        for (String constructor : constructors) {
            String type = constructor.split("\\(")[0];

            // Add each visit *function*
            //      case: R visitUnaryExpr(Unary expr) {}
            builder.append("\t\tR visit").append(type).append(base).append("(")
                    .append(type).append(" ").append(base.toLowerCase()).append(");\n");

        }

        builder.append("\t}\n\n");
    }
}
