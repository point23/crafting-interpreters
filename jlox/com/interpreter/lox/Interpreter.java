package com.interpreter.lox;

public class Interpreter implements Expr.Visitor<Object>{

    void interpret(Expr expr) {
        try {
            Object value = evaluate(expr);
            System.out.println(stringify(value));
        } catch(RuntimeError e) {
            Lox.runtimeError(e);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        Token operator = expr.operator;

        switch(operator.type) {
            // Comparison
            case GREATER:
                checkNumberOperands(operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(operator, left, right);
                return (double)left <= (double)right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            // Arithmetic
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                } else if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
            case MINUS:
                checkNumberOperands(operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(operator, left, right);
                return (double)left * (double)right;
        }

        return null;
    }


    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }

        return null;
    }

    // Helper methods
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (Boolean) obj;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;

        return left.equals(right);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    private void checkNumberOperands(Token operator, Object operand1, Object operand2) {
        if (operand1 instanceof Double && operand2 instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers");
    }

    private String stringify(Object obj) {
        if (obj == null) return "Nil";
        if (obj instanceof Double) {
            String number = obj.toString();
            if (number.endsWith(".0")) {
                number = number.substring(0, number.length() - 2);
            }

            return number;
        }
        return obj.toString();
    }
}
