package edu.cmu.lti.search;


import java.io.*;
import java.util.HashMap;

/**
 * Cache for document retrieval component.
 * TODO: replace this outdated way of cache
 *
 */
public class RetrievalCache {
	private static String cachePath;
	private final static String name = "IndriResult.cache";// file name

	private static HashMap<String, HashMap<String, Result[]>> cacheMap= new HashMap<String, HashMap<String, Result[]>>();

	public static HashMap<String, Result[]> LoadCache(String cachePath, String sourceID) {
		RetrievalCache.cachePath=cachePath;
		if(!cacheMap.containsKey(sourceID)){
			return Read(sourceID);
		}
		return cacheMap.get(sourceID);
	}

	/**
	 * read cache from persistent layer
	 */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Result[]> Read(String sourceID) {
		HashMap<String, Result[]> map=null;
		FileInputStream freader;
		try {
			freader = new FileInputStream(cachePath + sourceID + name);
			ObjectInputStream objectInputStream = new ObjectInputStream(freader);
			map = (HashMap<String, Result[]>) objectInputStream
					.readObject();
			System.out.println("Read cache: " + sourceID);
			objectInputStream.close();
		} catch (FileNotFoundException e) {
			map = new HashMap<String, Result[]>();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		cacheMap.put(sourceID, map);
		return map;
	}

	/**
	 * persistence
	 */
	public static void Write(String sourceID) {
		try {
			FileOutputStream outStream = new FileOutputStream(cachePath + sourceID + name);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					outStream);

			objectOutputStream.writeObject(cacheMap.get(sourceID));
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
