package part1_2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.stream.IntStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import part1_2.MathInterpreter.InputFunctionElementConstant.ConstantType;


public class MathInterpreter {

	/**
	 * Defining syntax:
	 * value like:
	 * x, y, (), 12.67, v3;
	 * unary:
	 * a, -, /, ln, e, !;
	 * sin, cos, tan, sih, coh, tah, ceil, floor;
	 * binary:
	 * +, *, ^, log
	 * binary operator meant to be between the operrands.
	 * no exception allowed
	 * */

	public static enum InputFunctionElementType {
		Xvariable, Yvariable, Parenthesis, Input, CustomVariable, Constant,
		Absolute, Minus, Inverse, NaturalLog, Exponential, Factorial,
		Sin, Cos, Tan, Sinh, Cosh, Tanh, Ceil, Floor, Round,
		Addition, Multiplication, Exponentiation, Logarithm, Modulation;
	}

	public static interface InputFunction {
		public double evaluate(double x, double y);

		public static abstract class InputFunctionValueObject implements InputFunction {
			public abstract double getValue();
			@Override
			public String toString() { return ""+getValue(); }
		}

		InputFunction xVariable = new InputFunction() {
			@Override
			public String toString() { return "x"; }
			@Override
			public double evaluate(double x, double y) { return x; }
		},
				yVariable = new InputFunction() {
			@Override
			public String toString() { return "y"; }
			@Override
			public double evaluate(double x, double y) { return y; }
		};
	}

	public static interface UnaryInputFunction {
		public double apply(double x, double y, double variable);

		default InputFunction setVariable(InputFunction input) {
			final String unaryFunctionName = toString();
			return (InputFunction) new InputFunction() {
				@Override
				public String toString() { return unaryFunctionName + "(" + input + ")"; }
				public double evaluate(double x, double y) {
					return apply(x, y, input.evaluate(x, y));
				}
			};
		}

		UnaryInputFunction absolute = new UnaryInputFunction() {
			@Override public String toString() { return "abs"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.abs(variable);
			}
		},
				minus = new UnaryInputFunction() {
			@Override public String toString() { return "-"; }
			@Override
			public double apply(double x, double y, double variable) {
				return -variable;
			}
		},
				inverse = new UnaryInputFunction() {
			@Override public String toString() { return "1.0/"; }
			@Override
			public double apply(double x, double y, double variable) {
				return 1./variable;
			}
		},
				naturalLog = new UnaryInputFunction() {
			@Override public String toString() { return "ln"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.log(variable);
			}
		},
				exponential = new UnaryInputFunction() {
			@Override public String toString() { return "e^"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.exp(variable);
			}
		},
				factorial = new UnaryInputFunction() {
			@Override public String toString() { return "!"; }
			public static double factorial(double value) {
				value = Math.round(value);
				int result = 1;
				for(int i = 1; i <= value; i++) { result *= i; }
				return result;
			}
			@Override
			public double apply(double x, double y, double variable) {
				return factorial(variable);
			}
		},
				sin = new UnaryInputFunction() {
			@Override public String toString() { return "sin"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.sin(variable);
			}
		},
				cos = new UnaryInputFunction() {
			@Override public String toString() { return "cos"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.cos(variable);
			}
		},
				tan = new UnaryInputFunction() {
			@Override public String toString() { return "tan"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.tan(variable);
			}
		},
				sinh = new UnaryInputFunction() {
			@Override public String toString() { return "sinh"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.sinh(variable);
			}
		},
				cosh = new UnaryInputFunction() {
			@Override public String toString() { return "cosh"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.cosh(variable);
			}
		},
				tanh = new UnaryInputFunction() {
			@Override public String toString() { return "tanh"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.tanh(variable);
			}
		},
				ceil = new UnaryInputFunction() {
			@Override public String toString() { return "ceil"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.ceil(variable);
			}
		},
				floor = new UnaryInputFunction() {
			@Override public String toString() { return "floor"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.floor(variable);
			}
		},
				round = new UnaryInputFunction() {
			@Override public String toString() { return "round"; }
			@Override
			public double apply(double x, double y, double variable) {
				return Math.round(variable);
			}
		};
	}

	public static interface BinaryInputFunction {
		public double apply(double x, double y, double left, double right);

		default InputFunction setVariables(InputFunction leftInput, InputFunction rightInput) {
			final String binaryFunctionName = toString();
			return (InputFunction) new InputFunction() {
				@Override
				public String toString() { return  "(" + leftInput + binaryFunctionName + rightInput + ")"; }
				public double evaluate(double x, double y) {
					return apply(x, y, leftInput.evaluate(x, y), rightInput.evaluate(x, y));
				}
			};
		}

		BinaryInputFunction addition = new BinaryInputFunction() {
			@Override public String toString() { return "+"; }
			@Override
			public double apply(double x, double y, double left, double right) {
				return left+right;
			}
		},
				multiplication = new BinaryInputFunction() {
			@Override public String toString() { return "*"; }
			@Override
			public double apply(double x, double y, double left, double right) {
				return left*right;
			}
		},
				exponentiation = new BinaryInputFunction() {
			@Override public String toString() { return "^"; }
			@Override
			public double apply(double x, double y, double left, double right) {
				return Math.pow(left, right);
			}
		},
				logarithm = new BinaryInputFunction() {
			@Override public String toString() { return "ln"; }
			@Override
			public double apply(double x, double y, double left, double right) {
				return Math.log(left) / Math.log(right);
			}
		},
				modulation = new BinaryInputFunction() {
			@Override public String toString() { return "%"; }
			@Override
			public double apply(double x, double y, double left, double right) {
				return left % right;
			}
		};
	}

	public static class InputFunctionElement {
		public InputFunctionElementType type;
		private boolean isInput, isUnary, isBinary, isTernary;
		public boolean isInput() { return isInput; }
		public boolean isUnary() { return isUnary; }
		public boolean isBinary() { return isBinary; }
		public boolean isTernary() { return isTernary; }
		public InputFunctionElement(InputFunctionElementType type) {
			this.type = type;
			isInput = false; isUnary = false; isBinary = false; isTernary = false;
			switch (type) {
			case Xvariable: case Yvariable: case Input: case Parenthesis:
				case CustomVariable: case Constant: isInput = true; break;
			case Absolute: case Minus: case Inverse: case NaturalLog: case Exponential: case Factorial:
			case Sin: case Cos: case Tan: case Sinh: case Cosh: case Tanh: case Ceil: case Floor: case Round:
				isUnary = true; break;
			case Addition: case Multiplication: case Exponentiation: case Logarithm: case Modulation: isBinary = true; break;
			default:

				break;
			}
		}
		public InputFunction getInput() {
			switch (type) {
			case Xvariable: return InputFunction.xVariable;
			case Yvariable: return InputFunction.yVariable;
			default: return null;
			}
		}
		public UnaryInputFunction getUnary() {
			switch (type) {
			case Absolute: return UnaryInputFunction.absolute;
			case Minus: return UnaryInputFunction.minus;
			case Inverse: return UnaryInputFunction.inverse;
			case NaturalLog: return UnaryInputFunction.naturalLog;
			case Exponential: return UnaryInputFunction.exponential;
			case Factorial: return UnaryInputFunction.factorial;
			case Sin: return UnaryInputFunction.sin;
			case Cos: return UnaryInputFunction.cos;
			case Tan: return UnaryInputFunction.tan;
			case Sinh: return UnaryInputFunction.sinh;
			case Cosh: return UnaryInputFunction.cosh;
			case Tanh: return UnaryInputFunction.tanh;
			case Ceil: return UnaryInputFunction.ceil;
			case Floor: return UnaryInputFunction.floor;
			case Round: return UnaryInputFunction.round;
			default: return null;
			}
		}
		public BinaryInputFunction getBinary() {
			switch (type) {
			case Addition: return BinaryInputFunction.addition;
			case Multiplication: return BinaryInputFunction.multiplication;
			case Exponentiation: return BinaryInputFunction.exponentiation;
			case Logarithm: return BinaryInputFunction.logarithm;
			case Modulation: return BinaryInputFunction.modulation;
			default: return null;
			}
		}
		@Override public String toString() { return type.toString(); }
		public String toStringHelper(String prefix, boolean isTail) {
			StringBuilder sb = new StringBuilder();
			sb.append(prefix);
			sb.append(isTail ? "└─ " : "├─ ");
			sb.append(type);
			sb.append("\n");
			return sb.toString();
		}
	}
	public static class InputFunctionElementParenthesis extends InputFunctionElement {
		public InputFunctionElementParenthesis(InputFunctionElementType type, String insides) {
			super(InputFunctionElementType.Parenthesis);
			elements = lex(insides);
		}
		public LinkedList<InputFunctionElement> elements;
		public InputFunction getInput() {
			return compose(elements);
		}
		@Override
		public String toString() { return toStringHelper("", true); }
		public String toStringHelper(String prefix, boolean isTail) {
			StringBuilder sb = new StringBuilder();
			sb.append(prefix);
			sb.append(isTail ? "└─ " : "├─ ");
			sb.append(type);
			sb.append('(');
			sb.append('\n');

			for (int i = 0; i < elements.size() - 1; i++) {
				InputFunctionElement child = elements.get(i);
				sb.append(child.toStringHelper(prefix + (isTail ? "   " : "│  "), false));
			}

			if (elements.size() > 0) {
				InputFunctionElement lastChild = elements.get(elements.size() - 1);
				sb.append(lastChild.toStringHelper(prefix + (isTail ? "   " : "│  "), true));
			}
			return sb.toString();
		}
	}
	public static class InputFunctionElementValue extends InputFunctionElement {
		public final double value;
		public InputFunctionElementValue(InputFunctionElementType type, double value) {
			super(InputFunctionElementType.Input);
			this.value = value;
		}
		public boolean isValue() { return true; }
		public InputFunction getInput() {
			return (InputFunction) new InputFunction.InputFunctionValueObject() {
				public double evaluate(double x, double y) {
					return value;
				}
				@Override
				public double getValue() { return value; }
			};
		}
	}
	public static class InputFunctionElementConstant extends InputFunctionElement {
		public static enum ConstantType {
			Pi(Math.PI), Tau(Math.PI * 2.), HPi(Math.PI / 2.),
			E(Math.E), Rad(Math.PI / 180.);

			public final double value;
			ConstantType(double value) {
				this.value = value;
			}
		}
		public final ConstantType constantType;
		public InputFunctionElementConstant(ConstantType constantType) {
			super(InputFunctionElementType.Constant);
			this.constantType = constantType;
		}
		public boolean isValue() { return true; }
		public InputFunction getInput() {
			return (InputFunction) new InputFunction.InputFunctionValueObject() {
				public double evaluate(double x, double y) { return constantType.value; }
				@Override
				public double getValue() { return constantType.value; }
			};
		}
	}
	public static class InputFunctionElementInput extends InputFunctionElement {
		public final InputFunction inputFunction;
		public InputFunctionElementInput(InputFunctionElementType type, InputFunction inputFunction) {
			super(InputFunctionElementType.Input);
			this.inputFunction = inputFunction;
		}
		public boolean isValue() { return true; }
		public InputFunction getInput() {
			return inputFunction;
		}
	}

	// Third part of Interpreting Math, to transform the structured elements into a function
	// It is not a perfect design but primarily because of simplicity for now it also handles order of the binary operations
	public static InputFunction compose(LinkedList<InputFunctionElement> elementSource) {
		// Preparing the workspace and the result:
		LinkedList<InputFunctionElement> elements = (LinkedList<InputFunctionElement>) elementSource.clone();
		// Solving the edge cases:
		if(elements.getFirst().isBinary()) {
			elements.add(0, new InputFunctionElementValue(null, 1.));
		}
		if(elements.getLast().isBinary()) {
			elements.add(new InputFunctionElementValue(null, 1.));
		}
		System.out.println("Composer is given those elements: { ");
		elements.forEach(e -> System.out.println(e));
		System.out.println("} Composer input!");
		// Processing all unary (also consequently every thing inside of unary functions) functions:
		{
		LinkedList<InputFunctionElement> unaries = new LinkedList<InputFunctionElement>();
		for(ListIterator<InputFunctionElement> lit = elements.listIterator(); lit.hasNext(); ) {
			InputFunctionElement element = lit.next(); // skipping non unary functions.
			for(; !element.isUnary() && lit.hasNext(); element = lit.next()) { }
			if(!lit.hasNext()) { break; } // Exiting at the end.
			for(; element.isUnary() && lit.hasNext(); element = lit.next()) {
				lit.remove(); unaries.add(0, element);
			}
			// All the unary functions will chain and produce an input function. Eventually this will point to that function.
			InputFunction inputFunction = element.isInput() ? element.getInput() : (x, y) -> 1.;
			// In case the unary functions are not followed by value we add a value of now to prevent errors
			//
			ListIterator<InputFunctionElement> ui = unaries.listIterator();
			for(; ui.hasNext();) {
				inputFunction = ui.next().getUnary().setVariable(inputFunction);
				ui.remove();
			}
			lit.set(new InputFunctionElementInput(null, inputFunction));
		}
		}
		// Restructuring the list in order to prevent errors. I guess this part can be removed.
		for(ListIterator<InputFunctionElement> lit = elements.listIterator(); lit.hasNext(); ) {
			InputFunctionElement element = lit.next();
			if(!lit.hasNext()) { break; }
			if(element.isInput()) {
				InputFunctionElement nextElement = lit.next();
				while(nextElement.isInput()) {
					lit.previous();
					lit.add(new InputFunctionElement(InputFunctionElementType.Multiplication));
					lit.next();
					element = nextElement;
					if(!lit.hasNext()) { break; }
					nextElement = lit.next();
				}
				continue;
			}
			if(!lit.hasNext()) { break; }
			if(element.isBinary()) {
				InputFunctionElement nextElement = lit.next();
				if(nextElement.isBinary()) {
					lit.previous(); lit.add(new InputFunctionElementValue(null, 1.));
				}
				element = nextElement;
			}
		}
		// Processing exponentiation and logarithym binary functions left to right.
		for(ListIterator<InputFunctionElement> lit = elements.listIterator(); lit.hasNext(); ) {
			InputFunctionElement left = lit.next();
			if(!lit.hasNext()) { break; }
			InputFunctionElement operator = lit.next();
			while(operator.type != InputFunctionElementType.Exponentiation &&
					operator.type != InputFunctionElementType.Logarithm && lit.hasNext()) {
				left = operator; operator = lit.next();
			}
			if(!lit.hasNext()) { break; }
			InputFunctionElement right = lit.next();
			lit.remove(); lit.previous(); lit.remove(); lit.previous();
			lit.set(new InputFunctionElementInput(null,
					operator.getBinary().setVariables(left.getInput(), right.getInput())));
		}
		// Processing Multiplication and Modulation functions left to right.
		for(ListIterator<InputFunctionElement> lit = elements.listIterator(); lit.hasNext(); ) {
			InputFunctionElement left = lit.next();
			if(!lit.hasNext()) { break; }
			InputFunctionElement operator = lit.next();
			while(operator.type != InputFunctionElementType.Multiplication &&
					operator.type != InputFunctionElementType.Modulation && lit.hasNext()) {
				left = operator; operator = lit.next();
			}
			if(!lit.hasNext()) { break; }
			InputFunctionElement right = lit.next();
			lit.remove(); lit.previous(); lit.remove(); lit.previous();
			lit.set(new InputFunctionElementInput(null,
					operator.getBinary().setVariables(left.getInput(), right.getInput())));
		}
		// Processing Addition functions left to right, thus evaluating the result.
		for(ListIterator<InputFunctionElement> lit = elements.listIterator(); lit.hasNext(); ) {
			InputFunctionElement left = lit.next();
			if(!lit.hasNext()) { break; }
			InputFunctionElement operator = lit.next();
			while(operator.type != InputFunctionElementType.Addition && lit.hasNext()) {
				left = operator; operator = lit.next();
			}
			if(!lit.hasNext()) { break; }
			InputFunctionElement right = lit.next();
			lit.remove(); lit.previous(); lit.remove(); lit.previous();
			lit.set(new InputFunctionElementInput(null,
					operator.getBinary().setVariables(left.getInput(), right.getInput())));
		}
//		System.out.println(elements.size());
		return elements.get(0).getInput();
	}

	/**
	 * Defining syntax:
	 * value like:
	 * x, y, (), 12.67, v3;
	 * unary:
	 * a, -, /, ln, e;
	 * sin, cos, tan, sih, coh, tah;
	 * binary:
	 * +, *, ^, log
	 * binary operator meant to be between the operands.
	 * no exception allowed
	 * */

	// First part of Interpreting Math, extracts labels from the text data. Also for now it also handles parts of the second part
	// I know It is not a perfect design but for this case specifically (because there is the same logic both inside and outside of Parenthesis) primarily because of simplicity
	public static LinkedList<InputFunctionElement> lex(String text) {
		LinkedList<InputFunctionElement> result = new LinkedList<InputFunctionElement>();
		// Those variables are used in multiple places a and because of the compiler I should declare them here.
		int startIndex; double value;
		for(int ti = 0; ti < text.length(); ti++) {
			value = 0.; // To prevent errors.
			switch(text.charAt(ti)) {
			case ' ': case '\t': break; // White space is handled. Here are the variables:
			case 'x': result.add(new InputFunctionElement(InputFunctionElementType.Xvariable)); break;
			case 'y': result.add(new InputFunctionElement(InputFunctionElementType.Yvariable)); break;
			case '(': // Note that the rules inside of the parenthesis are exactly the same as the ones outside.
				startIndex = ++ti; // Storing the beginning index.
				int paranthesis = 1; // Parenthesis counter.
				while(paranthesis > 0) { // Logic to find the end of original parenthesis.
					ti++; if(ti == text.length()) { break; }  // If the text ends without closing, we accept the end as closing.
					if(text.charAt(ti) == '(') { paranthesis++;
					} else if(text.charAt(ti) == ')') { paranthesis--; }
				}
				// The insides of the parenthesis will be lexed with the same function. Remember the note.
				result.add(new InputFunctionElementParenthesis(InputFunctionElementType.Parenthesis,
						text.substring(startIndex, ti)));
				break;
				// Handling values.
			case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0':
				startIndex = ti; // String the beginning. Finding the end.
				while(Character.isDigit(text.charAt(ti))) {
					ti++; if(ti == text.length()) { break; }
				} // Calculating the value.
				value = Integer.valueOf(text.substring(startIndex, ti));
				if(ti == text.length() || text.charAt(ti) != '.') { ti--;
					result.add(new InputFunctionElementValue(null, value));
					break; // In case there is not any fraction.
				}
			case '.': // User can write fractions without writing zero.
				int fractionBegin = ++ti;
				while(Character.isDigit(text.charAt(ti))) {
					ti++; if(ti == text.length()) { break; }
				}
				value += ((double) Integer.valueOf(text.substring(fractionBegin, ti))) / Math.pow(10., ti - fractionBegin);
				result.add(new InputFunctionElementValue(null, value)); ti--;
				break;
			case 'V': // TODO
				break;
			case 'C': // those are for the constants, later I may move them to the constructor of Constants.
				if(text.startsWith("tau", ti + 1)) {
					result.add(new InputFunctionElementConstant(ConstantType.Tau)); ti += 3; break;
				}
				if(text.startsWith("pi", ti + 1)) {
					result.add(new InputFunctionElementConstant(ConstantType.Pi)); ti += 2; break;
				}
				if(text.startsWith("hpi", ti + 1)) {
					result.add(new InputFunctionElementConstant(ConstantType.HPi)); ti += 3; break;
				}
				if(text.startsWith("e", ti + 1)) {
					result.add(new InputFunctionElementConstant(ConstantType.E)); ti += 1; break;
				}
				if(text.startsWith("rad", ti + 1)) {
					result.add(new InputFunctionElementConstant(ConstantType.Rad)); ti += 3; break;
				}
				break; // Some Unary Operators are simple.
			case '!': result.add(new InputFunctionElement(InputFunctionElementType.Factorial)); break;
			case 'a': result.add(new InputFunctionElement(InputFunctionElementType.Absolute)); ti += 2; break;
			case '-': result.add(new InputFunctionElement(InputFunctionElementType.Minus)); break;
			case '/': result.add(new InputFunctionElement(InputFunctionElementType.Inverse)); break;
			case 'e': result.add(new InputFunctionElement(InputFunctionElementType.Exponential)); break;
			case 'l':
				if(text.charAt(++ti) == 'n') {
					result.add(new InputFunctionElement(InputFunctionElementType.NaturalLog));
				}
				else if(text.charAt(ti) == 'o' && text.charAt(++ti) == 'g') {
					result.add(new InputFunctionElement(InputFunctionElementType.Logarithm));
				}
				break; // Some Binary Operators are simple.
			case '^': result.add(new InputFunctionElement(InputFunctionElementType.Exponentiation)); break;
			case '*': result.add(new InputFunctionElement(InputFunctionElementType.Multiplication)); break;
			case '+': result.add(new InputFunctionElement(InputFunctionElementType.Addition)); break;
			case '%': result.add(new InputFunctionElement(InputFunctionElementType.Modulation)); break;
			case 's': // Trigonometric Unary Operators are made of three letters, which is complex.
				if(text.charAt(ti + 1) != 'i') { break; }
				if(text.charAt(++ti + 1) != 'n') { break; }
				if(text.charAt(++ti + 1) != 'h') { // Making sure they are hyperbolic.
					result.add(new InputFunctionElement(InputFunctionElementType.Sin));
				} else {
					ti++; // The additional "h".
					result.add(new InputFunctionElement(InputFunctionElementType.Sinh));
				}
				break;
			case 'c': // Trigonometric Unary Operators are made of three letters, which is complex.
				if(text.startsWith("eil", ti + 1)) {
					result.add(new InputFunctionElement(InputFunctionElementType.Ceil)); ti += 3; break;
				}
				if(text.charAt(ti + 1) != 'o') { break; }
				if(text.charAt(++ti + 1) != 's') { break; }
				if(text.charAt(++ti + 1) != 'h') { // Making sure they are hyperbolic.
					result.add(new InputFunctionElement(InputFunctionElementType.Cos));
				} else {
					ti++; // The additional "h".
					result.add(new InputFunctionElement(InputFunctionElementType.Cosh));
				}
				break;
			case 't': // Trigonometric Unary Operators are made of three letters, which is complex.
				if(text.charAt(ti + 1) != 'a') { break; }
				if(text.charAt(++ti + 1) != 'n') { break; }
				if(text.charAt(++ti + 1) != 'h') {
					result.add(new InputFunctionElement(InputFunctionElementType.Tan));
				} else { // Making sure they are hyperbolic.
					ti++; // The additional "h".
					result.add(new InputFunctionElement(InputFunctionElementType.Tanh));
				}
				break;
			case 'r':
				if(text.startsWith("ound", ti + 1)) {
					result.add(new InputFunctionElement(InputFunctionElementType.Round)); ti += 4; break;
				}
			case 'f':
				if(text.startsWith("loor", ti + 1)) {
					result.add(new InputFunctionElement(InputFunctionElementType.Floor)); ti += 4; break;
				}
			default:
				System.out.println("Lexer found undefined token: \"" + text.charAt(ti) + "\" at " + ti);
				break;
			}
		}
//		result.forEach(e -> System.out.println(e)); System.out.println("\n\n");
		return result;
	}

	/**
	 * Defining syntax:
	 * value like:
	 * x, y, (), 12.67, v3;
	 * unary:
	 * a, -, /, ln, e;
	 * sin, cos, tan, sih, coh, tah;
	 * binary:
	 * +, *, ^, log
	 * binary operator meant to be between the operrands.
	 * no exception allowed
	 * */

	public static void main(String[] args) {

//		System.out.println(compose(lex("(x^(3.5)*4*sin(absx)")));

		//  style = "margin-top: 0px;"
		final String instructionsText = "<html>"
				+ "<body style = \"color: #000; background-color: #b6b6b6; margin: 0px 10px 0px 10px;\">"
				+ "<h1 style = \"color: #fff; background-color: #666;\">Math Interpreter Instruction Manual</h3>"
				+ "<p style = \"margin-top: 0px;\">This interpreter uses a syntax specific to itself.</p>"
				+ "<p style = \"margin-top: 0px;\">The idea of this notion was to use as least binary operators as possible</p>"
				+ "<h3>Operator Precedence (Order of Operations):</h3>"
				+ "<ol style = \"margin-top: 0px;\">"
				+ "<li> Expressions within Parenthesis have the maximum priority</li>"
				+ "<li> Unaries (functions with only one parameter) are done right left.(e.g., \"sinx\")</li>"
				+ "<li> Binary (functions with exactly 2 parameters) are in math Precedence.(e.g., \"x^2\")</li>"
				+ "</ol>"
				+ "<h3>Variables:</h3>"
				+ "<ul style = \"margin-top: 0px;\">"
				+ "<li><b>x</b>: Represents the horizontal axis input.</li>"
				+ "<li><b>y</b>: Represents the value of the slider.</li>"
				+ "</ul>"
				+ "<h3 style = \"margin-bottom: 0px;\">Constants:</h3>"
				+ "<h5 style = \"margin-top: 0px;\">(To use a constant write \"C\" followed by it's name (e.g., \"Cpi\" for pie))</h5>"
				+ "<b>pi</b>: Pi (3.14...) || "
				+ "<b>hpi</b>: Pi/2 (1.57...) || "
				+ "<b>tau</b>: Pi*2 (6.28...) || "
				+ "<b>e</b>: E (2.71...) || "
				+ "<b>rad</b>: Pi/180 (0.017...)"
				+ "<br/><br/>"
				+ "<h3 style = \"margin-bottom: 0px;\">Binary Operators (Written between operands):</h3>"
				+ "<h5 style = \"margin-top: 0px;\">(Notice there is not division or subtraction. for them take help from unary operators)</h5>"
				+ "<ul style = \"margin-top: 0px;\">"
				+ "<li><b>^</b>: Exponentiation (e.g., \"x^2\")</li>"
				+ "<li><b>log</b>: Logarithm of a with the base b (e.g., \"alogb\")</li>"
				+ "<li><b>*</b>: Multiplication (e.g., \"x*y\")</li>"
				+ "<li><b>mod</b>: Modulus (e.g., \"xmody\")</li>"
				+ "<li><b>+</b>: Addition (e.g., \"x+y\")</li>"
				+ "</ul>"
				+ "<h3 style = \"margin-bottom: 0px;\">Unary Operators (Written before operands):</h3>"
				+ "<h5 style = \"margin-top: 0px;\">(You do not necessarily need to use Paranthesis for any of them.)</h5>"
				+ "<ul style = \"margin-top: 0px;\">"
				+ "<li><b>-</b>: Negation (e.g., \"-x\")</li>"
				+ "<li><b>/</b>: Inverse (e.g., \"/x\")</li>"
				+ "<li><b>abs</b>: Absolute value (e.g., \"absx\")</li>"
				+ "<li><b>ln</b>: Natural logarithm (e.g., \"lnx\")</li>"
				+ "<li><b>e</b>: Exponential function (e.g., \"ex\")</li>"
				+ "<li><b>sin</b>, <b>cos</b>, <b>tan</b>: Trigonometric functions</li>"
				+ "<li><b>sinh</b>, <b>cosh</b>, <b>tanh</b>: Hyperbolic trigonometric functions</li>"
				+ "<li><b>ceil</b>, <b>floor</b>, <b>round</b>: Rounding functions</li>"
				+ "</ul>"
				+ "<h3>Important Notes:</h3>"
				+ "<ul style = \"margin-top: 0px;\">"
				+ "<li>Interpreter ignores all of the spaces except ones who seperate numeric values (e.g., \"35.3 0.23\")</li>"
				+ "<li>Use parentheses \"()\" to group expressions. Example: \"sin(x^2 + y^2) / e(x*y)\"</li>"
				+ "<li>The system support implicit multiplication (e.g., \"2x\" but use \"*\" with values.)</li>"
				+ "</ul>"
				+ "</body></html>";

//		JOptionPane.showMessageDialog(null, instructionsText, "Interpreter Instructions", JOptionPane.INFORMATION_MESSAGE);
		SwingUtilities.invokeLater(() -> {
			new JFrame() {

				public static final JPanel APPLICATION = new JPanel(), GRAPH = new JPanel() {
					@Override
					public void paintComponent(Graphics graphics) {
						graphPrepaint((Graphics2D) graphics);
						try {
							graphPaint((Graphics2D) graphics);
						} catch(Error e) {
							JOptionPane.showMessageDialog(null, "The Application failed to graph this function!");
							INPUT_TEXTBOX.setText(DEFAULT_FUNCTION);
//							INPUT_TEXTBOX;
						}
						graphPostpaint((Graphics2D) graphics);
					}
				};
				public static final JTextField INPUT_TEXTBOX = new JTextField(50);
				public static final JSlider Y_SLIDER = new JSlider(JSlider.HORIZONTAL, -300, 300, 0);

				static final int RADIUS = 2;
				public static void fillCircle(Graphics2D g, int x, int y) { g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS); }


				static int divider = 3, mid = divider / 2;
				static double fraction = 1./divider;
				static double formerHeight;
				static int mayIncreaseDivder = 0;

				public static void graphPrepaint(Graphics2D g) {
					g.setBackground(Color.BLACK);
					g.clearRect(0, 0, GRAPH.getWidth(), GRAPH.getHeight());
					g.setColor(new Color(255, 255, 255, 255 * 10 / (divider + 10) + 1));
				}
				public static void graphPaint(Graphics2D g) {
					divider = 3; mid = divider / 2; fraction = 1./divider;
					formerHeight = -mathFunction.evaluate((0 - GRAPH.getWidth()/2) / divider + fraction * -mid, yValue);
					mayIncreaseDivder = 0;
					for(int x = -30; x < GRAPH.getWidth(); x++) {
						for(int di = -mid; di <= mid; di++) {
							double height = -mathFunction.evaluate((x - GRAPH.getWidth()/2) / 3. + fraction * di, yValue);
							fillCircle(g, x, (int) Math.round(height) + GRAPH.getHeight()/2);
							if(Math.abs(formerHeight - height) > RADIUS * 2) {
								if(mayIncreaseDivder < 2) { mayIncreaseDivder++;
								} else {
									divider++; mid = divider / 2; fraction = 1./divider;
								}
							} else { mayIncreaseDivder = 0; }
							formerHeight = height;
						}
						if(divider * (GRAPH.getWidth() - x) * INPUT_TEXTBOX.getText().length() >
							180 * 180 * DEFAULT_FUNCTION.length() * 16 / Runtime.getRuntime().availableProcessors())
						{
							System.out.println("parallelizing: " + (divider * (GRAPH.getWidth() - x)));
							IntStream.range(x, GRAPH.getWidth()).parallel().forEach(xp -> {
								for(int di = -mid; di <= mid; di++) {
									double height = -mathFunction.evaluate((xp - GRAPH.getWidth()/2) / 3. + fraction * di, yValue);
									fillCircle(g, xp, (int) Math.round(height) + GRAPH.getHeight()/2);
									if(Math.abs(formerHeight - height) > RADIUS * 2) {
										if(mayIncreaseDivder < 2) { mayIncreaseDivder++;
										} else {
											divider++; mid = divider / 2; fraction = 1./divider;
										}
									} else { mayIncreaseDivder = 0; }
									formerHeight = height;
								}
							});
							break;
						}
					}
//					System.out.println(divider);
				}
				public static void graphPostpaint(Graphics2D g) {
					for(int i = 0; i < GRAPH.getWidth()/2; i += 20) {
						if(i % 100 == 0) { continue; }
						g.setColor(new Color(0, 0, 255, Math.max(127 - i/10, 0)));
						g.drawLine(GRAPH.getWidth()/2 + i, 0, GRAPH.getWidth()/2 + i, GRAPH.getHeight());
						g.drawLine(GRAPH.getWidth()/2 - i, 0, GRAPH.getWidth()/2 - i, GRAPH.getHeight());
					}
					for(int i = 0; i < GRAPH.getHeight()/2; i += 20) {
						if(i % 100 == 0) { continue; }
						g.setColor(new Color(0, 0, 255, Math.max(191 - i/5, 0)));
						g.drawLine(0, GRAPH.getHeight()/2 + i, GRAPH.getWidth(), GRAPH.getHeight()/2 + i);
						g.drawLine(0, GRAPH.getHeight()/2 - i, GRAPH.getWidth(), GRAPH.getHeight()/2 - i);
					}
					for(int i = 0; i < GRAPH.getWidth()/2; i += 100) {
						if(i % 400 == 0) { continue; }
						g.setColor(new Color(0, 255, 0, Math.max(159 - i/10, 0)));
						g.drawLine(GRAPH.getWidth()/2 + i, 0, GRAPH.getWidth()/2 + i, GRAPH.getHeight());
						g.drawLine(GRAPH.getWidth()/2 - i, 0, GRAPH.getWidth()/2 - i, GRAPH.getHeight());
					}
					for(int i = 0; i < GRAPH.getHeight()/2; i += 100) {
						if(i % 400 == 0) { continue; }
						g.setColor(new Color(0, 255, 0, Math.max(223 - i/4, 0)));
						g.drawLine(0, GRAPH.getHeight()/2 + i, GRAPH.getWidth(), GRAPH.getHeight()/2 + i);
						g.drawLine(0, GRAPH.getHeight()/2 - i, GRAPH.getWidth(), GRAPH.getHeight()/2 - i);
					}
					for(int i = 0; i < GRAPH.getWidth()/2; i += 400) {
						g.setColor(new Color(255, 0, 0, Math.max(255 - i/4, 0)));
						g.drawLine(GRAPH.getWidth()/2 + i, 0, GRAPH.getWidth()/2 + i, GRAPH.getHeight());
						g.drawLine(GRAPH.getWidth()/2 - i, 0, GRAPH.getWidth()/2 - i, GRAPH.getHeight());
					}
					for(int i = 0; i < GRAPH.getHeight()/2; i += 400) {
						g.setColor(new Color(255, 0, 0, Math.max(255 - i/2, 0)));
						g.drawLine(0, GRAPH.getHeight()/2 + i, GRAPH.getWidth(), GRAPH.getHeight()/2 + i);
						g.drawLine(0, GRAPH.getHeight()/2 - i, GRAPH.getWidth(), GRAPH.getHeight()/2 - i);
					}
//					g.drawLine(0, GRAPH.getHeight()/2, GRAPH.getWidth(), GRAPH.getHeight()/2);
//					g.drawLine(GRAPH.getWidth()/2, 0, GRAPH.getWidth()/2, GRAPH.getHeight());
				}

				public static InputFunction mathFunction;
				public static double yValue = 0.;

				public static final String DEFAULT_FUNCTION =
						"sin(xe3/(absx^0.7*0.3+(y+30)+e1))6sin(ln(absx0.13+(y+30)^2/8+e1)6)5ln((y+33)2+e1)";

				public void makeVisible() {
					System.out.println(DEFAULT_FUNCTION.length());
					INPUT_TEXTBOX.setText(DEFAULT_FUNCTION);
					mathFunction = compose(lex(INPUT_TEXTBOX.getText()));
					APPLICATION.setPreferredSize(new Dimension(800, 800));
					APPLICATION.setBackground(Color.GRAY);
					APPLICATION.setLayout(new BorderLayout()); // Using BorderLayout for the main application panel
					GRAPH.setBackground(Color.BLACK);
					APPLICATION.add(GRAPH, BorderLayout.CENTER);
					JPanel controlsPanel = new JPanel();
					controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
					controlsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
					INPUT_TEXTBOX.addActionListener(e -> {
						try {
							mathFunction = compose(lex(INPUT_TEXTBOX.getText()));
							System.out.println("Compose gave this: " + mathFunction);
						} catch(Error e1) {
							JOptionPane.showMessageDialog(null, "The Math Interpreter failed to understand this!");
						}
						GRAPH.repaint();
					});
					Y_SLIDER.setMajorTickSpacing(50);
					Y_SLIDER.setMinorTickSpacing(5);
					Y_SLIDER.setPaintTicks(true);
					Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
					for (int i = Y_SLIDER.getMinimum(); i <= Y_SLIDER.getMaximum(); i += Y_SLIDER.getMajorTickSpacing()) {
						double realValue = i / 10.0;
						labelTable.put(i, new JLabel(String.valueOf(realValue)));
					}
					Y_SLIDER.setLabelTable(labelTable);
					Y_SLIDER.setPaintLabels(true);
					Y_SLIDER.addChangeListener(e -> {
						yValue = Y_SLIDER.getValue() / 10.;
						GRAPH.repaint();
					});
					APPLICATION.add(GRAPH, BorderLayout.CENTER);
					JPanel textPanel = new JPanel();
					textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.X_AXIS));
					JButton instructionsButton = new JButton("Instructions Manual");
					instructionsButton.addActionListener(e -> {
						JOptionPane.showMessageDialog(APPLICATION, instructionsText, "Interpreter Instructions", JOptionPane.INFORMATION_MESSAGE);
					});
					textPanel.add(instructionsButton, 0);
					textPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Spacer between components
					textPanel.add(INPUT_TEXTBOX);
					controlsPanel.add(textPanel);
					controlsPanel.add(Y_SLIDER);
					APPLICATION.add(controlsPanel, BorderLayout.SOUTH);
					super.add(APPLICATION);
					super.pack();
					super.setDefaultCloseOperation(EXIT_ON_CLOSE);
					super.setLocationRelativeTo(null);
					super.setVisible(true);
					super.repaint();
				}
			} /*;// */.makeVisible();
		});
		/*
//		InputFunction func = compose(lex(
////				"x+y*6+lnex")); (/43*63^sinx*(x^2))
//				"x+y+e(x*y* /4+-(/4*64^cos(x*y* /12)+(x^2+y^2)* /8))"));
//				"(x+y*(/43*63^sinx *  (x^2)log(   sinh ( y +lny^2)+ee(-ex+\n/	ln34 * / y)+x)+-y)* /tanhy)+tanhtanex^3* /y"));
//				"((x*x+y*y)*.1*Cpi)"));
////				"(sin(x*y* /12))"));
////				"(64^cos(x*y* /12))"));
////				"-(/4*64^sin(x*y* /12))"));
////				"(x^2+y^2)* /8"));
//		System.out.println(func);
//		IntStream.range(0, 6).forEach(x -> {
//			System.out.println("X:"+x+" |Y:0~6");
//			IntStream.range(0, 6).forEach(y -> {
//				System.out.println(func.evaluate(x, y));
//			});
//		});
		*/
	}

}
