package org.gcalc.gcalc_lib;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;

/**
 * A superset of the contained Expression class, which can handle equalities. Attempts to algebraically simplify the Equation when it is constructed, so that as little variables as possible are needed to evaluate the equation.
 */
public class Equation {
    private Expression rhs;
    boolean isEmpty;

    /**
     * Parses either an expression (no = in the string) or an equation, and produces an intermediate representation which can be easily evaluated.
     *
     * @param rawEquation String representing the expression or equation
     * @throws InvalidParameterException if the equation is malformed
     */
    public Equation(String rawEquation) throws InvalidParameterException {
        rawEquation = rawEquation.replaceAll(" ", "");
        if(rawEquation.isEmpty()) {
            this.isEmpty = true;
            return;
        }

        String[] equationParts = rawEquation.split("=");

        if(equationParts.length > 2) throw new InvalidParameterException("Equation must not contain multiple equalities");

        if(!rawEquation.contains("="))
            // We assume that if no equality is specified, that the entire
            // expression is equal to y
            this.rhs = new Expression(rawEquation);
        else {
            // If an equality is specified, we need to make sure that the
            // equation is expressed in terms of y, so that the evaluate()
            // method works properly (it's rather naive)
            equationParts[1] = this.rearrange(equationParts[0], equationParts[1]);
            equationParts[0] = "y";
        }
    }

    /**
     * Finds all y values which satisfy the equation for a given x value. The returned array contains a value for each root of the equation. Each array position is guaranteed to represent the same root for all x values.
     *
     * A NaN value means that that root does not exist for the specified x value.
     *
     * @param x The x value to insert into the equation
     * @return Array of roots - each root is either a valid number, or NaN
     */
    public double[] evaluate(double x) {
        if(this.isEmpty) return new double[] { Double.NaN };
        else {
            Map<String, Double> args = new HashMap<>();
            args.put("x", x);

            return this.rhs.evaluate(args);
        }
    }

    /**
     * Retrieves the expression that is evaluated by evaluate()
     *
     * @return The right hand side of the Equation
     */
    public Expression getRightHandSide() {
        return this.rhs;
    }

    /**
     * Rearranges an equation to be expressed in terms of y.
     *
     * @param lhs Expression on left side of equals sign
     * @param rhs Expression on right side of equals sign
     * @return The expression on the right side of the rearranged equation's equals sign (left side is implied to be `y=`)
     */
    private String rearrange(String lhs, String rhs) {
        return null;
    }

    /**
     * Simple subset of the Equation type, which cannot contain equalities. Expressions can be evaluated, however, by supplying values for any variables in the expression.
     */
    public static class Expression {
        /**
         * String which was passed to the constructor
         */
        protected String rawExpression;

        /**
         * Operation stack which is executed sequentially, and nested Expressions called recursively, when the Expression is evaluated
         */
        protected ArrayList<Instruction> ops = new ArrayList<>();

        /**
         * Creates a parsed Expression which is prepared for evaluation.
         *
         * @param rawExpression String representing the expression to parse
         * @throws InvalidParameterException if the expression is malformed
         */
        public Expression(String rawExpression) throws InvalidParameterException {
            this.rawExpression = rawExpression.replaceAll(" ", "") // Eases parsing by removing whitespace
                    .replaceAll("-\\++", "-") // Simplify equivalent expressions:
                    .replaceAll("/\\++", "/").replaceAll("\\+\\++", "+").replaceAll("\\*\\++", "+");

            this.parseRecursive();
        }

        /**
         * Unwinds the operation stack recursively to help debug expression parsing.
         *
         * @return String representation of an Expression's call stack
         */
        @Override
        public String toString() {
            String ret = "Expression \"" + this.rawExpression + "\" {\n";

            int stackN = 0;
            for(Instruction i : this.ops) {
                ret += "    " + Integer.toString(stackN) + ": ";

                switch(i.instruction) {
                case ADD:
                    ret += "ADD";
                    break;
                case SUB:
                    ret += "SUB";
                    break;
                case MUL:
                    ret += "MUL";
                    break;
                case DIV:
                    ret += "DIV";
                    break;
                case FACT:
                    ret += "FACT";
                    break;
                case PLUSMINUS:
                    ret += "EVALBOTH_ADD_SUB";
                    break;
                case NATIVEFUNC:
                    ret += "NATIVEFUNC " + i.arg;
                    break;
                case EXPR:
                    ret += "EXPR => " + i.arg.toString().replaceAll("\n", "\n    ");
                    break;
                case PUSH:
                    ret += "PUSH " + Double.toString((Double) i.arg);
                    break;
                case PUSHVAR:
                    ret += "PUSHVAR " + i.arg;
                    break;
                }

                ret += "\n";
                stackN++;
            }

            return ret + "}";
        }

        /**
         * Runs down the operation list, performing each instruction in sequence using a stack machine. `PUSHVAR` instructions lookup keys in the vars map, and an exception is thrown if an operation references an undefined variable.
         *
         * Occasionally, an expression contains multiple roots. In this case, one result is returned for each, and the same position in the array is guaranteed to correlate to the same root between invocations.
         *
         * @param vars The variable values to use to evaluate the expression
         * @return A list of results, one for each root of the expression
         */
        public double[] evaluate(Map<String, Double> vars) throws IndexOutOfBoundsException {
            // TODO add support for multiple return values
            ArrayList<Double> stack = new ArrayList<>();

            // Used for instructions where order of operands matters
            double n1, n2;

            for(Instruction i : this.ops) {
                switch(i.instruction) {
                case ADD:
                    push(stack, pop(stack) + pop(stack));
                    break;
                case SUB:
                    n1 = pop(stack);
                    n2 = pop(stack);
                    push(stack, n2 - n1);
                    break;
                case MUL:
                    push(stack, pop(stack) * pop(stack));
                    break;
                case DIV:
                    n1 = pop(stack);
                    n2 = pop(stack);
                    push(stack, n2 / n1);
                    break;
                case FACT:
                    push(stack, Factorial.fact(pop(stack)));
                    break;
                case PLUSMINUS:
                    throw new NotImplementedException();
                case NATIVEFUNC:
                    push(stack, this.nativeFnInvoke(i, stack));
                    break;
                case EXPR:
                    push(stack, ((Expression) i.arg).evaluate(vars)[0]);
                    break;
                case PUSH:
                    push(stack, (double) i.arg);
                    break;
                case PUSHVAR:
                    push(stack, vars.get(i.arg));
                    break;
                }
            }

            return new double[] { pop(stack) };
        }

        /**
         * Recursively parses this.rawExpression, creating new Expressions for each bracketed region. Ensures that the rules of operator precedence are obeyed, and allows for some algebraic shorthand (such as 2x resulting in 2*x with a higher operator precedence than anything else).
         */
        protected void parseRecursive() {
            // Use a shorter name, since we use this value everywhere
            String raw = this.rawExpression;
            // Used to construct number literals to parse for PUSH ops
            StringBuilder literalBuilder = new StringBuilder();
            // Determine whether to read a - sign as part of a literal
            boolean lastCharWasOper = false;
            // Determine if a bracketed region is part of a function call
            Instruction fnInst = null;
            // Operation stack used to store lower-precedence operators
            ArrayList<Character> opStack = new ArrayList<>();
            // Used to prevent certain problems, such as multiplying a variable
            // with nothing if it is the first value in the expression
            boolean firstIter = true;

            parseLoop: for(int i = 0; i < raw.length(); i++) {
                char r = raw.charAt(i);

                // Read literal number and then push a push operation to the
                // operation stack which loads that number
                if(lastCharWasOper && r == '-' || r >= '0' && r <= '9' || r == '.') {
                    literalBuilder.append(r);

                    // Prevents [num]+-[num] forcing an immediate ADD instruction
                    if(r == '-') continue;

                    // If we reach the end of the string, we still need to push
                    // the number
                    if(i == raw.length() - 1) ops.add(new Instruction(Instruction.InstType.PUSH, Double.parseDouble(literalBuilder.toString())));
                }
                else {
                    // We're no longer reading a number
                    if(literalBuilder.length() > 0) ops.add(new Instruction(Instruction.InstType.PUSH, Double.parseDouble(literalBuilder.toString())));
                    literalBuilder = new StringBuilder();
                }

                // The factorial operator is a unary operator, meaning that it
                // has no operator precedence. Therefore, we just push it
                if(r == '!') {
                    ops.add(new Instruction(Instruction.InstType.FACT, null));
                }

                // Check to see if the next operation is a function (i.e. a
                // trigonometric operation) using a string lookahead
                fnInst = this.nativeFnLookahead(raw.substring(i));
                if(fnInst != null) {
                    // Increment character pointer and fetch new character
                    i += ((String) fnInst.arg).length();
                    r = raw.charAt(i);
                }
                else if(r >= 'a' && r <= 'z') {
                    // Probably a variable name, so we'll try to push it
                    ops.add(new Instruction(Instruction.InstType.PUSHVAR, String.valueOf(r)));

                    // If the variable immediately follows a literal or another
                    // variable, we multiply them
                    if(!lastCharWasOper && !firstIter) ops.add(new Instruction(Instruction.InstType.MUL, null));
                }

                // Search for end of enclosed region, then create an Expression
                // from it
                if(r == '(') {
                    int parenLoc = i;
                    int depth = 0;

                    // Continue outer string character iterator
                    for(; i < raw.length(); i++) {
                        char c = raw.charAt(i);

                        // Find end of initial region
                        if(c == '(') depth++;
                        else if(c == ')') depth--;

                        if(depth == 0) {
                            // Create expression from bracketed contents
                            String subExpr = raw.substring(parenLoc + 1, i);
                            ops.add(new Instruction(Instruction.InstType.EXPR, new Expression(subExpr)));

                            if(fnInst != null) {
                                // Push function call if the bracketed region
                                // was an argument to the function
                                ops.add(fnInst);
                                fnInst = null;
                            }

                            // Allow variables / literals to multiply bracketed
                            // regions and functions
                            if(!lastCharWasOper && !firstIter) {
                                ops.add(new Instruction(Instruction.InstType.MUL, null));
                            }

                            // Prevents [op](...)-[val] causing problems
                            lastCharWasOper = false;

                            // Skip throwing exception
                            continue parseLoop;
                        }
                    }

                    // There were more ('s than )'s
                    throw new InvalidParameterException("Uneven number of start and end parentheses");
                }

                // Handle operators and operator precedence
                if(r == '+' || r == '-' || r == '/' || r == '*' || r == '^') {
                    lastCharWasOper = true;

                    if(opStack.isEmpty()) {
                        opStack.add(r);
                    }
                    else {
                        if(higherPrecedence(opStack.get(opStack.size() - 1), r)) {
                            opStack.add(r);
                        }
                        else {
                            // Keep removing operators that are higher precedence
                            // than the current
                            while(!opStack.isEmpty()) {
                                char nextOp = opStack.get(opStack.size() - 1);

                                if(!higherPrecedence(nextOp, r)) {
                                    opStack.remove(opStack.size() - 1);
                                    this.ops.add(Instruction.fromOperator(nextOp));
                                }
                                else break;
                            }

                            opStack.add(r);
                        }
                    }
                }
                else {
                    lastCharWasOper = false;
                }

                firstIter = false;
            }

            // Dump remaining operations into operation queue
            while(!opStack.isEmpty()) {
                char nextOp = opStack.get(opStack.size() - 1);
                opStack.remove(opStack.size() - 1);
                this.ops.add(Instruction.fromOperator(nextOp));
            }
        }

        /**
         * Looks from the current string parsing cursor forward to determine if the parsed substring starts with a function call (e.g. "sin(")
         *
         * @param substr The rest of the string from the cursor onwards
         * @return A `NATIVEFUNC` instruction if appropriate, else `null`
         */
        protected Instruction nativeFnLookahead(String substr) {
            // Basically just check if the lookahead string starts with a
            // supported function. This _could_ be expressed as a mapping
            // table and loop or something, but I can't be bothered.
            if(substr.startsWith("sin(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "SIN");
            }
            else if(substr.startsWith("cos(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "COS");
            }
            else if(substr.startsWith("tan(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "TAN");
            }
            else if(substr.startsWith("asin(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "ASIN");
            }
            else if(substr.startsWith("acos(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "ACOS");
            }
            else if(substr.startsWith("atan(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "ATAN");
            }
            else if(substr.startsWith("sinh(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "SINH");
            }
            else if(substr.startsWith("cosh(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "COSH");
            }
            else if(substr.startsWith("tanh(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "TANH");
            }
            else if(substr.startsWith("ln(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "LN");
            }
            else if(substr.startsWith("log(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "LOG");
            }
            else if(substr.startsWith("sqrt(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "SQRT");
            }
            else if(substr.startsWith("cbrt(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "CBRT");
            }
            else if(substr.startsWith("floor(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "FLOOR");
            }
            else if(substr.startsWith("ceil(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "CEIL");
            }
            else if(substr.startsWith("round(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "ROUND");
            }
            else if(substr.startsWith("abs(")) {
                return new Instruction(Instruction.InstType.NATIVEFUNC, "ABS");
            }

            return null;
        }

        /**
         * Determines what kind of function a `NATIVEFUNC` instruction refers to, and how many operands are required, then calls the function.
         *
         * @param i     The currently executing instruction
         * @param stack The value stack for the current evaluation
         * @return The result of the evaluation (i.e. stack operands are popped, but the result is not pushed back)
         */
        protected double nativeFnInvoke(Instruction i, ArrayList<Double> stack) {
            switch((String) i.arg) {
            case "SIN":
                return Math.sin(pop(stack));
            case "COS":
                return Math.cos(pop(stack));
            case "TAN":
                return Math.tan(pop(stack));
            case "ASIN":
                return Math.asin(pop(stack));
            case "ACOS":
                return Math.acos(pop(stack));
            case "ATAN":
                return Math.atan(pop(stack));
            case "SINH":
                return Math.sinh(pop(stack));
            case "COSH":
                return Math.cosh(pop(stack));
            case "TANH":
                return Math.tanh(pop(stack));
            case "LN":
                return Math.log(pop(stack));
            case "LOG":
                return Math.log10(pop(stack));
            case "SQRT":
                return Math.sqrt(pop(stack));
            case "CBRT":
                return Math.cbrt(pop(stack));
            case "FLOOR":
                return Math.floor(pop(stack));
            case "CEIL":
                return Math.ceil(pop(stack));
            case "ROUND":
                return Math.round(pop(stack));
            case "ABS":
                return Math.abs(pop(stack));
            case "POW":
                double d1 = pop(stack), d2 = pop(stack);
                return Math.pow(d2, d1);
            default:
                throw new UnsupportedOperationException("Attempted to call unknown native function " + i.arg);
            }
        }

        /**
         * Compares the precedence of two basic operators
         *
         * @param lower  The operator that is expected to be lower
         * @param higher The operator that is expected to be higher
         * @return Whether lower has a lower precedence than higher
         */
        protected static boolean higherPrecedence(char lower, char higher) {
            // Nothing has lower precedence than + or -
            if(higher == '+' || higher == '-') return false;
            // Power operator has highest precedence
            if(higher == '^' && lower != '^') return true;
            // * and / are only higher precedence than + or -
            else return(lower == '+' || lower == '-');
        }

        /**
         * Convenience function to remove the last element from an ArrayList and return the removed value.
         *
         * @param arr The ArrayList to pop a value from
         * @return The value popped
         */
        private static double pop(ArrayList<Double> arr) {
            return arr.remove(arr.size() - 1);
        }

        /**
         * Convenience function to add an element to an ArrayList. This form is actually longer than `[list].add()`, but it keeps the stack metaphor consistent.
         *
         * @param arr The stack to add a value to
         * @param n   The value to add
         */
        private static void push(ArrayList<Double> arr, double n) {
            arr.add(n);
        }
    }
}

/**
 * For internal use by Equation.Expression. Don't use this manually.
 *
 * Acts as a structure which stores the type of the instruction, as well as any optional operands. Due to the use of a stack machine approach to evaluating instructions, most instructions do not have any operands.
 */
class Instruction {
    /**
     * All supported operation types.
     */
    public enum InstType {
        // No args
        ADD, SUB, MUL, DIV, FACT, PLUSMINUS,
        // Takes a string naming a math function to execute (e.g. "sin")
        NATIVEFUNC,
        // Takes an Expression instance, which is evaluated, and the result pushed
        EXPR,
        // Takes a Double to push onto the operand stack
        PUSH,
        // Takes a string naming a variable which will be supplied at eval time
        PUSHVAR
    }

    /**
     * The type of instruction (i.e. what it will do)
     */
    public InstType instruction;

    /**
     * An optional argument, or null
     */
    public Object arg;

    /**
     * Creates a new instruction definition. Many instructions do not need an argument, in which case, arg should be null. Others will take instruction- specific arguments.
     *
     * @param instruction The instruction to represent
     * @param arg         An optional argument for the ex
     */
    public Instruction(InstType instruction, Object arg) {
        this.instruction = instruction;
        this.arg = arg;
    }

    /**
     * Creates an Instruction from one of +,-,/,*
     *
     * @param op A char representing the operator to create
     */
    public static Instruction fromOperator(char op) {
        switch(op) {
        case '+':
            return new Instruction(InstType.ADD, null);
        case '-':
            return new Instruction(InstType.SUB, null);
        case '/':
            return new Instruction(InstType.DIV, null);
        case '*':
            return new Instruction(InstType.MUL, null);
        case '^':
            return new Instruction(InstType.NATIVEFUNC, "POW");
        default:
            return null;
        }
    }
}
