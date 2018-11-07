package promcheg.diffpackaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
		ArrayList<String> currentFileList = readFileList(currentDirectory);
		ArrayList<String> outputFileList = new ArrayList<>();
		Optional<String> initialFirstFile = initalFileList.stream().sorted().findFirst();
		Optional<String> currentFirstFile = currentFileList.stream().sorted().findFirst();
		
		//trim base directory
		AtomicReference<String> initialBaseDirectory = new AtomicReference<String>("");
		AtomicReference<String> currentBaseDirectory = new AtomicReference<String>("");
		String separator = "\\";
		String[] initialDirectoryStruct = initialFirstFile.get().replaceAll(Pattern.quote(separator), "/").split("/");
		String[] currentDirectoryStruct = currentFirstFile.get().replaceAll(Pattern.quote(separator), "/").split("/");
		int projectRootOffsetIdx = -1;
		for(int i = 1; i <= initialDirectoryStruct.length; i++) {
			if(		projectRootOffsetIdx == -1 && 
					!initialDirectoryStruct[initialDirectoryStruct.length-i]
							.equalsIgnoreCase(
									currentDirectoryStruct[currentDirectoryStruct.length-i])) {
				projectRootOffsetIdx = i;
			}
			
			if(projectRootOffsetIdx != -1) {
				initialBaseDirectory.set(initialDirectoryStruct[initialDirectoryStruct.length- i+1] + separator + initialBaseDirectory.get());
				if(currentDirectoryStruct.length-i+1 >= 0) {
					currentBaseDirectory.set(currentDirectoryStruct[currentDirectoryStruct.length-i+1] + separator + currentBaseDirectory.get());
				}
			}
		}
		initialBaseDirectory.set(initialDirectoryStruct[0]+separator+initialBaseDirectory.get());

		initalFileList.stream().forEach(absoluteFileName->{
			String initialFileName = absoluteFileName.substring(initialBaseDirectory.get().length());
			AtomicReference< String> currentAbsoluteFileName = new AtomicReference<String>();
			for(String fileName : currentFileList) {
				if(fileName.endsWith(initialFileName)) {
					currentAbsoluteFileName.set(fileName);
					break;
				}
			}
			
			if(currentAbsoluteFileName.get() == null || isModified(absoluteFileName, currentAbsoluteFileName.get())) {
				outputFileList.add(currentAbsoluteFileName.get());
			}
		});
	}

	private boolean isModified(String originalFile, String currentFile) {
		return false;
	}

	private ArrayList<String> readFileList(String directory) {
		ArrayList<String> result = new ArrayList<>();
		if(directory == null) {
			return result;
		}
		File dir = new File(directory);
		if(dir.isDirectory()) {
			Arrays.stream(dir.listFiles()).forEach(file->{
				if(file.isDirectory()) {
					ArrayList<String> subFiles = readFileList(file.getAbsolutePath());
					result.addAll(subFiles);
				} else if(isAllowed(file.getName())){
					result.add(file.getAbsolutePath());
				}
			});
		}
		return result;
	}

	private boolean isAllowed(String name) {
		AtomicBoolean found  = new AtomicBoolean(false);
		Arrays.stream(RELEVANT_FILES).forEach(ext->{
			if(!found.get() && name.endsWith(ext)) {
				found.set(true);
			}
		});
		return found.get();
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
