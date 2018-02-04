package mmm;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.moeaframework.util.statistics.KruskalWallisTest;

import model.MyResult;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.core.AttributeStats;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.functions.LibLINEAR;


public class DoClassification {

	public static void doBinaryClassification(String filename,String classes) throws Exception{

		ArrayList<ArrayList> res = new ArrayList<ArrayList>();
		PrintWriter writer = new PrintWriter(new FileOutputStream(new File(filename+"-results.csv"),  true));  
		PrintWriter writerstat_test = new PrintWriter(new FileOutputStream(new File("statistical_test.txt"),  true));  
		PrintWriter writer_results = new PrintWriter(new FileOutputStream(new File("accuracies.csv"),  true));  


		GetResults.LOGGER.info("----------------------------");
		GetResults.LOGGER.info(filename);


		for (String classI : Arrays.asList(classes.split(","))){
			ArrayList<Prediction> result_metric = new ArrayList<Prediction>();
			SummaryStatistics accuracies = new SummaryStatistics();
			// LOAD THE ARFF FILE
			int ninstances = 0;
			for (int n = 0 ; n < 5 ; n++){
				String train_name 				= filename.substring(0, filename.indexOf('.'))+"-"+classI+"-"+n+"-train.arff";
				String test_name 				= filename.substring(0, filename.indexOf('.'))+"-"+classI+"-"+n+"-test.arff";
				BufferedReader reader_train 	= new BufferedReader(new FileReader(train_name));
				BufferedReader reader_test 		= new BufferedReader(new FileReader(test_name));
				Instances train 				= new Instances(reader_train);
				Instances test 					= new Instances(reader_test);

				Remove rr = new Remove();
				rr.setAttributeIndices("1");
				rr.setInputFormat(train);
				Instances train2 = Filter.useFilter(train, rr);
				Instances test2 = Filter.useFilter(test, rr);
				train = train2;
				test = test2;
				train.setClassIndex(0);
				test.setClassIndex(0);

				LibLINEAR ll = new LibLINEAR();
				ll.setNormalize(true);
				//RandomForest ll = new RandomForest();
				ll.buildClassifier(train);
				Evaluation eval = new Evaluation(train);
				eval.evaluateModel(ll, test);

				double acc =  (eval.truePositiveRate(0) + eval.trueNegativeRate(0)) / (eval.truePositiveRate(0) + eval.trueNegativeRate(0) + eval.falsePositiveRate(0) + eval.falseNegativeRate(0));
				accuracies.addValue(acc);
				ninstances+=test.numInstances();

				ArrayList<Prediction> predictions = eval.predictions();
				result_metric.addAll(predictions);


				//GetResults.LOGGER.info(train_name);
				//GetResults.LOGGER.info("data size "+train.numInstances());
				//GetResults.LOGGER.info("n attributes size "+train.numAttributes());
				//GetResults.LOGGER.info("correct % "+eval.correct());
			}
			res.add(result_metric);

			GetResults.LOGGER.info(filename+"-"+classI+"\t" +ninstances+"\t"+accuracies.getMean());
			writer_results.println(filename+"-"+classI+"\t" +ninstances+"\t"+accuracies.getMean());
		}

		ArrayList<Prediction> binary = res.get(0);
		ArrayList<Prediction> tf = res.get(1);
		ArrayList<Prediction> tfidf = res.get(2);
		ArrayList<Prediction> bm25 = res.get(3);
		ArrayList<Prediction> deltatf = res.get(4);

		for(int x = 0 ; x < binary.size(); x++){
			String answer = binary.get(x).predicted()+","+tf.get(x).predicted()+","+tfidf.get(x).predicted()+","+bm25.get(x).predicted()+","+deltatf.get(x).predicted();
			//System.out.println(answer);
			writer.println(answer);
		}
		writer.flush();
		writer.close();

		KruskalWallisTest kw = new KruskalWallisTest(5);
		for(int x = 0 ; x < binary.size() ; x++){
			//System.out.println(binary.get(x).toString());
			kw.add(binary.get(x).predicted(),0);
			kw.add(tfidf.get(x).predicted(),1);
			kw.add(bm25.get(x).predicted(),1);
			kw.add(deltatf.get(x).predicted(),1);
		}

		System.out.println(filename+"\t p(0.05)= \t" +kw.test(0.05));
		writerstat_test.println(filename+"\t p(0.05)= \t" +kw.test(0.05));
		writerstat_test.flush();
		writerstat_test.close();
		writer_results.close();

	}

	public static void doCorrelation(String filename) throws Exception{

		// LOAD THE ARFF FILE
		BufferedReader reader 	= new BufferedReader(new FileReader(filename));
		Instances data 			= new Instances(reader);

		// remove the ID
		Remove rr = new Remove();
		rr.setAttributeIndices("1");
		rr.setInputFormat(data);
		Instances data2 = Filter.useFilter(data, rr);
		data = data2;

		// FIRST KEEP ONLY ONE CLASS AT A TIME
		// THEN DO EVALUATION ON THAT CLASS
		int	nclasses = 0;
		for(int x = 0 ; x < data.numAttributes() ; x++){
			if(data.attribute(x).name().substring(0, 1).equalsIgnoreCase("@")) nclasses++;
		}

		GetResults.LOGGER.info("----------------------------");
		GetResults.LOGGER.info(filename);
		GetResults.LOGGER.info("data size "+data.numInstances());
		GetResults.LOGGER.info("n attributes size "+data.numAttributes());

		ArrayList<String> res = new ArrayList<String>();
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(3);

		for(int x = 1 ; x <= nclasses; x++){
			Remove rm = new Remove();
			rm.setAttributeIndices(Integer.toString(x)+","+Integer.toString(nclasses+1)+"-last");
			rm.setInvertSelection(true);
			rm.setInputFormat(data);

			Instances newData 			= Filter.useFilter(data,rm);
			newData.setClassIndex(0);
			newData.randomize(new Random());
			AttributeStats stats = newData.attributeStats(0);
			int npositiveSamples = stats.nominalCounts[0];

			// SUBSAMPLE THE DATA TO HAVE BALANCED CLASSIFIER
			SpreadSubsample filterSample = new SpreadSubsample();
			filterSample.setInputFormat(newData);
			filterSample.setMaxCount(npositiveSamples);
			Instances newDataII = Filter.useFilter(newData, filterSample);
			newData.setClassIndex(0);

			// correlation 
			AttributeSelection attSelection = new AttributeSelection();
			attSelection.setEvaluator( new weka.attributeSelection.CorrelationAttributeEval() );
			attSelection.setRanking(true);
			Ranker rank = new Ranker();
			rank.setNumToSelect(10);
			attSelection.setSearch(rank);
			attSelection.SelectAttributes(newDataII);
			double [][] atts = attSelection.rankedAttributes();
			System.out.println(newData.attribute(0).name());

			for(int y = 0 ; y < 10 ; y++){
				res.add(newData.attribute((int)atts[y][0]).name()+" ("+df.format(atts[y][1])+")");
				System.out.println(newData.attribute((int)atts[y][0]).name()+" ("+atts[y][1]+")");
			}
			System.out.println("");

			//System.out.println(attSelection.toResultsString());
		}

		for(int x = 0 ; x < 10 ; x++)
			System.out.println(x+1+" & "+res.get(x)+" & "+res.get(x+10)+" & "+res.get(x+20)+" & "+res.get(x+30)+"");
	}

	public static void printLatexTable(String filename) throws IOException{

		BufferedReader reader 	= new BufferedReader(new FileReader(filename));

		ArrayList<MyResult> results = new ArrayList<MyResult>();

		System.out.println("starting");
		String line;
		while ((line = reader.readLine()) != null) {
			List<String> fields = Arrays.asList(line.split("\t"));
			String catName = fields.get(0).substring(fields.get(0).indexOf('@'),fields.get(0).indexOf('.') );
			String weight = fields.get(0).substring(fields.get(0).indexOf('-'),fields.get(0).length());
			String ninstances = fields.get(1);
			String accuracy = fields.get(2);

			MyResult aux = new MyResult();
			aux.setClassName(catName);
			int index = results.indexOf(aux);

			// new
			if(index < 0){
				ArrayList<Double> acc = new ArrayList<Double>();
				acc.add(Double.parseDouble(ninstances));
				acc.add(Double.parseDouble(accuracy));
				aux.setResultArray(acc);
				results.add(aux);
			}
			else{
				// not new
				MyResult oldresult = results.get(index);
				ArrayList<Double> acc = oldresult.getResultArray();
				acc.add(Double.parseDouble(accuracy));
				oldresult.setResultArray(acc);
				results.set(index, oldresult);
			}

		}

		System.out.println("finishing");
		for(MyResult rs : results){
			String output = rs.toString();
			System.out.println(output);
		}

	}



}



