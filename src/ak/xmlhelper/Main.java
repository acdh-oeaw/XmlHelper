package ak.xmlhelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import ak.xmlhelper.classes.XmlField;

public class Main {

	// CLI options
	static Options options = new Options();
	static OptionGroup optionGroup = new OptionGroup();

	public static void main(String[] args) {

		CommandLineParser clParser = new DefaultParser();

		// v: validate
		Option oValidate = Option
				.builder("v")
				.required(true)
				.longOpt("validate")
				.desc("Validate XML file(s)")
				.hasArg()
				.argName("PathToFileOrDir")
				.build();

		// m: merge
		Option oMerge = Option
				.builder("m")
				.required(true)
				.longOpt("merge")
				.desc("Merge multiple XML files to one single XML file. Args:"
						+ "\n 1. Path to XML files to merge"
						+ "\n 2. Element to merge (e. g. record)"
						+ "\n 3. Element level (for nested elements with same name. 1 for top (= first) level, 2 for second level, ...)"
						+ "\n 4. New output XML file with merged elements"
						+ "\n 5. Parent element in new XML file (e. g. collection)"
						+ "\n 6. Optional: Parent attributes for parent element. Escape double-quotes and surround with double quotes when there are spaces. Format: (prefix [optional], namespaceURI [optional],) localName [mandatory], value [mandatory]"
						+ "\n 7. Optional: Element attributes for merged elements. Escape double-quotes and surround with double quotes when there are spaces. Format: (prefix [optional], namespaceURI [optional],) localName [mandatory], value [mandatory]")
				.hasArgs()
				.numberOfArgs(7)
				.optionalArg(true)
				.build();

		// s: split
		Option oSplit= Option
				.builder("s")
				.required(true)
				.longOpt("split")
				.desc("Split one XML file into multiple single XML files. args:"
						+ "\n 1. File(s)ToSplit"
						+ "\n 2. NodeToExtractName (e. g. \"record\")"
						+ "\n 3. NodeToExtractCount"
						+ "\n 4. ConditionNodeForFilename (e. g. \"controlfield\")"
						+ "\n 5. ConditionAttrsForFilename (e. g. \"attr1=value1,attr2=value2,...\" or \"null\" if none)"
						+ "\n 6. DestinationDir")
				.hasArgs()
				.numberOfArgs(6)
				.build();

		// c: count
		Option oCount= Option
				.builder("c")
				.required(true)
				.longOpt("count")
				.desc("Count XML elements in a XML file. args:"
						+ "\n 1. Path to XML file"
						+ "\n 2. Name of XML tag to be counted. E. g. if you want to count <name>...</name> tags, use \"name\"."
						+ "\n 3. Name of the attribute in the XML tag you want to count or \"null\". E. g. if you want to count all <name attr=\"...\" /> tags, use \"attr\"."
						+ "\n 4. Value of the attribute in the XML tag you want to count or \"null\". E. g. if you want to count all <name attr=\"value\" /> tags, use \"value\".")
				.hasArgs()
				.numberOfArgs(4)
				.build();

		// w: countWithin
		Option oCountWithin = Option
				.builder("w")
				.required(true)
				.longOpt("count-within")
				.desc("Count XML elements within another XML element in a XML file. args:"
						+ "\n 1. Path to XML file"
						+ "\n 2. Name of XML tag to be counted. E. g. if you want to count <name>...</name> tags, use \"name\"."
						+ "\n 3. Name of the attribute in the XML tag you want to count or \"null\". E. g. if you want to count all <name attr=\"...\" /> tags, use \"attr\"."
						+ "\n 4. Value of the attribute in the XML tag you want to count or \"null\". E. g. if you want to count all <name attr=\"value\" /> tags, use \"value\"."
						+ "\n 5. Name of XML tag the other XML element should be counted in. E. c. if you want to count the \"name\" tags in a structure like <record> <name>...</name> <name>...</name> </record>, specify \"record\"."
						+ "\n 6. A semicolon separated list of comma separated lists that consists of a tag name, attribute name and attribute value, for which the content should be printed, e. g. \"controlfied,tag,001;controlfied,tag,002\"."
						+ "\n 7. Path to a text file where the output should be written, e. g. \"/home/username/myresults.txt\".")

				.hasArgs()
				.numberOfArgs(7)
				.build();

		// l: cLean
		Option oClean= Option
				.builder("l")
				.required(true)
				.longOpt("clean")
				.desc("Clean XML syntax (removing forbidden characters) in a given XML-file(s). args:"
						+ "\n 1. Path to XML file(s). This could be a single file with the ending \".xml\" or a directory with multiple XML files."
						+ "\n 2. \"true\" if you want to save the cleaned data to the same file (the original will be lost!), \"false\" otherwise.")
				.hasArgs()
				.numberOfArgs(2)
				.build();

		// h: help
		Option oHelp = Option
				.builder("h")
				.required(true)
				.longOpt("help")
				.desc("Show help")
				.build();

		optionGroup.addOption(oValidate);
		optionGroup.addOption(oMerge);
		optionGroup.addOption(oSplit);
		optionGroup.addOption(oCount);
		optionGroup.addOption(oCountWithin);
		optionGroup.addOption(oClean);
		optionGroup.addOption(oHelp);
		options.addOptionGroup(optionGroup);


		try {
			CommandLine cmd = clParser.parse(options, args, true);
			String selectedMainOption = optionGroup.getSelected();

			if (args.length <= 0 || cmd.hasOption("h")) {
				HelpFormatter helpFormatter = new HelpFormatter();
				helpFormatter.setWidth(170);
				helpFormatter.printHelp("BetullamXmlHelper", "", options, "", true);
				return;
			}

			// Switch between main options
			switch (selectedMainOption) {
			case "v": {

				String path = cmd.getOptionValue("v");
				File file = new File(path);
				XmlValidator validator = new XmlValidator();

				if (file.isDirectory()) {
					File[] fileList = file.listFiles();
					for (File fileInList : fileList) {
						if (fileInList.isFile() && fileInList.canRead() && fileInList.getName().endsWith(".xml")) {
							System.out.print("\nValidating XML file " + fileInList.getName());
							if(!validator.validateXML(fileInList.getAbsolutePath())) {
								System.err.println("XML file not valid: " + fileInList.getAbsolutePath());
								return; // TODO: break instead of return?
							}
						}
					}
					System.out.println("All XML files are valid."); // We got this far, everything seems to be OK
				} else {
					if (file.canRead() && file.getName().endsWith(".xml")) {
						if(!validator.validateXML(file.getAbsolutePath())) {
							System.err.println("XML file not valid: " + file.getAbsolutePath());
							return; // TODO: break instead of return?
						}
					}
					System.out.println("XML file is valid."); // We got this far, everything seems to be OK
				}

				break;
			}

			case "m": {
				String[] mergeArgs = cmd.getOptionValues("m");
				String pathToFiles = (mergeArgs[0] != null) ? mergeArgs[0] : null;
				String elementToMerge = (mergeArgs[1] != null) ? mergeArgs[1] : null;
				int elementLevel = (mergeArgs[2] != null) ? Integer.valueOf(mergeArgs[2]) : 0;
				String newFile = (mergeArgs[3] != null) ? mergeArgs[3] : null;
				String newXmlParentElement = (mergeArgs[4] != null) ? mergeArgs[4] : null;
				String parentAttributes = (mergeArgs.length > 5 && mergeArgs[5] != null) ? mergeArgs[5] : null;
				String elementAttributes = (mergeArgs.length > 6 && mergeArgs[6] != null) ? mergeArgs[6] : null;

				if (pathToFiles != null && elementToMerge != null && newFile != null && newXmlParentElement != null) {
					XmlMerger xmlm2 = new XmlMerger(); // Start merging
					boolean isMergingSuccessful = xmlm2.mergeElements(pathToFiles, newFile, newXmlParentElement, elementToMerge, elementLevel, parentAttributes, elementAttributes);

					if (isMergingSuccessful) {
						System.out.println("Merging files was successful.");
					} else {
						System.err.println("Error while merging files!");
						return;
					}
				}

				break;
			}

			case "s": {
				String[] splitArgs = cmd.getOptionValues("s");
				String strFileToSplit = (splitArgs[0] != null) ? splitArgs[0] : null;
				String nodeToExtractName = (splitArgs[1] != null) ? splitArgs[1] : null;
				int nodeToExtractCount = (splitArgs[2] != null) ? Integer.valueOf(splitArgs[2]) : 0;
				String conditionNodeForFilename = (splitArgs[3] != null) ? splitArgs[3] : null;
				Map<String, String> conditionAttrsForFilename = new HashMap<String, String>();
				String destinationDir = (splitArgs[5] != null) ? splitArgs[5] : null;
				List<File> filesToSplit = null;

				if (strFileToSplit != null && nodeToExtractName != null && conditionNodeForFilename != null && destinationDir != null) {

					XmlSplitter xmls = new XmlSplitter(destinationDir);

					// Check if file or path:
					File fileToSplit = new File(strFileToSplit);
					boolean isDir = false;
					if (fileToSplit.isDirectory()) {
						isDir = true;
					}

					if (isDir) {
						if (fileToSplit != null && fileToSplit.canRead()) {
							// Get all xml-files recursively
							filesToSplit = (List<File>)FileUtils.listFiles(fileToSplit, new String[] {"xml", "XML"}, true);

							// Sort XML files by name. Is oldest to newest when using timestamp as filename.
							Collections.sort(filesToSplit);
						}
					}

					String strConditionAttrsForFilename = (splitArgs[4] != null) ? splitArgs[4] : null;
					if (strConditionAttrsForFilename != null && !strConditionAttrsForFilename.equals("null") && !strConditionAttrsForFilename.isEmpty()) {
						String[] arrConditionAttrsForFilename = strConditionAttrsForFilename.split("\\s*,\\s*");
						for (String conditionAttrForFilename : arrConditionAttrsForFilename) {
							String[] attrValuePair = conditionAttrForFilename.split("\\s*=\\s*");
							if (attrValuePair.length == 2) {
								String attr = attrValuePair[0];
								String value = attrValuePair[1];
								conditionAttrsForFilename.put(attr, value);
							}
						}
					}

					if (isDir && !filesToSplit.isEmpty()) {
						// Split XMLs. Files will be overwritten by newer files with same name:
						int fileCounter = 0;
						for (File fileForSplitting : filesToSplit) {
							fileCounter++;
							System.out.print("Splitting file " + fileCounter + " from " + filesToSplit.size() + ": " + fileForSplitting.getAbsolutePath() + "                                                        \n");
							xmls.split(fileForSplitting.getAbsolutePath(), nodeToExtractName, nodeToExtractCount, conditionNodeForFilename, conditionAttrsForFilename);
						}
					} else if (!isDir){
						System.out.print("Splitting file 1 from 1: " + fileToSplit.getAbsolutePath() + "                                                        \n");
						xmls.split(fileToSplit.getAbsolutePath(), nodeToExtractName, nodeToExtractCount, conditionNodeForFilename, conditionAttrsForFilename);
					}
				}

				break;
			}

			case "c": {

				System.out.println("\nStart counting XML elements.");
				String[] countArgs = cmd.getOptionValues("c");

				String xmlFile = (countArgs[0] != null) ? countArgs[0] : null;
				String tagName = (countArgs[1] != null) ? countArgs[1] : null;
				String attrName = (countArgs[2] != null && !countArgs[2].equals("null")) ? countArgs[2] : null;
				String attrValue = (countArgs[3] != null && !countArgs[3].equals("null")) ? countArgs[3] : null;

				if (xmlFile != null && tagName != null) {
					XmlCounter xmlc = new XmlCounter();
					int noOfElements = xmlc.count(xmlFile, tagName, attrName, attrValue, true);
					System.out.print("\nTotal elements: " + noOfElements + "\n");
				}
				break;
			}

			case "w": {
				System.out.println("Start counting XML elements within another XML element.");
				String[] countWithinArgs = cmd.getOptionValues("w");

				String xmlFile = (countWithinArgs[0] != null) ? countWithinArgs[0] : null;
				String tagNameCount = (countWithinArgs[1] != null) ? countWithinArgs[1] : null;
				String attrNameCount = (countWithinArgs[2] != null && !countWithinArgs[2].equals("null")) ? countWithinArgs[2] : null;
				String attrValueCount = (countWithinArgs[3] != null && !countWithinArgs[3].equals("null")) ? countWithinArgs[3] : null;
				String tagNameCountWithin = (countWithinArgs[4] != null) ? countWithinArgs[4] : null;
				String tagNamesOutStr = (countWithinArgs[5] != null) ? countWithinArgs[5] : null;
				List<String> allOuts = Arrays.asList(tagNamesOutStr.split("\\s*;\\s*"));
				ArrayList<XmlField> xmlFields = new ArrayList<XmlField>();
				String outFile = (countWithinArgs[6] != null) ? countWithinArgs[6] : null;

				for(String allOut : allOuts) {
					List<String> tagAttrValue = Arrays.asList(allOut.split("\\s*,\\s*"));
					String tagName = (tagAttrValue.get(0) != null) ? tagAttrValue.get(0) : null;
					String attrName = (tagAttrValue.get(1) != null && !tagAttrValue.get(1).equals("null")) ? tagAttrValue.get(1) : null;
					String attrValue = (tagAttrValue.get(2) != null && !tagAttrValue.get(2).equals("null")) ? tagAttrValue.get(2) : null;
					XmlField xmlField = new XmlField(tagName, attrName, attrValue);
					xmlFields.add(xmlField);
				}				

				if (xmlFile != null && tagNameCount != null && tagNameCountWithin != null) {
					XmlCounter xmlc = new XmlCounter();
					int noOfElements = xmlc.countWithin(xmlFile, tagNameCount, attrNameCount, attrValueCount, tagNameCountWithin, xmlFields, outFile);
					System.out.print("\nTotal elements: " + noOfElements + "\n");
				}

				break;
			}

			case "l": {

				System.out.println("\nStart cleaning XML file(s).");
				String[] cleanArgs = cmd.getOptionValues("l");

				String xmlFile = (cleanArgs[0] != null) ? cleanArgs[0] : null;
				boolean saveToSameFile = (cleanArgs[1] != null) ? Boolean.parseBoolean(cleanArgs[1]) : null;
				XmlCleaner xmlcl = new XmlCleaner();
				xmlcl.cleanXml(xmlFile, saveToSameFile);
				System.out.println("Path to cleaned file(s): " + xmlcl.getCleanedFile());

				break;
			}

			}
		} catch (ParseException e) {
			System.err.println("Command line parse exception.");
			e.printStackTrace();
		}
	}

}
