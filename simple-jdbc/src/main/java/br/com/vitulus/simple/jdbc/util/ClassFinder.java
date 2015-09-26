package br.com.vitulus.simple.jdbc.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassFinder {

	public static FileFilter DEFAULT_FILTER = new DefaultClassFilter();
	public static FileFilter DEEP_FILTE = new DeepClassFilter();
	
	
	public static Class<?>[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		return getClasses(packageName,ClassFinder.DEFAULT_FILTER);
	}
	
    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
	public static Class<?>[] getClasses(String packageName,FileFilter filter) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = tralatePackageToPath(packageName);
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName,filter));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
	private static List<Class<?>> findClasses(File directory, String packageName,FileFilter filter) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles(filter);
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName(),filter));
            }
            String className = formatClassName(file.getName());
            classes.add(Class.forName(packageName + '.' + className));
        }
        return classes;
    }

	private static String formatClassName(String name){
		name = name.endsWith(".class") ? name.substring(0,name.lastIndexOf(".")) : name ;
		return name;
	}
	
	private static String tralatePackageToPath(String pacote){
		return pacote.replace(".", "/");
	}
    
	public static class DeepClassFilter implements FileFilter{
		@Override
		public boolean accept(File file) {
			return file != null &&
			file.canRead() &&
			file.isDirectory() ||
			(file.isFile() &&
					file.getName().endsWith(".class"));
		}
		
	}
	
	public static class DefaultClassFilter implements FileFilter{
		@Override
		public boolean accept(File file) {
			return file != null &&
			file.isFile() &&
			file.canRead() &&
			file.getName().endsWith(".class");
		}
		
	}
	
}