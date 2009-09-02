/**
 *
 */
package de.fu_berlin.inf.dpp.fileupload;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

/**
 * @author Lisa Dohrmann, 09.06.2009
 * 
 */
public class FileUploadHandler {

	public static final String ROOT_DIR = "./webapps/data/";

	public static final String DIR_STATISTIC = "saros-statistic/";
	public static final String DIR_ERROR_LOGS = "saros-error-logs/";

	public static final String INTERNAL_DIR = "internal/";
	public static final String RAND_NAME_PREFIX = "unknown_";
	public static final long MAX_REQUEST_SIZE = 2 * 1024 * 1024; // max 2MB

	/**
	 * Localhost 127.0.0.1<br>
	 * Eduroam: 87.77.x.x<br>
	 * FU VPN: 130.133.x.x<br>
	 * FU Netz: 160.45.x.x
	 */
	public static final String[] INTERNAL_IPS = { "127.0.0.1", "160.45.",
			"130.133.", "87.77." };

	public static String[] delimiters = { "/", "\\" };

	protected boolean isInternal;
	protected HttpServletRequest request;
	protected String currentRootDir;

	public FileUploadHandler(HttpServletRequest request) {
		this.request = request;
		this.isInternal = isInternal(request);
		this.currentRootDir = ROOT_DIR;

		/*
		 * determine which kind of request we got, to choose a the right root
		 * directory for the uploaded file
		 */
		String idString = request.getParameter("id");
		int id = 0;

		try {
			id = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			log.warn("The id " + idString + " couldn't be parsed.");
		}

		switch (id) {
		case 1:
			currentRootDir += DIR_STATISTIC;
			break;
		case 2:
			currentRootDir += DIR_ERROR_LOGS;
			break;
		default:
			// HACK put in statistic dir
			currentRootDir += DIR_STATISTIC;
		}

	}

	protected static final Logger log = Logger
			.getLogger(FileUploadHandler.class.getName());

	public boolean processRequest() {
		// create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);

		// don't handle very big requests
		upload.setSizeMax(MAX_REQUEST_SIZE);

		List<?> items = null;
		// parse the request
		try {
			items = upload.parseRequest(request);
		} catch (FileUploadException e) {
			log.error("Errors encountered while processing the request", e);
			return false;
		}

		boolean success = true;

		for (Iterator<?> iter = items.iterator(); iter.hasNext();) {
			FileItem item = (FileItem) iter.next();

			if (!item.isFormField()) {
				String fileName = item.getName();
				File uploadedFile = createEmptyFile(fileName);
				log.info("Recieved file " + fileName
						+ ". File will be saved at "
						+ uploadedFile.getAbsolutePath());

				try {
					item.write(uploadedFile);
				} catch (Exception e) {
					log.error("Couldn't write data to disk", e);
					success = false;
					continue;
				}
				log.info("File " + uploadedFile.getName()
						+ " was succesfully stored");
			} else {
				log
						.info("Recieved a simple form field: "
								+ item.getFieldName());
			}
		}
		return success;

	}

	protected static String getRandomName() {
		Random rand = new Random();
		return RAND_NAME_PREFIX + rand.nextInt(Integer.MAX_VALUE);
	}

	protected static String extractFileName(String path) {
		if (path == null) {
			return getRandomName();
		}
		for (String delim : delimiters) {
			// did we get a directory path?
			if (path.endsWith(delim)) {
				return getRandomName();
			}
			if (path.contains(delim)) {
				int index = path.lastIndexOf(delim);
				path = path.substring(index + 1);
			}
		}

		return path;
	}

	protected File createEmptyFile(String filename) {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dirName = dateFormat.format(date) + File.separator;
		/*
		 * Create a subdirectory with today's date. If this was an internal
		 * request, create a subsubdirectory "internal"
		 */
		File dir = isInternal ? new File(currentRootDir + dirName
				+ INTERNAL_DIR) : new File(currentRootDir + dirName);

		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				log.warn("Couldn't create directories " + currentRootDir
						+ dirName);
				// try to use ROOT as parent
				dir = new File(currentRootDir);
				if (!dir.exists()) {
					log.error("Even the root directory couldn't be created."
							+ " The file uplaod was therefore not saved.");
				}
			}
		}

		File file = new File(dir, extractFileName(filename));
		return file;
	}

	protected static boolean isInternal(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		for (String ipPrefix : INTERNAL_IPS) {
			if (remoteAddr.startsWith(ipPrefix))
				return true;
		}
		return false;

	}

}
