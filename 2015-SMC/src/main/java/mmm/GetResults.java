package mmm;
import java.util.Arrays;
import model.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;



public class GetResults {

	public static final Logger LOGGER = Logger.getLogger(GetResults.class);

	public static void main(String[] args) throws Exception {


		BasicConfigurator.configure();

		//CreateWekaFiles.createAllFiles("ARFF_private/data_quadrants_private.arff"); 
		//for(String quadrantI : Arrays.asList("+V+A,+V-A,-V+A,-V-A".split(",")))
		//	DoClassification.doBinaryClassification(Properties.getDir()+"/ARFF_private/@"+quadrantI+".arff", "binary,tf,tfidf,bm25,deltatf");
		DoClassification.printLatexTable("accuracies.csv");

		/*CreateWekaFiles.createAllFiles("ARFF/data_moods.arff"); 
		CreateWekaFiles.createAllFiles("ARFF/data_moods.arff"); 
		CreateWekaFiles.createAllFiles("ARFF/data_tags.arff"); 



		for(String groupI : Arrays.asList("G5,G12,G2,G29,G28,G1,G8,G15,G6,G25,G17,G16".split(",")))
			DoClassification.doBinaryClassification(Properties.getDir()+"/ARFF/@"+groupI+".arff", "binary,tf,tfidf,bm25");

		for(String tagI : Arrays.asList("Mellow,chillout,happy.upbeat,aggressive,angry,soothing,melancholic,calm,sad,Reflective,cheer up,depressing,depressive,dark,depression,happiness,heartache,Calming,wistful,sunny,cheerful,Heartbreaking,rage,angst,cool down,".split(",")))
			DoClassification.doBinaryClassification(Properties.getDir()+"/ARFF/@"+tagI+".arff", "binary,tf,tfidf,bm25");

		DoClassification.printLatexTable("accuracies.csv");
		 */

		//DoClassification.doCorrelation(Properties.getDir()+"/ARFF/tf.arff");
	}

}
