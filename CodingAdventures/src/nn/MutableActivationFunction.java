package nn;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

public class MutableActivationFunction {
	
	// You might wont make a layer that big but here it is.
	private final static int PARALELLIZATION_START = 180 * 180;
	
	private static class MAFlayer {
		public double[] weigths[], biases;
		private int inpC, outC;
		public int getInputCount() { return inpC; }
		public int getOutputCount() { return outC; }

		public MAFlayer(int inputCount, int outputCount) {
			inpC = inputCount;
			outC = outputCount;
			weigths = new double[inputCount][outputCount];
			biases = new double[outputCount];
		}
		
		public void randomizeWeigths(double center, double width) {
			for(int oi = 0; oi < outC; oi++) {
				for(int ii = 0; ii < inpC; ii++) {
					weigths[ii][oi] = center + Math.random() * width * 2. - width;
				}
			}
		}
		public void randomizeBiases(double center, double width) {
			for(int oi = 0; oi < outC; oi++) {
				biases[oi] = center + Math.random() * width * 2. - width;
			}
		}
		
		public double[] calculate(double[] inps, DoubleUnaryOperator activationFunction) {
			// Those are commented out because in practical use there will never be such a case.
//			if(outC * inpC > MutableActivationFunction.PARALELLIZATION_START) {
//				return calculateParalelOutput(inps, activationFunction);
//			} else {
				return calculateSequential(inps, activationFunction);
//			}
		}
		
		public double[] calculateSequential(double[] inps, DoubleUnaryOperator activationFunction) {
			double[] outs = new double[outC];
			for(int oi = 0; oi < outs.length; oi++) {
				outs[oi] = biases[oi];
				for(int ii = 0; ii < inpC; ii++) {
					outs[oi] += inps[ii] * weigths[ii][oi];
				}
				outs[oi] = activationFunction.applyAsDouble(outs[oi]);
			}
			return outs;
		}
		
		public double[] calculateParalelOutput(double[] inps, DoubleUnaryOperator activationFunction) {
			double[] outs = new double[outC];
			IntStream.range(0, outC).parallel().forEach(oif -> {
				outs[oif] = biases[oif];
				for(int ii = 0; ii < inpC; ii++) {
					outs[oif] += inps[ii] * weigths[ii][oif];
				}
				outs[oif] = activationFunction.applyAsDouble(outs[oif]);
			});
			return outs;
		}
		
	}
	
	public MAFlayer[] layers;
	public DoubleUnaryOperator activationFunction;
	
	public MutableActivationFunction(int[] layerData) {
		layers = new MAFlayer[layerData.length - 1];
		for(int i = 0; i < layers.length; i++) {
			layers[i] = new MAFlayer(layerData[i], layerData[i + 1]);
			layers[i].randomizeBiases(0., Math.exp(3.));
			layers[i].randomizeWeigths(0., Math.exp(1.));
		}
//		IntStream.range(0, layers.length).parallel().forEach(i -> {
//			layers[i] = new MAFlayer(layerData[i], layerData[i + 1]);
//			layers[i].randomizeBiases(0., Math.exp(3.));
//			layers[i].randomizeWeigths(0., Math.exp(1.));
//		});
	}
	
	public double[] calculateNetwork(double[] inps) {
		inps = inps.clone();
		for (MAFlayer layer : layers) {
			inps = layer.calculate(inps, activationFunction);
		}
		return inps;
	}
	

	public static void main(String[] args) {
		// A simple activation function for the test
		DoubleUnaryOperator activation = x -> 1.0 / (1.0 + Math.exp(-x));
		Random random = new Random();
		int maxProblemSize = 10_00;
		int step = 4;
		
		System.out.println("Available CPU cores for parallel streams: " + Runtime.getRuntime().availableProcessors());
		System.out.println("Beginning performance test to find parallelization threshold.");
		System.out.println("----------------------------------------------------------");
		
		for (int problemSize = 20; problemSize <= maxProblemSize; problemSize += step) {
			// Set up a layer with a fixed number of inputs but increasing outputs
			
			MAFlayer layer = new MAFlayer(problemSize, problemSize);
			layer.randomizeBiases(0, 1);
			layer.randomizeWeigths(0, 1);
			
			double[] inputs = new double[problemSize];
			for (int i = 0; i < problemSize; i++) {
				inputs[i] = random.nextDouble();
			}

			// Warm-up the JVM
			for (int i = 0; i < 1000; i++) {
				layer.calculateSequential(inputs, activation);
				layer.calculateParalelOutput(inputs, activation);
			}

			long seqTime = 0;
			long parTime = 0;
			int runs = 500;
			
			for (int i = 0; i < runs; i++) {
				long startSeq = System.nanoTime();
				layer.calculateSequential(inputs, activation);
				seqTime += System.nanoTime() - startSeq;
				
				long startPar = System.nanoTime();
				layer.calculateParalelOutput(inputs, activation);
				parTime += System.nanoTime() - startPar;
			}
			
			double avgSeqTimeMs = seqTime / (double) runs / 1_000_000;
			double avgParTimeMs = parTime / (double) runs / 1_000_000;
			
			System.out.printf("Problem Size: %d (Inp=%d, Out=%d), Avg Sequential Time: %.3f ms, Avg Parallel Time: %.3f ms%n",
					problemSize, problemSize, problemSize, avgSeqTimeMs, avgParTimeMs);
		}
	}

}
