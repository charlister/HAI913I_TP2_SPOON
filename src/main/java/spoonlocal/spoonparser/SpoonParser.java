package spoonlocal.spoonparser;

import parser.Parser;

import spoon.Launcher;
import spoon.processing.Processor;

public class SpoonParser extends Parser<Launcher> {

	public SpoonParser(String projectPath) {
		super(projectPath);
	}
	
	public void setLauncher(boolean autoImports, boolean commentsEnabled) {
		parser = new Launcher(); // create launcher
		parser.addInputResource(getProjectSrcPath()); // set project source path
		parser.getEnvironment().setAutoImports(autoImports); // set auto imports
		parser.getEnvironment().setCommentEnabled(commentsEnabled); // set comments enabled
	}

	
	public void configure() {
		setLauncher(true, true);
	}

	public void addProcessor(Processor<?> processor) {
		parser.addProcessor(processor);
	}

	public void run() {
		parser.run();
	}
}
