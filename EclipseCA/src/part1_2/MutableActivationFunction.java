package part1_2;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

public class MutableActivationFunction {
	
	// You might wont make a layer that big but here it is.
	private final static int PARALELLIZATION_START = 180 * 180;
	
	public static class NodeData {
		public double sourceWeights[], targetWeights[], bias;
		public int layer;
	}
	
	private static class MAFlayer {
		public double[] weights[], biases;
		public int getInputCount() { return weights.length; }
		public int getOutputCount() { return biases.length; }

		public MAFlayer(int inputCount, int outputCount) {
			weights = new double[inputCount][outputCount];
			biases = new double[outputCount];
		}
		
		public void appendInput(double radnCenter, double randWidth) {
			double[] nWeights[] = new double[getInputCount() + 1][];
			for(int ii = 0; ii < getInputCount(); ii++) { nWeights[ii] = weights[ii]; }
			nWeights[getInputCount()] = new double[getOutputCount()];
			for(int oi = 0; oi < getOutputCount(); oi++) {
				nWeights[getInputCount()][getOutputCount()] = radnCenter + Math.random() * randWidth * 2. - randWidth;
			}
			weights = nWeights;
		}
		
		public double[] removeInput(int index) {
			double[] former[] = weights;
			weights = new double[getInputCount() - 1][];
			for(int ii = 0; ii < index; ii++) { weights[ii] = former[ii]; }
			for(int ii = index; ii < former.length; ii++) { weights[ii + 1] = former[ii]; }
			return former[index];
		}
		
		public void appendOutput(double radnCenter, double randWidth) {
			double nBiases[] = new double[getOutputCount() + 1];
			for(int oi = 0; oi < getOutputCount(); oi++) { nBiases[oi] = biases[oi]; }
			for(int ii = 0; ii < getOutputCount(); ii++) {
				double[] former = weights[ii];
				weights[ii] = new double[nBiases.length];
				for(int oi = 0; oi < getOutputCount(); oi++) { weights[ii][oi] = former[oi]; }
				weights[ii][getOutputCount()] = radnCenter + Math.random() * randWidth * 2. - randWidth;
			}
			biases = nBiases;
		}
		
		public double[] removeOutput(int index) {
			double formerB[] = biases;
			double removedW[] = new double[getInputCount()];
			biases = new double[getOutputCount() - 1];
			for(int oi = 0; oi < index; oi++) { biases[oi] = formerB[oi]; }
			for(int oi = index; oi < formerB.length; oi++) { biases[oi] = formerB[oi + 1]; }
			for(int ii = 0; ii < getOutputCount(); ii++) {
				double[] formerW = weights[ii];
				weights[ii] = new double[getOutputCount()];
				for(int oi = 0; oi < index; oi++) { weights[ii][oi] = formerW[oi]; }
				for(int oi = index; oi < getInputCount(); oi++) { weights[ii][oi] = formerW[oi + 1]; }
				removedW[ii] = formerW[index];
			}
			return removedW;
		}
		
		public void randomizeWeights(double center, double width) {
			for(int oi = 0; oi < getOutputCount(); oi++) {
				for(int ii = 0; ii < getInputCount(); ii++) {
					weights[ii][oi] = center + Math.random() * width * 2. - width;
				}
			}
		}
		public void randomizeBiases(double center, double width) {
			for(int oi = 0; oi < getOutputCount(); oi++) {
				biases[oi] = center + Math.random() * width * 2. - width;
			}
		}
		
		public double[] calculate(double[] inps, DoubleUnaryOperator activationFunction) {
			// Those are commented out because in practical use there will never be such a case.
//			if(getOutputCount() * getInputCount() > MutableActivationFunction.PARALELLIZATION_START) {
//				return calculateParalelOutput(inps, activationFunction);
//			} else {
				return calculateSequential(inps, activationFunction);
//			}
		}
		
		public double[] calculateSequential(double[] inps, DoubleUnaryOperator activationFunction) {
			double[] outs = new double[getOutputCount()];
			for(int oi = 0; oi < outs.length; oi++) {
				outs[oi] = biases[oi];
				for(int ii = 0; ii < getInputCount(); ii++) {
					outs[oi] += inps[ii] * weights[ii][oi];
				}
				outs[oi] = activationFunction.applyAsDouble(outs[oi]);
			}
			return outs;
		}
		
		public double[] calculateParalelOutput(double[] inps, DoubleUnaryOperator activationFunction) {
			double[] outs = new double[getOutputCount()];
			IntStream.range(0, getOutputCount()).parallel().forEach(oif -> {
				outs[oif] = biases[oif];
				for(int ii = 0; ii < getInputCount(); ii++) {
					outs[oif] += inps[ii] * weights[ii][oif];
				}
				outs[oif] = activationFunction.applyAsDouble(outs[oif]);
			});
			return outs;
		}
		
	}
	
	public MAFlayer[] layers;
	public DoubleUnaryOperator activationFunction = a -> a > 0. ? a : 0.;
	
	public MutableActivationFunction(DoubleUnaryOperator activationFunction, int[] layerData) {
		layers = new MAFlayer[layerData.length - 1];
		for(int i = 0; i < layers.length; i++) {
			layers[i] = new MAFlayer(layerData[i], layerData[i + 1]);
			layers[i].randomizeWeights(0., Math.exp(-0.4));
		}
		randomizeMultipliers(0., Math.exp(-0.1));
		randomizeBiases(0., Math.exp(-0.03));
	}
	
	/** Unchecked since in the place it will be used there is not need. */
	public NodeData removeHiddenNode(int layer, int index) {
		NodeData result = new NodeData();
		result.layer = layer;
		result.bias = layers[layer].biases[index];
		result.sourceWeights = layers[layer].removeOutput(index);
		result.targetWeights = layers[layer + 1].removeInput(index);
		return result;
	}
	
	public void randomizeMultipliers(double center, double width) {
		for(int i = 0; i < layers.length; i++) {
			layers[i].randomizeWeights(center, width);
		}
	}
	public void randomizeBiases(double center, double width) {
		for(int i = 0; i < layers.length; i++) {
			layers[i].randomizeBiases(center, width);
		}
	}
	public double[] calculateNetwork(double[] inps) {
		for (MAFlayer layer : layers) {
			inps = layer.calculate(inps, activationFunction);
		}
		return inps;
	}
	

	public static void main(String[] args) {
		// A simple activation function for the test
		DoubleUnaryOperator activation = x -> 1.0 / (1.0 + Math.exp(-x));
		Random random = new Random();
		int maxProblemSize = 5_00;
		int step = 4;
		
		System.out.println("Available CPU cores for parallel streams: " + Runtime.getRuntime().availableProcessors());
		System.out.println("Beginning performance test to find parallelization threshold.");
		System.out.println("----------------------------------------------------------");
		
		for (int problemSize = 20; problemSize <= maxProblemSize; problemSize += step) {
			// Set up a layer with a fixed number of inputs but increasing outputs
			
			MAFlayer layer = new MAFlayer(problemSize, problemSize);
			layer.randomizeBiases(0, 1);
			layer.randomizeWeights(0, 1);
			
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
