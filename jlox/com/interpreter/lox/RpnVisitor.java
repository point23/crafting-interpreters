//package com.interpreter.lox;
//
//// @deprecated
//public class RpnVisitor implements Expr.Visitor<String> {
//
//    String print(Expr expr) {
//        return expr.accept(this);
//    }
//
//    private String format(String op, Expr... exprs) {
//        StringBuilder builder = new StringBuilder();
//        for (Expr expr : exprs) {
//            // This is where recursive happens
//            builder.append(" ").append(expr.accept(this));
//        }
//
//        builder.append(" ").append(op);
//        return builder.toString();
//    }
//
//    @Override
//    public String visitAssignExpr(Expr.Assign expr) {
//        return null;
//    }
//
//    @Override
//    public String visitBinaryExpr(Expr.Binary expr) {
//        // case: a + b ==> (+ a b)
//        return format(expr.operator.lexeme, expr.left, expr.right);
//    }
//
//    @Override
//    public String visitGroupingExpr(Expr.Grouping expr) {
//        return format("group", expr.expression);
//    }
//
//    // The "terminal" to the parenthesis method
//    @Override
//    public String visitLiteralExpr(Expr.Literal expr) {
//        if (expr.value == null) return "nil";
//        return expr.value.toString();
//    }
//
//    @Override
//    public String visitUnaryExpr(Expr.Unary expr) {
//        return format(expr.operator.lexeme, expr.right);
//    }
//
//    @Override
//    public String visitVariableExpr(Expr.Variable expr) {
//        return null;
//    }
//
//    public static void main(String[] args) {
//        // (1 + 2) * (4 - 3)
//        Expr left = new Expr.Binary(
//                new Expr.Literal(1),
//                new Token(TokenType.PLUS, "+", null, 1),
//                new Expr.Literal(2));
//        Expr right = new Expr.Binary(
//                new Expr.Literal(4),
//                new Token(TokenType.MINUS, "-", null, 1),
//                new Expr.Literal(3));
//        Expr multiply = new Expr.Binary(left, new Token(TokenType.STAR, "*", null, 1),right);
//
//        System.out.println(new RpnVisitor().print(multiply));
//    }
//}