package part1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ProceduralArtApp extends JFrame {

	private final NeuralNetworkCanvas canvas;

	public ProceduralArtApp() {
		super.setTitle("Neural Network Procedural Art");
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		super.setSize(800, 800);
		super.setLocationRelativeTo(null);
		
		int[] layerData = {2, 30, 60, 30, 3};
		this.canvas = new NeuralNetworkCanvas(layerData, a -> Math.tanh(a));
//		this.canvas = new NeuralNetworkCanvas(layerData, a -> a > 0. ? a : 0.);
//		this.canvas = new NeuralNetworkCanvas(layerData, x -> 1.0 / (1.0 + Math.exp(-x)));
		super.add(canvas);
		super.pack();
		
		super.setVisible(true);
	}

	private static class NeuralNetworkCanvas extends JPanel {
		private int[] layerData;
		public MutableActivationFunction network;
		public DoubleUnaryOperator activationFunction;
		private double zoomLevel = 1.0;
		public double virtualX = 0, virtualY = 0;
		private Point lastMousePosition;
		
		public double getVirtualXbyDisplayX(double dx) {
			return virtualX + (((double) dx - getWidth()/2) / getWidth()* 4.) / zoomLevel;
		}
		public double getVirtualYbyDisplayY(double dy) {
			return virtualY + (((double) dy - getHeight()/2) / getHeight()* 4.) / zoomLevel;
		}
		
		public NeuralNetworkCanvas(int[] layerData, DoubleUnaryOperator activationFunction) {
			super.setPreferredSize(new Dimension(800, 600));

			this.layerData = layerData;
			this.network = new MutableActivationFunction(activationFunction, layerData);
			this.activationFunction = activationFunction;
			
			super.addMouseWheelListener(new MouseAdapter() {
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					double formerX = getVirtualXbyDisplayX(e.getPoint().x);
					double formerY = getVirtualYbyDisplayY(e.getPoint().y);
					double zoomFactor = Math.pow(1.1, -e.getWheelRotation());
					zoomLevel *= zoomFactor;
					double newX = getVirtualXbyDisplayX(e.getPoint().x);
					double newY = getVirtualYbyDisplayY(e.getPoint().y);
					virtualX += formerX - newX;
					virtualY += formerY - newY;
					repaint();
				}
			});

			super.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if(e.getButton() == MouseEvent.BUTTON1) {
						lastMousePosition = e.getPoint();
					} else if(e.getButton() == MouseEvent.BUTTON3) {
						repaint();
					} else {
						network = new MutableActivationFunction(activationFunction, layerData);
						repaint();
					}
					
				}
				
			});

			addMouseMotionListener(new MouseAdapter() {
				@Override
				public void mouseDragged(MouseEvent e) {
					if (lastMousePosition != null) {
						double lx = getVirtualXbyDisplayX(lastMousePosition.x);
						double ly = getVirtualYbyDisplayY(lastMousePosition.y);
						lastMousePosition = e.getPoint();
						double nx = getVirtualXbyDisplayX(lastMousePosition.x);
						double ny = getVirtualYbyDisplayY(lastMousePosition.y);
						virtualX += lx - nx;
						virtualY += ly - ny;
						repaint();
					}
				}
			});
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2d = (Graphics2D) g;
			int width = getWidth();
			int height = getHeight();
			final int[][] rgbArray = new int[width * height][];
			IntStream.range(0, width * height).parallel().forEach(i -> {
				int x = i % width, y = i / width;
				// Calculate virtual coordinates based on screen position, zoom, and pan
				double networkX = getVirtualXbyDisplayX(x);
				double networkY = getVirtualYbyDisplayY(y);

				double[] input = {networkX, networkY};
				double[] output = network.calculateNetwork(input);

				// Assuming a single output value between 0 and 1
				int redValue = (int) (Math.min(Math.max(output[0], 0.0), 1.0) * 255);
				int greenValue = (int) (Math.min(Math.max(output[1], 0.0), 1.0) * 255);
				int blueValue = (int) (Math.min(Math.max(output[2], 0.0), 1.0) * 255);
				rgbArray[i] = new int[] { redValue, greenValue, blueValue };
			});
			for(int i = 0; i < width * height; i++) {
				Color pixelColor = new Color(rgbArray[i][0],rgbArray[i][1], rgbArray[i][2]);

				g2d.setColor(pixelColor);
				g2d.fillRect(i % width, i / width, 1, 1);
			}
		}
	}
	
	public static void main(String[] args) {
		new ProceduralArtApp();
	}
}