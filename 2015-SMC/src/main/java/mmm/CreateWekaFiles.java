package mmm;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import model.Properties;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.LibSVMSaver;
import weka.filters.Filter;
import weka.filters.supervised.instance.SpreadSubsample;
import weka.filters.unsupervised.attribute.AddID;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.Randomize;

public class CreateWekaFiles {


	public static void createAllFiles(String source) throws Exception{

		String dataSource 		= Properties.getDir()+"/"+source;
		String folder 			= Properties.getDir();
		BufferedReader reader 	= new BufferedReader(new FileReader(dataSource));
		Instances data 			= new Instances(reader);

		// randomize data at the begining
		Randomize randomize = new Randomize();
		randomize.setInputFormat(data);
		randomize.setRandomSeed(50);
		Instances data2 = 	Filter.useFilter(data, randomize);
		data = data2;

		// then add the id
		AddID addid = new AddID();
		addid.setAttributeName("ID");
		addid.setIDIndex("1");
		addid.setInputFormat(data);
		Instances newData = 	Filter.useFilter(data, addid);

		// CREATE BOW REPRESENTATION AND SAVE INTO FILES
		String granularity = source.substring(source.indexOf('/'), source.indexOf('.'));
		// BM25, TF, IDF, WORDCOUNT, DELTA
		bow(newData,false,false,false,true,false,"tf1"); // just for stats
		bow(newData,false,false,false,false,false,"binary"); // binary
		bow(newData,false,true,false,true,false,"tf"); // tf
		bow(newData,false,false,true,true,false,"tfidf"); // tf-idf
		bow(newData,true,false,false,true,false,"bm25"); // bm25
		bow(newData,false,false,false,true,true,"deltatf"); // delta

	}

	public static void saveARFF (String folder, String name, Instances instances) throws IOException{
		/*	ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(new File(folder+"/ARFF/"+name+".arff"));
		saver.setDestination(new File(folder+"/ARFF/"+name+".arff"));
		saver.writeBatch();
		saver.
		saver.resetWriter();
		 */

		BufferedWriter writer = new BufferedWriter(new FileWriter(folder+"/ARFF_private/"+name+".arff"));
		writer.write(instances.toString());
		writer.flush();
		writer.close();
	}

	public static void saveLIBSVM (String folder, String name, Instances instances) throws IOException{
		LibSVMSaver saver = new LibSVMSaver();
		saver.setClassIndex("1");
		saver.setInstances(instances);
		saver.setFile(new File(folder+"/"+name+".libsvm"));
		saver.setDestination(new File(folder+"/"+name+".libsvm"));
		saver.writeBatch();
	}

	public static Instances bow (Instances data, boolean bm25, boolean tf, boolean idf, boolean wordcount, boolean delta, String filename) throws Exception{
		// STRING TO WORDS
		StringToWordVector filter = new StringToWordVector();
		filter.setAttributeIndices("2");
		filter.setWordsToKeep(500000); // keep all the words in the dataset
		filter.setDoNotOperateOnPerClassBasis(false); 
		filter.setAttributeNamePrefix("_");
		filter.setLowerCaseTokens(true);
		filter.setOutputWordCounts(wordcount);
		filter.setMinTermFreq(1); 
		filter.setIDFTransform(idf);
		//filter.setStopwordsHandler(new File(Properties.getStopWordsPath()));
		//new StopWordsHandler
		filter.setTFTransform(tf);
		filter.setInputFormat(data); 
		Instances dataBOW 	= Filter.useFilter(data,filter);

		saveARFF(Properties.getDir(),filename,dataBOW);



		// FIRST KEEP ONLY ONE CLASS AT A TIME
		// THEN DO EVALUATION ON THAT CLASS
		int	nclasses = 0;
		for(int x = 0 ; x < dataBOW.numAttributes() ; x++){
			if(dataBOW.attribute(x).name().substring(0, 1).equalsIgnoreCase("@")) nclasses++;
		}

		for(int x = 2 ; x < nclasses+2; x++){

			Remove rm = new Remove();
			rm.setAttributeIndices("1,"+Integer.toString(x)+","+Integer.toString(nclasses+2)+"-last");
			System.out.println("1,"+Integer.toString(x)+","+Integer.toString(nclasses+2)+"-last");
			rm.setInvertSelection(true);
			rm.setInputFormat(dataBOW);
			Instances dataRM = Filter.useFilter(dataBOW,rm);
			System.out.println(dataRM.numAttributes()+" "+ nclasses+" "+dataRM.attribute(1).name());

			AttributeStats stats = dataRM.attributeStats(1);
			int npositiveSamples = stats.nominalCounts[0];
			int nsamples = Math.min(200, npositiveSamples);
			dataRM.setClassIndex(1);
			// RESAMPLE AND STRATIFY 
			SpreadSubsample filterSample = new SpreadSubsample();
			filterSample.setInputFormat(dataRM);
			filterSample.setMaxCount(nsamples);
			Instances dataBinaryClass = Filter.useFilter(dataRM, filterSample);

			// CREATE BM25 REPRESENTATION
			if (bm25 ==true){
				Instances databm25 = doBM25(dataBinaryClass, nclasses);
				dataBinaryClass = databm25;
			}

			int nfolds = 5;
			dataBinaryClass.stratify(nfolds);

			for (int n = 0; n < nfolds; n++) {	
				Instances train = dataBinaryClass.trainCV(nfolds, n);
				Instances test = dataBinaryClass.testCV(nfolds, n);
				// SAVE THE FILE

				// THE DELTA TF-IDF : 
				// has to be done here to avoid using class distribution information obtained from the test instances
				// moreover, we use a binary classification problem and here is where i change the problem to binary
				if (delta == true){
					double[] df_true = getTermDistribution(train, true);
					double[] df_false = getTermDistribution(train, false);
					train = deltatfidf(train, df_true, df_false);
					test = deltatfidf(test, df_true, df_false);
				}

				saveARFF(Properties.getDir(),dataBinaryClass.attribute(1).name()+"-"+filename+"-"+n+"-train",train);
				saveARFF(Properties.getDir(),dataBinaryClass.attribute(1).name()+"-"+filename+"-"+n+"-test",test);
			}

		}
		return dataBOW;


	}

	public static Instances doBM25(Instances data, int nattributes){


		System.out.println("doing BM25...");
		// COMPUTE THE METRICS FOR BM25
		// f: document frequency
		// n: number of documents in collection with this term 
		// N: number of documents
		// r: is the number of relevant documents indexed by this term,
		// R:  total number of relevant documents,
		// L: normalised document length (i.e. the length of this document divided by the average length of documents in the collection).

		DescriptiveStatistics stats = new DescriptiveStatistics();
		double dfvalues[] = new double[data.numAttributes()];
		for (int x = 0 ; x < data.numInstances(); x++){
			double length = 0;
			Instance instanceI = data.get(x);
			for(int y = nattributes ; y < data.numAttributes(); y++){
				length+=Math.min(instanceI.value(y),1);
				dfvalues[y]+=Math.min(data.get(x).value(y),1);
			}
			stats.addValue(length);
		}
		int ndocs = data.numInstances();
		int nterms = data.numAttributes();
		double avg_length =  stats.getMean();

		//System.out.println(avg_length);
		// CALCULATE THE BM25 METRIC FOR EACH INSTANCE
		//double k1 = 1.1;
		double b = 0.75;
		double k1 = 1.2;

		for (int x = 0 ; x < data.numInstances(); x++){

			double length = 0;
			Instance instanceI = data.get(x);
			//	System.out.println(data.get(x).toString());
			for(int y = nattributes ; y < data.numAttributes(); y++){
				length+=Math.min(instanceI.value(y),1);
			}

			for(int y = nattributes ; y < data.numAttributes(); y++){
				double bm25_weight = instanceI.value(y)*(k1+1) / (k1*((1-b)+b*(length/avg_length))+instanceI.value(y));
				double idf_weight = Math.log((data.numInstances() - dfvalues[y] +0.5) / (dfvalues[y]+0.5));
				data.get(x).setValue(y, Math.max(0, bm25_weight*idf_weight)); // crop to 0
			}
			//System.out.println(data.get(x).toString());
			//System.out.println("");

			stats.addValue(length);
		}
		return data;

	}

	public static double[] getTermDistribution(Instances data, boolean label) throws IOException{
		double dfvalues[] = new double[data.numAttributes()];

		// INIT DFVALUES
		for (int x = 0 ; x < data.numAttributes() ; x++) dfvalues[x] = 0;

		for (int x = 0 ; x < data.numInstances(); x++){
			Instance instanceI = data.get(x);
			for(int y = 2 ; y < data.numAttributes(); y++){
				if (label == true && instanceI.classValue() == 0) dfvalues[y]+=Math.min(data.get(x).value(y),1);
				if (label == false && instanceI.classValue() == 1) dfvalues[y]+=Math.min(data.get(x).value(y),1);
			}
		}
		return dfvalues;
	}

	public static Instances deltatfidf(Instances data,  double[] df_true, double[] df_false){
		for(int x = 0 ; x < data.numInstances(); x++){
			for(int y = 2 ; y < data.numAttributes(); y++){	
				double tf = data.get(x).value(y);
				data.get(x).setValue(y, tf*Math.log((df_true[y]+1)/(df_false[y]+1))/Math.log(2));
			}
		}
		return data;
	}

}