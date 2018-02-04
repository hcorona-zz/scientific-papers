package mmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.Randomize;

public class PaperStatistics {

	public static void main(String[] args) throws Exception {

		BufferedReader reader 	= new BufferedReader(new FileReader("ARFF/binary.arff"));
		Instances data 			= new Instances(reader);

		Remove rm = new Remove();
		rm.setAttributeIndices("1");
		rm.setInputFormat(data);
		Instances newdata =	Filter.useFilter(data,rm);
		data = newdata;

		// DOCUMENT FREQUENCY OF TERMS
		double dfvalues[] = new double[data.numAttributes()];
		double val0 = 0;
		for (int x = 0 ; x < data.numInstances() ; x++){
			for (int n = 4 ; n < data.numAttributes() ; n++){
				dfvalues[n]+=data.get(x).value(n);
			}
			val0+=data.get(x).value(0);
		}
		System.out.println("df= "+Arrays.toString(dfvalues)); // number of unique terms



		int tfvalues[][] = new int[4][data.numAttributes()];
		// TERM DISTRIBUTION ACROSS CLASSES
		System.out.println("ninstances "+ data.numInstances());
		System.out.println("nterms "+ (data.numAttributes()-4));
		for (int x = 0  ; x < data.numInstances() ; x++){
			int nclass = 0;
			if (data.get(x).value(0) == 0.0) nclass=0;
			else if (data.get(x).value(1) == 0.0) nclass=1;
			else if (data.get(x).value(2) == 0.0) nclass=2;
			else if (data.get(x).value(3) == 0.0) nclass=3;
			for (int n = 4 ; n < data.numAttributes() ; n++){
				if (data.get(x).value(n) > 0) tfvalues[nclass][n]=1;
			}
		}

		int sum[] = new int[4];
		for (int n = 4 ; n < data.numAttributes() ; n++){
			int nclasses = (int) (tfvalues[0][n]+tfvalues[1][n]+tfvalues[2][n]+tfvalues[3][n]-1);
			//if (nclasses == 0.0) System.out.println("nclases = 0, "+n);
			sum[nclasses]++;
		}
		System.out.println("distribution across classes: "+Arrays.toString(sum)); // number of unique terms


		// NUMBER OF DISTINCT TERMS
		// NUMBER OF UNIQUE TERMS 
		// LENGTH OF SONG 
		// NUMBER OF UNIQUE TERMS PER SONG
		reader 	= new BufferedReader(new FileReader("ARFF/tf1.arff"));
		data 	= new Instances(reader);
		rm = new Remove();
		rm.setAttributeIndices("1");
		rm.setInputFormat(data);
		newdata =	Filter.useFilter(data,rm);
		data = newdata;


		// USE RAW TF TO GET THE DELTAS
		//getDeltas(data);


		for (int x = 0  ; x < data.numInstances() ; x++){
			int nclass = 0;
			if (data.get(x).value(0) == 0.0) nclass=0;
			else if (data.get(x).value(1) == 0.0) nclass=1;
			else if (data.get(x).value(2) == 0.0) nclass=2;
			else if (data.get(x).value(3) == 0.0) nclass=3;
			for (int n = 4 ; n < data.numAttributes() ; n++){
				if (data.get(x).value(n) > 0) tfvalues[nclass][n]+=data.get(x).value(n);
			}
		}

		int nvals[] = new int[4];
		int nvals_unique[] = new int[4];
		// NUMBER OF DISTINCT TERMS 
		for(int n = 4 ; n < data.numAttributes(); n++){
			if(tfvalues[0][n] >0) nvals[0]++;
			if(tfvalues[1][n] >0) nvals[1]++;
			if(tfvalues[2][n] >0) nvals[2]++;
			if(tfvalues[3][n] >0) nvals[3]++;

			if ((tfvalues[0][n] > 0) & (tfvalues[1][n] < 1) & (tfvalues[2][n] < 1) & (tfvalues[3][n] < 1)) nvals_unique[0]++;
			if ((tfvalues[1][n] > 0) & (tfvalues[0][n] < 1) & (tfvalues[2][n] < 1) & (tfvalues[3][n] < 1)) nvals_unique[1]++;
			if ((tfvalues[2][n] > 0) & (tfvalues[1][n] < 1) & (tfvalues[0][n] < 1) & (tfvalues[3][n] < 1)) nvals_unique[2]++;
			if ((tfvalues[3][n] > 0) & (tfvalues[1][n] < 1) & (tfvalues[2][n] < 1) & (tfvalues[0][n] < 1)) nvals_unique[3]++;	
		}
		System.out.println("distinct terms: "+Arrays.toString(nvals)); // number of distinct terms 
		System.out.println("unique terms: "+Arrays.toString(nvals_unique)); // number of unique terms

		// DISTRIBUTION OF TERMS ACROSS CLASSES
		for(int n = 4 ; n < data.numAttributes(); n++){
			if(tfvalues[0][n] >0) tfvalues[0][n]=1;
			if(tfvalues[1][n] >0) tfvalues[1][n]=1;
			if(tfvalues[2][n] >0) tfvalues[2][n]=1;
			if(tfvalues[3][n] >0) tfvalues[3][n]=1;
		}

		// NUMBER OF TERMS PER SONG 
		DescriptiveStatistics terms_song0= new DescriptiveStatistics();
		DescriptiveStatistics terms_song1= new DescriptiveStatistics();
		DescriptiveStatistics terms_song2= new DescriptiveStatistics();
		DescriptiveStatistics terms_song3= new DescriptiveStatistics();
		for (int n = 0 ; n < data.numInstances(); n++){
			terms_song0.addValue(data.get(n).value(0) * (data.get(n).numValues()-3));
			terms_song1.addValue(data.get(n).value(1) * (data.get(n).numValues()-3));
			terms_song2.addValue(data.get(n).value(2) * (data.get(n).numValues()-3));
			terms_song3.addValue(data.get(n).value(3) * (data.get(n).numValues()-3));
		}

		// NUMBER OF UNIQUE TERMS PER SONG 
		System.out.println("number of unique terms per song");
		System.out.println(terms_song0.getMean()+"\t"+terms_song0.getStandardDeviation());
		System.out.println(terms_song1.getMean()+"\t"+terms_song1.getStandardDeviation());
		System.out.println(terms_song2.getMean()+"\t"+terms_song2.getStandardDeviation());
		System.out.println(terms_song3.getMean()+"\t"+terms_song3.getStandardDeviation());

		// NUMBER OF TERMS PER SONG 
		terms_song0= new DescriptiveStatistics();
		terms_song1= new DescriptiveStatistics();
		terms_song2= new DescriptiveStatistics();
		terms_song3= new DescriptiveStatistics();
		for (int n = 0 ; n < data.numInstances(); n++){
			double[] instancearray = data.get(n).toDoubleArray();
			int val =0;
			for(int x = 0 ; x < data.numAttributes();x++) val+=(instancearray[x]);

			terms_song0.addValue(data.get(n).value(0) *( val -1));
			terms_song1.addValue(data.get(n).value(1) * ( val -1));
			terms_song2.addValue(data.get(n).value(2) * ( val -1));
			terms_song3.addValue(data.get(n).value(3) * ( val -1));

		}

		// NUMBER OF UNIQUE TERMS PER SONG 
		System.out.println("number of terms per song");
		System.out.println(terms_song0.getMean()+"\t"+terms_song0.getStandardDeviation());
		System.out.println(terms_song1.getMean()+"\t"+terms_song1.getStandardDeviation());
		System.out.println(terms_song2.getMean()+"\t"+terms_song2.getStandardDeviation());
		System.out.println(terms_song3.getMean()+"\t"+terms_song3.getStandardDeviation());


		// TERM FREQUENCY DISTRIBUTION
		int frec_values[] =new  int[1000];
		for (int n = 0 ; n < data.numInstances(); n++){
			double[] instancearray = data.get(n).toDoubleArray();
			for(int x = 0; x < data.numAttributes() ; x++){
				frec_values[(int) instancearray[x]]++;
			}
		}
		System.out.println("a="+Arrays.toString(frec_values)); // number of unique terms




	}

	public static void getDeltas(Instances data) throws Exception{


		// RANDOMIZE DATA 
		Randomize randomize = new Randomize();
		randomize.setInputFormat(data);
		randomize.setRandomSeed(50);
		Instances data2 = 	Filter.useFilter(data, randomize);
		data = data2;

		// FIRST KEEP ONLY ONE CLASS AT A TIME
		// THEN DO EVALUATION ON THAT CLASS
		int	nclasses = 0;
		for(int x = 0 ; x < data.numAttributes() ; x++){
			if(data.attribute(x).name().substring(0, 1).equalsIgnoreCase("@")) nclasses++;
		}

		GetResults.LOGGER.info("----------------------------");

		PrintWriter writer = new PrintWriter(new FileOutputStream(new File("deltas.txt"),  true));  
		double distro [][] = new double[nclasses+1][data.numAttributes()-3];

		for(int x = 1 ; x <= nclasses; x++){
			Remove rm = new Remove();
			rm.setAttributeIndices(Integer.toString(x)+","+Integer.toString(nclasses+1)+"-last");
			rm.setInvertSelection(true);
			rm.setInputFormat(data);

			// RANDOMIZE
			Instances newData 			= Filter.useFilter(data,rm);
			newData.setClassIndex(0);
			newData.randomize(new Random());
			AttributeStats stats = newData.attributeStats(0);
			int npositiveSamples = stats.nominalCounts[0];

			// RESAMPLE AND STRATIFY 
			SpreadSubsample filterSample = new SpreadSubsample();
			filterSample.setInputFormat(newData);
			filterSample.setMaxCount(npositiveSamples);
			Instances newDataII = Filter.useFilter(newData, filterSample);

			//double[] df_true = DoClassification.getTermDistribution(newDataII, true);
			//double[] df_false = DoClassification.getTermDistribution(newDataII, false);

			//for (int x1 = 2 ; x1 < newDataII.numAttributes() ; x1++){
			//	distro[x][x1] = Math.log((df_true[x1]+1)/(df_false[x1]+1))/Math.log(2);
			//	}

		}

		//for (int n=0; n<5 ; n++){
		//	writer.println("x"+n+"= "+Arrays.toString(distro[n])+";"); // number of unique terms
		//}
		//writer.close();
	}



}
