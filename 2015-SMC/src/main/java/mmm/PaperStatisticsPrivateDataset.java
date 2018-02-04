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

public class PaperStatisticsPrivateDataset {

	public static void main(String[] args) throws Exception {

		BufferedReader reader 	= new BufferedReader(new FileReader("data_quadrants2.arff"));
		Instances data 			= new Instances(reader);

		int tfvalues[][] = new int[4][data.numAttributes()];
		System.out.println("ninstances "+ data.numInstances());
		System.out.println("nterms "+ (data.numAttributes()-1));
		
		// TERM DISTRIBUTION ACROSS CLASSES
		for (int x = 0  ; x < data.numInstances() ; x++){
			int nclass = (int) data.get(x).value(0);
			for (int n = 1 ; n < data.numAttributes() ; n++){
				if (data.get(x).value(n) > 0) tfvalues[nclass][n]=1;
			}
		}

		int sum[] = new int[4];
		for (int n = 1 ; n < data.numAttributes() ; n++){
			int nclasses = (int) (tfvalues[0][n]+tfvalues[1][n]+tfvalues[2][n]+tfvalues[3][n]-1);
			//if (nclasses == 0.0) System.out.println("nclases = 0, "+n);
			sum[nclasses]++;
		}
		System.out.println("distribution across classes: "+Arrays.toString(sum)); // number of unique terms


		// NUMBER OF DISTINCT TERMS
		// NUMBER OF UNIQUE TERMS 
		// LENGTH OF SONG 
		// NUMBER OF UNIQUE TERMS PER SONG
		reader 	= new BufferedReader(new FileReader("data_quadrants2.arff"));
		data 	= new Instances(reader);


		for (int x = 0  ; x < data.numInstances() ; x++){
			int nclass = (int) data.get(x).value(0);
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
	}
}
