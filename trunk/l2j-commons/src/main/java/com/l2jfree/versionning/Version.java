package com.l2jfree.versionning;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.lang.L2System;

public class Version {
	private String _revisionNumber = "exported";
	private String _versionNumber = "-1";
	private String _buildJdk = "";

	private static final Log _log = LogFactory.getLog(Version.class);

	static {
		L2System.milliTime();
	}

	public Version() {
	}

	public Version(Class<?> c) {
		loadInformation(c);
	}

	public void loadInformation(Class<?> c) {
		File jarName = null;
		try {
			jarName = Locator.getClassSource(c);
			JarFile jarFile = new JarFile(jarName);

			Attributes attrs = jarFile.getManifest().getMainAttributes();

			setBuildJdk(attrs);

			setRevisionNumber(attrs);

			setVersionNumber(attrs);
		} catch (IOException e) {
			if (_log.isErrorEnabled())
				_log.error(
						"Unable to get Soft information\nFile name '"
								+ (jarName == null ? "null" : jarName
										.getAbsolutePath())
								+ "' isn't a valid jar", e);
		}

	}

	/**
	 * @param attrs
	 */
	private void setVersionNumber(Attributes attrs) {
		String versionNumber = attrs.getValue("Implementation-Version");
		if (versionNumber != null) {
			_versionNumber = versionNumber;
		} else {
			_versionNumber = "-1";
		}
	}

	/**
	 * @param attrs
	 */
	private void setRevisionNumber(Attributes attrs) {
		String revisionNumber = attrs.getValue("Implementation-Build");
		if (revisionNumber != null) {
			_revisionNumber = revisionNumber;
		} else {
			_revisionNumber = "-1";
		}
	}

	/**
	 * @param attrs
	 */
	private void setBuildJdk(Attributes attrs) {
		String buildJdk = attrs.getValue("Build-Jdk");
		if (buildJdk != null) {
			_buildJdk = buildJdk;
		} else {
			buildJdk = attrs.getValue("Created-By");
			if (buildJdk != null) {
				_buildJdk = buildJdk;
			} else {
				_buildJdk = "-1";
			}
		}
	}

	public String getRevisionNumber() {
		return _revisionNumber;
	}

	public String getVersionNumber() {
		return _versionNumber;
	}

	public String getBuildJdk() {
		return _buildJdk;
	}

}
