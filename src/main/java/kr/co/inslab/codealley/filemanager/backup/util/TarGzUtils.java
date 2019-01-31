package kr.co.inslab.codealley.filemanager.backup.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TarGzUtils {
	private static Log log = LogFactory.getLog(TarGzUtils.class);
	
	private static final int BUFFER = 2048;

	public static File createTarGz(String sourcePaths, String fileName) throws IOException {
		log.info("Compressing [" + sourcePaths + "]  into [" + fileName + "]");
		return createTarGz(new String[] { sourcePaths }, fileName, "", false);
	}
	
	public static File createTarGz(final String[] sourcePaths, String fileName,
			final String base, final boolean addRoot) throws IOException {
		
		File tarGzFile = new File(fileName); 
		
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		GzipCompressorOutputStream gzOut = null;
		TarArchiveOutputStream tOut = null;
		try {
			fOut = new FileOutputStream(tarGzFile);
			bOut = new BufferedOutputStream(fOut);
			gzOut = new GzipCompressorOutputStream(bOut);
			tOut = new TarArchiveOutputStream(gzOut);
			
			tOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
			
			for (String path : sourcePaths) {
				addFileToTarGz(tOut, path, base, addRoot);
			}
		} finally {
			if (tOut != null) {
				tOut.close();
			}
			if (gzOut != null) {
				gzOut.close();
			}
			if (bOut != null) {
				bOut.close();
			}
			if (fOut != null) {
				fOut.close();
			}
		}

		return tarGzFile;
	}
	
	private static void addFileToTarGz(final TarArchiveOutputStream tOut,
			final String path, final String base, final boolean addRoot)
			throws IOException {
		try {
			File f = new File(path);

			Path filePath = Paths.get(path);
			FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(filePath, FileOwnerAttributeView.class);
	        UserPrincipal owner = ownerAttributeView.getOwner();
	        
			String entryName = base + f.getName();
			
			if (f.isFile()) {
				
					TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
					tarEntry.setUserName(owner.toString());
					tOut.putArchiveEntry(tarEntry);
					
					//IOUtils.copy(new FileInputStream(f), tOut);
					FileInputStream in = new FileInputStream(f);
					IOUtils.copy(in, tOut);
					in.close();
					
					tOut.closeArchiveEntry();
			} else {
				if (addRoot) {
					TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
					tarEntry.setUserName(owner.toString());
					tOut.putArchiveEntry(tarEntry);
					tOut.closeArchiveEntry();
				}
				File[] children = f.listFiles();
				if (children != null) {
					for (File child : children) {
						if (addRoot) {
							addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/", true);
						} else {
							addFileToTarGz(tOut, child.getAbsolutePath(), "", true);
						}
					}
				}
			}
		}catch(NoSuchFileException e) {
			log.error(e);
		}
	}

	public static void extract(final File source, final String destination) throws IOException {
		extract(source, destination, null);
	}
	
	/**
	 * * Extract a tar.gz file.
	 * 
	 * @param source
	 *            The file to extract from.
	 * @param destination
	 *            The destination folder.
	 * @throws IOException
	 *             An error occured during the extraction.
	 */
	public static void extract(final File source, final String destination, String subfolder)
			throws IOException {
		log.info(String.format("Extracting %s to %s", source, destination));
		log.info(String.format("subfolder[%s]", subfolder));
		
		if (!FilenameUtils.getExtension(source.getName().toLowerCase()).equals("gz")) {
			throw new IllegalArgumentException("Expecting tar.gz file: "
					+ source.getAbsolutePath());
		}
		
		File destinationDir = new File(destination);
		if (!destinationDir.exists() ) {
			destinationDir.mkdirs();
		}
		
		if (!new File(destination).isDirectory()) {
			throw new IllegalArgumentException("Destination should be a folder: " + destination);
		}

		/** create a TarArchiveInputStream object. **/
		FileInputStream fin = new FileInputStream(source);
		BufferedInputStream in = new BufferedInputStream(fin);
		GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);

		TarArchiveEntry entry = null;

		/** Read the tar entries using the getNextEntry method **/
		while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
			
			if(subfolder == null || entry.getName().startsWith(subfolder)) {
				/** If the entry is a directory, create the directory. **/
				if (entry.isDirectory()) {

					File f = new File(destination, entry.getName());
					f.mkdirs();
			        
				} else {

					try{
						int count;
						byte[] data = new byte[BUFFER];
						File ff = new File(destination, entry.getName());
						FileOutputStream fos = new FileOutputStream(ff);
						BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
						while ((count = tarIn.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.close();
				        
					}catch(FileNotFoundException e) {
						log.error(e);
					}
				}
				
				if(!entry.getUserName().equals("") && entry.getUserName() != null) {
					Path filePath = Paths.get(destination + "/" + entry.getName());
					UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
					UserPrincipal userPrincipal = lookupService.lookupPrincipalByName(entry.getUserName());
			        Files.setOwner(filePath, userPrincipal);
				}
				
			}
		}

		/** Close the input stream **/
		tarIn.close();
	}
}
