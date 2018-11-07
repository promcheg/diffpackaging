package promcheg.diffpackaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class DiffPackaging {
	public static final String[] RELEVANT_FILES = {".cs", ".aspx", ".html"};
	private final String initialDirectory;
	private final String currentDirectory; 
	private final String outpuDirectory;	

	public DiffPackaging(String initialDirectory, String currentDirectory, String outpuDirectory) {
		super();
		this.initialDirectory = initialDirectory;
		this.currentDirectory = currentDirectory;
		this.outpuDirectory = outpuDirectory;
	}

	public static void main(String[] args) {
		if(args.length < 3) {
			DiffPackaging.printHelp("Missing parameter");
		}
		AtomicReference<String> initialDirectory = new AtomicReference<String>(null);
		AtomicReference<String> currentDirectory = new AtomicReference<String>(null);
		AtomicReference<String> outputDirectory = new AtomicReference<String>(null);
		
		Arrays.stream(args).forEach(param->{
			if(param.toUpperCase().startsWith("INITIAL_STATE")) {
				initialDirectory.set(param.substring(param.indexOf("=")+1));
			}
			if(param.toUpperCase().startsWith("CURRENT_STATE")) {
				currentDirectory.set(param.substring(param.indexOf("=")+1));
			}
			if(param.toUpperCase().startsWith("OUTPUT")) {
				outputDirectory.set(param.substring(param.indexOf("=")+1));
			}
		});
		
		
		
		DiffPackaging mainClass = new DiffPackaging(initialDirectory.get(), currentDirectory.get(), outputDirectory.get());
		mainClass.process();
	}
	
	private void process() {
		ArrayList<String> initalFileList = readFileList(initialDirectory);
		
		System.out.println(initialDirectory);
		System.out.println(currentDirectory);
		System.out.println(outpuDirectory);
	}

	private ArrayList<String> readFileList(String directory) {
		ArrayList<String> result = new ArrayList<>();
		
		File dir = new File(directory);
		if(dir.isDirectory()) {
			Arrays.stream(dir.listFiles()).forEach(file->{
				if(file.isDirectory()) {
					ArrayList<String> subFiles = readFileList(file.getAbsolutePath());
					result.addAll(subFiles);
				} else {
					result.add(file.getAbsolutePath());
				}
			});
		}
		return result;
	}

	protected static void printHelp(String errorMessage) {
		if(errorMessage != null) 
		{
			System.err.println(errorMessage);
		}
		
		System.out.println("Diff packaging app");
		System.out.println("Creates a list of modified project files (eFB portal)");
		System.out.println("Usage:");
		System.out.println("\tjava -jar diffpackaging.jar [arguments]");
		System.out.println("\tArguments:");
		System.out.println("\t\tintial_state={directory with initial state}");
		System.out.println("\t\tcurrent_state={directory with current state}");
		System.out.println("\t\toutput={output directory}");
	}
}
