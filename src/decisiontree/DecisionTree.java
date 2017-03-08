package decisiontree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import toolkit.Matrix;
import toolkit.SupervisedLearner;

public class DecisionTree extends SupervisedLearner {
	private DTNode decisionTree;
	private Matrix myFeatures;
	private Matrix myLabels;
	private Matrix testFeatures;
	private Matrix testLabels;

	private double calculateEntropy(Map<Double, Integer> map) {
		int totalSize = this.calculateValueLength(map);
		double returnValue = 0;
		for (double key : map.keySet()) {
			double value = map.get(key);
			double temp = -1 * (value / totalSize) * logB2(value / totalSize);
			returnValue += temp;
		}
		return returnValue;
	}

	private void myTrain(Matrix features, Matrix labels, DTNode node) {
		// go for number of attributes
		// calculate biggest entropy
		double[] labelsArray = this.translateLabelsToDoubleArray(labels);
		Map<Double, Integer> mapOfOuterEntropy = calculateSplit(labelsArray);
		int mapOfOuterEntropyValueLength = this.calculateValueLength(mapOfOuterEntropy);
		double outerEntropy = this.calculateEntropy(mapOfOuterEntropy);
		double[] infoGains = new double[features.cols()];
		for (int x = 0; x < features.cols(); x++) {
			// Go through each column in features
			double[] column = features.col(x);
			infoGains[x] = outerEntropy;
			Map<Double, Integer> map = calculateSplit(column);
			// The outer map data structure splits it up like so:
			// {0.0=8, 1.0=8, 2.0=8}
			double[] individualEntropies = new double[map.keySet().size()];
			double[] fractions = new double[map.keySet().size()];
			int iter = 0;

			for (double key : map.keySet()) {
				// find splits in the individual outputs
				// Now we go ahead and split the things even further.
				// if key == 0, you compare the key to the output. So we could
				// end up with {0.0=2, 1.0=2, 2.0=4 }

				Map<Double, Integer> compareToOutput = new HashMap<>();
				for (int i = 0; i < column.length; i++) {

					if (column[i] == key) {
						double label = labels.row(i)[0];
						if (compareToOutput.get(label) == null) {
							compareToOutput.put(label, 1);
						} else {
							int newCount = compareToOutput.get(label) + 1;
							compareToOutput.put(label, newCount);
						}
					}
				}
				// do the 3/4 * log(3/4) / log 2 ... etc. etc.
				individualEntropies[iter] = this.calculateEntropy(compareToOutput);
				double valueLength = this.calculateValueLength(compareToOutput);

				fractions[iter] = valueLength / mapOfOuterEntropyValueLength;
				infoGains[x] -= (fractions[iter] * individualEntropies[iter]);

				iter++;
			}

		}
		int bestInfoGainIndex = -1;
		double MAX_INFO_GAIN = 0;
		for (int i = 0; i < infoGains.length; i++) {
			if (infoGains[i] > MAX_INFO_GAIN) {
				MAX_INFO_GAIN = infoGains[i];
				bestInfoGainIndex = i;
			}
		}
		if (bestInfoGainIndex == -1) {

			node.setValue(labels.m_enum_to_str.get(0).get((int) labels.row(0)[0]));
			return;
		}

		String value = features.m_attr_name.get(bestInfoGainIndex);
		node.setValue(value);

		int attrNameIndex = features.m_attr_name.indexOf(node.getValue());
		Map<String, Integer> nodes = features.m_str_to_enum.get(attrNameIndex);
		for (String key : nodes.keySet()) {
			node.setNode(key, new DTNode());
			node.getNode(key).setFeatures(features);
			node.getNode(key).setLabels(labels);
		}
		node.setNode("unknown", new DTNode());
		node.getNode("unknown").setFeatures(features);
		node.getNode("unknown").setLabels(labels);

		// get new features and labels
		double[] bestInfoGainColumn = features.col(bestInfoGainIndex);
		Map<Double, Integer> bestInfoGainMap = this.calculateSplit(bestInfoGainColumn);
		for (double key : bestInfoGainMap.keySet()) {

			Matrix newFeatures = new Matrix(features, 0, 0, features.rows(), features.cols());
			Matrix newLabels = new Matrix(labels, 0, 0, labels.rows(), labels.cols());
			for (int i = features.rows() - 1; i > -1; i--) {
				if (features.get(i, bestInfoGainIndex) != key) {

					try {
						newFeatures.removeRow(i);
						newLabels.removeRow(i);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			String featureName = features.m_enum_to_str.get(attrNameIndex).get((int) key);
			if (featureName == null) {
				featureName = "unknown";
			}

			DTNode newNode = node.getNode(featureName);
			if (newNode != null) {
				newNode.setFeatures(newFeatures);
				newNode.setLabels(newLabels);
				this.myTrain(newFeatures, newLabels, newNode);
			}

		}
	}

	private int calculateValueLength(Map<Double, Integer> map) {
		int returnValue = 0;
		for (double key : map.keySet()) {
			returnValue += map.get(key);
		}
		return returnValue;
	}

	@Override
	public void train(Matrix features, Matrix labels) throws Exception {
		decisionTree = new DTNode();
		myFeatures = new Matrix(features, 0, 0, features.rows(), features.cols());
		myLabels = new Matrix(labels, 0, 0, labels.rows(), labels.cols());
		myTrain(features, labels, decisionTree);
	}

	@Override
	public void predict(double[] features, double[] labels) throws Exception {
		DTNode nodeDaddy = decisionTree;
		String response = response(nodeDaddy, features);
		labels[0] = myLabels.m_str_to_enum.get(0).get(response);

	}

	private String[] featuresToNames(double[] features) {
		String[] arr = new String[features.length];
		for (int i = 0; i < features.length; i++) {
			arr[i] = myFeatures.m_enum_to_str.get(i).get((int) features[i]);
		}
		return arr;
	}

	private double getPopularElement(double[] a) {
		int count = 1, tempCount;
		int popular = (int) a[0];
		int temp = 0;
		for (int i = 0; i < (a.length - 1); i++) {
			temp = (int) a[i];
			tempCount = 0;
			for (int j = 1; j < a.length; j++) {
				if (temp == a[j])
					tempCount++;
			}
			if (tempCount > count) {
				popular = temp;
				count = tempCount;
			}
		}
		return popular;
	}

	private double[] mDataToArr(List<double[]> m_data) {
		double[] arr = new double[m_data.size()];
		for (int i = 0; i < m_data.size(); i++) {
			arr[i] = m_data.get(i)[0];
		}
		return arr;
	}

	private String labelNumberToStringValue(int i) {
		return myLabels.m_enum_to_str.get(0).get(i);
	}

	private String response(DTNode node, double[] features) {
		if (node.getNodes().size() == 0) {
			if (node.getValue() != null) {
				return node.getValue();
			} else {
				Matrix labels = node.getLabels();
				double[] arr = mDataToArr(labels.m_data);
				double popElement = getPopularElement(arr);
				String ret = labelNumberToStringValue((int) popElement);

				return ret;
			}

		}
		for (int i = 0; i < features.length; i++) {
			String attribute = myFeatures.m_attr_name.get(i);
			String feature = myFeatures.m_enum_to_str.get(i).get((int) features[i]);
			if (feature == null) {
				feature = "unknown";
			}
			if (attribute.equals(node.getValue())) {
				// if (node.getValue().equals(attribute)) {
				Map<String, DTNode> nodes = node.getNodes();
				for (String key : nodes.keySet()) {
					if (key.equals(feature)) {
						return response(nodes.get(key), features);
					}
				}
			}
		}
		return null;
	}

	@Override
	public void setTestSet(Matrix testFeatures, Matrix testLabels) throws Exception {
		// TODO do i need to do this one?
		this.testFeatures = testFeatures;
		this.testLabels = testLabels;
	}

	private Map<Double, Integer> calculateSplit(double[] column) {
		Map<Double, Integer> map = new HashMap<>();
		for (int i = 0; i < column.length; i++) {
			double label = column[i];
			if (map.get(label) == null) {
				map.put(label, 1);
			} else {
				int newCount = map.get(label) + 1;
				map.put(label, newCount);
			}
		}
		return map;
	}

	private double[] translateLabelsToDoubleArray(Matrix labels) {
		double[] labelsArr = new double[labels.rows()];
		for (int i = 0; i < labels.rows(); i++) {
			labelsArr[i] = labels.row(i)[0];
		}
		return labelsArr;
	}

	private double logB2(double x) {
		return log(x) / log(2);
	}

	private double log(double x) {
		return Math.log(x);
	}
}
